package org.model;

import com.opencsv.bean.CsvBindByName;
import com.opencsv.bean.CsvDate;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
public class Symbol {
    @CsvBindByName(column = "date", required = true)
    @CsvDate("dd/MM/yyyy")
    Date date;
    @CsvBindByName(column = "symbol", required = true)
    String symbol;
    @CsvBindByName(column = "commission", required = true)
    Double commission;
    @CsvBindByName(column = "fluctuation", required = true)
    Double fluctuation;
    @CsvBindByName(column = "tickValue", required = true)
    Float tickValue;
}
