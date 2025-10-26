package ar.edu.itba.pod.collator;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import com.hazelcast.mapreduce.Collator;

import ar.edu.itba.pod.data.Pair;

public class TripsCountCollator implements Collator<Map.Entry<Pair<String, String>, Long>, List<String>> {
    @Override
    public List<String> collate(Iterable<Map.Entry<Pair<String, String>, Long>> values) {
        return StreamSupport.stream(values.spliterator(), false)
        .sorted(
            Comparator
                .<Map.Entry<Pair<String, String>, Long>, Long>comparing(Map.Entry::getValue).reversed() 
                .thenComparing(e -> e.getKey().getLeft()) 
                .thenComparing(e -> e.getKey().getRight())
        )
        .map(e -> String.format("%s;%s;%d",
                    e.getKey().getLeft(),
                    e.getKey().getRight(),
                    e.getValue()))
        .collect(Collectors.toList());
    }
}
