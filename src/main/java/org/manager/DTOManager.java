package org.manager;

import org.model.DataObjectFileType;
import org.model.account.Account;
import org.model.account.AccountDTO;
import org.model.symbol.Symbol;
import org.model.symbol.SymbolDTO;
import org.service.DataObjectService;

import java.util.ArrayList;
import java.util.List;

public class DTOManager {
    public static List<Symbol> getAllSymbols() {
        SymbolDTO symbols = DataObjectService.loadObject(DataObjectFileType.SYMBOLS);
        if (symbols == null) {
            DataObjectService.saveObject(new SymbolDTO(), DataObjectFileType.SYMBOLS);
            symbols = DataObjectService.loadObject(DataObjectFileType.SYMBOLS);
        }
        return symbols.getSymbols();
    }

    public static void addSymbol(Symbol symbol) {
        SymbolDTO symbols = DataObjectService.loadObject(DataObjectFileType.SYMBOLS);
        if (symbols != null) {
            symbols.addSymbol(symbol);
            DataObjectService.saveObject(symbols, DataObjectFileType.SYMBOLS);
        }
    }

    public static void removeSymbol(Symbol symbol) {
        SymbolDTO symbols = DataObjectService.loadObject(DataObjectFileType.SYMBOLS);
        if (symbols != null) {
            symbols.deleteSymbol(symbol);
            DataObjectService.saveObject(symbols, DataObjectFileType.SYMBOLS);
        }
    }


    public static void addAllSymbol(List<Symbol> symbolList) {
        SymbolDTO symbols = DataObjectService.loadObject(DataObjectFileType.SYMBOLS);
        if (symbols != null) {
            symbols.addAllSymbols(new ArrayList<>(symbolList));
            DataObjectService.saveObject(symbols, DataObjectFileType.SYMBOLS);
        }
    }

    public static List<Account> getAllAccountTransactions() {
        AccountDTO accountTransactions = DataObjectService.loadObject(DataObjectFileType.ACCOUNT_TRANSACTIONS);
        if (accountTransactions == null) {
            DataObjectService.saveObject(new AccountDTO(), DataObjectFileType.ACCOUNT_TRANSACTIONS);
            accountTransactions = DataObjectService.loadObject(DataObjectFileType.ACCOUNT_TRANSACTIONS);
        }
        return accountTransactions.getAllTransactions();
    }

    public static void addAccountTransaction(Account transaction) {
        AccountDTO accountTransactions = DataObjectService.loadObject(DataObjectFileType.ACCOUNT_TRANSACTIONS);
        if (accountTransactions != null) {
            accountTransactions.addTransaction(transaction);
            DataObjectService.saveObject(accountTransactions, DataObjectFileType.ACCOUNT_TRANSACTIONS);
        }
    }

    public static void removeAccountTransaction(Account transaction) {
        AccountDTO accountTransactions = DataObjectService.loadObject(DataObjectFileType.ACCOUNT_TRANSACTIONS);
        if (accountTransactions != null) {
            accountTransactions.deleteTransaction(transaction);
            DataObjectService.saveObject(accountTransactions, DataObjectFileType.ACCOUNT_TRANSACTIONS);
        }
    }

    public static void addAllAccountTransactions(List<Account> transactionList) {
        AccountDTO accountTransactions = DataObjectService.loadObject(DataObjectFileType.ACCOUNT_TRANSACTIONS);
        if (accountTransactions != null) {
            accountTransactions.addAllTransactions(new ArrayList<>(transactionList));
            DataObjectService.saveObject(accountTransactions, DataObjectFileType.ACCOUNT_TRANSACTIONS);
        }
    }
}
