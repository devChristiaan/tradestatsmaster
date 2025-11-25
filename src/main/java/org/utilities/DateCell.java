package org.utilities;

import javafx.scene.control.TableCell;
import javafx.scene.control.TreeTableCell;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import static org.context.GlobalContext.datePattern;

public class DateCell<S> extends TableCell<S, LocalDate> {
    DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern(datePattern); // Example format

    @Override
    protected void updateItem(LocalDate item, boolean empty) {
        super.updateItem(item, empty);

        if (empty || item == null) {
            setText(null);
        } else {
            setText(dateFormatter.format(item));
        }
    }
}

