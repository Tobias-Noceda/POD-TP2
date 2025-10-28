package ar.edu.itba.pod.client;

import ar.edu.itba.pod.client.lib.ParametersParser;
import ar.edu.itba.pod.client.lib.ParsedParameters;
import ar.edu.itba.pod.collator.CompanyYTDCollator;
import ar.edu.itba.pod.data.Pair;
import ar.edu.itba.pod.data.TripsRow;
import ar.edu.itba.pod.mapper.CompanyYTDMapper;
import ar.edu.itba.pod.reducer.CompanyYTDReducerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class Query5Client extends BaseClient<Pair<String, YearMonth>> {

    private static final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    protected Pair<String, YearMonth> getTripKey(TripsRow trip) {
        return new Pair<>(trip.getCompany(),
                YearMonth.of(trip.getRequestDateTime().getYear(),
                        trip.getRequestDateTime().getMonth()));
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
                .mapper(new CompanyYTDMapper())
                .reducer(new CompanyYTDReducerFactory())
                .submit(new CompanyYTDCollator())
                .get();
    }

    @Override
    protected void writeResults(List<String> queryResults, List<String> timeResults, String outputPath) throws IOException {
        Path queryOutput = Paths.get(outputPath, "query5.csv");
        Path timeOutput = Paths.get(outputPath, "time5.txt");

        // query5.csv
        Files.write(queryOutput, List.of("company;year;month;milesYTD"), StandardCharsets.UTF_8);
        Files.write(queryOutput, queryResults, StandardCharsets.UTF_8, StandardOpenOption.APPEND);

        // time5.txt
        Files.write(timeOutput, timeResults, StandardCharsets.UTF_8, StandardOpenOption.APPEND);
    }

    public static void main(String[] args) throws IOException, ExecutionException, InterruptedException {
        ParsedParameters params = ParametersParser.parseConsoleParameters();
        Query5Client client = new Query5Client();
        client.start(params);
    }
}
