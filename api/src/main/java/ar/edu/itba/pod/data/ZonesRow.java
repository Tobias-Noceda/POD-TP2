package ar.edu.itba.pod.data;

import java.io.IOException;

import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.DataSerializable;

public class ZonesRow implements DataSerializable {

    private Long locationID = null;
    private String neighbourhood = null;
    private String zone = null;

    public ZonesRow() {
        //just for hazel
    }

    public ZonesRow(Long locationID, String neighbourhood, String zone) {
        this.locationID = locationID;
        this.neighbourhood = neighbourhood;
        this.zone = zone;
    }

    public Long getLocationID() {
        return locationID;
    }

    public String getNeighbourhood() {
        return neighbourhood;
    }

    public String getZone() {
        return zone;
    }

    @Override
    public void readData(ObjectDataInput in) throws IOException {
        locationID = in.readLong();
        neighbourhood = in.readUTF();
        zone = in.readUTF();
    }

    @Override
    public void writeData(ObjectDataOutput out) throws IOException {
        out.writeLong(locationID);
        out.writeUTF(neighbourhood);
        out.writeUTF(zone);
    }
    
}
