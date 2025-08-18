package org.controller;

import atlantafx.base.theme.Styles;
import javafx.collections.ListChangeListener;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import org.app.App;
import org.context.ControllerRegistry;
import org.context.GlobalContext;
import org.model.transaction.Transaction;
import org.utilities.CalculateStats;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.Year;

import static org.context.GlobalContext.datePattern;
import static org.utilities.Utilities.*;

public class StatsController extends VBox {
    @FXML
    DatePicker fromDate;
    @FXML
    DatePicker toDate;
    @FXML
    private Button currentDateFrom;
    @FXML
    private Button currentDateFromBeginning;
    @FXML
    private Button currentDateTo;
    @FXML
    private Button currentMonth;
    @FXML
    private Button currentWeek;
    @FXML
    Label totalProfit;
    @FXML
    Label totalCommission;
    @FXML
    Label netReturn;
    @FXML
    Label winRate;
    @FXML
    Label commissionRatio;
    @FXML
    Label payoffRatio;
    @FXML
    Label winRatio;
    @FXML
    Label accountBal;
    @FXML
    Label accountBalPercentage;

    public StatsController() {
        FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource("/org/app/fxml/stats.fxml"));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);
        this.getStylesheets().add(getClass().getResource("/org/app/CSS/stats.css").toExternalForm());
        try {
            fxmlLoader.load();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
    }

    public void initialize() {
        ControllerRegistry.register(StatsController.class, this);

        ///Set default date values and patterns
        toDate.setValue(LocalDate.now());
        fromDate.setConverter(calendarToStringConverter(datePattern));
        toDate.setConverter(calendarToStringConverter(datePattern));

        fromDate.setPromptText(datePattern.toUpperCase());
        toDate.setPromptText(datePattern.toUpperCase());

        double accountBalance = calculateAccountBalance(GlobalContext.getTransactionsMasterList());
        BigDecimal accountBalancePercentage = calculateBalancePercentage(accountBalance);

        accountBal.setText("$ " + String.format("%.2f", accountBalance));
        accountBalPercentage.setText("$ " + String.format("%.2f", accountBalancePercentage));

        ///Date Filter logic
        fromDate.valueProperty().addListener((observable, oldValue, newValue) -> {
            GlobalContext.getFilteredTransactions().setPredicate(transaction -> {
                if (transaction == null) {
                    return true;
                }
                return transaction.getDate().isAfter(newValue.minusDays(1)) && transaction.getDate().isBefore(toDate.getValue().plusDays(1));
            });
        });
        toDate.valueProperty().addListener((observable, oldValue, newValue) -> {
            GlobalContext.getFilteredTransactions().setPredicate(transaction -> {
                if (transaction == null) {
                    return true;
                }
                return transaction.getDate().isBefore(newValue.plusDays(1)) && transaction.getDate().isAfter(fromDate.getValue().minusDays(1));
            });
        });

        ///Set Transaction Listener
        GlobalContext.getFilteredTransactions().addListener((ListChangeListener<? super Transaction>) c -> {
            this.populateStats(new CalculateStats(GlobalContext.getFilteredTransactions()));
        });

        ///Set Initial Stat values
        this.populateStats(new CalculateStats(GlobalContext.getFilteredTransactions()));


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

    public void populateStats(CalculateStats stats) {
        totalProfit.setText("$ " + String.format("%.2f", stats.getTotalProfit()));
        totalCommission.setText("$ " + String.format("%.2f", stats.getTotalCommission()));
        netReturn.setText("$ " + String.format("%.2f", stats.getNetIncome()));
        winRate.setText(stats.getWinRate() + " %");
        commissionRatio.setText(String.format("%.2f", stats.getTotalProfit() > 0 ? stats.getCommissionRatio() * 100 : 0) + " %");
        payoffRatio.setText(String.format("%.2f", stats.getPayoffRatio()));
        winRatio.setText(String.format("%.2f", stats.getWinRatio()));
    }

}
