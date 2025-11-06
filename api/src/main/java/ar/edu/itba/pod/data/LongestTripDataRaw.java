package ar.edu.itba.pod.data;

import java.io.IOException;
import java.time.LocalDateTime;

import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.DataSerializable;

public class LongestTripDataRaw implements DataSerializable {

    private Long pickupLocationId = null;
    private Long dropoffLocationId = null;
    private LocalDateTime requestDateTime = null;
    private Double miles = null;
    private String company = null;


    public LongestTripDataRaw() {
        //just for hazel
    }

    public LongestTripDataRaw(Long pickupLocationId, Long dropoffLocationId, LocalDateTime requestDateTime,
    Double miles, String company) {
        this.pickupLocationId = pickupLocationId;
        this.dropoffLocationId = dropoffLocationId;
        this.requestDateTime = requestDateTime;
        this.miles = miles;
        this.company = company;
    }

    public String getCompany() {
        return company;
    }

    public LocalDateTime getRequestDateTime() {
        return requestDateTime;
    }
    
    public Long getPickupLocation() {
        return pickupLocationId;
    }

    public Long getDropoffLocation() {
        return dropoffLocationId;
    }

    public Double getMiles() {
        return miles;
    }

    @Override
    public void readData(ObjectDataInput in) throws IOException {
        pickupLocationId = in.readLong();
        dropoffLocationId = in.readLong();
        requestDateTime = in.readObject();
        miles = in.readDouble();
        company = in.readUTF();
    }

    @Override
    public void writeData(ObjectDataOutput out) throws IOException {
        out.writeLong(pickupLocationId);
        out.writeLong(dropoffLocationId);
        out.writeObject(requestDateTime);
        out.writeDouble(miles);
        out.writeUTF(company);
    }

}
