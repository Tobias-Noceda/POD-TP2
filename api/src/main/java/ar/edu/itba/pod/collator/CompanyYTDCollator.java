package ar.edu.itba.pod.collator;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.time.YearMonth;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import com.hazelcast.mapreduce.Collator;

import ar.edu.itba.pod.data.Pair;

public class CompanyYTDCollator implements Collator<Map.Entry<Pair<String, YearMonth>, Double>, List<String>> {

    @Override
    public List<String> collate(Iterable<Map.Entry<Pair<String, YearMonth>, Double>> values) {

        DecimalFormat df = new DecimalFormat("#.##");
        df.setRoundingMode(RoundingMode.DOWN);

        AtomicReference<String> currentCompany = new AtomicReference<>("");
        AtomicReference<Integer> currentYear = new AtomicReference<>(-1);
        AtomicReference<Double> accumulated = new AtomicReference<>(0.0);
        // Note: Hazelcast calls collate() on a single thread, so sequential accumulation is safe
        return StreamSupport.stream(values.spliterator(), false)
        .sorted(
            Comparator
                .comparing((Map.Entry<Pair<String, YearMonth>, Double> e) -> e.getKey().getLeft())
                .thenComparing(e -> e.getKey().getRight().getYear())
                .thenComparing(e -> e.getKey().getRight().getMonthValue())
            )
        .map(e -> {
            String company = e.getKey().getLeft();
            YearMonth ym = e.getKey().getRight();
            double value = e.getValue();

            if (!company.equals(currentCompany.get()) || ym.getYear() != currentYear.get()) {
                currentCompany.set(company);
                currentYear.set(ym.getYear());
                accumulated.set(0.0);
            }
            accumulated.set(accumulated.get() + value);

            return String.format("%s;%d;%d;%s",
                e.getKey().getLeft(),                      
                e.getKey().getRight().getYear(),           
                e.getKey().getRight().getMonthValue(),     
                df.format(accumulated.get()));
        })
        .collect(Collectors.toList());
    }
}
