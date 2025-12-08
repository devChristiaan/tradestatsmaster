package org.model.journal;

import com.gluonhq.richtextarea.model.Document;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Journal {
    Integer id;
    LocalDate date;
    String symbol;
    Document document;
}
