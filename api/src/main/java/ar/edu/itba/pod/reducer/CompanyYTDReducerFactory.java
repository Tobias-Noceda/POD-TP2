package ar.edu.itba.pod.reducer;

import java.time.YearMonth;

import com.hazelcast.mapreduce.Reducer;
import com.hazelcast.mapreduce.ReducerFactory;

import ar.edu.itba.pod.data.Pair;

public class CompanyYTDReducerFactory implements ReducerFactory<Pair<String, YearMonth>, Double, Double> {
    
    @Override
    public Reducer<Double, Double> newReducer(Pair<String, YearMonth> key) {
        return new CompanyYTDReducer();
    }

    private class CompanyYTDReducer extends Reducer<Double, Double> {
        private Double monthlyMiles = 0.0;

        @Override
        public void reduce(Double value) {
            monthlyMiles += value;
        }

        @Override
        public Double finalizeReduce() {
            return monthlyMiles;
        }
    }
}