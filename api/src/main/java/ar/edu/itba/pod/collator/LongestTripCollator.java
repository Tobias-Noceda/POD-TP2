package ar.edu.itba.pod.collator;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import com.hazelcast.mapreduce.Collator;

import ar.edu.itba.pod.data.LongestTripData;

public class LongestTripCollator implements Collator<Map.Entry<String, LongestTripData>, List<String>> {

    @Override
    public List<String> collate(Iterable<Map.Entry<String, LongestTripData>> values) {

        DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
        DecimalFormat df = new DecimalFormat("0.00");
        df.setRoundingMode(RoundingMode.DOWN);

        return StreamSupport.stream(values.spliterator(), false)
        .sorted(
            Comparator
                .<Map.Entry<String, LongestTripData>, String>comparing(Map.Entry::getKey)
            )
        .map(e -> {
            LongestTripData v = e.getValue();
                return String.format("%s;%s;%s;%s;%s",
                e.getKey(),                        
                v.getDropoffLocation(),                
                outputFormatter.format(v.getRequestDateTime()),             
                df.format(v.getMiles()),           
                v.getCompany())
        ;})
        .collect(Collectors.toList());
    }
}
