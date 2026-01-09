package org.utilities;

import javafx.collections.transformation.FilteredList;
import lombok.Getter;
import org.context.GlobalContext;
import org.model.Formation;
import org.model.transaction.Transaction;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.OptionalDouble;

import static org.utilities.Utilities.getTextFormater;

@Getter
public class CalculateStatsOverview {
    private double totalProfit = 0.0;
    private double totalLoss = 0.0;
    private double totalCommission = 0.0;
    private double netIncome = 0.0;
    private int winRate = 0;
    private double winRatio = 0.0;
    private double commissionRatio = 0.0;
    private double payoffRatio = 0.0;
    private final List<Formation> formationList = (List<Formation>) GlobalContext.get(GlobalContext.ContextItems.FORMATION_LIST);
    private List<Formation> formationsWinRate = new ArrayList<>();
    DecimalFormat df = getTextFormater();

    public CalculateStatsOverview(FilteredList<Transaction> filteredList) {
        if (!filteredList.isEmpty()) {
            for (Transaction tran : filteredList) {
                populateTotals(tran);
            }
            List<Transaction> transactionsWithProfitPositive = filteredList.stream().filter(transaction -> transaction.getProfit() > 0).toList();
            List<Transaction> transactionsWithProfitNegative = filteredList.stream().filter(transaction -> transaction.getProfit() < 0).toList();

            this.netIncome = this.totalProfit + this.totalLoss - this.totalCommission;
            this.winRate = Math.round((float) transactionsWithProfitPositive.size() / filteredList.size() * 100);

            if (!transactionsWithProfitNegative.isEmpty()) {
                this.winRatio = calculateWinRate(transactionsWithProfitPositive, transactionsWithProfitNegative);
                this.payoffRatio = calculatePayoffRatio(transactionsWithProfitPositive, transactionsWithProfitNegative);
            }
            if (this.totalProfit > 0) {
                this.commissionRatio = calculateCommissionRatio(this.totalProfit, this.totalCommission);
            }
        }

        formationList.forEach(formation -> calculateWinRateFormation(filteredList, formation.getFormation()));
    }

    void populateTotals(Transaction tran) {
        if (tran.getProfit() > 0) {
            this.totalProfit += tran.getProfit();
        } else {
            this.totalLoss += tran.getProfit();
        }
        this.totalCommission += tran.getCommission();
    }

    private Double calculateCommissionRatio(Double profit, Double commission) {
        BigDecimal dbProfit = BigDecimal.valueOf(profit);
        BigDecimal dbCommission = BigDecimal.valueOf(commission);
        return dbCommission.divide(dbProfit, 2, RoundingMode.HALF_UP).doubleValue() * 100;
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

    private void calculateWinRateFormation(List<Transaction> transactions,
                                           String formation) {
        BigDecimal amountOfTradesWon = new BigDecimal(transactions.stream().filter(transaction -> transaction.getProfit() > 0 && transaction.getFormation().equals(formation)).toList().size());
        BigDecimal amountOfOccurrences = new BigDecimal(transactions.stream().filter(transaction -> transaction.getFormation().equals(formation)).toList().size());
        formationsWinRate.add(new Formation(formation, amountOfOccurrences.intValueExact() == 0 ? 0.00 : amountOfTradesWon.divide(amountOfOccurrences, 2, RoundingMode.HALF_UP).multiply(new BigDecimal(100)).doubleValue()));
    }

    public String getTotalCommission() {
        return df.format(this.totalCommission);
    }

    public String getTotalProfitFormat() {
        return df.format(this.totalProfit);
    }

    public String getTotalLossFormat() {
        return df.format(this.totalLoss);
    }

    public Double getTotalProfit() {
        return this.totalProfit;
    }

    public String getNetIncome() {
        return df.format(this.netIncome);
    }
}
