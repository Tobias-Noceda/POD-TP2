package ar.edu.itba.pod.reducer;

import com.hazelcast.mapreduce.Reducer;
import com.hazelcast.mapreduce.ReducerFactory;

import ar.edu.itba.pod.data.Pair;

public class TripsCountReducerFactory implements ReducerFactory<Pair<String, String>, Long, Long> {

    
    @Override
    public Reducer<Long, Long> newReducer(Pair<String, String> key) {
        return new TripsCountReducer();
    }

    private class TripsCountReducer extends Reducer<Long, Long> {
        private Long sum = 0L;

        @Override
        public void reduce(Long value) {
            sum += value;
        }

        @Override
        public Long finalizeReduce() {
            return sum;
        }
    }
}