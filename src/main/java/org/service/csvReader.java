package org.service;

import com.opencsv.bean.CsvToBeanBuilder;
import com.opencsv.bean.StatefulBeanToCsv;
import com.opencsv.bean.StatefulBeanToCsvBuilder;
import com.opencsv.exceptions.CsvDataTypeMismatchException;
import com.opencsv.exceptions.CsvRequiredFieldEmptyException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class csvReader {
    private static final Logger log = LogManager.getLogger(csvReader.class);

    public static List<Formation> getAllFormations() {
        List<Formation> formations = new ArrayList<>();
        try {
            InputStreamReader reader = new InputStreamReader(Objects.requireNonNull(App.class.getModule().getResourceAsStream("org/app/data/formation.csv")));
            formations = new CsvToBeanBuilder<Formation>(reader).withType(Formation.class).build().parse();
        } catch (IOException e) {
            log.error("Failed to parse formation.csv");
        }
        return formations;
    }

    public static List<Transaction> getAllTransactions(String fileLocation) {
        List<Transaction> transactions = new ArrayList<>();
        try {
            FileReader fileReader = new FileReader(fileLocation);
            transactions = new CsvToBeanBuilder<Transaction>(fileReader).withType(Transaction.class).build().parse();
        } catch (IOException e) {
            log.error("Failed to open file {}", fileLocation, e);
        }
        return transactions;
    }

    public static List<Symbol> getAllSymbols(String fileLocation) {
        List<Symbol> symbols = new ArrayList<>();
        try {
            FileReader fileReader = new FileReader(fileLocation);
            symbols = new CsvToBeanBuilder<Symbol>(fileReader).withType(Symbol.class).build().parse();
        } catch (IOException e) {
            log.error("Failed to open file {}", fileLocation, e);
        }
        return symbols;
    }

    public static List<Account> getAllAccountTransactions(String fileLocation) {
        List<Account> transactions = new ArrayList<>();
        try {
            FileReader fileReader = new FileReader(fileLocation);
            transactions = new CsvToBeanBuilder<Account>(fileReader).withType(Account.class).build().parse();
        } catch (IOException e) {
            log.error("Failed to open file {}", fileLocation, e);
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
            log.error("Failed to write items to file: {}", e.getMessage());
        }
    }
}
