package org.model.dailyPrep;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
public class DailyPrepItems {
    LocalDate date;
    int dailyPrepId;
    int dailyPrepDateId;
    String dailyEvents = null;
    String symbol;
    String hourlyTrend;
    String halfHourlyTrend;
    String dailyTrend;
    Double hh_ll_3_bars_high;
    Double hh_ll_3_bars_low;
    Double hh_ll_any_high;
    Double hh_ll_any_low;

//    public DailyPrepItems(String symbol, int dailyPrepDateId, int dailyPrepId) {
//        this.date = null;
//        this.symbol = symbol;
//        this.dailyPrepId = dailyPrepId;
//        this.dailyPrepDateId = dailyPrepDateId;
//    }
}
