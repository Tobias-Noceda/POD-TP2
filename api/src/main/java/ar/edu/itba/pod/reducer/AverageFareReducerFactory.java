package ar.edu.itba.pod.reducer;

import com.hazelcast.mapreduce.Reducer;
import com.hazelcast.mapreduce.ReducerFactory;

import ar.edu.itba.pod.data.Pair;

public class AverageFareReducerFactory implements ReducerFactory<Pair<String, String>, Double, Double> {
    
    @Override
    public Reducer<Double, Double> newReducer(Pair<String, String> key) {
        return new AverageFareReducer();
    }

    private class AverageFareReducer extends Reducer<Double, Double> {
        private Double sum = 0.0;
        private Long trips = 0L;

        @Override
        public void reduce(Double value) {
            sum += value;
            trips++;
        }

        @Override
        public Double finalizeReduce() {
            return sum/trips;
        }
    }
}