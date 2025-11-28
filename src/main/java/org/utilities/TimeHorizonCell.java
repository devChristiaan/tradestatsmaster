package org.utilities;

import javafx.scene.control.TableCell;
import org.model.goal.ETimeHorizon;

public class TimeHorizonCell<S> extends TableCell<S, ETimeHorizon> {

    @Override
    protected void updateItem(ETimeHorizon item, boolean empty) {
        super.updateItem(item, empty);

        if (empty || item == null) {
            setText(null);
        } else {
            setText(item.getDescription());
        }
    }
}
