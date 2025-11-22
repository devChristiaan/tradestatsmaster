package org.controller;

import atlantafx.base.theme.Styles;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TabPane;
import javafx.scene.layout.VBox;
import org.context.ControllerRegistry;
import org.context.GlobalContext;

import java.net.URL;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.Year;
import java.util.ResourceBundle;

import static org.context.GlobalContext.datePattern;
import static org.utilities.Utilities.*;

public class StatsController extends VBox implements Initializable {

    @FXML
    TabPane tabPane;
    @FXML
    DatePicker fromDate;
    @FXML
    DatePicker toDate;
    @FXML
    Button currentDateFrom;
    @FXML
    Button currentDateFromBeginning;
    @FXML
    Button currentDateTo;
    @FXML
    Button currentMonth;
    @FXML
    Button currentWeek;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        ControllerRegistry.register(StatsController.class, this);

        ///Set default date values and patterns
        tabPane.getStyleClass().add(Styles.DENSE);
        toDate.setValue(LocalDate.now());
        fromDate.setConverter(calendarToStringConverter(datePattern));
        toDate.setConverter(calendarToStringConverter(datePattern));

        fromDate.setPromptText(datePattern.toUpperCase());
        toDate.setPromptText(datePattern.toUpperCase());

        ///Date Filter logic
        fromDate.valueProperty().addListener((observable, oldValue, newValue) -> {
            GlobalContext.getFilteredTransactions().setPredicate(transaction -> {
                if (transaction == null) {
                    return true;
                }
                return transaction.getDate().isAfter(newValue.minusDays(1)) && transaction.getDate().isBefore(toDate.getValue().plusDays(1));
            });
            GlobalContext.getFilteredDailyPrep().setPredicate(dailyPrepDate -> {
                if (dailyPrepDate == null) {
                    return true;
                }
                return dailyPrepDate.getDate().isAfter(newValue.minusDays(1)) && dailyPrepDate.getDate().isBefore(toDate.getValue().plusDays(1));
            });
        });
        toDate.valueProperty().addListener((observable, oldValue, newValue) -> {
            GlobalContext.getFilteredTransactions().setPredicate(transaction -> {
                if (transaction == null) {
                    return true;
                }
                return transaction.getDate().isBefore(newValue.plusDays(1)) && transaction.getDate().isAfter(fromDate.getValue().minusDays(1));
            });
            GlobalContext.getFilteredDailyPrep().setPredicate(dailyPrepDate -> {
                if (dailyPrepDate == null) {
                    return true;
                }
                return dailyPrepDate.getDate().isBefore(newValue.plusDays(1)) && dailyPrepDate.getDate().isAfter(fromDate.getValue().minusDays(1));
            });
        });

        ///Set Styling Properties
        currentDateFrom.getStyleClass().add(Styles.FLAT);
        currentDateTo.getStyleClass().add(Styles.FLAT);
        currentDateFromBeginning.getStyleClass().add(Styles.FLAT);
        currentMonth.getStyleClass().add(Styles.FLAT);
        currentWeek.getStyleClass().add(Styles.FLAT);
    }

    @FXML
    private void setCurrentDateFromDate() {
        fromDate.setValue(LocalDate.now());
    }

    @FXML
    private void setCurrentDateFromBeginning() {
        fromDate.setValue(Year.now().atDay(1));
    }

    @FXML
    private void setCurrentDateToDate() {
        toDate.setValue(LocalDate.now());
    }

    @FXML
    private void setCurrentMonth() {
        fromDate.setValue(LocalDate.now().withDayOfMonth(1));
    }

    @FXML
    private void setCurrentWeek() {
        fromDate.setValue(LocalDate.now().with(DayOfWeek.MONDAY));
    }

}
