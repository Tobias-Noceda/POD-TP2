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

public class AverageFareCollator implements Collator<Map.Entry<Pair<String, String>, Double>, List<String>> {

    @Override
    public List<String> collate(Iterable<Map.Entry<Pair<String, String>, Double>> values) {

        DecimalFormat df = new DecimalFormat("#.##");
        df.setRoundingMode(RoundingMode.DOWN);

        return StreamSupport.stream(values.spliterator(), false)
        .sorted(
            Comparator
                .<Map.Entry<Pair<String, String>, Double>, Double>comparing(Map.Entry::getValue).reversed()
                .thenComparing(e -> e.getKey().getLeft()) 
                .thenComparing(e -> e.getKey().getRight())
            )
        .map(e -> {
                return String.format("%s;%s;%s",
                e.getKey().getLeft(),           
                e.getKey().getRight(),                                       
                df.format(e.getValue()))
        ;})
        .collect(Collectors.toList());
    }
}
