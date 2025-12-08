package org.model.goal;

import com.gluonhq.richtextarea.model.Document;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@AllArgsConstructor
@Setter
@Getter
public class Goal {
    Integer id;
    LocalDate date;
    ETimeHorizon timeHorizon;
    Document document;
    Boolean achieved;
}
