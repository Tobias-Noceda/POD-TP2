package ar.edu.itba.pod.reducer;

import com.hazelcast.mapreduce.Reducer;
import com.hazelcast.mapreduce.ReducerFactory;

import ar.edu.itba.pod.data.Pair;

public class LongestDelayReducerFactory implements ReducerFactory<String, Pair<String, Double>, Pair<String, Double>> {
    
    @Override
    public Reducer<Pair<String, Double>, Pair<String, Double>> newReducer(String key) {
        return new LongestDelayReducer();
    }

    private class LongestDelayReducer extends Reducer<Pair<String, Double>, Pair<String, Double>> {
        private Pair<String, Double> longestDelay = null;

        @Override
        public void reduce(Pair<String, Double> value) {
            if (longestDelay==null) longestDelay = value;
            else if(longestDelay.getRight()<value.getRight()) longestDelay = value;
            else if(longestDelay.getRight().equals(value.getRight()) && longestDelay.getLeft().compareTo(value.getLeft())>0) longestDelay = value;
        }

        @Override
        public Pair<String, Double> finalizeReduce() {
            return longestDelay;
        }
    }
}