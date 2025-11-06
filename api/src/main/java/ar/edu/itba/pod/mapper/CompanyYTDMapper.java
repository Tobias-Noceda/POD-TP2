package ar.edu.itba.pod.mapper;

import java.time.YearMonth;

import com.hazelcast.mapreduce.Context;
import com.hazelcast.mapreduce.Mapper;

import ar.edu.itba.pod.data.Pair;
import ar.edu.itba.pod.data.TripsRow;
import ar.edu.itba.pod.data.YTDDataRaw;

public class CompanyYTDMapper implements Mapper<Long, YTDDataRaw, Pair<String, YearMonth>, Double> {

    @Override
    public void map(Long key, YTDDataRaw row, Context<Pair<String, YearMonth>, Double> context) {
        YearMonth ym = YearMonth.of(
            row.getYearMonth().getYear(),
            row.getYearMonth().getMonthValue()
        );
        context.emit(new Pair<>(row.getCompany(), ym), row.getMiles());
    }
}