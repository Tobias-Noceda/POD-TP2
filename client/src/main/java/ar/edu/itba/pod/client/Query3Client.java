package ar.edu.itba.pod.client;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.ExecutionException;

import com.hazelcast.mapreduce.JobCompletableFuture;

import ar.edu.itba.pod.client.lib.ParametersParser;
import ar.edu.itba.pod.client.lib.ParsedParameters;
import ar.edu.itba.pod.collator.AverageFareCollator;
import ar.edu.itba.pod.data.Pair;
import ar.edu.itba.pod.data.TripsRow;
import ar.edu.itba.pod.mapper.AverageFareMapper;
import ar.edu.itba.pod.reducer.AverageFareReducerFactory;

public class Query3Client extends Client<Pair<Long, String>> {

    private static final DateTimeFormatter DTF = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public Query3Client(ParsedParameters args) {
        super(args, DTF, 3);
    }

    @Override
    public Pair<Long, String> getTripKey(TripsRow trip) {
        return new Pair<>(trip.getPickupLocationID(), trip.getCompany());
    }

    @Override
    public TripsRow parseTripsRow(String[] fields) {
        return new TripsRow(
                fields[0],
                LocalDateTime.parse(fields[1], DTF),
                LocalDateTime.parse(fields[2], DTF),
                LocalDateTime.parse(fields[3], DTF),
                Long.valueOf(fields[4]),
                Long.valueOf(fields[5]),
                Double.valueOf(fields[6]),
                Double.valueOf(fields[7])
        );
    }

    public static void main(String[] args) throws IOException, ExecutionException, InterruptedException {
        ParsedParameters params = ParametersParser.parseConsoleParameters();
        Query3Client client = new Query3Client(params);

        client.start();

        JobCompletableFuture<List<String>> job = client.getTripsJob()
                .mapper(new AverageFareMapper())
                .reducer(new AverageFareReducerFactory())
                .submit(new AverageFareCollator());

        client.executeJob(job, "pickUpBorough;company;avgFare");

        client.end();
    }
}
