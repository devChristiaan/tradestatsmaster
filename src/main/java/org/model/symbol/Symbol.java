package org.model.symbol;

import com.opencsv.bean.CsvBindByName;
import com.opencsv.bean.CsvDate;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Symbol implements Serializable {
    @CsvDate("dd/MM/yyyy")
    @CsvBindByName(column = "date", required = true)
    LocalDate date;
    @CsvBindByName(column = "symbol", required = true)
    String symbol;
    @CsvBindByName(column = "commission", required = true)
    Double commission;
    @CsvBindByName(column = "fluctuation", required = true)
    Double fluctuation;
    @CsvBindByName(column = "tickValue", required = true)
    Double tickValue;
}
