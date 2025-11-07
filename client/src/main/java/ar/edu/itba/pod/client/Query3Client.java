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
import ar.edu.itba.pod.combiner.AverageFareCombinerFactory;
import ar.edu.itba.pod.data.Pair;
import ar.edu.itba.pod.data.TripFareData;
import ar.edu.itba.pod.data.TripsRow;
import ar.edu.itba.pod.mapper.AverageFareMapper;
import ar.edu.itba.pod.reducer.AverageFareReducerFactory;
import ar.edu.itba.pod.reducer.AverageFareReducerFactoryNoCombiner;

public class Query3Client extends Client<TripFareData> {

    private static final DateTimeFormatter DTF = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public Query3Client(ParsedParameters args) {
        super(args, DTF, 3);
    }

    @Override
    public TripFareData getTripKey(String[] fields) {
        return new TripFareData(
                Long.valueOf(fields[4]),
                Long.valueOf(fields[5]),
                Double.valueOf(fields[7]),
                fields[0]
        );
    }

    @SuppressWarnings("deprecation")
    public static void main(String[] args) throws IOException, ExecutionException, InterruptedException {
        ParsedParameters params = ParametersParser.parseConsoleParameters();
        Query3Client client = new Query3Client(params);

        client.start();

    JobCompletableFuture<List<String>> job = client.getTripsJob()
        .mapper(new AverageFareMapper())
        .combiner(new AverageFareCombinerFactory())
        .reducer(new AverageFareReducerFactory())
        .submit(new AverageFareCollator());

        client.executeJob(job, "pickUpBorough;company;avgFare");

        client.end();
    }
}
