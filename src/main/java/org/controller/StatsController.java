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
import org.model.dailyPrep.DailyPrep;
import org.model.journal.Journal;
import org.model.transaction.Transaction;

import java.net.URL;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.Year;
import java.util.ResourceBundle;
import java.util.function.Function;
import java.util.function.Predicate;

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
            GlobalContext.getFilteredTransactions().setPredicate(setStartDatePredicate(Transaction::getDate, newValue, toDate.getValue()));
            GlobalContext.getFilteredDailyPrep().setPredicate(setStartDatePredicate(DailyPrep::getDate, newValue, toDate.getValue()));
            GlobalContext.getFilteredJournalEntriesList().setPredicate(setStartDatePredicate(Journal::getDate, newValue, toDate.getValue()));
        });
        toDate.valueProperty().addListener((observable, oldValue, newValue) -> {
            GlobalContext.getFilteredTransactions().setPredicate(setStartDatePredicate(Transaction::getDate, newValue, fromDate.getValue()));
            GlobalContext.getFilteredDailyPrep().setPredicate(setStartDatePredicate(DailyPrep::getDate, newValue, fromDate.getValue()));
            GlobalContext.getFilteredJournalEntriesList().setPredicate(setStartDatePredicate(Journal::getDate, newValue, toDate.getValue()));
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

    private <T> Predicate<T> setStartDatePredicate(Function<T, LocalDate> getter,
                                                   LocalDate from,
                                                   LocalDate to) {
        LocalDate start = from.minusDays(1);
        LocalDate end = to.plusDays(1);
        return item -> item != null && getter.apply(item).isAfter(start) && getter.apply(item).isBefore(end);
    }

}
