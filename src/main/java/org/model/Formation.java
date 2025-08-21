package org.model;

import com.opencsv.bean.CsvBindByName;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Formation {
    public enum Direction {
        LONG,
        SHORT
    }

    @CsvBindByName(column = "formation", required = true)
    String formation;

    Double winRate;
}
