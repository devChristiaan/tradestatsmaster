package org.manager;

import org.model.dailyPrep.DailyPrep;
import org.model.dailyPrep.DailyPrepDate;
import org.model.dailyPrep.DailyPrepItems;
import org.model.transaction.Transaction;
import org.service.SqliteConnection;

import java.io.IOException;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class DbManager {
    Connection bdConnection;

    public void setBdConnection() throws IOException {
        try {
            bdConnection = SqliteConnection.getConnection();
        } catch (IOException e) {
            System.out.println("Failed to connect to DB");
            e.printStackTrace();
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
            System.out.println("DB error");
            e.printStackTrace();
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
                System.err.println("Error checking" + tableName + " existence: " + e.getMessage());
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
            } catch (SQLException e) {
                System.err.println("Error checking foreign_keys existence: " + e.getMessage());
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
            } catch (SQLException e) {
                e.printStackTrace();
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
            } catch (SQLException e) {
                e.printStackTrace();
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
            } catch (SQLException e) {
                e.printStackTrace();
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
            } catch (SQLException e) {
                e.printStackTrace();
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
                throw new RuntimeException(e);
            } finally {
                ps.close();
                rs.close();
            }
        }
        System.out.println("Get All Transactions failed! DB is not connected");
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
        } catch (Exception e) {
            throw new RuntimeException(e);
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
        } catch (Exception e) {
            throw new RuntimeException(e);
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
        } catch (Exception e) {
            throw new RuntimeException(e);
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
                throw new RuntimeException(e);
            } finally {
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
                throw new RuntimeException(e);
            } finally {
                ps.close();
                rs.close();
            }
        }
        System.out.println("Get All Transactions failed! DB is not connected");
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
                throw new RuntimeException(e);
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
                throw new RuntimeException(e);
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
                throw new RuntimeException(e);
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
                return new DailyPrep(dailyPrepDate.getDailyPrepDateId(), dailyPrepDate.getDate(), null, new ArrayList<>());
            } catch (Exception e) {
                throw new RuntimeException(e);
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
            throw new RuntimeException(e);
        }
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
            } catch (SQLException e) {
                e.printStackTrace();
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
            } catch (SQLException e) {
                e.printStackTrace();
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
            } catch (SQLException e) {
                e.printStackTrace();
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
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public void dbStartUpChecks(DbManager db) throws SQLException {
        boolean transactionTable = doesTableExist("transactions", db);
        boolean isForeignKeyEnabled = foreignKeyEnabled(db);
        boolean dailyPrepDate = doesTableExist("DailyPrepDate", db);
        boolean dailyPrep = doesTableExist("DailyPrep", db);

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
    }
}
