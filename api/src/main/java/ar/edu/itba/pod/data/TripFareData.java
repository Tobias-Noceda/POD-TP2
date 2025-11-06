package ar.edu.itba.pod.data;

import com.hazelcast.nio.serialization.DataSerializable;

public class TripFareData implements DataSerializable {
    
    private Long pickupLocationId = null;
    private Long dropoffLocationId = null;
    private Double fare = null;
    private String company = null;

    public TripFareData() {
        //just for hazel
    }

    public TripFareData(Long pickupLocationId, Long dropoffLocationId, Double fare, String company) {
        this.pickupLocationId = pickupLocationId;
        this.dropoffLocationId = dropoffLocationId;
        this.fare = fare;
        this.company = company;
    }

    public Long getPickupLocation() {
        return pickupLocationId;
    }

    public Long getDropoffLocation() {
        return dropoffLocationId;
    }

    public Double getFare() {
        return fare;
    }

    public String getCompany() {
        return company;
    }

    @Override
    public void writeData(com.hazelcast.nio.ObjectDataOutput out) throws java.io.IOException{
        out.writeLong(pickupLocationId);
        out.writeLong(dropoffLocationId);
        out.writeDouble(fare);
        out.writeUTF(company);
    }

    @Override
    public void readData(com.hazelcast.nio.ObjectDataInput in) throws java.io.IOException {
        pickupLocationId = in.readLong();
        dropoffLocationId = in.readLong();
        fare = in.readDouble();
        company = in.readUTF();
    }
}
