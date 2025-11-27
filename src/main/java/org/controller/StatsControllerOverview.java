package org.controller;

import javafx.collections.ListChangeListener;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import org.context.ControllerRegistry;
import org.context.GlobalContext;
import org.model.account.Account;
import org.model.transaction.Transaction;
import org.utilities.CalculateStatsOverview;

import java.math.BigDecimal;
import java.net.URL;
import java.util.ResourceBundle;

import static org.utilities.Utilities.calculateBalancePercentage;
import static org.utilities.Utilities.calculateAccountBalance;
import static org.utilities.Utilities.getTextFormater;

public class StatsControllerOverview extends VBox implements Initializable {

    @FXML
    Label totalProfit;
    @FXML
    Label totalLoss;
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
        ControllerRegistry.register(StatsControllerOverview.class, this);
        populateAccountBalance();

        ///Set Transaction and Account Balance Listeners
        GlobalContext.getAccounts().getFiltered().addListener((ListChangeListener<? super Account>) c -> {
            populateAccountBalance();
        });
        GlobalContext.getTransactions().getFiltered().addListener((ListChangeListener<? super Transaction>) c -> {
            this.populateStatsOverview(new CalculateStatsOverview(GlobalContext.getTransactions().getFiltered()));
            populateAccountBalance();
        });

        ///Set Initial Stat values
        this.populateStatsOverview(new CalculateStatsOverview(GlobalContext.getTransactions().getFiltered()));
    }

    public void populateStatsOverview(CalculateStatsOverview stats) {
        totalProfit.setText("$ " + stats.getTotalProfitFormat());
        totalLoss.setText("$ " + stats.getTotalLossFormat());
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

    private void populateAccountBalance() {
        Double accountBalance = calculateAccountBalance(GlobalContext.getAccounts().getFiltered().stream().mapToDouble(Account::getAmount).sum());
        BigDecimal accountBalancePercentage = calculateBalancePercentage(accountBalance);

        accountBal.setText("$ " + getTextFormater().format(accountBalance));
        accountBalPercentage.setText("$ " + getTextFormater().format(accountBalancePercentage));
    }
}
