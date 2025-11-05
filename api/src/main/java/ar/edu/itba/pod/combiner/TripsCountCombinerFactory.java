package ar.edu.itba.pod.combiner;

import com.hazelcast.mapreduce.Combiner;
import com.hazelcast.mapreduce.CombinerFactory;

import ar.edu.itba.pod.data.Pair;

@SuppressWarnings("deprecation")
public class TripsCountCombinerFactory implements CombinerFactory<Pair<String, String>, Long, Long> {

    @Override
    public Combiner<Long, Long> newCombiner(Pair<String, String> key) {
        return new TripsCountCombiner();
    }

    private static class TripsCountCombiner extends Combiner<Long, Long> {
        private long count;

        @Override
        public void combine(Long value) {
            count += value;
        }

        @Override
        public Long finalizeChunk() {
            return count;
        }

        @Override
        public void reset() {
            count = 0L;
        }
    }
}
