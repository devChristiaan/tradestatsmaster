package org.controller;

import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextField;
import javafx.util.StringConverter;
import org.context.ControllerRegistry;
import org.context.GlobalContext;
import org.model.ETradePerformanceTypes;
import org.model.account.Account;
import org.model.transaction.Transaction;

import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import static org.utilities.Utilities.simpleMovingAverage;

public class ChartController implements Initializable {

    @FXML
    LineChart<Number, Number> chart;
    @FXML
    NumberAxis dateAxis;
    @FXML
    TextField MA;
    @FXML
    ChoiceBox<String> chartTypes;

    XYChart.Series<Number, Number> chartData = new XYChart.Series<>();
    XYChart.Series<Number, Number> chartMovingAvg = new XYChart.Series<>();

    StatsController statsController;
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd");

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        this.statsController = ControllerRegistry.get(StatsController.class);
        dateAxis.setTickLabelFormatter(new StringConverter<>() {
            @Override
            public String toString(Number value) {
                return LocalDate.ofEpochDay(value.longValue()).format(formatter);
            }

            @Override
            public Number fromString(String string) {
                return LocalDate.parse(string, formatter).toEpochDay();
            }
        });
        chartData.setName("Profit/Loss");
        chartMovingAvg.setName("4 Day Moving Avg");
        ArrayList<String> types = ETradePerformanceTypes.getDescriptions();
        for (int i = 0; i < types.size(); i++) {
            if (i == 0) {
                chartTypes.getItems().add(types.get(i));
                chartTypes.setValue(types.get(i));
            } else {
                chartTypes.getItems().add(types.get(i));
            }
        }

        populateChart(statsController.fromDate.getValue());
        chart.getData().addAll(chartData, chartMovingAvg);

        this.statsController.fromDate.valueProperty().addListener((observable, oldValue, newValue) -> {
            populateChart(newValue);
        });

        this.MA.textProperty().addListener((observable, oldValue, newValue) -> {
            populateChart(this.statsController.fromDate.getValue());
        });

        chartTypes.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            populateChart(this.statsController.fromDate.getValue());
        });
    }

    private void populateChart(LocalDate fromDate) {
        ETradePerformanceTypes type = ETradePerformanceTypes.fromDescription(chartTypes.getValue());
        TreeMap<LocalDate, Double> dailyTotals = GlobalContext.getTransactions().getMaster().stream().collect(Collectors.groupingBy(Transaction::getDate, TreeMap::new, Collectors.summingDouble(t ->
                t.getProfit() - t.getCommission()
        )));
        TreeMap<LocalDate, Double> runningAccountBalance;
        Map<LocalDate, Double> movingAverage = new TreeMap<>();
        NavigableMap<LocalDate, Double> visibleData = new TreeMap<>();

        switch (type) {
            case NET_CASH_FLOWS:
                runningAccountBalance = runningBalance(dailyTotals, false);
                visibleData =
                        new TreeMap<>(runningAccountBalance.tailMap(fromDate, true));
                movingAverage = simpleMovingAverage(visibleData, MA.getText().isEmpty() ? 1 : Integer.parseInt(MA.getText()));
                break;
            case TRADES:
                visibleData =
                        new TreeMap<>(dailyTotals.tailMap(fromDate, true));
                movingAverage = simpleMovingAverage(visibleData, MA.getText().isEmpty() ? 1 : Integer.parseInt(MA.getText()));
                break;
            case TRADES_CUMULATIVE:
                visibleData =
                        new TreeMap<>(cumulativeTradeBalance(dailyTotals).tailMap(fromDate, true));
                movingAverage = simpleMovingAverage(visibleData, MA.getText().isEmpty() ? 1 : Integer.parseInt(MA.getText()));
                break;
            case CASH_FLOW_ADJUSTED:
                runningAccountBalance = runningBalance(dailyTotals, true);
                visibleData =
                        new TreeMap<>(runningAccountBalance.tailMap(fromDate, true));
                movingAverage = simpleMovingAverage(visibleData, MA.getText().isEmpty() ? 1 : Integer.parseInt(MA.getText()));
                break;
            default:
                break;
        }

        if (visibleData.isEmpty()) return;

        updateSeries(chartData, visibleData);
        updateSeries(chartMovingAvg, movingAverage);

        formatChartDate(chartData);
    }

    private void updateSeries(
            XYChart.Series<Number, Number> series,
            Map<LocalDate, Double> data) {

        series.getData().clear();
        data.forEach((date, value) ->
                series.getData().add(
                        new XYChart.Data<>(date.toEpochDay(), value)
                )
        );
    }

    private void formatChartDate(XYChart.Series<Number, Number> series) {
        long minDay = series.getData().stream()
                .mapToLong(d -> d.getXValue().longValue())
                .min()
                .orElseThrow();

        long maxDay = series.getData().stream()
                .mapToLong(d -> d.getXValue().longValue())
                .max()
                .orElseThrow();

        NumberAxis xAxis = (NumberAxis) chart.getXAxis();
        xAxis.setAutoRanging(false);
        xAxis.setLowerBound(minDay - 1);
        xAxis.setUpperBound(maxDay + 1);
    }

    private TreeMap<LocalDate, Double> runningBalance(Map<LocalDate, Double> dailyTotals,
                                                      boolean netCashFlows) {
        double accountValue = 0.0;
        ObservableList<Account> intialList = GlobalContext.getAccounts().getMaster();
        for (int i = 0; i < intialList.size(); i++) {
            if (i == 0) {
                accountValue += intialList.get(i).getAmount();
            } else {
                ///Multiplying by negative one flips the deposit and withdrawal amounts
                accountValue += netCashFlows ? (intialList.get(i).getAmount() * -1) : intialList.get(i).getAmount();
            }
        }

        TreeMap<LocalDate, Double> runningBalance = new TreeMap<>();
        final double[] balance = {accountValue};

        dailyTotals.forEach((date, amount) -> {
            balance[0] += amount;
            runningBalance.put(date, balance[0]);
        });

        return runningBalance;
    }

    private TreeMap<LocalDate, Double> cumulativeTradeBalance(Map<LocalDate, Double> dailyTotals) {
        TreeMap<LocalDate, Double> runningBalance = new TreeMap<>();
        double balance = 0.0;

        for (Map.Entry<LocalDate, Double> entry : dailyTotals.entrySet()) {
            balance += entry.getValue();
            runningBalance.put(entry.getKey(), balance);

        }
        return runningBalance;
    }
}

