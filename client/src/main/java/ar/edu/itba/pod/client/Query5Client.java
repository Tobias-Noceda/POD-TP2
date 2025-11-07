package ar.edu.itba.pod.client;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.ExecutionException;

import com.hazelcast.mapreduce.JobCompletableFuture;

import ar.edu.itba.pod.client.lib.ParametersParser;
import ar.edu.itba.pod.client.lib.ParsedParameters;
import ar.edu.itba.pod.collator.CompanyYTDCollator;
import ar.edu.itba.pod.data.Pair;
import ar.edu.itba.pod.data.TripsRow;
import ar.edu.itba.pod.data.YTDDataRaw;
import ar.edu.itba.pod.mapper.CompanyYTDMapper;
import ar.edu.itba.pod.reducer.CompanyYTDReducerFactory;

public class Query5Client extends Client<YTDDataRaw> {

    private static final DateTimeFormatter DTF = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public Query5Client(ParsedParameters args) {
        super(args, DTF, 5);
    }

    @Override
    public YTDDataRaw getTripKey(String[] fields) {
        return new YTDDataRaw(
                fields[0],
                YearMonth.of(
                        LocalDateTime.parse(fields[1], DTF).getYear(),
                        LocalDateTime.parse(fields[1], DTF).getMonthValue()
                ),
                Double.parseDouble(fields[6])
        );
    }

    @SuppressWarnings("deprecation")
    public static void main(String[] args) throws IOException, ExecutionException, InterruptedException {
        ParsedParameters params = ParametersParser.parseConsoleParameters();
        Query5Client client = new Query5Client(params);

        client.start();

        JobCompletableFuture<List<String>> job = client.getTripsJob()
                .mapper(new CompanyYTDMapper())
                .reducer(new CompanyYTDReducerFactory())
                .submit(new CompanyYTDCollator());

        client.executeJob(job, "company;year;month;milesYTD");
    }
}
