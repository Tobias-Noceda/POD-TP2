package ar.edu.itba.pod.keyPredicate;

import com.hazelcast.mapreduce.KeyPredicate;

import ar.edu.itba.pod.data.Pair;

public class DistinctZonesPredicate implements KeyPredicate<Pair<Long, Long>> {
    
    @Override
    public boolean evaluate(Pair<Long, Long> key) {
        return !key.getLeft().equals(key.getRight());
    }
}