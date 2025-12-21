package org.manager.DBManager;

import org.model.transaction.Transaction;
import org.service.SqliteConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.Date;
import java.util.List;

import static org.manager.DBManager.RepositoryHelper.*;

public class TransactionRepository {
    private final Logger log = LoggerFactory.getLogger(TransactionRepository.class);
    private final Connection conn;

    public TransactionRepository(SqliteConnection db) {
        this.conn = db.getConnection();
    }

    public List<Transaction> getAllTransactions() {
        String sql = "SELECT * FROM Transactions ORDER BY date ASC";

        return queryList(conn, sql, null, rs -> new Transaction(
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

    public Transaction getLatestTransaction() {
        String sql = "SELECT * FROM Transactions ORDER BY id DESC LIMIT 1;";

        return queryItem(conn, sql, null, rs -> new Transaction(
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

    public void addTransaction(Transaction t) {
        String sql = """
                INSERT INTO Transactions
                (date, symbol, quantity, commission, direction, open, close, profit,
                 formation, ATR, ATRRisk, possibleProfitTicks, possibleLossTicks,
                 actualLossTicks, timePeriod)
                VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)
                """;

        update(conn, sql, ps -> {
            ps.setDate(1, Date.valueOf(t.getDate()));
            ps.setString(2, t.getSymbol());
            ps.setInt(3, t.getQuantity());
            ps.setDouble(4, t.getCommission());
            ps.setString(5, t.getDirection());
            ps.setDouble(6, t.getOpen());
            ps.setDouble(7, t.getClose());
            ps.setDouble(8, t.getProfit());
            ps.setString(9, t.getFormation());
            ps.setDouble(10, t.getATR());
            ps.setDouble(11, t.getATRRisk());
            ps.setDouble(12, t.getPossibleProfitTicks());
            ps.setDouble(13, t.getPossibleLossTicks());
            ps.setDouble(14, t.getActualLossTicks());
            ps.setString(15, t.getTimePeriod());
        });
        log.info("Transaction added successfully");
    }

    public void deleteTransaction(int id) {
        update(conn, "DELETE FROM Transactions WHERE id=?", ps -> ps.setInt(1, id));
        log.info("Transaction id: {} deleted successfully.", id);
    }

    public void updateTransaction(Transaction t) {
        String sql = "UPDATE Transactions " +
                "set date = ?, " +
                "symbol = ?, " +
                "quantity = ?, " +
                "commission = ?, " +
                "direction = ?, " +
                "open = ?, " +
                "close = ?, " +
                "profit = ?, " +
                "formation = ?, " +
                "ATR = ?, " +
                "ATRRisk = ?, " +
                "possibleProfitTicks = ?, " +
                "possibleLossTicks = ?, " +
                "actualLossTicks = ?, " +
                "timePeriod = ? WHERE id = ?";
        update(conn, sql, ps -> {
            ps.setDate(1, Date.valueOf(t.getDate()));
            ps.setString(2, t.getSymbol());
            ps.setInt(3, t.getQuantity());
            ps.setDouble(4, t.getCommission());
            ps.setString(5, t.getDirection());
            ps.setDouble(6, t.getOpen());
            ps.setDouble(7, t.getClose());
            ps.setDouble(8, t.getProfit());
            ps.setString(9, t.getFormation());
            ps.setDouble(10, t.getATR());
            ps.setDouble(11, t.getATRRisk());
            ps.setDouble(12, t.getPossibleProfitTicks());
            ps.setDouble(13, t.getPossibleLossTicks());
            ps.setDouble(14, t.getActualLossTicks());
            ps.setString(15, t.getTimePeriod());
            ps.setInt(16, t.getId());
        });
        log.info("Transaction id:{} updated successfully", t.getId());
    }
}
