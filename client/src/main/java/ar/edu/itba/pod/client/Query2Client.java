package ar.edu.itba.pod.client;

import ar.edu.itba.pod.client.lib.ParametersParser;
import ar.edu.itba.pod.client.lib.ParsedParameters;
import ar.edu.itba.pod.collator.LongestTripCollator;
import ar.edu.itba.pod.data.TripsRow;
import ar.edu.itba.pod.mapper.LongestTripMapper;
import ar.edu.itba.pod.reducer.LongestTripReducerFactory;

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

public class Query2Client extends BaseClient<Long> {

    private static final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    protected Long getTripKey(TripsRow trip) {
        return trip.getPickupLocationID();
    }

    @Override
    protected TripsRow parseTripsRow(String[] fields) {
        return new TripsRow(
                fields[0],
                LocalDateTime.parse(fields[1], dateTimeFormatter),
                LocalDateTime.parse(fields[2], dateTimeFormatter),
                LocalDateTime.parse(fields[3], dateTimeFormatter),
                Long.parseLong(fields[4]),
                Long.parseLong(fields[5]),
                Double.parseDouble(fields[6]),
                Double.parseDouble(fields[7])
        );
    }

    @Override
    protected List<String> executeJob() throws ExecutionException, InterruptedException {
        return jobTracker.newJob(tripsKeyValueSource)
                .mapper(new LongestTripMapper())
                .reducer(new LongestTripReducerFactory())
                .submit(new LongestTripCollator())
                .get();
    }

    @Override
    protected void writeResults(List<String> queryResults, List<String> timeResults, String outputPath) throws IOException {
        Path queryOutput = Paths.get(outputPath, "query2.csv");
        Path timeOutput = Paths.get(outputPath, "time2.txt");

        // query2.csv
        Files.write(queryOutput, List.of("pickUpZone;longestDOZone;longestPUDateTime;longestMiles;longestCompany"), StandardCharsets.UTF_8);
        Files.write(queryOutput, queryResults, StandardCharsets.UTF_8, StandardOpenOption.APPEND);

        // time2.txt
        Files.write(timeOutput, timeResults, StandardCharsets.UTF_8, StandardOpenOption.APPEND);
    }

    public static void main(String[] args) throws IOException, ExecutionException, InterruptedException {
        ParsedParameters params = ParametersParser.parseConsoleParameters();
        Query2Client client = new Query2Client();
        client.start(params);
    }
}
