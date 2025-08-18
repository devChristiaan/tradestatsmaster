package org.model.transaction;

import com.opencsv.bean.CsvBindByName;
import com.opencsv.bean.CsvDate;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Transaction {
    Integer id;
    @CsvDate("dd/MM/yyyy")
    @CsvBindByName(column = "date", required = true)
    LocalDate date;
    @CsvBindByName(column = "symbol", required = true)
    String symbol;
    @CsvBindByName(column = "quantity", required = true)
    Integer quantity;
    @CsvBindByName(column = "commission", required = true)
    Double commission;
    @CsvBindByName(column = "direction", required = true)
    String direction;
    @CsvBindByName(column = "open", required = true)
    Double open;
    @CsvBindByName(column = "close", required = true)
    Double close;
    @CsvBindByName(column = "profit", required = true)
    Double profit;
    @CsvBindByName(column = "formation", required = true)
    String formation;
}
