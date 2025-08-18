package org.utilities;

import javafx.collections.transformation.FilteredList;
import lombok.Getter;
import org.model.transaction.Transaction;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

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
                BigDecimal dbWinRatio = new BigDecimal(transactionsWithProfitPositive.size() / transactionsWithProfitNegative.size()).setScale(2, RoundingMode.HALF_UP);
                this.winRatio = dbWinRatio.doubleValue();
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
}
