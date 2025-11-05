package ar.edu.itba.pod.combiner;

import com.hazelcast.mapreduce.Combiner;
import com.hazelcast.mapreduce.CombinerFactory;

import ar.edu.itba.pod.data.Pair;

@SuppressWarnings("deprecation")
public class AverageFareCombinerFactory implements CombinerFactory<Pair<String, String>, Double, Pair<Double, Long>> {

    @Override
    public Combiner<Double, Pair<Double, Long>> newCombiner(Pair<String, String> key) {
        return new AverageFareCombiner();
    }

    private static class AverageFareCombiner extends Combiner<Double, Pair<Double, Long>> {
        private double sum;
        private long trips;

        @Override
        public void combine(Double value) {
            sum += value;
            trips++;
        }

        @Override
        public Pair<Double, Long> finalizeChunk() {
            return new Pair<>(sum, trips);
        }

        @Override
        public void reset() {
            sum = 0.0;
            trips = 0L;
        }
    }
}
