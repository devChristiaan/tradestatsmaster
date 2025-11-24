package org.service;

import com.opencsv.bean.CsvToBeanBuilder;
import com.opencsv.bean.StatefulBeanToCsv;
import com.opencsv.bean.StatefulBeanToCsvBuilder;
import com.opencsv.exceptions.CsvDataTypeMismatchException;
import com.opencsv.exceptions.CsvRequiredFieldEmptyException;
import org.app.App;
import org.model.Formation;
import org.model.account.Account;
import org.model.symbol.Symbol;
import org.model.transaction.Transaction;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.FileReader;
import java.io.Writer;
import java.io.FileWriter;
import java.util.List;
import java.util.Objects;

public class csvReader {

    public static List<Formation> getAllFormations() throws IOException {
        List<Formation> formations;
        InputStreamReader reader = new InputStreamReader(Objects.requireNonNull(App.class.getModule().getResourceAsStream("org/app/data/formation.csv")));
        formations = new CsvToBeanBuilder<Formation>(reader).withType(Formation.class).build().parse();
        return formations;
    }

    public static List<Transaction> getAllTransactions(String fileLocation) {
        List<Transaction> transactions;
        try {
            FileReader fileReader = new FileReader(fileLocation);
            transactions = new CsvToBeanBuilder<Transaction>(fileReader).withType(Transaction.class).build().parse();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return transactions;
    }

    public static List<Symbol> getAllSymbols(String fileLocation) {
        List<Symbol> symbols;
        try {
            FileReader fileReader = new FileReader(fileLocation);
            symbols = new CsvToBeanBuilder<Symbol>(fileReader).withType(Symbol.class).build().parse();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return symbols;
    }

    public static List<Account> getAllAccountTransactions(String fileLocation) {
        List<Account> transactions;
        try {
            FileReader fileReader = new FileReader(fileLocation);
            transactions = new CsvToBeanBuilder<Account>(fileReader).withType(Account.class).build().parse();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return transactions;
    }

    public static <T> void writeItemsToCSV(List<T> items,
                                           String path) throws IOException {
        Writer writer = new FileWriter(path);
        StatefulBeanToCsv<T> beanToCsv = new StatefulBeanToCsvBuilder<T>(writer)
                .build();
        try {
            beanToCsv.write(items);
            writer.close();
        } catch (CsvRequiredFieldEmptyException |
                 CsvDataTypeMismatchException e) {
            throw new RuntimeException(e);
        }
    }
}
