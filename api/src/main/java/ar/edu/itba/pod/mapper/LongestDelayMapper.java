package ar.edu.itba.pod.mapper;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.HazelcastInstanceAware;
import com.hazelcast.core.IMap;
import com.hazelcast.mapreduce.Context;
import com.hazelcast.mapreduce.Mapper;

import ar.edu.itba.pod.data.Pair;
import ar.edu.itba.pod.data.TripsRow;
import ar.edu.itba.pod.data.ZonesRow;

public class LongestDelayMapper implements Mapper<Long, TripsRow, String, Pair<String, Double>>, HazelcastInstanceAware {
    private transient IMap<Long, ZonesRow> zonesMap;

    private final String borough;  

    public LongestDelayMapper(String borough) {
        this.borough = borough;  
    }

    @Override
    public void map(Long key, TripsRow row, Context<String, Pair<String, Double>> context) {
        ZonesRow pickupZone = zonesMap.get(key);
        ZonesRow dropoffZone = zonesMap.get(row.getDropoffLocationID());
        if(pickupZone != null && dropoffZone != null && pickupZone.getNeighbourhood().equals(borough)){
            long delay = java.time.Duration.between(
                row.getRequestDateTime(),  
                row.getPickupDateTime()    
            ).getSeconds();
            context.emit(pickupZone.getZone(), new Pair<>(dropoffZone.getZone(), Double.valueOf(delay)));
        }  
    }

    @Override
    public void setHazelcastInstance(HazelcastInstance hazelcastInstance) {
        this.zonesMap = hazelcastInstance.getMap("zones");
    }
}