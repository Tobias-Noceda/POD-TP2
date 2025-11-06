package ar.edu.itba.pod.reducer;

import com.hazelcast.mapreduce.Reducer;
import com.hazelcast.mapreduce.ReducerFactory;

import ar.edu.itba.pod.data.Pair;

public class LongestDelayReducerFactory implements ReducerFactory<String, Pair<String, Long>, Pair<String, Long>> {
    
    @Override
    public Reducer<Pair<String, Long>, Pair<String, Long>> newReducer(String key) {
        return new LongestDelayReducer();
    }

    private class LongestDelayReducer extends Reducer<Pair<String, Long>, Pair<String, Long>> {
        private Pair<String, Long> longestDelay = null;

        @Override
        public void reduce(Pair<String, Long> value) {
            if (longestDelay==null) longestDelay = value;
            else if(longestDelay.getRight()<value.getRight()) longestDelay = value;
            else if(longestDelay.getRight().equals(value.getRight()) && longestDelay.getLeft().compareTo(value.getLeft())>0) longestDelay = value;
        }

        @Override
        public Pair<String, Long> finalizeReduce() {
            return longestDelay;
        }
    }
}