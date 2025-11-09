package org.model.dailyPrep;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;
import java.util.List;


@AllArgsConstructor
@Getter
public class DailyPrep {
    int dailyPrepDateId;
    LocalDate date;
    String symbol = null;

    List<DailyPrepItems> dailyPrepItemsList;
}