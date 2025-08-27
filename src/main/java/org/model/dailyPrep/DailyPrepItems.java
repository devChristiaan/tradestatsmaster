package org.model.dailyPrep;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class DailyPrepItems {
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
}
