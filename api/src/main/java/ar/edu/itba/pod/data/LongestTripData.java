package ar.edu.itba.pod.data;

import java.io.IOException;
import java.time.LocalDateTime;

import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.DataSerializable;

public class LongestTripData implements DataSerializable {

    private String pickupLocation = null;
    private String dropoffLocation = null;
    private LocalDateTime requestDateTime = null;
    private Double miles = null;
    private String company = null;


    public LongestTripData() {
        //just for hazel
    }

    public LongestTripData(String pickupLocation, String dropoffLocation, LocalDateTime requestDateTime,
    Double miles, String company) {
        this.pickupLocation = pickupLocation;
        this.dropoffLocation = dropoffLocation;
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
    
    public String getPickupLocation() {
        return pickupLocation;
    }

    public String getDropoffLocation() {
        return dropoffLocation;
    }

    public Double getMiles() {
        return miles;
    }

    @Override
    public void readData(ObjectDataInput in) throws IOException {
        pickupLocation = in.readUTF();
        dropoffLocation = in.readUTF();
        requestDateTime = in.readObject();
        miles = in.readDouble();
        company = in.readUTF();
    }

    @Override
    public void writeData(ObjectDataOutput out) throws IOException {
        out.writeUTF(pickupLocation);
        out.writeUTF(dropoffLocation);
        out.writeObject(requestDateTime);
        out.writeDouble(miles);
        out.writeUTF(company);
    }

}
