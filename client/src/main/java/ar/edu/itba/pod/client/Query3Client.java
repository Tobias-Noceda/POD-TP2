package ar.edu.itba.pod.client;

import ar.edu.itba.pod.client.lib.ParametersParser;
import ar.edu.itba.pod.client.lib.ParsedParameters;
import ar.edu.itba.pod.collator.AverageFareCollator;
import ar.edu.itba.pod.data.Pair;
import ar.edu.itba.pod.data.TripsRow;
import ar.edu.itba.pod.mapper.AverageFareMapper;
import ar.edu.itba.pod.reducer.AverageFareReducerFactory;

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

public class Query3Client extends BaseClient<Pair<Long, String>> {

    private static final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    protected Pair<Long, String> getTripKey(TripsRow trip) {
        return new Pair<>(trip.getPickupLocationID(), trip.getCompany());
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
                .mapper(new AverageFareMapper())
                .reducer(new AverageFareReducerFactory())
                .submit(new AverageFareCollator())
                .get();
    }

    @Override
    protected void writeResults(List<String> queryResults, List<String> timeResults, String outputPath) throws IOException {
        Path queryOutput = Paths.get(outputPath, "query3.csv");
        Path timeOutput = Paths.get(outputPath, "time3.txt");

        // query3.csv
        Files.write(queryOutput, List.of("pickUpBorough;company;avgFare"), StandardCharsets.UTF_8);
        Files.write(queryOutput, queryResults, StandardCharsets.UTF_8, StandardOpenOption.APPEND);

        // time3.txt
        Files.write(timeOutput, timeResults, StandardCharsets.UTF_8, StandardOpenOption.APPEND);
    }

    public static void main(String[] args) throws IOException, ExecutionException, InterruptedException {
        Query3Client client = new Query3Client();
        ParsedParameters params = ParametersParser.parseConsoleParameters();
        client.start(params);
    }
}
