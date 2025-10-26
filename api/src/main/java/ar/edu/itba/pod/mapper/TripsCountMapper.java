package ar.edu.itba.pod.mapper;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.HazelcastInstanceAware;
import com.hazelcast.core.IMap;
import com.hazelcast.mapreduce.Context;
import com.hazelcast.mapreduce.Mapper;

import ar.edu.itba.pod.data.Pair;
import ar.edu.itba.pod.data.TripsRow;
import ar.edu.itba.pod.data.ZonesRow;

public class TripsCountMapper implements Mapper<Pair<Long, Long>, TripsRow, Pair<String, String>, Long>, HazelcastInstanceAware {
    private transient IMap<Long, ZonesRow> zonesMap;

    private static final Long ONE = 1L;

    @Override
    public void map(Pair<Long, Long> key, TripsRow row, Context<Pair<String, String>, Long> context) {
        ZonesRow pickupZone = zonesMap.get(key.getLeft());
        ZonesRow dropoffZone = zonesMap.get(key.getRight());
        if(pickupZone != null && dropoffZone != null) context.emit(new Pair<>(pickupZone.getZone(), dropoffZone.getZone()), ONE);
    }

    @Override
    public void setHazelcastInstance(HazelcastInstance hazelcastInstance) {
        this.zonesMap = hazelcastInstance.getMap("zones");
    }
}