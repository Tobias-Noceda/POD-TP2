package ar.edu.itba.pod.mapper;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.HazelcastInstanceAware;
import com.hazelcast.core.IMap;
import com.hazelcast.mapreduce.Context;
import com.hazelcast.mapreduce.Mapper;

import ar.edu.itba.pod.data.LongestTripData;
import ar.edu.itba.pod.data.TripsRow;
import ar.edu.itba.pod.data.ZonesRow;

public class LongestTripMapper implements Mapper<Long, TripsRow, String, LongestTripData>, HazelcastInstanceAware {
    private transient IMap<Long, ZonesRow> zonesMap;

    @Override
    public void map(Long key, TripsRow row, Context<String, LongestTripData> context) {
        ZonesRow pickupZone = zonesMap.get(key);
        ZonesRow dropoffZone = zonesMap.get(key);
        if(pickupZone != null && dropoffZone != null && !pickupZone.getNeighbourhood().equals("N/A") && !dropoffZone.getNeighbourhood().equals("N/A"))
            context.emit(pickupZone.getZone(), new LongestTripData(
                                                    pickupZone.getZone(),
                                                    dropoffZone.getZone(),
                                                    row.getRequestDateTime(),
                                                    row.getMiles(),
                                                    row.getCompany()));
    }

    @Override
    public void setHazelcastInstance(HazelcastInstance hazelcastInstance) {
        this.zonesMap = hazelcastInstance.getMap("zones");
    }
}