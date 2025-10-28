package ar.edu.itba.pod.client;

import ar.edu.itba.pod.client.lib.ParametersParser;
import ar.edu.itba.pod.client.lib.ParsedParameters;
import ar.edu.itba.pod.collator.TripsCountCollator;
import ar.edu.itba.pod.data.Pair;
import ar.edu.itba.pod.data.TripsRow;
import ar.edu.itba.pod.keyPredicate.DistinctZonesPredicate;
import ar.edu.itba.pod.mapper.TripsCountMapper;
import ar.edu.itba.pod.reducer.TripsCountReducerFactory;
import com.hazelcast.mapreduce.Job;

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

public class Query1Client extends BaseClient<Pair<Long, Long>> {

    @Override
    protected Pair<Long, Long> getTripKey(TripsRow trip) {
        return new Pair<>(trip.getPickupLocationID(), trip.getDropoffLocationID());
    }

    @Override
    protected TripsRow parseTripsRow(String[] f) {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return new TripsRow(f[0], LocalDateTime.parse(f[1], dtf), LocalDateTime.parse(f[2], dtf),
                LocalDateTime.parse(f[3], dtf), Long.parseLong(f[4]), Long.parseLong(f[5]),
                Double.parseDouble(f[6]), Double.parseDouble(f[7]));
    }

    @Override
    protected String getTripsMapName() {
        return "trips";
    }

    @Override
    protected List<String> executeJob() throws ExecutionException, InterruptedException {
        Job<Pair<Long, Long>, TripsRow> job = jobTracker.newJob(tripsKeyValueSource);
        return job
                .keyPredicate(new DistinctZonesPredicate())
                .mapper(new TripsCountMapper())
                .reducer(new TripsCountReducerFactory())
                .submit(new TripsCountCollator())
                .get();
    }

    @Override
    protected void writeResults(List<String> queryResults, List<String> timeResults, String outputPath) throws IOException {
        Path queryOutput = Paths.get(outputPath, "query1.csv");
        Path timeOutput = Paths.get(outputPath, "time1.txt");

        // query1.csv
        Files.write(queryOutput, List.of("pickUpZone;dropOffZone;trips"), StandardCharsets.UTF_8);
        Files.write(queryOutput, queryResults, StandardCharsets.UTF_8, StandardOpenOption.APPEND);

        // time1.txt
        Files.write(timeOutput, timeResults, StandardCharsets.UTF_8, StandardOpenOption.APPEND);
    }

    public static void main(String[] args) throws IOException, ExecutionException, InterruptedException {
        ParsedParameters params = ParametersParser.parseConsoleParameters();
        new Query1Client().start(params);
    }

}
