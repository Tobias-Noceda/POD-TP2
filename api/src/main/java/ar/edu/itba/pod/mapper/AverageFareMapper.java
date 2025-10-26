package ar.edu.itba.pod.mapper;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.HazelcastInstanceAware;
import com.hazelcast.core.IMap;
import com.hazelcast.mapreduce.Context;
import com.hazelcast.mapreduce.Mapper;

import ar.edu.itba.pod.data.Pair;
import ar.edu.itba.pod.data.TripsRow;
import ar.edu.itba.pod.data.ZonesRow;

public class AverageFareMapper implements Mapper<Pair<Long, String>, TripsRow, Pair<String, String>, Double>, HazelcastInstanceAware {
    private transient IMap<Long, ZonesRow> zonesMap;

    @Override
    public void map(Pair<Long, String> key, TripsRow row, Context<Pair<String, String>, Double> context) {
        ZonesRow pickupZone = zonesMap.get(key.getLeft());
        ZonesRow dropoffZone = zonesMap.get(row.getDropoffLocationID());
        if(pickupZone != null && dropoffZone != null && !pickupZone.getNeighbourhood().equals("N/A"))
            context.emit(new Pair<>(pickupZone.getNeighbourhood(), row.getCompany()), row.getFare());
    }

    @Override
    public void setHazelcastInstance(HazelcastInstance hazelcastInstance) {
        this.zonesMap = hazelcastInstance.getMap("zones");
    }
}