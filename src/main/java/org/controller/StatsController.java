package org.controller;

import atlantafx.base.theme.Styles;
import javafx.collections.ListChangeListener;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import org.context.ControllerRegistry;
import org.context.GlobalContext;
import org.model.transaction.Transaction;
import org.utilities.CalculateStats;

import java.math.BigDecimal;
import java.net.URL;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.Year;
import java.util.ResourceBundle;

import static org.context.GlobalContext.datePattern;
import static org.utilities.Utilities.*;

public class StatsController extends VBox implements Initializable {
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

    @FXML
    Label rh;
    @FXML
    Label rh_TTE;
    @FXML
    Label oneTwoThree;
    @FXML
    Label oneTwoThree_TTE;
    @FXML
    Label reversalBar;
    @FXML
    Label consolidation;
    @FXML
    Label insideReversalBar;
    @FXML
    Label HH_LL;
    @FXML
    Label HH_LL_3_Days;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        ControllerRegistry.register(StatsController.class, this);

        ///Set default date values and patterns
        toDate.setValue(LocalDate.now());
        fromDate.setConverter(calendarToStringConverter(datePattern));
        toDate.setConverter(calendarToStringConverter(datePattern));

        fromDate.setPromptText(datePattern.toUpperCase());
        toDate.setPromptText(datePattern.toUpperCase());

        double accountBalance = calculateAccountBalance(GlobalContext.getTransactionsMasterList());
        BigDecimal accountBalancePercentage = calculateBalancePercentage(accountBalance);

        accountBal.setText("$ " + getTextFormater().format(accountBalance));
        accountBalPercentage.setText("$ " + getTextFormater().format(accountBalancePercentage));

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
        totalProfit.setText("$ " + stats.getTotalProfitFormat());
        totalCommission.setText("$ " + stats.getTotalCommission());
        netReturn.setText("$ " + stats.getNetIncome());
        winRate.setText(stats.getWinRate() + " %");
        commissionRatio.setText(String.format("%.2f", stats.getTotalProfit() > 0 ? stats.getCommissionRatio() : 0) + " %");
        payoffRatio.setText(String.format("%.2f", stats.getPayoffRatio()));
        winRatio.setText(String.format("%.2f", stats.getWinRatio()));

        rh.setText(stats.getFormationsWinRate().stream().filter(formation -> formation.getFormation().equals("Ross Hook")).findFirst().get().getWinRate() + " %");
        rh_TTE.setText(stats.getFormationsWinRate().stream().filter(formation -> formation.getFormation().equals("Ross Hook - TTE")).findFirst().get().getWinRate() + " %");
        oneTwoThree.setText(stats.getFormationsWinRate().stream().filter(formation -> formation.getFormation().equals("1 2 3")).findFirst().get().getWinRate() + " %");
        oneTwoThree_TTE.setText(stats.getFormationsWinRate().stream().filter(formation -> formation.getFormation().equals("1 2 3 - TTE")).findFirst().get().getWinRate() + " %");
        reversalBar.setText(stats.getFormationsWinRate().stream().filter(formation -> formation.getFormation().equals("Reversal/Gimme Bar")).findFirst().get().getWinRate() + " %");
        consolidation.setText(stats.getFormationsWinRate().stream().filter(formation -> formation.getFormation().equals("Consolidation")).findFirst().get().getWinRate() + " %");
        insideReversalBar.setText(stats.getFormationsWinRate().stream().filter(formation -> formation.getFormation().equals("Inside Reversal Bar")).findFirst().get().getWinRate() + " %");
        HH_LL.setText(stats.getFormationsWinRate().stream().filter(formation -> formation.getFormation().equals("Highest High/Lowest Low")).findFirst().get().getWinRate() + " %");
        HH_LL_3_Days.setText(stats.getFormationsWinRate().stream().filter(formation -> formation.getFormation().equals("Highest high/Lowest Low of 3 days")).findFirst().get().getWinRate() + " %");
    }

}
