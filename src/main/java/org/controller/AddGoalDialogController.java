package org.controller;

import atlantafx.base.theme.Styles;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import org.context.ControllerRegistry;
import org.context.GlobalContext;
import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.material2.Material2AL;
import org.manager.DbManager;
import org.model.goal.ETimeHorizon;
import org.model.goal.Goal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ResourceBundle;
import java.util.Objects;

import static org.context.GlobalContext.datePattern;
import static org.utilities.Utilities.*;


public class AddGoalDialogController implements Initializable {
    private static final Logger log = LoggerFactory.getLogger(AddGoalDialogController.class);

    @FXML
    public Button save;
    @FXML
    private DatePicker date;
    @FXML
    private VBox errorContainer;
    @FXML
    private ChoiceBox<String> timeHorizon;

    MainController mainController;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        this.save.getStyleClass().add(Styles.BUTTON_OUTLINED);
        this.mainController = ControllerRegistry.get(MainController.class);

        this.date.setConverter(calendarToStringConverter(datePattern));
        this.date.setValue(LocalDate.now());

        this.timeHorizon.getItems().addAll(ETimeHorizon.getDescriptions());

        ///Rest checked default items
        this.date.setOnAction(event -> {
            errorContainer.getChildren().clear();
        });
        this.timeHorizon.setOnAction(event -> {
            errorContainer.getChildren().clear();
        });
    }

    @FXML
    public void cancel() {
        mainController.hideModal();
    }

    @FXML
    public void saveGoal() {
        errorContainer.getChildren().clear();
        if (isValid()) {
            DbManager db = new DbManager();
            try {
                db.setBdConnection();
                db.addGoal(new Goal(null, date.getValue(), ETimeHorizon.fromDescription(timeHorizon.getValue()), goalTemplate, false));
                GlobalContext.getGoals().replaceMaster(db.getAllGoals());
                db.closeBdConnection();
                this.cancel();
            } catch (IOException | SQLException ignored) {
            }
        }
    }

    private boolean isValid() {
        boolean hasErrors = false;
        String selectedHorizon = timeHorizon.getValue();
        LocalDate selectedDate = date.getValue();

        // Basic validation
        if (selectedDate == null) {
            errorContainer.getChildren().add(createErrorLabel("Select a valid date."));
            hasErrors = true;
        }

        if (selectedHorizon == null) {
            errorContainer.getChildren().add(createErrorLabel("Select a time horizon."));
            hasErrors = true;
        }

        if (hasErrors) {
            log.error("Add Gaol form validation failed.");
            return false;
        }


        ETimeHorizon horizon = ETimeHorizon.fromDescription(selectedHorizon);
        LocalDate periodStart = periodStartFor(horizon, date.getValue());
        LocalDate periodEnd = periodEndFor(horizon, date.getValue());

        // Filter existing goals for this horizon and check if any falls inside current period
        boolean duplicateInPeriod = GlobalContext.getGoals()
                .getMaster()
                .stream()
                .filter(g -> g.getTimeHorizon() == horizon)
                .map(Goal::getDate)
                .filter(Objects::nonNull)
                .anyMatch(d -> !d.isBefore(periodStart) && !d.isAfter(periodEnd)); // inclusive

        if (duplicateInPeriod) {
            errorContainer.getChildren().add(createErrorLabel(getDuplicateMessage(horizon)));
            log.error("Add Gaol form validation failed.");
            return false;
        }

        log.info("Goal form is valid");
        return true;
    }

    private String getDuplicateMessage(ETimeHorizon horizon) {
        return switch (horizon) {
            case SHORT_TERM -> "You already have a short term goal";
            case MID_TERM -> "You already have a mid term goal";
            case LONG_TERM -> "You already have a long term goal";
        };
    }

    private Label createErrorLabel(String error) {
        Label label = new Label(error, new FontIcon(Material2AL.LABEL));
        label.getStyleClass().add(Styles.DANGER);
        return label;
    }
}