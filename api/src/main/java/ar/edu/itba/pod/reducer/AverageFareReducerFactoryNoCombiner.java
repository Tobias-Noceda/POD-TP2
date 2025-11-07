package ar.edu.itba.pod.reducer;

import com.hazelcast.mapreduce.Reducer;
import com.hazelcast.mapreduce.ReducerFactory;

import ar.edu.itba.pod.data.Pair;

@SuppressWarnings("deprecation")
public class AverageFareReducerFactoryNoCombiner implements ReducerFactory<Pair<String, String>, Double, Double> {

    @Override
    public Reducer<Double, Double> newReducer(Pair<String, String> key) {
        return new AverageFareReducer();
    }

    private class AverageFareReducer extends Reducer<Double, Double> {
        private double sum = 0.0;
        private long trips = 0L;

        @Override
        public void reduce(Double value) {
            sum += value;
            trips++;
        }

        @Override
        public Double finalizeReduce() {
            if (trips == 0L) {
                return 0.0;
            }
            return sum / trips;
        }
    }
}