package org.utilities;

import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.util.StringConverter;
import org.context.GlobalContext;
import org.model.Formation;
import org.model.transaction.Transaction;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class Utilities {

    public static void closeApp() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Close Application");
        alert.setHeaderText(null);
        alert.setContentText("Are you sure you want to close the application?");

        if (alert.showAndWait().get() == ButtonType.OK) {
            Platform.exit();
        }
    }

    public static StringConverter<LocalDate> calendarToStringConverter(String datePattern) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(datePattern);
        return new StringConverter<LocalDate>() {
            @Override
            public String toString(LocalDate date) {
                return (date != null) ? formatter.format(date) : "";
            }

            @Override
            public LocalDate fromString(String string) {
                return (string != null && !string.isEmpty()) ? LocalDate.parse(string, formatter) : null;
            }
        };
    }

    public static boolean isNumeric(String str) {
        try {
            Integer.parseInt(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public static boolean isDoubleNumeric(String str) {
        try {
            Double.parseDouble(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public static BigDecimal tickDifference(Formation.Direction direction, BigDecimal openAmount, BigDecimal closeAmount) {
        return direction == Formation.Direction.LONG ? closeAmount.subtract(openAmount) : openAmount.subtract(closeAmount);
    }

    public static double calculateProfit(Formation.Direction direction, BigDecimal openAmount, BigDecimal closeAmount, BigDecimal fluctuation, BigDecimal tickValue, BigDecimal quantity) {
        BigDecimal bd = tickDifference(direction, openAmount, closeAmount).divide(fluctuation, RoundingMode.HALF_UP).multiply(tickValue).multiply(quantity).setScale(2, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }

    public static double calculateCommission(double commission, int quantity) {
        ///Commission value is for one side of the transaction only. Multiply by 2 for open and close
        BigDecimal bd = BigDecimal.valueOf(commission * 2 * quantity).setScale(2, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }

    public static double calculateRunningTotal(double amount, double previousAmount) {
        BigDecimal bd = BigDecimal.valueOf(previousAmount + amount).setScale(2, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }

    public static double calculateAccountBalance(ObservableList<Transaction> transactions) {
        double accountBalance = GlobalContext.openingBalance;
        for (Transaction tran : GlobalContext.getTransactionsMasterList()) {
            accountBalance += tran.getProfit();
            accountBalance -= tran.getCommission();
        }
        return accountBalance;
    }

    public static BigDecimal calculateBalancePercentage(Double accountBalance) {
        return BigDecimal.valueOf(accountBalance).multiply(new BigDecimal("0.08")).setScale(2, RoundingMode.HALF_UP);
    }

    public static DecimalFormat getTextFormater() {
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.US);
        symbols.setGroupingSeparator(',');

        // Create DecimalFormat with the pattern and symbols
        return new DecimalFormat("#,##0.00", symbols);
    }

}
