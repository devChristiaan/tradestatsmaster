package org.service;

import com.opencsv.bean.CsvToBeanBuilder;
import com.opencsv.bean.StatefulBeanToCsv;
import com.opencsv.bean.StatefulBeanToCsvBuilder;
import com.opencsv.exceptions.CsvDataTypeMismatchException;
import com.opencsv.exceptions.CsvRequiredFieldEmptyException;
import org.app.App;
import org.model.Formation;
import org.model.Symbol;
import org.model.transaction.Transaction;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.FileReader;
import java.io.Writer;
import java.io.FileWriter;
import java.util.List;
import java.util.Objects;

public class csvReader {
    public static List<Symbol> getAllSymbols() throws IOException {
        List<Symbol> symbols;
        InputStreamReader reader = new InputStreamReader(Objects.requireNonNull(App.class.getModule().getResourceAsStream("org/app/data/symbol.csv")));
        symbols = new CsvToBeanBuilder<Symbol>(reader).withType(Symbol.class).build().parse();
        return symbols;
    }

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

    public static void writeTransactionsToCSV(List<Transaction> transactions, String path) throws IOException {
        Writer writer = new FileWriter(path + "/" + "transactions.csv");
        StatefulBeanToCsv<Transaction> beanToCsv = new StatefulBeanToCsvBuilder<Transaction>(writer)
                .build();
        try {
            beanToCsv.write(transactions);
            writer.close();
        } catch (CsvRequiredFieldEmptyException | CsvDataTypeMismatchException e) {
            throw new RuntimeException(e);
        }
    }
}
