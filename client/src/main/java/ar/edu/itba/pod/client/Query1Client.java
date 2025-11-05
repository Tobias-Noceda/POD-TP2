package ar.edu.itba.pod.client;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.ExecutionException;

import com.hazelcast.mapreduce.JobCompletableFuture;

import ar.edu.itba.pod.client.lib.ParametersParser;
import ar.edu.itba.pod.client.lib.ParsedParameters;
import ar.edu.itba.pod.collator.TripsCountCollator;
import ar.edu.itba.pod.combiner.TripsCountCombinerFactory;
import ar.edu.itba.pod.data.Pair;
import ar.edu.itba.pod.data.TripsRow;
import ar.edu.itba.pod.keyPredicate.DistinctZonesPredicate;
import ar.edu.itba.pod.mapper.TripsCountMapper;
import ar.edu.itba.pod.reducer.TripsCountReducerFactory;

public class Query1Client extends Client<Pair<Long, Long>> {

    private static final DateTimeFormatter DTF = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public Query1Client(ParsedParameters args) {
        super(args, DTF, 1);
    }

    @Override
    public Pair<Long, Long> getTripKey(TripsRow trip) {
        return new Pair<>(trip.getPickupLocationID(), trip.getDropoffLocationID());
    }

    @Override
    public TripsRow parseTripsRow(String[] f) {
        return new TripsRow(f[0], LocalDateTime.parse(f[1], DTF), LocalDateTime.parse(f[2], DTF),
                LocalDateTime.parse(f[3], DTF), Long.valueOf(f[4]), Long.valueOf(f[5]),
                Double.valueOf(f[6]), Double.valueOf(f[7]));
    }

    @SuppressWarnings("deprecation")
    public static void main(String[] args) throws IOException, ExecutionException, InterruptedException {
        ParsedParameters params = ParametersParser.parseConsoleParameters();

        Query1Client client = new Query1Client(params);

        client.start();

    JobCompletableFuture<List<String>> job = client.getTripsJob()
        .keyPredicate(new DistinctZonesPredicate())
        .mapper(new TripsCountMapper())
        .combiner(new TripsCountCombinerFactory())
        .reducer(new TripsCountReducerFactory())
        .submit(new TripsCountCollator());

        client.executeJob(job, "pickUpZone;dropOffZone;tripsCount");

        client.end();
    }
}
