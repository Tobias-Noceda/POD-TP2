package ar.edu.itba.pod.mapper;

import com.hazelcast.mapreduce.Context;
import com.hazelcast.mapreduce.Mapper;

import ar.edu.itba.pod.data.Pair;
import ar.edu.itba.pod.data.TripsRow;

public class TripsTokenizerMapper implements Mapper<Pair<Long, Long>, TripsRow, Pair<Long, Long>, Long> {
    private static final Long ONE = 1L;

    @Override
    public void map(Pair<Long, Long> key, TripsRow row, Context<Pair<Long, Long>, Long> context) {
        context.emit(key, ONE);
    }
}