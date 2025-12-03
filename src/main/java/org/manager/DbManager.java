package org.manager;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.model.dailyPrep.DailyPrep;
import org.model.dailyPrep.DailyPrepDate;
import org.model.dailyPrep.DailyPrepItems;
import org.model.goal.ETimeHorizon;
import org.model.goal.Goal;
import org.model.journal.Journal;
import org.model.transaction.Transaction;
import org.service.SqliteConnection;

import java.io.IOException;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class DbManager {
    private static final Logger log = LogManager.getLogger(DbManager.class);
    Connection bdConnection;

    public void setBdConnection() throws IOException {
        try {
            bdConnection = SqliteConnection.getConnection();
        } catch (IOException e) {
            log.error("Failed to connect to DB", e);
        }
    }

    public void closeBdConnection() throws SQLException {
        if (isDbConnected()) {
            bdConnection.close();
        }
    }

    public boolean isDbConnected() {
        try {
            return !bdConnection.isClosed();
        } catch (SQLException e) {
            log.error("DB error: {}", e.getMessage());
            return false;
        }
    }

    public boolean doesTableExist(String tableName, DbManager db) {
        if (db.isDbConnected()) {
            boolean exists = false;
            try {
                Statement stmt = bdConnection.createStatement();
                String sql = "SELECT name FROM sqlite_master WHERE type='table' AND name='" + tableName + "'";
                ResultSet rs = stmt.executeQuery(sql);
                exists = rs.next(); // If rs.next() returns true, a row was found, meaning the table exists.
            } catch (SQLException e) {
                log.error("Error checking {} existence: {}", tableName, e.getMessage());
                return exists;
            } finally {
                return exists;
            }
        } else {
            return false;
        }
    }

    public boolean foreignKeyEnabled(DbManager db) {
        if (db.isDbConnected()) {
            boolean exists = false;
            try {
                Statement stmt = bdConnection.createStatement();
                String sql = "PRAGMA foreign_keys;";
                ResultSet rs = stmt.executeQuery(sql);
                exists = rs.getInt(1) == 1;
                log.info("Foreign key enabled: {}", exists);
            } catch (SQLException e) {
                log.error("Error checking foreign_keys existence: {}", e.getMessage());
                return exists;
            } finally {
                return exists;
            }
        } else {
            return false;
        }
    }

    public void createTransactionTable(DbManager db) throws SQLException {
        if (db.isDbConnected()) {
            Statement statement = null;
            try {
                statement = bdConnection.createStatement();
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
                statement.execute(createTableSQL);
                log.info("Table transactions created");
            } catch (SQLException e) {
                log.error("Failed to create new DB: {}", e.getMessage());
            } finally {
                statement.close();
            }
        }
    }

    public void enableForeignKeys(DbManager db) throws SQLException {
        if (db.isDbConnected()) {
            Statement statement = null;
            try {
                statement = bdConnection.createStatement();
                String createTableSQL = "PRAGMA foreign_keys = ON;";
                statement.execute(createTableSQL);
                log.info("Foreign key enabled");
            } catch (SQLException e) {
                log.error("Failed to enable foreign keys on new DB: {}", e.getMessage());
            } finally {
                statement.close();
            }
        }
    }

    public void createDailyPrepDateTable(DbManager db) throws SQLException {
        if (db.isDbConnected()) {
            Statement statement = null;
            try {
                statement = bdConnection.createStatement();
                String createTableSQL = "CREATE TABLE DailyPrepDate (\n" +
                        "dailyPrepDateId INTEGER PRIMARY KEY AUTOINCREMENT,\n" +
                        "date ANY NOT NULL\n" +
                        ");";
                statement.execute(createTableSQL);
                log.info("Table DailPrepDate created");
            } catch (SQLException e) {
                log.error("Failed to create table DailyPrepDate on new DB: {}", e.getMessage());
            } finally {
                statement.close();
            }
        }
    }

    public void createDailyPrepTable(DbManager db) throws SQLException {
        if (db.isDbConnected()) {
            Statement statement = null;
            try {
                statement = bdConnection.createStatement();
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
                statement.execute(createTableSQL);
                log.info("Table DailyPrep created");
            } catch (SQLException e) {
                log.error("Failed to create table DailyPrep on new DB: {}", e.getMessage());
            } finally {
                statement.close();
            }
        }
    }

    public void createJournalTable(DbManager db) throws SQLException {
        if (db.isDbConnected()) {
            Statement statement = null;
            try {
                statement = bdConnection.createStatement();
                String createTableSQL = "CREATE TABLE IF NOT EXISTS Journal (" +
                        "id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT," +
                        "date ANY NOT NULL," +
                        "text TEXT NOT NULL," +
                        "symbol TEXT NOT NULL" +
                        ");";
                statement.execute(createTableSQL);
                log.info("Table Journal created");
            } catch (SQLException e) {
                log.error("Failed to create table Journal on new DB: {}", e.getMessage());
            } finally {
                statement.close();
            }
        }
    }

    public void createGoalTable(DbManager db) throws SQLException {
        if (db.isDbConnected()) {
            Statement statement = null;
            try {
                statement = bdConnection.createStatement();
                String createTableSQL = "CREATE TABLE IF NOT EXISTS Goal (" +
                        "id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT," +
                        "date ANY NOT NULL," +
                        "timeHorizon TEXT NOT NULL," +
                        "text TEXT NOT NULL," +
                        "achieved INTEGER NOT NULL" +
                        ");";
                statement.execute(createTableSQL);
                log.info("Table Goal created");
            } catch (SQLException e) {
                log.error("Failed to create table Goal on new DB: {}", e.getMessage());
            } finally {
                statement.close();
            }
        }
    }

    public List<Transaction> getAllTransactions() throws SQLException {
        if (isDbConnected()) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            String query = "select * from transactions ORDER BY date ASC";
            try {
                ps = bdConnection.prepareStatement(query);
                rs = ps.executeQuery();
                List<Transaction> transactions = new ArrayList<>();
                while (rs.next()) {
                    transactions.add(new Transaction(
                            rs.getInt("id"),
                            rs.getDate("date").toLocalDate(),
                            rs.getString("symbol"),
                            rs.getInt("quantity"),
                            rs.getDouble("commission"),
                            rs.getString("direction"),
                            rs.getDouble("open"),
                            rs.getDouble("close"),
                            rs.getDouble("profit"),
                            rs.getString("formation"),
                            rs.getDouble("ATR"),
                            rs.getDouble("ATRRisk"),
                            rs.getDouble("possibleProfitTicks"),
                            rs.getDouble("possibleLossTicks"),
                            rs.getDouble("actualLossTicks"),
                            rs.getString("timePeriod")
                    ));
                }
                return transactions;
            } catch (Exception e) {
                log.error("Get All Transactions failed: {}", e.getMessage());
            } finally {
                log.info("All transaction retrieved.");
                ps.close();
                rs.close();
            }
        }
        return null;
    }

    public void addTransaction(Transaction transaction) throws SQLException {
        PreparedStatement ps = null;
        String query = "insert into transactions(date,symbol,quantity,commission,direction,open,close,profit,formation,ATR,ATRRisk,possibleProfitTicks,possibleLossTicks,actualLossTicks,timePeriod) VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
        try {
            ps = bdConnection.prepareStatement(query);
            ps.setDate(1, Date.valueOf(transaction.getDate()));
            ps.setString(2, transaction.getSymbol());
            ps.setInt(3, transaction.getQuantity());
            ps.setDouble(4, transaction.getCommission());
            ps.setString(5, transaction.getDirection());
            ps.setDouble(6, transaction.getOpen());
            ps.setDouble(7, transaction.getClose());
            ps.setDouble(8, transaction.getProfit());
            ps.setString(9, transaction.getFormation());
            ps.setDouble(10, transaction.getATR());
            ps.setDouble(11, transaction.getATRRisk());
            ps.setDouble(12, transaction.getPossibleProfitTicks());
            ps.setDouble(13, transaction.getPossibleLossTicks());
            ps.setDouble(14, transaction.getActualLossTicks());
            ps.setString(15, transaction.getTimePeriod());
            ps.executeUpdate();
            ps.close();
            log.info("Transaction added successfully");
        } catch (Exception e) {
            log.error("Add Transaction failed: {}", e.getMessage());
        }
    }

    public void updateTransaction(Transaction transaction) throws SQLException {
        PreparedStatement ps = null;
        String query = "update transactions set date = ?, symbol = ?, quantity = ?, commission = ?, direction = ?, open = ?, close = ?, profit = ?, formation = ?, ATR = ?, ATRRisk = ?, possibleProfitTicks = ?, possibleLossTicks = ?, actualLossTicks = ?, timePeriod = ? WHERE id = ?";
        try {
            ps = bdConnection.prepareStatement(query);
            ps.setDate(1, Date.valueOf(transaction.getDate()));
            ps.setString(2, transaction.getSymbol());
            ps.setInt(3, transaction.getQuantity());
            ps.setDouble(4, transaction.getCommission());
            ps.setString(5, transaction.getDirection());
            ps.setDouble(6, transaction.getOpen());
            ps.setDouble(7, transaction.getClose());
            ps.setDouble(8, transaction.getProfit());
            ps.setString(9, transaction.getFormation());
            ps.setDouble(10, transaction.getATR());
            ps.setDouble(11, transaction.getATRRisk());
            ps.setDouble(12, transaction.getPossibleProfitTicks());
            ps.setDouble(13, transaction.getPossibleLossTicks());
            ps.setDouble(14, transaction.getActualLossTicks());
            ps.setString(15, transaction.getTimePeriod());
            ps.setInt(16, transaction.getId());
            ps.executeUpdate();
            ps.close();
            log.info("Transaction id:{} updated successfully", transaction.getId());
        } catch (Exception e) {
            log.error("Update Transaction failed: {}", e.getMessage());
        }
    }

    public void addDailyPrepItem(
            DailyPrepItems dailyPrepItem) throws SQLException {
        PreparedStatement ps = null;
        String query = "update DailyPrep set dailyEvents = ?, hourlyTrend = ?, halfHourlyTrend = ?, dailyTrend = ?, hh_ll_3_bars_high = ?, hh_ll_3_bars_low = ?, hh_ll_any_high = ?, hh_ll_any_low = ? WHERE dailyPrepId = ?";
        try {
            ps = bdConnection.prepareStatement(query);
            ps.setString(1, dailyPrepItem.getDailyEvents());
            ps.setString(2, dailyPrepItem.getHourlyTrend());
            ps.setString(3, dailyPrepItem.getHalfHourlyTrend());
            ps.setString(4, dailyPrepItem.getDailyTrend());
            ps.setDouble(5, dailyPrepItem.getHh_ll_3_bars_high());
            ps.setDouble(6, dailyPrepItem.getHh_ll_3_bars_low());
            ps.setDouble(7, dailyPrepItem.getHh_ll_any_high());
            ps.setDouble(8, dailyPrepItem.getHh_ll_any_low());
            ps.setInt(9, dailyPrepItem.getDailyPrepId());
            ps.executeUpdate();
            ps.close();
            log.info("DailyPrepItem id:{} updated successfully", dailyPrepItem.getDailyPrepId());
        } catch (Exception e) {
            log.error("Failed to add DailyPrepItem: {}", e.getMessage());
        }
    }

    public Transaction getLatestTransaction() throws SQLException {
        if (isDbConnected()) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            String query = "SELECT * FROM transactions ORDER BY id DESC LIMIT 1;";
            try {
                ps = bdConnection.prepareStatement(query);
                rs = ps.executeQuery();
                Transaction transaction = null;
                while (rs.next()) {
                    transaction = new Transaction(
                            rs.getInt("id"),
                            rs.getDate("date").toLocalDate(),
                            rs.getString("symbol"),
                            rs.getInt("quantity"),
                            rs.getDouble("commission"),
                            rs.getString("direction"),
                            rs.getDouble("open"),
                            rs.getDouble("close"),
                            rs.getDouble("profit"),
                            rs.getString("formation"),
                            rs.getDouble("ATR"),
                            rs.getDouble("ATRRisk"),
                            rs.getDouble("possibleProfitTicks"),
                            rs.getDouble("possibleLossTicks"),
                            rs.getDouble("actualLossTicks"),
                            rs.getString("timePeriod")
                    );
                }
                return transaction;
            } catch (Exception e) {
                log.error("Failed to retrieve the last transaction: {}", e.getMessage());
            } finally {
                log.info("Latest transaction retrieved successfully.");
                ps.close();
                rs.close();
            }
        }
        return null;
    }

    public List<DailyPrep> getAllDailyPrepData() throws SQLException {
        if (isDbConnected()) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            String query = "select * from DailyPrepDate ORDER BY date ASC";
            try {
                ps = bdConnection.prepareStatement(query);
                rs = ps.executeQuery();
                List<DailyPrep> dailyPreps = new ArrayList<>();
                List<DailyPrepDate> dailyPrepDates = new ArrayList<>();
                while (rs.next()) {
                    dailyPrepDates.add(new DailyPrepDate(
                            rs.getInt("dailyPrepDateId"),
                            rs.getDate("date").toLocalDate()
                    ));
                }

                for (DailyPrepDate dailyPrepDate : dailyPrepDates) {
                    List<DailyPrepItems> dailyPrepItems = getDailyPrepItems(dailyPrepDate.getDailyPrepDateId());
                    dailyPreps.add(new DailyPrep(dailyPrepDate.getDailyPrepDateId(), dailyPrepDate.getDate(), null, dailyPrepItems));
                }
                return dailyPreps;
            } catch (Exception e) {
                log.error("Failed to retrieve the dailyPrepData: {}", e.getMessage());
            } finally {
                log.info("All DailyPredData entries retrieved.");
                ps.close();
                rs.close();
            }
        }
        return null;
    }

    public List<DailyPrepItems> getDailyPrepItems(int id) throws SQLException {
        if (isDbConnected()) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            String query = "SELECT *" +
                    "FROM DailyPrep \n" +
                    "WHERE dailyPrepDateId = ?;";
            try {
                ps = bdConnection.prepareStatement(query);
                ps.setInt(1, id);
                rs = ps.executeQuery();
                List<DailyPrepItems> dailyPrepItems = new ArrayList<>();
                while (rs.next()) {
                    dailyPrepItems.add(new DailyPrepItems(
                            rs.getDate("date").toLocalDate(),
                            rs.getInt("dailyPrepId"),
                            rs.getInt("dailyPrepDateId"),
                            rs.getString("dailyEvents"),
                            rs.getString("symbol"),
                            rs.getString("hourlyTrend"),
                            rs.getString("halfHourlyTrend"),
                            rs.getString("dailyTrend"),
                            rs.getDouble("hh_ll_3_bars_high"),
                            rs.getDouble("hh_ll_3_bars_low"),
                            rs.getDouble("hh_ll_any_high"),
                            rs.getDouble("hh_ll_any_low")));
                }
                return dailyPrepItems;
            } catch (Exception e) {
                log.error("Get daily prep item id: {} failed: {}", id, e.getMessage());
            }
        }
        return null;
    }

    public DailyPrepItems getDailyPrepItem(int id,
                                           String symbol) throws SQLException {
        if (isDbConnected()) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            String query = "SELECT *" +
                    "FROM DailyPrep \n" +
                    "WHERE dailyPrepDateId = ? AND symbol = ?;";
            try {
                ps = bdConnection.prepareStatement(query);
                ps.setInt(1, id);
                ps.setString(2, symbol);
                rs = ps.executeQuery();
                DailyPrepItems dailyPrepItem = null;
                while (rs.next()) {
                    dailyPrepItem = new DailyPrepItems(
                            rs.getDate("date").toLocalDate(),
                            rs.getInt("dailyPrepId"),
                            rs.getInt("dailyPrepDateId"),
                            rs.getString("dailyEvents"),
                            rs.getString("symbol"),
                            rs.getString("hourlyTrend"),
                            rs.getString("halfHourlyTrend"),
                            rs.getString("dailyTrend"),
                            rs.getDouble("hh_ll_3_bars_high"),
                            rs.getDouble("hh_ll_3_bars_low"),
                            rs.getDouble("hh_ll_any_high"),
                            rs.getDouble("hh_ll_any_low"));
                }

                return dailyPrepItem;
            } catch (Exception e) {
                log.error("Failed to retrieve dailyPrepItem id:{} : {}", id, e.getMessage());
            }
        }
        return null;
    }

    public DailyPrepItems addDailyPrepItem(int id,
                                           String symbol,
                                           LocalDate date) throws SQLException {
        if (isDbConnected()) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            String query = "insert into DailyPrep(dailyPrepDateId, symbol, date) VALUES(?,?, ?)";
            try {
                ps = bdConnection.prepareStatement(query);
                ps.setInt(1, id);
                ps.setString(2, symbol);
                ps.setDate(3, Date.valueOf(date));
                ps.executeUpdate();
                ps.close();
                return getDailyPrepItem(id, symbol);
            } catch (Exception e) {
                log.error("Failed to add dailyPrepItem with id: {} : {}", id, e.getMessage());
            }
        }
        return null;
    }

    public DailyPrep addDailyPrepDate(LocalDate date) throws SQLException {
        if (isDbConnected()) {
            PreparedStatement ps = null;
            String query = "insert into DailyPrepDate(date) VALUES(?)";
            try {
                ps = bdConnection.prepareStatement(query);
                ps.setDate(1, Date.valueOf(date));
                ps.executeUpdate();
                ps.close();
                DailyPrepDate dailyPrepDate = getDailyPrepDate(date);
                log.info("Daily Prep date added successfully.");
                return new DailyPrep(dailyPrepDate.getDailyPrepDateId(), dailyPrepDate.getDate(), null, new ArrayList<>());
            } catch (Exception e) {
                log.error("Failed to add dailyPrepDate with date: {} : {}", Date.valueOf(date), e.getMessage());
            }
        }
        return null;
    }

    public DailyPrepDate getDailyPrepDate(LocalDate date) throws SQLException {
        PreparedStatement ps = null;
        ResultSet rs = null;
        String query = "SELECT * FROM DailyPrepDate WHERE date = ? LIMIT 1;";
        try {
            ps = bdConnection.prepareStatement(query);
            ps.setDate(1, Date.valueOf(date));
            rs = ps.executeQuery();
            DailyPrepDate dailyPrepDate = null;
            while (rs.next()) {
                dailyPrepDate = new DailyPrepDate(rs.getInt("dailyPrepDateId"), rs.getDate("date").toLocalDate());
            }
            ps.close();
            return dailyPrepDate;
        } catch (Exception e) {
            log.error("Failed to retrieve dailyPrepDate with date: {} : {}", Date.valueOf(date), e.getMessage());
        }
        return null;
    }

    public void deleteTransaction(Transaction transaction) throws SQLException {
        if (isDbConnected()) {
            String sql = "DELETE FROM transactions WHERE id = ?";
            PreparedStatement preparedStatement = null;
            try {
                preparedStatement = bdConnection.prepareStatement(sql);
                preparedStatement.setInt(1, transaction.getId()); // Set the ID value
                preparedStatement.executeUpdate();
                preparedStatement.close();
                log.info("Transaction id: {} deleted successfully.", transaction.getId());
            } catch (SQLException e) {
                log.error("Failed to delete transaction id: {} : {}", transaction.getId(), e.getMessage());
            }
        }
    }

    public void deleteDay(int id) throws SQLException {
        if (isDbConnected()) {
            String sql = "DELETE FROM DailyPrepDate WHERE dailyPrepDateId = ?";
            PreparedStatement preparedStatement = null;
            try {
                preparedStatement = bdConnection.prepareStatement(sql);
                preparedStatement.setInt(1, id); // Set the ID value
                preparedStatement.executeUpdate();
                preparedStatement.close();
                log.info("Day id: {} deleted successfully.", id);
            } catch (SQLException e) {
                log.error("Failed to delete day id: {} : {}", id, e.getMessage());
            }
        }
    }

    public void deleteSymbolByDay(int id) throws SQLException {
        if (isDbConnected()) {
            String sql = "DELETE FROM DailyPrep WHERE dailyPrepDateId = ?";
            PreparedStatement preparedStatement = null;
            try {
                preparedStatement = bdConnection.prepareStatement(sql);
                preparedStatement.setInt(1, id); // Set the ID value
                preparedStatement.executeUpdate();
                preparedStatement.close();
                log.info("Symbol deleted successfully by dateid: {}", id);
            } catch (SQLException e) {
                log.error("Failed to delete symbol by dateid: {} : {}", id, e.getMessage());
            }
        }
    }

    public void deleteSymbol(int id) throws SQLException {
        if (isDbConnected()) {
            String sql = "DELETE FROM DailyPrep WHERE dailyPrepId = ?";
            PreparedStatement preparedStatement = null;
            try {
                preparedStatement = bdConnection.prepareStatement(sql);
                preparedStatement.setInt(1, id); // Set the ID value
                preparedStatement.executeUpdate();
                preparedStatement.close();
                log.info("Symbol deleted successfully id: {}", id);
            } catch (SQLException e) {
                log.error("Failed to delete symbol by id: {} : {}", id, e.getMessage());
            }
        }
    }

    public List<Journal> getAllJournalEntries() throws SQLException {
        if (isDbConnected()) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            String query = "select * from Journal ORDER BY date ASC";
            try {
                ps = bdConnection.prepareStatement(query);
                rs = ps.executeQuery();
                List<Journal> journalEntries = new ArrayList<>();
                while (rs.next()) {
                    journalEntries.add(new Journal(
                            rs.getInt("id"),
                            rs.getDate("date").toLocalDate(),
                            rs.getString("symbol"),
                            rs.getString("text")
                    ));
                }
                return journalEntries;
            } catch (Exception e) {
                log.error("Failed to retrieve all journal entries : {}", e.getMessage());
            } finally {
                log.info("All journal entries retrieved.");
                ps.close();
                rs.close();
            }
        }
        System.out.println("Get All Journal Entries failed! DB is not connected");
        return null;
    }

    public void addJournalEntry(Journal journal) throws SQLException {
        if (isDbConnected()) {
            PreparedStatement ps = null;
            String query = "insert into Journal(date, symbol, text) VALUES(?,?,?)";
            try {
                ps = bdConnection.prepareStatement(query);
                ps.setDate(1, Date.valueOf(journal.getDate()));
                ps.setString(2, journal.getSymbol());
                ps.setString(3, journal.getText());
                ps.executeUpdate();
                ps.close();
                log.info("Journal entry {} symbol {} added successfully.", Date.valueOf(journal.getDate()), journal.getSymbol());
            } catch (Exception e) {
                log.error("Failed to add journal entry with date: {} : {}", journal.getDate(), e.getMessage());
            }
        }
    }

    public void deleteJournalDay(LocalDate date) throws SQLException {
        if (isDbConnected()) {
            String sql = "DELETE FROM Journal WHERE date = ?";
            PreparedStatement preparedStatement = null;
            try {
                preparedStatement = bdConnection.prepareStatement(sql);
                preparedStatement.setDate(1, Date.valueOf(date)); //
                preparedStatement.executeUpdate();
                preparedStatement.close();
                log.info("Day: {} deleted successfully.", Date.valueOf(date));
            } catch (SQLException e) {
                log.error("Failed to delete journal by date: {} : {}", Date.valueOf(date), e.getMessage());
            }
        }
    }

    public void deleteJourneyEntryBySymbol(int id) throws SQLException {
        if (isDbConnected()) {
            String sql = "DELETE FROM Journal WHERE id = ?";
            PreparedStatement preparedStatement = null;
            try {
                preparedStatement = bdConnection.prepareStatement(sql);
                preparedStatement.setInt(1, id); // Set the ID value
                preparedStatement.executeUpdate();
                preparedStatement.close();
                log.info("Journal entry with symbol id {} deleted successfully.", id);
            } catch (SQLException e) {
                log.error("Failed to delete Journal entry by symbol id:{} : {}", id, e.getMessage());
            }
        }
    }

    public void updateJournalEntrySymbol(
            Journal entry) throws SQLException {
        PreparedStatement ps = null;
        String query = "update Journal set text = ? WHERE id = ?";
        try {
            ps = bdConnection.prepareStatement(query);
            ps.setString(1, entry.getText());
            ps.setInt(2, entry.getId());
            ps.executeUpdate();
            ps.close();
            log.info("Journal entry id:{} updates successfully", entry.getId());
        } catch (Exception e) {
            log.error("Failed to update Journal entry with id: {} : {}", entry.getId(), e.getMessage());
        }
    }

    public List<Goal> getAllGoals() throws SQLException {
        if (isDbConnected()) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            String query = "select * from Goal ORDER BY date ASC";
            try {
                ps = bdConnection.prepareStatement(query);
                rs = ps.executeQuery();
                List<Goal> goals = new ArrayList<>();
                while (rs.next()) {
                    goals.add(new Goal(
                            rs.getInt("id"),
                            rs.getDate("date").toLocalDate(),
                            ETimeHorizon.valueOf(rs.getString("timeHorizon")),
                            rs.getString("text"),
                            rs.getBoolean("achieved")
                    ));
                }
                return goals;
            } catch (Exception e) {
                log.error("Failed to retrieve all goals : {}", e.getMessage());
            } finally {
                log.info("All goal entries retrieved.");
                ps.close();
                rs.close();
            }
        }
        return null;
    }

    public void addGoal(
            Goal goal) throws SQLException {
        PreparedStatement ps = null;
        String query = "insert into Goal(date, timeHorizon, text, achieved) VALUES(?,?,?,?)";
        try {
            ps = bdConnection.prepareStatement(query);
            ps.setDate(1, Date.valueOf(goal.getDate()));
            ps.setString(2, String.valueOf(goal.getTimeHorizon()));
            ps.setString(3, goal.getText());
            ps.setBoolean(4, goal.getAchieved());
            ps.executeUpdate();
            ps.close();
            log.info("Goal added successfully");
        } catch (Exception e) {
            log.error("Failed to add goal with date: {} : {}", goal.getDate(), e.getMessage());
        }
    }

    public void deleteGoal(int id) throws SQLException {
        if (isDbConnected()) {
            String sql = "DELETE FROM Goal WHERE id = ?";
            PreparedStatement preparedStatement = null;
            try {
                preparedStatement = bdConnection.prepareStatement(sql);
                preparedStatement.setInt(1, id); // Set the ID value
                preparedStatement.executeUpdate();
                preparedStatement.close();
                log.info("Goal deleted successfully");
            } catch (SQLException e) {
                log.error("Failed to delete goal witg id:{} : {}", id, e.getMessage());
            }
        }
    }

    public void updateGoal(
            Goal goal) throws SQLException {
        PreparedStatement ps = null;
        String query = "update Goal set text = ?, achieved = ? WHERE id = ?";
        try {
            ps = bdConnection.prepareStatement(query);
            ps.setString(1, goal.getText());
            ps.setInt(2, goal.getId());
            ps.setBoolean(3, goal.getAchieved());
            ps.executeUpdate();
            ps.close();
            log.info("Goal updated successfully");
        } catch (Exception e) {
            log.error("Failed to update goal id:{} : {}", goal.getId(), e.getMessage());
        }
    }

    public void dbStartUpChecks(DbManager db) throws SQLException {
        boolean transactionTable = doesTableExist("transactions", db);
        boolean isForeignKeyEnabled = foreignKeyEnabled(db);
        boolean dailyPrepDate = doesTableExist("DailyPrepDate", db);
        boolean dailyPrep = doesTableExist("DailyPrep", db);
        boolean journal = doesTableExist("Journal", db);
        boolean goal = doesTableExist("Goal", db);

        if (!transactionTable) {
            createTransactionTable(db);
        }
        if (!isForeignKeyEnabled) {
            enableForeignKeys(db);
        }
        if (!dailyPrepDate) {
            createDailyPrepDateTable(db);
        }
        if (!dailyPrep) {
            createDailyPrepTable(db);
        }
        if (!journal) {
            createJournalTable(db);
        }
        if (!goal) {
            createGoalTable(db);
        }
    }
}
