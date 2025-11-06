package ar.edu.itba.pod.collator;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import com.hazelcast.mapreduce.Collator;

import ar.edu.itba.pod.data.Pair;

public class LongestDelayCollator implements Collator<Map.Entry<String, Pair<String, Long>>, List<String>> {

    @Override
    public List<String> collate(Iterable<Map.Entry<String, Pair<String, Long>>> values) {

        DecimalFormat df = new DecimalFormat("#.##");
        df.setRoundingMode(RoundingMode.DOWN);

        return StreamSupport.stream(values.spliterator(), false)
        .sorted(
            Comparator
                .<Map.Entry<String, Pair<String, Long>>, String>comparing(Map.Entry::getKey)
            )
        .map(e -> {
            Pair<String, Long> v = e.getValue();
                return String.format("%s;%s;%s",
                e.getKey(),
                v.getLeft(),
                v.getRight())
        ;})
        .collect(Collectors.toList());
    }
}
