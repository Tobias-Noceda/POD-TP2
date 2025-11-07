package ar.edu.itba.pod.client;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.ExecutionException;

import com.hazelcast.mapreduce.JobCompletableFuture;

import ar.edu.itba.pod.client.lib.ParametersParser;
import ar.edu.itba.pod.client.lib.ParsedParameters;
import ar.edu.itba.pod.collator.TripsCountCollator;
import ar.edu.itba.pod.data.Pair;
import ar.edu.itba.pod.mapper.TripsCountMapper;
import ar.edu.itba.pod.reducer.TripsCountReducerFactory;

public class Query1Client extends Client<Pair<Long, Long>> {

    private static final DateTimeFormatter DTF = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public Query1Client(ParsedParameters args) {
        super(args, DTF, 1);
    }

    @Override
    public Pair<Long, Long> getTripKey(String[] fields) {
        return new Pair<>(Long.valueOf(fields[4]), Long.valueOf(fields[5]));
    }

    @SuppressWarnings("deprecation")
    public static void main(String[] args) throws IOException, ExecutionException, InterruptedException {
        ParsedParameters params = ParametersParser.parseConsoleParameters();

        Query1Client client = new Query1Client(params);

        client.start();

    JobCompletableFuture<List<String>> job = client.getTripsJob()
        .mapper(new TripsCountMapper())
        .reducer(new TripsCountReducerFactory())
        .submit(new TripsCountCollator());

        client.executeJob(job, "pickUpZone;dropOffZone;trips");

        client.end();
    }
}
