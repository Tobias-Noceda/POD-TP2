package ar.edu.itba.pod.client;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hazelcast.client.HazelcastClient;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.client.config.ClientNetworkConfig;
import com.hazelcast.config.GroupConfig;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.ICompletableFuture;
import com.hazelcast.core.MultiMap;
import com.hazelcast.mapreduce.Job;
import com.hazelcast.mapreduce.JobTracker;
import com.hazelcast.mapreduce.KeyValueSource;

import ar.edu.itba.pod.collator.TripCollator;
import ar.edu.itba.pod.data.Pair;
import ar.edu.itba.pod.data.TripsRow;
import ar.edu.itba.pod.keyPredicate.DistinctZonesPredicate;
import ar.edu.itba.pod.mapper.TripsTokenizerMapper;
import ar.edu.itba.pod.reducer.TripsCountReducerFactory;

public class Client {

    private static final Logger logger = LoggerFactory.getLogger(Client.class);

    public static void main(String[] args) throws IOException, ExecutionException, InterruptedException {
        logger.info("POD-TP2 Client Starting ...");

        try {
            // Group Config
            GroupConfig groupConfig = new GroupConfig().setName("g2").setPassword("g2-pass");

            // Client Network Config
            ClientNetworkConfig clientNetworkConfig = new ClientNetworkConfig();
            clientNetworkConfig.addAddress("127.0.0.1");

            // Client Config
            ClientConfig clientConfig = new ClientConfig().setGroupConfig(groupConfig).setNetworkConfig(clientNetworkConfig);

            // Node Client
            HazelcastInstance hazelcastInstance = HazelcastClient.newHazelcastClient(clientConfig);

            // Key Value Source
            MultiMap<Pair<Long, Long>, TripsRow> tripsMultiMap = hazelcastInstance.getMultiMap("trips");
            KeyValueSource<Pair<Long, Long>, TripsRow> tripsKeyValueSource = KeyValueSource.fromMultiMap(tripsMultiMap);

            // Job Tracker
            JobTracker jobTracker = hazelcastInstance.getJobTracker("trips");

            // Text File Reading and Key Value Source Loading
            DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            try (Stream<String> lines = Files.lines(Paths.get(args[2]), StandardCharsets.UTF_8)) {
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
            ICompletableFuture<List<Map.Entry<Pair<Long, Long>, Long>>> future = job
                    .keyPredicate(new DistinctZonesPredicate())
                    .mapper(new TripsTokenizerMapper())
                    .reducer(new TripsCountReducerFactory())
                    .submit(new TripCollator());

            // Wait and retrieve the result
            List<Map.Entry<Pair<Long, Long>, Long>> result = future.get();

            // Print results
            result.forEach(e -> 
                System.out.printf("%d;%d;%d\n",
                    e.getKey().getLeft(),
                    e.getKey().getRight(),
                    e.getValue())
            );
        } finally {
            HazelcastClient.shutdownAll();
        }
    }

}
