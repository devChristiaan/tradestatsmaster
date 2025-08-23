package org.context;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import org.model.Scenes;
import org.model.transaction.Transaction;

import java.util.*;

public class GlobalContext {
    //Zero index for 5 trades moving average
    public static int movingAvgNr = 4;
    public static String datePattern = "dd/MM/yyyy"; // Global Date Format
    private static final Map<ContextItems, Object> globalContext = new HashMap<>();
    private static ObservableList<Transaction> transactionsMasterList = FXCollections.observableList(new ArrayList<>());
    private static SortedList<Transaction> sortedData = new SortedList<>(transactionsMasterList);
    private static FilteredList<Transaction> filteredTransactions = new FilteredList<>(sortedData, p -> true);
    ///Temp solution -- need to fix with a user entered number
    public static double openingBalance = 30683.88;

    public enum ContextItems {
        SYMBOL_LIST,
        FORMATION_LIST,
        TRANSACTION_LIST,
    }

    public static <T> void add(ContextItems itemName, T instance) {
        globalContext.put(itemName, instance);
    }

    public static Object get(ContextItems itemName) {
        return globalContext.get(itemName);
    }

    public static void delete(ContextItems itemName) {
        globalContext.remove(itemName);
    }

    public boolean isEmpty(ContextItems itemName) {
        return globalContext.get(itemName) == null;
    }

    public static void setTransactionsMasterList(List<Transaction> list) {
        transactionsMasterList.addAll(list);
    }

    public static void replaceMasterList(List<Transaction> list) {
        transactionsMasterList.clear();
        transactionsMasterList.addAll(list);
    }

    public static void addTransactionToMasterList(Transaction transaction) {
        transactionsMasterList.add(transaction);
    }

    public static ObservableList<Transaction> getTransactionsMasterList() {
        return transactionsMasterList;
    }

    public static FilteredList<Transaction> getFilteredTransactions() {
        return filteredTransactions;
    }

}
