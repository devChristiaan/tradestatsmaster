package org.manager.DBManager;

import org.service.SqliteConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;

import static org.manager.DBManager.RepositoryHelper.*;

public class StartUpRepository {
    private final Logger log = LoggerFactory.getLogger(StartUpRepository.class);
    private final Connection conn;

    public StartUpRepository(SqliteConnection db) {
        this.conn = db.getConnection();
    }

    public boolean foreignKeyEnabled() {
        String sql = "PRAGMA foreign_keys;";
        return checkForeignKey(conn, sql);
    }

    public boolean doesTableExist(String tableName){
        String sql = "SELECT name FROM sqlite_master WHERE type='table' AND name='" + tableName + "'";
        return check(conn, sql);
    }

    public void enableForeignKeys() {
        String createTableSQL = "PRAGMA foreign_keys = ON;";
        create(conn, createTableSQL);
        log.info("Foreign key enabled");
    }

    public void createTransactionTable() {
        String createTableSQL = "CREATE TABLE IF NOT EXISTS transactions (" +
                "id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT," +
                "date ANY NOT NULL," +
                "symbol TEXT NOT NULL," +
                "quantity INTEGER NOT NULL," +
                "commission REAL NOT NULL," +
                "direction TEXT NOT NULL," +
                "open REAL NOT NULL," +
                "close REAL NOT NULL," +
                "profit REAL NOT NULL," +
                "formation TEXT NOT NULL," +
                "ATR REAL NOT NULL," +
                "ATRRisk REAL NOT NULL," +
                "possibleProfitTicks REAL NOT NULL," +
                "possibleLossTicks REAL NOT NULL," +
                "actualLossTicks REAL NOT NULL," +
                "timePeriod TEXT NOT NULL" +
                ");";
        create(conn, createTableSQL);
        log.info("Table transactions created");
    }

    public void createDailyPrepDateTable() {
        String createTableSQL = "CREATE TABLE DailyPrepDate (\n" +
                "dailyPrepDateId INTEGER PRIMARY KEY AUTOINCREMENT,\n" +
                "date ANY NOT NULL\n" +
                ");";
        create(conn, createTableSQL);
        log.info("Table DailPrepDate created");
    }

    public void createDailyPrepTable() {
        String createTableSQL = "CREATE TABLE DailyPrep (\n" +
                "dailyPrepId INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,\n" +
                "date ANY,\n" +
                "dailyPrepDateId INTEGER,\n" +
                "symbol TEXT,\n" +
                "dailyEvents TEXT,\n" +
                "hourlyTrend TEXT,\n" +
                "halfHourlyTrend TEXT,\n" +
                "dailyTrend TEXT,\n" +
                "hh_ll_3_bars_high REAL,\n" +
                "hh_ll_3_bars_low REAL,\n" +
                "hh_ll_any_high REAL,\n" +
                "hh_ll_any_low REAL,\n" +
                "FOREIGN KEY (dailyPrepDateId) REFERENCES parent_table(DailyPrepDate) ON DELETE CASCADE ON UPDATE NO ACTION\n" +
                ");";
        create(conn, createTableSQL);
        log.info("Table DailyPrep created");
    }

    public void createJournalTable() {
        String createTableSQL = "CREATE TABLE IF NOT EXISTS Journal (" +
                "id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT," +
                "date ANY NOT NULL," +
                "document BLOB NOT NULL," +
                "symbol TEXT NOT NULL" +
                ");";
        create(conn, createTableSQL);
        log.info("Table Journal created");
    }

    public void createGoalTable() {
        String createTableSQL = "CREATE TABLE IF NOT EXISTS Goal (" +
                "id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT," +
                "date ANY NOT NULL," +
                "timeHorizon TEXT NOT NULL," +
                "document BLOB NOT NULL," +
                "achieved INTEGER NOT NULL" +
                ");";
        create(conn, createTableSQL);
        log.info("Table Goal created");
    }

    public void dbStartUpChecks() {
        ensureTable("transactions", this::createTransactionTable);
        ensureTable("DailyPrepDate", this::createDailyPrepDateTable);
        ensureTable("DailyPrep", this::createDailyPrepTable);
        ensureTable("Journal", this::createJournalTable);
        ensureTable("Goal", this::createGoalTable);

        ensureForeignKeys();
    }

    private void ensureTable(String table, Runnable creator) {
        if (!doesTableExist(table)) {
            creator.run();
        }
    }

    private void ensureForeignKeys() {
        if (!foreignKeyEnabled()) {
            enableForeignKeys();
        }
    }

}
