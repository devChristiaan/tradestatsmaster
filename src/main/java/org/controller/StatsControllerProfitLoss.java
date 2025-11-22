package org.controller;

import javafx.collections.ListChangeListener;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import org.context.ControllerRegistry;
import org.context.GlobalContext;
import org.model.transaction.Transaction;
import org.utilities.CalculateStatsStopLoss;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class StatsControllerProfitLoss extends VBox implements Initializable {

    @FXML
    private ChoiceBox<String> symbols;
    @FXML
    private ChoiceBox<String> timePeriods;
    @FXML
    TextField targetTicks;

    @FXML
    Label rh_stopLoss;
    @FXML
    Label rh_TTE_stopLoss;
    @FXML
    Label oneTwoThree_stopLoss;
    @FXML
    Label oneTwoThree_TTE_stopLoss;
    @FXML
    Label reversalBar_stopLoss;
    @FXML
    Label consolidation_stopLoss;
    @FXML
    Label insideReversalBar_stopLoss;
    @FXML
    Label HH_LL_stopLoss;
    @FXML
    Label HH_LL_3_Days_stopLoss;
    @FXML
    Label targetProfits;
    @FXML
    Label actualLosses;
    @FXML
    Label netProfits;
    @FXML
    Label nrTrades;
    @FXML
    Label nrWins;
    @FXML
    Label nrLosses;
    @FXML
    Label winRatio;
    @FXML
    Label possibleProfits;
    @FXML
    Label possibleLosses;
    @FXML
    Label winRate;
    @FXML
    Label averageWinAmount;
    @FXML
    Label averageLossAmount;
    @FXML
    Label averageATR;
    @FXML
    Label stopLossObjectiveRatio;
    @FXML
    Label avgLossAvgWinRatio;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        ControllerRegistry.register(StatsControllerProfitLoss.class, this);
        timePeriods.setDisable(true);
        targetTicks.setDisable(true);

        ///Set Transaction Listener
        GlobalContext.getFilteredTransactions().addListener((ListChangeListener<? super Transaction>) c -> {
            this.setSymbols();
        });

        ///Set Initial Stat values
        this.setSymbols();

        ///Listen for select symbol and select time frame
        symbols.getSelectionModel().selectedItemProperty().addListener((ov, value, new_value) -> {
            this.setTimePeriods();
            this.populateStatsProfitLoss(new CalculateStatsStopLoss(GlobalContext.getFilteredTransactions(), new_value, timePeriods.getValue(), Double.parseDouble(targetTicks.getText().isEmpty() ? String.valueOf(0) : targetTicks.getText())));
        });
        timePeriods.getSelectionModel().selectedItemProperty().addListener((ov, value, new_value) -> {
            targetTicks.setDisable(false);
            this.populateStatsProfitLoss(new CalculateStatsStopLoss(GlobalContext.getFilteredTransactions(), symbols.getValue(), timePeriods.getValue(), Double.valueOf(targetTicks.getText().isEmpty() ? String.valueOf(0) : targetTicks.getText())));
        });

    }

    private void populateStatsProfitLoss(CalculateStatsStopLoss stats) {
        rh_stopLoss.setText(stats.getFormationsWinRate().stream().filter(formation -> formation.getFormation().equals("Ross Hook")).findFirst().get().getWinRate() + " %");
        rh_TTE_stopLoss.setText(stats.getFormationsWinRate().stream().filter(formation -> formation.getFormation().equals("Ross Hook - TTE")).findFirst().get().getWinRate() + " %");
        oneTwoThree_stopLoss.setText(stats.getFormationsWinRate().stream().filter(formation -> formation.getFormation().equals("1 2 3")).findFirst().get().getWinRate() + " %");
        oneTwoThree_TTE_stopLoss.setText(stats.getFormationsWinRate().stream().filter(formation -> formation.getFormation().equals("1 2 3 - TTE")).findFirst().get().getWinRate() + " %");
        reversalBar_stopLoss.setText(stats.getFormationsWinRate().stream().filter(formation -> formation.getFormation().equals("Reversal/Gimme Bar")).findFirst().get().getWinRate() + " %");
        consolidation_stopLoss.setText(stats.getFormationsWinRate().stream().filter(formation -> formation.getFormation().equals("Consolidation")).findFirst().get().getWinRate() + " %");
        insideReversalBar_stopLoss.setText(stats.getFormationsWinRate().stream().filter(formation -> formation.getFormation().equals("Inside Reversal Bar")).findFirst().get().getWinRate() + " %");
        HH_LL_stopLoss.setText(stats.getFormationsWinRate().stream().filter(formation -> formation.getFormation().equals("Highest High/Lowest Low")).findFirst().get().getWinRate() + " %");
        HH_LL_3_Days_stopLoss.setText(stats.getFormationsWinRate().stream().filter(formation -> formation.getFormation().equals("Highest high/Lowest Low of 3 days")).findFirst().get().getWinRate() + " %");

        targetProfits.setText("$ " + stats.getDf().format(stats.getTargetProfits()));
        actualLosses.setText("$ " + stats.getDf().format(stats.getActualLosses()));
        netProfits.setText("$ " + stats.getDf().format(stats.getNetProfits()));
        nrTrades.setText(String.valueOf(stats.getNrTrades()));
        nrWins.setText(String.valueOf(stats.getNrWins()));
        nrLosses.setText(String.valueOf(stats.getNrLosses()));
        winRatio.setText(String.format("%.2f", stats.getWinRatio() > 0 ? stats.getWinRatio() : 0));
        possibleProfits.setText("$ " + stats.getDf().format(stats.getPossibleProfits()));
        possibleLosses.setText("$ " + stats.getDf().format(stats.getPossibleLosses()));
        winRate.setText(stats.getWinRate() + " %");
        averageWinAmount.setText("$ " + stats.getDf().format(stats.getAverageWinAmount()));
        averageLossAmount.setText("$ " + stats.getDf().format(stats.getAverageLossAmount()));
        averageATR.setText(String.valueOf(stats.getAverageATR()));
        avgLossAvgWinRatio.setText(String.format("%.4f", stats.getAvgLossAvgWinRatio() > 0 ? stats.getAvgLossAvgWinRatio() : 0));
        stopLossObjectiveRatio.setText(String.format("%.4f", stats.getStopLossObjectiveRatio() > 0 ? stats.getStopLossObjectiveRatio() : 0));
    }

    private void setSymbols() {
        symbols.getItems().clear();
        targetTicks.clear();
        for (Transaction tran : GlobalContext.getFilteredTransactions()) {
            if (!symbols.getItems().contains(tran.getSymbol())) {
                symbols.getItems().add(tran.getSymbol());
            }
        }
        symbols.setDisable(symbols.getItems().isEmpty());
    }

    private void setTimePeriods() {
        timePeriods.getItems().clear();
        List<Transaction> symbolFilteredList = GlobalContext.getFilteredTransactions().stream().filter(p -> p.getSymbol().equals(symbols.getValue())).toList();
        for (Transaction tran : symbolFilteredList) {
            if (!timePeriods.getItems().contains(tran.getTimePeriod())) {
                timePeriods.getItems().add(tran.getTimePeriod());
            }
        }
        timePeriods.setDisable(timePeriods.getItems().isEmpty());
    }

    @FXML
    public void setTargetTicks() {
        this.populateStatsProfitLoss(new CalculateStatsStopLoss(GlobalContext.getFilteredTransactions(), symbols.getValue(), timePeriods.getValue(), Double.valueOf(targetTicks.getText().isEmpty() ? String.valueOf(0) : targetTicks.getText())));
    }

}
