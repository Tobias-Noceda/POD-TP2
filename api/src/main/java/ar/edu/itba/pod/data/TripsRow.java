package ar.edu.itba.pod.data;

import java.io.IOException;
import java.time.LocalDateTime;

import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.DataSerializable;

public class TripsRow implements DataSerializable {

    private String company = null;
    private LocalDateTime requestDateTime = null;
    private LocalDateTime pickupDateTime = null;
    private LocalDateTime dropoffDateTime = null;
    private Long pickupLocationID = null;
    private Long dropoffLocationID = null;
    private Double miles = null;
    private Double fare = null;


    public TripsRow() {
        //just for hazel
    }

    public TripsRow(String company, LocalDateTime requestDateTime, LocalDateTime pickupDateTime, LocalDateTime dropoffDateTime,
    Long pickupLocationID, Long dropoffLocationID, Double miles, Double fare) {
        this.company = company;
        this.requestDateTime = requestDateTime;
        this.pickupDateTime = pickupDateTime;
        this.dropoffDateTime = dropoffDateTime;
        this.pickupLocationID = pickupLocationID;
        this.dropoffLocationID = dropoffLocationID;
        this.miles = miles;
        this.fare = fare;
    }

    public String getCompany() {
        return company;
    }

    public LocalDateTime getRequestDateTime() {
        return requestDateTime;
    }

    public LocalDateTime getPickupDateTime() {
        return pickupDateTime;
    }

    public LocalDateTime getDropoffDateTime() {
        return dropoffDateTime;
    }

    public Long getPickupLocationID() {
        return pickupLocationID;
    }

    public Long getDropoffLocationID() {
        return dropoffLocationID;
    }

    public Double getMiles() {
        return miles;
    }

    public Double getFare() {
        return fare;
    }

    @Override
    public void readData(ObjectDataInput in) throws IOException {
        company = in.readUTF();
        requestDateTime = in.readObject();
        pickupDateTime = in.readObject();
        dropoffDateTime = in.readObject();
        pickupLocationID = in.readLong();
        dropoffLocationID = in.readLong();
        miles = in.readDouble();
        fare = in.readDouble();
    }

    @Override
    public void writeData(ObjectDataOutput out) throws IOException {
        out.writeUTF(company);
        out.writeObject(requestDateTime);
        out.writeObject(pickupDateTime);
        out.writeObject(dropoffDateTime);
        out.writeLong(pickupLocationID);
        out.writeLong(dropoffLocationID);
        out.writeDouble(miles);
        out.writeDouble(fare);
    }

}
