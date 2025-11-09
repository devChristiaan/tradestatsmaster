package org.model.dailyPrep;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@AllArgsConstructor
public class DailyPrepDate {
    int dailyPrepDateId;
    LocalDate date;
}
