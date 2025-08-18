package org.utilities;

import javafx.scene.control.TableCell;
import java.text.NumberFormat;
import java.util.Locale; // Optional: for specific locale currency formatting

public class CurrencyCell<S> extends TableCell<S, Double> {

    private final NumberFormat currencyFormat;

    public CurrencyCell() {
        this.currencyFormat = NumberFormat.getCurrencyInstance(Locale.US);
    }

    @Override
    protected void updateItem(Double item, boolean empty) {
        super.updateItem(item, empty);

        if (empty || item == null) {
            setText(null);
            setGraphic(null);
        } else {
            setText(currencyFormat.format(item));
        }
    }
}