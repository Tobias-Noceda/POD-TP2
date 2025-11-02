package ar.edu.itba.pod.client;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.ExecutionException;

import com.hazelcast.mapreduce.JobCompletableFuture;

import ar.edu.itba.pod.client.lib.ParametersParser;
import ar.edu.itba.pod.client.lib.ParsedParameters;
import ar.edu.itba.pod.collator.LongestDelayCollator;
import ar.edu.itba.pod.data.TripsRow;
import ar.edu.itba.pod.mapper.LongestDelayMapper;
import ar.edu.itba.pod.reducer.LongestDelayReducerFactory;

public class Query4Client extends Client<Long> {

    private static final DateTimeFormatter DTF = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public Query4Client(ParsedParameters params) {
        super(params, DTF, 4);
    }

    @Override
    public Long getTripKey(TripsRow trip) {
        return trip.getPickupLocationID();
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

        Query4Client client = new Query4Client(params);
        
        client.start();

        JobCompletableFuture<List<String>> job = client.getTripsJob()
                .mapper(new LongestDelayMapper(params.borough))
                .reducer(new LongestDelayReducerFactory())
                .submit(new LongestDelayCollator());

        client.executeJob(job, "pickUpZone;dropOffZone;delayInSeconds");

        client.end();
    }
}
