package ar.edu.itba.pod.client;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.stream.Stream;

import ar.edu.itba.pod.client.lib.ParametersParser;
import ar.edu.itba.pod.client.lib.ParsedParameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hazelcast.client.HazelcastClient;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.client.config.ClientNetworkConfig;
import com.hazelcast.config.GroupConfig;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.ICompletableFuture;
import com.hazelcast.core.IMap;
import com.hazelcast.core.MultiMap;
import com.hazelcast.mapreduce.Job;
import com.hazelcast.mapreduce.JobTracker;
import com.hazelcast.mapreduce.KeyValueSource;

import ar.edu.itba.pod.collator.TripsCountCollator;
import ar.edu.itba.pod.data.Pair;
import ar.edu.itba.pod.data.TripsRow;
import ar.edu.itba.pod.data.ZonesRow;
import ar.edu.itba.pod.keyPredicate.DistinctZonesPredicate;
import ar.edu.itba.pod.mapper.TripsCountMapper;
import ar.edu.itba.pod.reducer.TripsCountReducerFactory;

public class Q1Client {

    private static final Logger logger = LoggerFactory.getLogger(Q1Client.class);

    public static void main(String[] args) throws IOException, ExecutionException, InterruptedException {
        logger.info("POD-TP2 Q1Client Starting ...");

        try {
            // Parameters
            ParsedParameters params = ParametersParser.parseConsoleParameters();

            String address = params.address;
            String inPath = params.inPath;
            String outPath = params.outPath;

            // Group Config
            GroupConfig groupConfig = new GroupConfig().setName("g2").setPassword("g2-pass");

            // Client Network Config
            ClientNetworkConfig clientNetworkConfig = new ClientNetworkConfig();
            clientNetworkConfig.addAddress("127.0.0.1");

            // Client Config
            ClientConfig clientConfig = new ClientConfig().setGroupConfig(groupConfig).setNetworkConfig(clientNetworkConfig);

            // Node Client
            HazelcastInstance hazelcastInstance = HazelcastClient.newHazelcastClient(clientConfig);

            // Zones Key Value Source
            IMap<Long, ZonesRow> zonesMap = hazelcastInstance.getMap("zones");
            // Zones File Reading and Key Value Source Loading
            try (Stream<String> lines = Files.lines(Paths.get(args[0]), StandardCharsets.UTF_8)) {
                lines.skip(1)
                        .map(line -> line.split(";"))
                        .map(line -> new ZonesRow(
                            Long.parseLong(line[0]),
                            line[1],
                            line[2]))
                        .forEach(zone -> zonesMap.put(zone.getLocationID(), zone));
            }

            // Trips Key Value Source
            MultiMap<Pair<Long, Long>, TripsRow> tripsMultiMap = hazelcastInstance.getMultiMap("trips");
            KeyValueSource<Pair<Long, Long>, TripsRow> tripsKeyValueSource = KeyValueSource.fromMultiMap(tripsMultiMap);

            // Job Tracker
            JobTracker jobTracker = hazelcastInstance.getJobTracker("trips");

            // Trips File Reading and Key Value Source Loading
            DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            try (Stream<String> lines = Files.lines(Paths.get(args[1]), StandardCharsets.UTF_8)) {
                lines.skip(1)
                        .map(line -> line.split(";"))
                        .map(line -> new TripsRow(
                                    line[0], 
                                    LocalDateTime.parse(line[1], dateTimeFormatter), 
                                    LocalDateTime.parse(line[2], dateTimeFormatter), 
                                    LocalDateTime.parse(line[3], dateTimeFormatter), 
                                    Long.parseLong(line[4]), 
                                    Long.parseLong(line[5]), 
                                    Double.parseDouble(line[6]), 
                                    Double.parseDouble(line[7])))
                        .forEach(trip -> tripsMultiMap.put(new Pair<>(trip.getPickupLocationID(), trip.getDropoffLocationID()), trip));
            }

            // MapReduce Job
            Job<Pair<Long, Long>, TripsRow> job = jobTracker.newJob(tripsKeyValueSource);
            ICompletableFuture<List<String>> future = job
                    .keyPredicate(new DistinctZonesPredicate())
                    .mapper(new TripsCountMapper())
                    .reducer(new TripsCountReducerFactory())
                    .submit(new TripsCountCollator());

            // Wait and retrieve the result
            List<String> result = future.get();

            // Print results
            System.out.println("Writing...\n");
            Path outputPath = Paths.get("Q1resultado.csv");
            Files.write(outputPath, List.of("pickUpZone;dropOffZone;trips"), StandardCharsets.UTF_8);
            Files.write(outputPath, result, StandardCharsets.UTF_8, StandardOpenOption.APPEND);
            System.out.println("Finished!\n");
        } finally {
            HazelcastClient.shutdownAll();
        }
    }

}
