package ar.edu.itba.pod.mapper;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.HazelcastInstanceAware;
import com.hazelcast.core.IMap;
import com.hazelcast.mapreduce.Context;
import com.hazelcast.mapreduce.Mapper;

import ar.edu.itba.pod.data.LongestTripData;
import ar.edu.itba.pod.data.LongestTripDataRaw;
import ar.edu.itba.pod.data.Pair;
import ar.edu.itba.pod.data.ZonesRow;

public class LongestTripMapper implements Mapper<Long, Pair<Long, LongestTripDataRaw>, String, LongestTripData>, HazelcastInstanceAware {
    private transient IMap<Long, ZonesRow> zonesMap;

    @Override
    public void map(Long key, Pair<Long, LongestTripDataRaw> value, Context<String, LongestTripData> context) {
        ZonesRow pickupZone = zonesMap.get(value.getRight().getPickupLocation());
        ZonesRow dropoffZone = zonesMap.get(value.getRight().getDropoffLocation());
        if(pickupZone != null && dropoffZone != null && !pickupZone.getNeighbourhood().equals("N/A") && !dropoffZone.getNeighbourhood().equals("N/A"))
            context.emit(pickupZone.getZone(), new LongestTripData(
                                                    pickupZone.getZone(),
                                                    dropoffZone.getZone(),
                                                    value.getRight().getRequestDateTime(),
                                                    value.getRight().getMiles(),
                                                    value.getRight().getCompany()));
    }

    @Override
    public void setHazelcastInstance(HazelcastInstance hazelcastInstance) {
        this.zonesMap = hazelcastInstance.getMap("zones");
    }
}