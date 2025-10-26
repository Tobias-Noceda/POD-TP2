package ar.edu.itba.pod.mapper;

import java.time.YearMonth;

import com.hazelcast.mapreduce.Context;
import com.hazelcast.mapreduce.Mapper;

import ar.edu.itba.pod.data.Pair;
import ar.edu.itba.pod.data.TripsRow;

public class CompanyYTDMapper implements Mapper<Pair<String, YearMonth>, TripsRow, Pair<String, YearMonth>, Double> {

    @Override
    public void map(Pair<String, YearMonth> key, TripsRow row, Context<Pair<String, YearMonth>, Double> context) {
        YearMonth ym = YearMonth.of(
            row.getRequestDateTime().getYear(),
            row.getRequestDateTime().getMonth()
        );
        context.emit(new Pair<>(row.getCompany(), ym), row.getMiles());
    }
}