package ar.edu.itba.pod.combiner;

import java.time.YearMonth;

import com.hazelcast.mapreduce.Combiner;
import com.hazelcast.mapreduce.CombinerFactory;

import ar.edu.itba.pod.data.Pair;

@SuppressWarnings("deprecation")
public class CompanyYTDCombinerFactory implements CombinerFactory<Pair<String, YearMonth>, Double, Double> {

    @Override
    public Combiner<Double, Double> newCombiner(Pair<String, YearMonth> key) {
        return new CompanyYTDCombiner();
    }

    private static class CompanyYTDCombiner extends Combiner<Double, Double> {
        private double miles;

        @Override
        public void combine(Double value) {
            miles += value;
        }

        @Override
        public Double finalizeChunk() {
            return miles;
        }

        @Override
        public void reset() {
            miles = 0.0;
        }
    }
}
