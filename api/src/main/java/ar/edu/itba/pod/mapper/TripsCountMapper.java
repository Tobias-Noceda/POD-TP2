package ar.edu.itba.pod.mapper;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.HazelcastInstanceAware;
import com.hazelcast.core.IMap;
import com.hazelcast.mapreduce.Context;
import com.hazelcast.mapreduce.Mapper;

import ar.edu.itba.pod.data.Pair;
import ar.edu.itba.pod.data.ZonesRow;

public class TripsCountMapper implements Mapper<Long, Pair<Long, Long>, Pair<String, String>, Long>, HazelcastInstanceAware {
    private transient IMap<Long, ZonesRow> zonesMap;

    private static final Long ONE = 1L;

    @Override
    public void map(Long key, Pair<Long, Long> row, Context<Pair<String, String>, Long> context) {
        if (!row.getLeft().equals(row.getRight())) {
            ZonesRow pickupZone = zonesMap.get(row.getLeft());
            ZonesRow dropoffZone = zonesMap.get(row.getRight());
            if(pickupZone != null && dropoffZone != null) context.emit(new Pair<>(pickupZone.getZone(), dropoffZone.getZone()), ONE);
        }
    }

    @Override
    public void setHazelcastInstance(HazelcastInstance hazelcastInstance) {
        this.zonesMap = hazelcastInstance.getMap("zones");
    }
}