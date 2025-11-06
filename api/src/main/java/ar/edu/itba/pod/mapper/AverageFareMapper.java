package ar.edu.itba.pod.mapper;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.HazelcastInstanceAware;
import com.hazelcast.core.IMap;
import com.hazelcast.mapreduce.Context;
import com.hazelcast.mapreduce.Mapper;

import ar.edu.itba.pod.data.Pair;
import ar.edu.itba.pod.data.TripFareData;
import ar.edu.itba.pod.data.TripsRow;
import ar.edu.itba.pod.data.ZonesRow;

public class AverageFareMapper implements Mapper<Long, TripFareData, Pair<String, String>, Double>, HazelcastInstanceAware {
    private transient IMap<Long, ZonesRow> zonesMap;

    @Override
    public void map(Long key, TripFareData row, Context<Pair<String, String>, Double> context) {
        ZonesRow pickupZone = zonesMap.get(row.getPickupLocation());
        ZonesRow dropoffZone = zonesMap.get(row.getDropoffLocation());
        if(pickupZone != null && dropoffZone != null && !pickupZone.getNeighbourhood().equals("N/A"))
            context.emit(new Pair<>(pickupZone.getNeighbourhood(), row.getCompany()), row.getFare());
    }

    @Override
    public void setHazelcastInstance(HazelcastInstance hazelcastInstance) {
        this.zonesMap = hazelcastInstance.getMap("zones");
    }
}