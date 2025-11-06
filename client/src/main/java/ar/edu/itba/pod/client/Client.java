package ar.edu.itba.pod.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hazelcast.client.HazelcastClient;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.client.config.ClientNetworkConfig;
import com.hazelcast.config.GroupConfig;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import com.hazelcast.core.MultiMap;
import com.hazelcast.mapreduce.Job;
import com.hazelcast.mapreduce.JobCompletableFuture;
import com.hazelcast.mapreduce.KeyValueSource;

import ar.edu.itba.pod.client.lib.ParsedParameters;
import ar.edu.itba.pod.data.Pair;
import ar.edu.itba.pod.data.ZonesRow;

@SuppressWarnings("deprecation")
public abstract class Client<K> {

    private final static String ZONES_FILE_NAME = "zones.csv";
    private final static String TRIPS_FILE_NAME = "trips-2025-01.csv";
    
    private final static String ZONES_MAP_NAME = "zones";
    private final static String TRIPS_MAP_NAME = "trips";

    private static final int TRIPS_BATCH_SIZE = 10_000;


    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final ParsedParameters args;
    private final DateTimeFormatter dtf;
    private final int queryNumber;

    private HazelcastInstance hazelcastInstance;
    private KeyValueSource<Long, K> tripsKeyValueSource;

    private PrintWriter logWriter;

    public Client(
        ParsedParameters args,
        DateTimeFormatter dtf,
        int queryNumber
    ) {
        this.args = args;
        this.queryNumber = queryNumber;
        this.dtf = dtf;
    }

    public void start() throws IOException, ExecutionException, InterruptedException {
        logger.info("Starting query {}...", queryNumber);

        logWriter = new PrintWriter(Files.newBufferedWriter(
            Paths.get(args.outPath, "time" + queryNumber + ".txt"),
            StandardCharsets.UTF_8
        ));

        // Hazelcast setup
        GroupConfig groupConfig = new GroupConfig().setName("g2").setPassword("g2-pass");
        ClientNetworkConfig clientNetworkConfig = new ClientNetworkConfig().addAddress(args.address);
        ClientConfig clientConfig = new ClientConfig().setGroupConfig(groupConfig).setNetworkConfig(clientNetworkConfig);
        hazelcastInstance = HazelcastClient.newHazelcastClient(clientConfig);

        // Load zones and trips
        IMap<Long, ZonesRow> zonesMap = hazelcastInstance.getMap(ZONES_MAP_NAME);
        MultiMap<Long, K> tripsMultiMap = hazelcastInstance.getMultiMap(TRIPS_MAP_NAME);

        logWriter.println("%s INFO [main] %s - Inicio de la lectura del archivo".formatted(dtf.format(LocalDateTime.now()), this.getClass().getSimpleName()));

        // Open folder inPath and load zonesFileName and tripsFileName
        loadZones(Paths.get(args.inPath, ZONES_FILE_NAME), zonesMap);
        loadTripsThreaded(Paths.get(args.inPath, TRIPS_FILE_NAME), tripsMultiMap);

        logWriter.println("%s INFO [main] %s - Fin de lectura del archivo".formatted(dtf.format(LocalDateTime.now()), this.getClass().getSimpleName()));
    }

    public void executeJob(
        JobCompletableFuture<List<String>> queryJob,
        String header
    ) throws ExecutionException, InterruptedException {
        try (
            PrintWriter queryWriter = new PrintWriter(
                Files.newBufferedWriter(
                    Paths.get(args.outPath, "query" + queryNumber + ".txt"),
                    StandardCharsets.UTF_8
                )
            )
        ) {
            logWriter.println("%s INFO [main] %s - Inicio del trabajo map/reduce".formatted(dtf.format(LocalDateTime.now()), this.getClass().getSimpleName()));
            
            // MapReduce job
            List<String> queryResults = queryJob.get();
            
            // Write query results
            queryWriter.println(header);
            for (String result : queryResults) {
                queryWriter.println(result);
            }

            logWriter.println("%s INFO [main] %s - Fin del trabajo map/reduce".formatted(dtf.format(LocalDateTime.now()), this.getClass().getSimpleName()));
        } catch (IOException e) {
            logger.error("Error writing query results", e);

            logWriter.println("Error writing query results");
        } finally {
            logWriter.close();
            HazelcastClient.shutdownAll();
        }
    }

    public void end() {
        logWriter.close();
        HazelcastClient.shutdownAll();
    }

    public Job<Long, K> getTripsJob() {
        if (tripsKeyValueSource == null) {
            throw new IllegalStateException("Trips data not loaded");
        }
        return hazelcastInstance.getJobTracker("trips").newJob(tripsKeyValueSource);
    }

    private void loadZones(Path path, IMap<Long, ZonesRow> zonesMap) throws IOException {
        try (Stream<String> lines = Files.lines(path, StandardCharsets.UTF_8)) {
            lines.skip(1) // Skip header
                    .map(line -> line.split(";"))
                    .filter(fields -> fields.length >= 3)
                    .forEach(fields -> {
                        Long locationID = Long.valueOf(fields[0]);
                        String neighbourhood = fields[1];
                        String zone = fields[2];
                        zonesMap.put(locationID, new ZonesRow(locationID, neighbourhood, zone));
                    });
        }
    }

    private void loadTrips(Path path, MultiMap<Long, K> tripsMultiMap) throws IOException {
        // TODO: preguntar si puedo usar un CSVParser
        try (Stream<String> lines = Files.lines(path, StandardCharsets.UTF_8)) {
            AtomicLong lineCount = new AtomicLong(0);
            lines.skip(1)
                    .forEach(trip -> {
                        K key = getTripKey(trip.split(";"));
                        if (key != null) {
                            tripsMultiMap.put(lineCount.getAndIncrement(), key);
                        }
                    });
        }
        tripsKeyValueSource = KeyValueSource.fromMultiMap(tripsMultiMap);
    }

    private void loadTripsParallel(Path path, MultiMap<Long, K> tripsMultiMap) throws IOException {
        // Parallel stream variant kept for benchmarking vs the threaded loader
        try (Stream<String> lines = Files.lines(path, StandardCharsets.UTF_8)) {
            AtomicLong lineCount = new AtomicLong(0);
            lines.skip(1)
                    .parallel()
                    .map(line -> line.split(";"))
                    .forEach(trip -> {
                        K key = getTripKey(trip);
                        if (key != null) {
                            tripsMultiMap.put(lineCount.getAndIncrement(), key);
                        }
                    });
        }
        tripsKeyValueSource = KeyValueSource.fromMultiMap(tripsMultiMap);
    }

        // Executor service my absolute beloved. Me salvaste sims y ahora compartimos POD. Nunca cambies. Espero olvidarme de sacar este comentario antes de entregar para que sepan cuanto cariño te tengo
    private void loadTripsThreaded(Path path, MultiMap<Long, K> tripsMultiMap) throws IOException {
        ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        List<Future<?>> futures = new ArrayList<>();

        try (BufferedReader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            
            // Sacamos el header
            reader.readLine();

            List<Pair<Long, String>> batch = new ArrayList<>(TRIPS_BATCH_SIZE);
            String line;
            Long lineNumber = 0L;
            while ((line = reader.readLine()) != null) {
                batch.add(new Pair<>(lineNumber++, line));
                if (batch.size() >= TRIPS_BATCH_SIZE) {
                    futures.add(submitTripsBatch(new ArrayList<>(batch), tripsMultiMap, executor));
                    batch.clear();
                }
            }

            if (!batch.isEmpty()) {
                futures.add(submitTripsBatch(new ArrayList<>(batch), tripsMultiMap, executor));
            }
        } finally {
            executor.shutdown();
        }

        for (Future<?> future : futures) {
            try {
                future.get();
            } catch (ExecutionException e) {
                throw new IOException("Failed to load trips batch", e.getCause());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new IOException("Interrupted while loading trips", e);
            }
        }

        try {
            if (!executor.awaitTermination(1, TimeUnit.MINUTES)) {
                logger.warn("Trip loading executor did not terminate within timeout");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("Interrupted while awaiting executor termination", e);
        }

        tripsKeyValueSource = KeyValueSource.fromMultiMap(tripsMultiMap);
    }

    private Future<?> submitTripsBatch(List<Pair<Long, String>> batch, MultiMap<Long, K> tripsMultiMap, ExecutorService executor) {
        return executor.submit(() -> processTripsBatch(batch, tripsMultiMap));
    }

    private void processTripsBatch(List<Pair<Long, String>> batch, MultiMap<Long, K> tripsMultiMap) {
        for (Pair<Long, String> pair : batch) {
            tripsMultiMap.put(pair.getLeft(), getTripKey(pair.getRight().split(";")));
        }
        System.out.println("Sent batch");
    }

    public abstract K getTripKey(String[] fields);
}
