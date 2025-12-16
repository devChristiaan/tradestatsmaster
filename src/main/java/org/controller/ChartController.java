package org.controller;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.util.StringConverter;
import javafx.util.converter.NumberStringConverter;
import org.context.ControllerRegistry;
import org.context.GlobalContext;
import org.model.account.Account;
import org.model.transaction.Transaction;

import java.net.URL;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import static org.context.GlobalContext.datePattern;
import static org.utilities.Utilities.simpleMovingAverage;

public class ChartController implements Initializable {

    @FXML
    LineChart<Number, Number> chart;
    @FXML
    NumberAxis dateAxis;

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

        populateChart(statsController.fromDate.getValue());
        chart.getData().addAll(chartData, chartMovingAvg);

        this.statsController.fromDate.valueProperty().addListener((observable, oldValue, newValue) -> {
            populateChart(newValue);
        });

    }

    private void populateChart(LocalDate fromDate) {
        double accountValue = GlobalContext.getAccounts().getMaster().stream().mapToDouble(Account::getAmount).sum();

        Map<LocalDate, Double> dailyTotals = GlobalContext.getTransactions().getMaster().stream().collect(Collectors.groupingBy(Transaction::getDate, TreeMap::new, Collectors.summingDouble(t ->
                t.getProfit() - t.getCommission()
        )));

        TreeMap<LocalDate, Double> runningBalance = new TreeMap<>();
        final double[] balance = {accountValue};

        dailyTotals.forEach((date, amount) -> {
            balance[0] += amount;
            runningBalance.put(date, balance[0]);
        });

        NavigableMap<LocalDate, Double> visibleData =
                new TreeMap<>(runningBalance.tailMap(fromDate, true));

        Map<LocalDate, Double> movingAverage = simpleMovingAverage(visibleData);

        chartData.getData().clear();
        chartMovingAvg.getData().clear();

        updateSeries(chartData, visibleData);
        updateSeries(chartMovingAvg, movingAverage);

        long minDay = chartData.getData().stream()
                .mapToLong(d -> d.getXValue().longValue())
                .min()
                .orElseThrow();

        long maxDay = chartData.getData().stream()
                .mapToLong(d -> d.getXValue().longValue())
                .max()
                .orElseThrow();

        NumberAxis xAxis = (NumberAxis) chart.getXAxis();
        xAxis.setAutoRanging(false);
        xAxis.setLowerBound(minDay - 1);
        xAxis.setUpperBound(maxDay + 1);
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
}

