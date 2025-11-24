package org.context;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import org.model.symbol.Symbol;
import org.model.dailyPrep.DailyPrep;
import org.model.transaction.Transaction;

import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.List;

public class GlobalContext {
    //Zero index for 5 trades moving average
    public static int movingAvgNr = 4;
    public static String datePattern = "dd/MM/yyyy"; // Global Date Format
    private static final Map<ContextItems, Object> globalContext = new HashMap<>();

    /// Trade Vars
    private static ObservableList<Transaction> transactionsMasterList = FXCollections.observableList(new ArrayList<>());
    private static SortedList<Transaction> sortedData = new SortedList<>(transactionsMasterList);
    private static FilteredList<Transaction> filteredTransactions = new FilteredList<>(sortedData, p -> true);

    /// Daily Prep Date Vars
    private static ObservableList<DailyPrep> dailyPrepMasterList = FXCollections.observableList(new ArrayList<>());
    private static FilteredList<DailyPrep> filteredDailyPrep = new FilteredList<>(dailyPrepMasterList, p -> true);

    private static ObservableList<Symbol> symbolsMasterList = FXCollections.observableList(new ArrayList<>());
    private static FilteredList<Symbol> filteredSymbolList = new FilteredList<>(symbolsMasterList, p -> true);

    /// Temp solution -- need to fix with a user entered number
    public static double openingBalance = 30683.89;

    public enum ContextItems {
        FORMATION_LIST,
    }

    public static <T> void add(ContextItems itemName, T instance) {
        globalContext.put(itemName, instance);
    }

    public static Object get(ContextItems itemName) {
        return globalContext.get(itemName);
    }

    public static void setSymbolsMasterList(List<Symbol> symbols) {
        symbolsMasterList.addAll(symbols);
    }

    public static void addSymbolToMasterList(Symbol symbol) {
        symbolsMasterList.add(symbol);
    }

    public static FilteredList<Symbol> getFilteredSymbolList() {
        return filteredSymbolList;
    }

    public static void setTransactionsMasterList(List<Transaction> list) {
        transactionsMasterList.addAll(list);
    }

    public static void setDailyPrepMasterList(List<DailyPrep> list) {
        dailyPrepMasterList.addAll(list);
    }

    public static void reSetDailyPrepMasterList(List<DailyPrep> list) {
        dailyPrepMasterList.clear();
        dailyPrepMasterList.addAll(list);
    }

    public static List<DailyPrep> getDailyPrepMasterList() {
        return dailyPrepMasterList;
    }

    public static FilteredList<DailyPrep> getFilteredDailyPrep() {
        return filteredDailyPrep;
    }

    public static void replaceMasterList(List<Transaction> list) {
        transactionsMasterList.clear();
        transactionsMasterList.addAll(list);
    }

    public static void addTransactionToMasterList(Transaction transaction) {
        transactionsMasterList.add(transaction);
    }

    public static void replaceTransactionInMasterList(Transaction transaction) {
        transactionsMasterList.set(transactionsMasterList.indexOf(transaction), transaction);
    }

    public static ObservableList<Transaction> getTransactionsMasterList() {
        return transactionsMasterList;
    }

    public static FilteredList<Transaction> getFilteredTransactions() {
        return filteredTransactions;
    }

}
