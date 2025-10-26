package ar.edu.itba.pod.reducer;

import com.hazelcast.mapreduce.Reducer;
import com.hazelcast.mapreduce.ReducerFactory;

import ar.edu.itba.pod.data.LongestTripData;

public class LongestTripReducerFactory implements ReducerFactory<String, LongestTripData, LongestTripData> {
    
    @Override
    public Reducer<LongestTripData, LongestTripData> newReducer(String key) {
        return new LongestTripReducer();
    }

    private class LongestTripReducer extends Reducer<LongestTripData, LongestTripData> {
        private LongestTripData longestMiles = null;

        @Override
        public void reduce(LongestTripData value) {
            if(longestMiles == null) longestMiles = value;
            else if(longestMiles.getMiles()<value.getMiles()) longestMiles = value;
            else if(longestMiles.getMiles().equals(value.getMiles()) && value.getRequestDateTime().isBefore(longestMiles.getRequestDateTime())) longestMiles = value;
        }

        @Override
        public LongestTripData finalizeReduce() {
            return longestMiles;
        }
    }
}