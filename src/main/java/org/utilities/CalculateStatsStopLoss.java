package org.utilities;

import javafx.collections.transformation.FilteredList;
import lombok.Getter;
import org.context.GlobalContext;
import org.model.Formation;
import org.model.symbol.Symbol;
import org.model.transaction.Transaction;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.OptionalDouble;

import static org.utilities.Utilities.getTextFormater;

@Getter
public class CalculateStatsStopLoss {
    DecimalFormat df = getTextFormater();

    private final List<Formation> formationList = (List<Formation>) GlobalContext.get(GlobalContext.ContextItems.FORMATION_LIST);
    private final List<Symbol> symbolList = GlobalContext.getFilteredSymbolList();
    private List<Formation> formationsWinRate = new ArrayList<>();

    private double targetProfits = 0.0;
    private double actualLosses = 0.0;
    private double netProfits = 0.0;
    private int nrTrades = 0;
    private int nrWins = 0;
    private int nrLosses = 0;
    private double winRatio = 0.0;
    private double possibleProfits = 0.0;
    private double possibleLosses = 0.0;
    private int winRate = 0;
    private double averageWinAmount = 0.0;
    private double averageLossAmount = 0.0;
    private double averageATR = 0.0;
    private double payoffRatio = 0.0;
    private double avgLossAvgWinRatio = 0.0;
    private double stopLossObjectiveRatio = 0.0;

    /// Figure this out last stat

    public CalculateStatsStopLoss(FilteredList<Transaction> filteredList,
                                  String selectedSymbol,
                                  String timePeriod,
                                  Double targetTicks) {
        if (!filteredList.isEmpty()) {
            List<Transaction> symbolFilteredList = selectedSymbol != null && timePeriod != null ?
                    filteredList.stream().filter(p -> p.getSymbol().equals(selectedSymbol) && p.getTimePeriod().equals(timePeriod)).toList() : selectedSymbol != null ?
                    filteredList.stream().filter(p -> p.getSymbol().equals(selectedSymbol)).toList() : filteredList;

            List<Transaction> transactionsWithProfitPositive = symbolFilteredList.stream().filter(transaction -> transaction.getProfit() > 0).toList();
            List<Transaction> transactionsWithProfitNegative = symbolFilteredList.stream().filter(transaction -> transaction.getProfit() < 0).toList();

            calculateTargetProfits(transactionsWithProfitPositive, targetTicks);
            calculateProfits(symbolFilteredList);
            calculateAvgATR(symbolFilteredList);
            this.nrTrades = symbolFilteredList.size();
            this.nrWins = transactionsWithProfitPositive.size();
            this.nrLosses = transactionsWithProfitNegative.size();
            calculateActualLosses(transactionsWithProfitNegative);
            this.netProfits = targetTicks == 0 ? 0 : targetProfits - (actualLosses * -1);
            this.winRate = Math.round((float) transactionsWithProfitPositive.size() / symbolFilteredList.size() * 100);
            if (!transactionsWithProfitPositive.isEmpty()) {
                this.averageWinAmount = calculateAvgAmount(transactionsWithProfitPositive);
            }
            if (!transactionsWithProfitNegative.isEmpty()) {
                this.averageLossAmount = calculateAvgAmount(transactionsWithProfitNegative);
                this.winRatio = calculateWinRate(transactionsWithProfitPositive, transactionsWithProfitNegative);
                this.payoffRatio = calculatePayoffRatio(transactionsWithProfitPositive, transactionsWithProfitNegative);
            }
            ///avg loss is multipled by -1 to make the value positive
            this.avgLossAvgWinRatio = targetProfits > 0 ? (averageLossAmount * -1) / targetProfits : 0.0;
            ///average atr / 2 is used to calculate the stop loss.
            this.stopLossObjectiveRatio = targetProfits > 0 && averageATR > 0 ? (averageATR / 2) / targetProfits : 0.0;
            formationList.forEach(formation -> calculateWinRateFormation(symbolFilteredList, formation.getFormation()));
        } else {
            formationList.forEach(formation -> calculateWinRateFormation(filteredList, formation.getFormation()));
        }
    }

    private double calculateAvgAmount(List<Transaction> transactions) {
        double runningTotal = 0;
        for (Transaction transaction : transactions) {
            runningTotal += transaction.getProfit();
        }
        return runningTotal / transactions.size();
    }

    private void calculateAvgATR(List<Transaction> transactions) {
        double runningTotal = 0;
        for (Transaction transaction : transactions) {
            runningTotal += transaction.getATR();
        }
        averageATR = runningTotal / transactions.size();
    }

    private void calculateTargetProfits(List<Transaction> transactionsWithProfitPositive,
                                        Double targetTicks) {
        if (!transactionsWithProfitPositive.isEmpty() || targetTicks != 0) {
            Symbol symbol = null;
            for (Transaction transaction : transactionsWithProfitPositive) {
                if (symbol == null) {
                    symbol = symbolList.stream().filter(p -> p.getSymbol().equals(transaction.getSymbol())).findFirst().get();
                }
                double profitTicks = transaction.getProfit() / symbol.getFluctuation();
                if (profitTicks >= targetTicks) {
                    targetProfits += targetTicks * symbol.getTickValue();
                }
            }
        }
    }

    private void calculateProfits(List<Transaction> transactions) {
        for (Transaction transaction : transactions) {
            Double symbolTickValue = symbolList.stream().filter(p -> p.getSymbol().equals(transaction.getSymbol())).findFirst().get().getTickValue();
            possibleProfits += transaction.getPossibleProfitTicks() * symbolTickValue;
            possibleLosses += transaction.getPossibleLossTicks() * symbolTickValue;
        }
    }

    private void calculateActualLosses(List<Transaction> transactions) {
        for (Transaction transaction : transactions) {
            actualLosses += transaction.getProfit();
        }
    }

    private void calculateWinRateFormation(List<Transaction> transactions,
                                           String formation) {
        BigDecimal amountOfTradesWon = new BigDecimal(transactions.stream().filter(transaction -> transaction.getProfit() > 0 && transaction.getFormation().equals(formation)).toList().size());
        BigDecimal amountOfOccurrences = new BigDecimal(transactions.stream().filter(transaction -> transaction.getFormation().equals(formation)).toList().size());
        formationsWinRate.add(new Formation(formation, amountOfOccurrences.intValueExact() == 0 ? 0.00 : amountOfTradesWon.divide(amountOfOccurrences, 2, RoundingMode.HALF_UP).multiply(new BigDecimal(100)).doubleValue()));
    }

    private Double averageListProfit(List<Transaction> transactions) {
        OptionalDouble average = transactions.stream().mapToDouble(Transaction::getProfit).average();

        if (average.isPresent()) {
            return average.getAsDouble();
        } else {
            return 0.0;
        }
    }

    private Double calculatePayoffRatio(List<Transaction> positiveTransactions,
                                        List<Transaction> negativeTransactions) {
        BigDecimal averageNegativeTrans = BigDecimal.valueOf(this.averageListProfit(negativeTransactions));
        BigDecimal averagePositiveTrans = BigDecimal.valueOf(this.averageListProfit(positiveTransactions));
        return averagePositiveTrans.doubleValue() == 0 ? 0.00 : averageNegativeTrans.multiply(new BigDecimal(-1)).divide(averagePositiveTrans, 2, RoundingMode.HALF_UP).setScale(2, RoundingMode.HALF_UP).doubleValue();
    }

    private Double calculateWinRate(List<Transaction> positiveTransactions,
                                    List<Transaction> negativeTransactions) {
        BigDecimal positiveSize = new BigDecimal(positiveTransactions.size());
        BigDecimal negativeSize = new BigDecimal(negativeTransactions.size());
        return positiveSize.divide(negativeSize, 2, RoundingMode.HALF_UP).doubleValue();
    }

}
