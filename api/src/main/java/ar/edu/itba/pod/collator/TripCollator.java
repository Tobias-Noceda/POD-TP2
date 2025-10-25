package ar.edu.itba.pod.collator;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import com.hazelcast.mapreduce.Collator;

import ar.edu.itba.pod.data.Pair;

public class TripCollator implements Collator<Map.Entry<Pair<Long, Long>, Long>, List<String>> {
    @Override
    public List<String> collate(Iterable<Map.Entry<Pair<Long, Long>, Long>> values) {
        return StreamSupport.stream(values.spliterator(), false)
        .sorted(Map.Entry.<Pair<Long, Long>, Long>comparingByValue().reversed())
        //TODO: order by PU and DO names
        .map(e -> String.format("%d;%d;%d",
                    e.getKey().getLeft(),//TODO:print names not IDs
                    e.getKey().getRight(),
                    e.getValue()))
        .collect(Collectors.toList());
    }
}
