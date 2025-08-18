package org.utilities;

import javafx.collections.transformation.FilteredList;
import lombok.Getter;
import org.model.transaction.Transaction;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.OptionalDouble;

@Getter
public class CalculateStats {
    private double totalProfit = 0.0;
    private double totalCommission = 0.0;
    private double netIncome = 0.0;
    private int winRate = 0;
    private double winRatio = 0.0;
    private double commissionRatio = 0.0;
    private double payoffRatio = 0.0;

    public CalculateStats(FilteredList<Transaction> filteredList) {
        if (!filteredList.isEmpty()) {
            for (Transaction tran : filteredList) {
                populateTotals(tran);
            }
            List<Transaction> transactionsWithProfitPositive = filteredList.stream().filter(transaction -> transaction.getProfit() > 0).toList();
            List<Transaction> transactionsWithProfitNegative = filteredList.stream().filter(transaction -> transaction.getProfit() < 0).toList();

            this.netIncome = this.totalProfit - this.totalCommission;
            this.winRate = Math.round((float) transactionsWithProfitPositive.size() / filteredList.size() * 100);

            if (!transactionsWithProfitNegative.isEmpty()) {
                this.winRatio = calculateWinRate(transactionsWithProfitPositive, transactionsWithProfitNegative);
                BigDecimal dbPayoffRation = BigDecimal.valueOf(transactionsWithProfitPositive.stream().mapToDouble(Transaction::getProfit).average().getAsDouble() * -1 / transactionsWithProfitNegative.stream().mapToDouble(Transaction::getProfit).average().getAsDouble()).setScale(2, RoundingMode.HALF_UP);
                this.payoffRatio = dbPayoffRation.doubleValue();
            }
            BigDecimal dbCommissionRatio = new BigDecimal(totalProfit / totalCommission).setScale(2, RoundingMode.HALF_UP);
            this.commissionRatio = dbCommissionRatio.doubleValue();
        }
    }

    void populateTotals(Transaction tran) {
        this.totalProfit += tran.getProfit();
        this.totalCommission += tran.getCommission();
    }

    private Double averageListProfit(List<Transaction> transactions) {
        OptionalDouble average = transactions.stream().mapToDouble(Transaction::getProfit).average();

        if (average.isPresent()) {
            return average.getAsDouble();
        } else {
            return 0.0;
        }
    }

    private Double calculateWinRate(List<Transaction> positiveTransactions, List<Transaction> negativeTransactions) {
        BigDecimal positiveSize = new BigDecimal(positiveTransactions.size());
        BigDecimal negativeSize = new BigDecimal(negativeTransactions.size());
        return positiveSize.divide(negativeSize, 2, RoundingMode.HALF_UP).doubleValue();
    }
}
