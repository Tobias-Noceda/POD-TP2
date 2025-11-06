package ar.edu.itba.pod.data;

import java.io.IOException;

import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.DataSerializable;

public class TripDelayRaw implements DataSerializable {
    private Long pickupLocationId;
    private Long dropoffLocationId;
    private Long delay;

    public TripDelayRaw() {
        //just for hazel
    }

    public TripDelayRaw(Long pickupLocationId, Long dropoffLocationId, Long delay) {
        this.pickupLocationId = pickupLocationId;
        this.dropoffLocationId = dropoffLocationId;
        this.delay = delay;
    }
    
    public Long getPickupLocationId() {
        return pickupLocationId;
    }

    public Long getDropoffLocationId() {
        return dropoffLocationId;
    }

    public Long getDelay() {
        return delay;
    }

    public void setPickupLocationId(Long pickupLocationId) {
        this.pickupLocationId = pickupLocationId;
    }

    public void setDropoffLocationId(Long dropoffLocationId) {
        this.dropoffLocationId = dropoffLocationId;
    }

    public void setDelay(Long delay) {
        this.delay = delay;
    }

    @Override
    public void writeData(ObjectDataOutput out) throws IOException {
        out.writeLong(pickupLocationId);
        out.writeLong(dropoffLocationId);
        out.writeLong(delay);
    }

    @Override
    public void readData(ObjectDataInput in) throws IOException {
        pickupLocationId = in.readLong();
        dropoffLocationId = in.readLong();
        delay = in.readLong();
    }
}
