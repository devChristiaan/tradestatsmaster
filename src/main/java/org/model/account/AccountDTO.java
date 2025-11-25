package org.model.account;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Objects;

public class AccountDTO extends Account implements Serializable {
    private final ArrayList<Account> listOfAccountTransactions = new ArrayList<>();

    public void addTransaction(Account transaction) {
        this.listOfAccountTransactions.add(transaction);
    }

    public void addAllTransactions(ArrayList<Account> listOfTrans) {
        this.listOfAccountTransactions.addAll(listOfTrans);
    }

    public void deleteTransaction(Account transaction) {
        Account symbolIndex = listOfAccountTransactions.stream().filter(p -> p.getDate().equals(transaction.getDate()) && Objects.equals(p.getAmount(), transaction.getAmount())).findFirst().orElse(null);
        this.listOfAccountTransactions.remove(symbolIndex);
    }

    public ArrayList<Account> getAllTransactions() {
        return listOfAccountTransactions;
    }
}
