package org.model.account;

import com.opencsv.bean.CsvBindByName;
import com.opencsv.bean.CsvDate;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDate;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Account implements Serializable {
    @CsvDate("dd/MM/yyyy")
    @CsvBindByName(column = "date", required = true)
    LocalDate date;
    @CsvBindByName(column = "amount", required = true)
    Double amount;
    @CsvBindByName(column = "notes", required = true)
    String notes;
}
