package ar.edu.itba.pod.reducer;

import com.hazelcast.mapreduce.Reducer;
import com.hazelcast.mapreduce.ReducerFactory;

import ar.edu.itba.pod.data.Pair;

@SuppressWarnings("deprecation")
public class AverageFareReducerFactory implements ReducerFactory<Pair<String, String>, Pair<Double, Long>, Double> {
    
    @Override
    public Reducer<Pair<Double, Long>, Double> newReducer(Pair<String, String> key) {
        return new AverageFareReducer();
    }

    private class AverageFareReducer extends Reducer<Pair<Double, Long>, Double> {
        private double sum = 0.0;
        private long trips = 0L;

        @Override
        public void reduce(Pair<Double, Long> value) {
            sum += value.getLeft();
            trips += value.getRight();
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