package ar.edu.itba.pod.data;

import java.io.IOException;
import java.time.YearMonth;

import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.DataSerializable;

public class YTDDataRaw implements DataSerializable {
    private String company;
    private YearMonth yearMonth;
    private double miles;

    public YTDDataRaw() {
        // just for hazel
    }

    public YTDDataRaw(String company, YearMonth yearMonth, double miles) {
        this.company = company;
        this.yearMonth = yearMonth;
        this.miles = miles;
    }

    public String getCompany() {
        return company;
    }

    public YearMonth getYearMonth() {
        return yearMonth;
    }

    public double getMiles() {
        return miles;
    }

    @Override
    public void readData(ObjectDataInput in) throws IOException {
        company = in.readUTF();
        yearMonth = YearMonth.of(in.readInt(), in.readInt());
        miles = in.readDouble();
    }

    @Override
    public void writeData(ObjectDataOutput out) throws IOException {
        out.writeUTF(company);
        out.writeInt(yearMonth.getYear());
        out.writeInt(yearMonth.getMonthValue());
        out.writeDouble(miles);
    }
}
