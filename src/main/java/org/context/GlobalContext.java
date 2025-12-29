package org.context;

import lombok.Getter;
import org.model.account.Account;
import org.model.goal.Goal;
import org.model.journal.Journal;
import org.model.symbol.Symbol;
import org.model.dailyPrep.DailyPrep;
import org.model.transaction.Transaction;

import java.util.Map;
import java.util.HashMap;

public class GlobalContext {
    public static String datePattern = "dd/MM/yyyy"; // Global Date Format
    private static final Map<ContextItems, Object> globalContext = new HashMap<>();

    @Getter
    private static final ListContext<Transaction> transactions = new ListContext<>();
    @Getter
    private static final ListContext<DailyPrep> dailyPrep = new ListContext<>();
    @Getter
    private static final ListContext<Journal> journals = new ListContext<>();
    @Getter
    private static final ListContext<Symbol> symbols = new ListContext<>();
    @Getter
    private static final ListContext<Account> accounts = new ListContext<>();
    @Getter
    private static final ListContext<Goal> goals = new ListContext<>();

    public enum ContextItems {
        FORMATION_LIST,
    }

    public static <T> void add(ContextItems itemName, T instance) {
        globalContext.put(itemName, instance);
    }

    public static Object get(ContextItems itemName) {
        return globalContext.get(itemName);
    }
}
