package org.model;

import com.opencsv.bean.CsvBindByName;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Formation {
    public enum Direction {
        LONG,
        SHORT
    }

    @CsvBindByName(column = "formation", required = true)
    String formation;
}
