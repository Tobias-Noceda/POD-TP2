package ar.edu.itba.pod.mapper;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.HazelcastInstanceAware;
import com.hazelcast.core.IMap;
import com.hazelcast.mapreduce.Context;
import com.hazelcast.mapreduce.Mapper;

import ar.edu.itba.pod.data.Pair;
import ar.edu.itba.pod.data.TripDelayRaw;
import ar.edu.itba.pod.data.ZonesRow;

public class LongestDelayMapper implements Mapper<Long, TripDelayRaw, String, Pair<String, Long>>, HazelcastInstanceAware {
    private transient IMap<Long, ZonesRow> zonesMap;

    private final String borough;  

    public LongestDelayMapper(String borough) {
        this.borough = borough;  
    }

    @Override
    public void map(Long key, TripDelayRaw row, Context<String, Pair<String, Long>> context) {
        ZonesRow pickupZone = zonesMap.get(row.getPickupLocationId());
        ZonesRow dropoffZone = zonesMap.get(row.getDropoffLocationId());
        if(pickupZone != null && dropoffZone != null && pickupZone.getNeighbourhood().equals(borough)){
            long delay = row.getDelay();
            context.emit(pickupZone.getZone(), new Pair<>(dropoffZone.getZone(), delay));
        }  
    }

    @Override
    public void setHazelcastInstance(HazelcastInstance hazelcastInstance) {
        this.zonesMap = hazelcastInstance.getMap("zones");
    }
}