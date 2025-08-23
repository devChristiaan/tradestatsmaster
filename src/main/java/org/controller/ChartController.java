package org.controller;

import javafx.collections.ListChangeListener;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import org.context.ControllerRegistry;
import org.model.transaction.Transaction;
import org.utilities.CalculateStats;

import java.net.URL;
import java.time.LocalDate;
import java.util.ResourceBundle;

import static org.context.GlobalContext.getFilteredTransactions;
import static org.context.GlobalContext.movingAvgNr;
import static org.utilities.Utilities.calculateRunningTotal;

public class ChartController extends Pane implements Initializable {

    @FXML
    private AnchorPane chartAnchorPane;

    @FXML
    LineChart<String, Double> chart;
    XYChart.Series<String, Double> chartData = new XYChart.Series<>();
    XYChart.Series<String, Double> chartMovingAvg = new XYChart.Series<>();

    StatsController statsController;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        chart.setTitle("Profit/Loss over time");
        chart.getXAxis().setLabel("Date");
        chart.getYAxis().setLabel("Profit");
        chartData.setName("Cumulative Profit");
        chartMovingAvg.setName("4 transaction Moving Avg");

        ///Data not populating real time and not sure if running total works.
        Double runningTotal = 0.0;
        LocalDate currentDate = null;
        FilteredList<Transaction> transactionList = getFilteredTransactions();
        Double movingAvg = 0.0;

        for (int i = 0; i < transactionList.size(); i++) {
            runningTotal = calculateRunningTotal(transactionList.get(i).getProfit(), runningTotal);

            ///To show first transaction
            if (currentDate == null) {
                currentDate = transactionList.get(i).getDate().minusDays(1);
            }

            if (i >= movingAvgNr) {
                ///Formula might be wrong. Need to look into the sum total aspect. Not just average values. They wont scale with chart
                double nr5 = transactionList.get(i - 4).getProfit();
                double nr4 = transactionList.get(i - 3).getProfit();
                double nr3 = transactionList.get(i - 2).getProfit();
                double nr2 = transactionList.get(i - 1).getProfit();
                double nr1 = transactionList.get(i).getProfit();
                movingAvg += (nr5 + nr4 + nr3 + nr2 + nr1) / (movingAvgNr + 1);

            }
            if (!currentDate.isEqual(transactionList.get(i).getDate())) {
                chartData.getData().add(
                        new XYChart.Data<>(
                                transactionList.get(i).getDate().toString(),
                                runningTotal
                        ));
                if (i >= movingAvgNr) {
                    chartMovingAvg.getData().add(
                            new XYChart.Data<>(
                                    transactionList.get(i).getDate().toString(),
                                    movingAvg
                            ));
                }
            }
            currentDate = transactionList.get(i).getDate();
        }

        chart.getData().addAll(chartData, chartMovingAvg);

        statsController = ControllerRegistry.get(StatsController.class);
        this.statsController.populateStats(new CalculateStats(getFilteredTransactions()));

        /// Populate chart values
        getFilteredTransactions().addListener((ListChangeListener<? super Transaction>) c -> {
            this.statsController.populateStats(new CalculateStats(getFilteredTransactions()));
            ///TODO
            //Logic for updating the chart
//            this.chartData.getData().add(
//                    new XYChart.Data<>(
//                            getFilteredTransactions().get(getFilteredTransactions().size() - 1).getDate().toString(),
//                            chartData.getData().get(chartData.getData().size() - 1).getYValue() + getFilteredTransactions().get(getFilteredTransactions().size() - 1).getProfit()
//                    )
//            );
        });
    }

//    void populateGraph() {
//
//        chart.getXAxis().
//    }
}

