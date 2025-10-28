package ar.edu.itba.pod.client;

import ar.edu.itba.pod.client.lib.ParametersParser;
import ar.edu.itba.pod.client.lib.ParsedParameters;
import ar.edu.itba.pod.data.TripsRow;
import ar.edu.itba.pod.data.ZonesRow;
import com.hazelcast.client.HazelcastClient;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.client.config.ClientNetworkConfig;
import com.hazelcast.config.GroupConfig;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import com.hazelcast.core.MultiMap;
import com.hazelcast.mapreduce.JobTracker;
import com.hazelcast.mapreduce.KeyValueSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.stream.Stream;

public abstract class BaseClient<K> {



    protected final Logger logger = LoggerFactory.getLogger(this.getClass());

    protected HazelcastInstance hazelcastInstance;
    protected IMap<Long, ZonesRow> zonesMap;
    protected MultiMap<K, TripsRow> tripsMultiMap;
    protected KeyValueSource<K, TripsRow> tripsKeyValueSource;
    protected JobTracker jobTracker;

    private final static String zonesFileName = "zones.csv";
    private final static String tripsFileName = "trips-2025-01.csv";

    public void start(ParsedParameters args) throws IOException, ExecutionException, InterruptedException {
        logger.info("{} Starting ...", this.getClass().getSimpleName());

        // Hazelcast setup
        GroupConfig groupConfig = new GroupConfig().setName("g2").setPassword("g2-pass");
        ClientNetworkConfig clientNetworkConfig = new ClientNetworkConfig().addAddress("127.0.0.1");
        ClientConfig clientConfig = new ClientConfig().setGroupConfig(groupConfig).setNetworkConfig(clientNetworkConfig);
        hazelcastInstance = HazelcastClient.newHazelcastClient(clientConfig);

        // Load zones and trips
        String inPath = args.inPath;
        zonesMap = hazelcastInstance.getMap("zones");
        tripsMultiMap = hazelcastInstance.getMultiMap(getTripsMapName());

        // Open folder inPath and load zonesFileName and tripsFileName
        loadZones(Paths.get(inPath, zonesFileName));
        loadTrips(Paths.get(inPath, tripsFileName));

        // JobTracker
        jobTracker = hazelcastInstance.getJobTracker("trips");

        // MapReduce job
        List<String> queryResults = executeJob();

        // Times
        List<String> timeResults = null; // TODO: Implement the time logs

        // Write results
        writeResults(queryResults, timeResults, args.outPath);

        HazelcastClient.shutdownAll();
    }

    protected void loadZones(Path path) throws IOException {
        try (Stream<String> lines = Files.lines(path, StandardCharsets.UTF_8)) {
            lines.skip(1)
                    .map(line -> line.split(";"))
                    .map(line -> new ZonesRow(Long.parseLong(line[0]), line[1], line[2]))
                    .forEach(zone -> zonesMap.put(zone.getLocationID(), zone));
        }
    }

    protected void loadTrips(Path path) throws IOException {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        try (Stream<String> lines = Files.lines(path, StandardCharsets.UTF_8)) {
            lines.skip(1)
                    .map(line -> line.split(";"))
                    .map(this::parseTripsRow)
                    .forEach(trip -> tripsMultiMap.put(getTripKey(trip), trip));
        }
        tripsKeyValueSource = KeyValueSource.fromMultiMap(tripsMultiMap);
    }

    protected abstract K getTripKey(TripsRow trip);
    protected abstract TripsRow parseTripsRow(String[] fields);
    protected abstract String getTripsMapName();
    protected abstract List<String> executeJob() throws ExecutionException, InterruptedException;
    protected abstract void writeResults(List<String> queryResults, List<String> timeResults, String outputPath) throws IOException;
}
