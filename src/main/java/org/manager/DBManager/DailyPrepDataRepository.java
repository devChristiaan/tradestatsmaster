package org.manager.DBManager;

import org.model.dailyPrep.DailyPrep;
import org.model.dailyPrep.DailyPrepDate;
import org.model.dailyPrep.DailyPrepItems;
import org.model.transaction.Transaction;
import org.service.SqliteConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.Date;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.manager.DBManager.RepositoryHelper.*;

public class DailyPrepDataRepository {
    private final Logger log = LoggerFactory.getLogger(DailyPrepDataRepository.class);
    private final Connection conn;

    public DailyPrepDataRepository(SqliteConnection db) {
        this.conn = db.getConnection();
    }

    public List<DailyPrepItems> getDailyPrepItems(int id) {
        String sql = "SELECT *" +
                "FROM DailyPrep \n" +
                "WHERE dailyPrepDateId = ?;";

        return queryList(conn, sql, ps -> ps.setInt(1, id), rs -> new DailyPrepItems(
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

    public List<DailyPrep> getAllDailyPrepData() {
        String sql = "SELECT * FROM DailyPrepDate ORDER BY date ASC";

        List<DailyPrep> dailyPreps = new ArrayList<>();
        List<DailyPrepDate> dailyPrepDates = queryList(conn, sql, null, rs -> new DailyPrepDate(
                rs.getInt("dailyPrepDateId"),
                rs.getDate("date").toLocalDate()
        ));
        for (DailyPrepDate dailyPrepDate : dailyPrepDates) {
            List<DailyPrepItems> dailyPrepItems = getDailyPrepItems(dailyPrepDate.getDailyPrepDateId());
            dailyPreps.add(new DailyPrep(dailyPrepDate.getDailyPrepDateId(), dailyPrepDate.getDate(), null, dailyPrepItems));
        }
        log.info("All DailyPredData entries retrieved.");
        return dailyPreps;
    }

    public DailyPrepItems getDailyPrepItem(int id,
                                           String symbol) {
        String sql = "SELECT *" +
                "FROM DailyPrep \n" +
                "WHERE dailyPrepDateId = ? AND symbol = ?;";

        return queryItem(conn, sql, ps -> {
            ps.setInt(1, id);
            ps.setString(2, symbol);
        }, rs -> new DailyPrepItems(
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

    public void addDailyPrepItem(DailyPrepItems dailyPrepItem) {
        String sql = "update DailyPrep set dailyEvents = ?, hourlyTrend = ?, halfHourlyTrend = ?, dailyTrend = ?, hh_ll_3_bars_high = ?, hh_ll_3_bars_low = ?, hh_ll_any_high = ?, hh_ll_any_low = ? WHERE dailyPrepId = ?";

        update(conn, sql, ps -> {
            ps.setString(1, dailyPrepItem.getDailyEvents());
            ps.setString(2, dailyPrepItem.getHourlyTrend());
            ps.setString(3, dailyPrepItem.getHalfHourlyTrend());
            ps.setString(4, dailyPrepItem.getDailyTrend());
            ps.setDouble(5, dailyPrepItem.getHh_ll_3_bars_high());
            ps.setDouble(6, dailyPrepItem.getHh_ll_3_bars_low());
            ps.setDouble(7, dailyPrepItem.getHh_ll_any_high());
            ps.setDouble(8, dailyPrepItem.getHh_ll_any_low());
            ps.setInt(9, dailyPrepItem.getDailyPrepId());
        });
        log.info("DailyPrepItem id:{} updated successfully", dailyPrepItem.getDailyPrepId());
    }

    public DailyPrepItems addDailyPrepItem(int id,
                                         String symbol,
                                         LocalDate date) {
        String sql = "insert into DailyPrep(dailyPrepDateId, symbol, date) VALUES(?,?, ?)";

        update(conn, sql, ps -> {
            ps.setInt(1, id);
            ps.setString(2, symbol);
            ps.setDate(3, Date.valueOf(date));
        });
        return getDailyPrepItem(id, symbol);
    }

    public DailyPrep addDailyPrepDate(LocalDate date) {
        String sql = "insert into DailyPrepDate(date) VALUES(?)";

        update(conn, sql, ps -> ps.setDate(1, Date.valueOf(date)));
        log.info("Daily Prep date added successfully.");
        DailyPrepDate dailyPrepDate = getDailyPrepDate(date);
        return new DailyPrep(dailyPrepDate.getDailyPrepDateId(), dailyPrepDate.getDate(), null, new ArrayList<>());
    }

    public DailyPrepDate getDailyPrepDate(LocalDate date) {
        String sql = "SELECT * FROM DailyPrepDate WHERE date = ? LIMIT 1;";

        return queryItem(conn, sql, ps -> ps.setDate(1, Date.valueOf(date)), rs -> new DailyPrepDate(rs.getInt("dailyPrepDateId"), rs.getDate("date").toLocalDate()));
    }

    public void deleteDay(int id) {
        update(conn, "DELETE FROM DailyPrepDate WHERE dailyPrepDateId = ?", ps -> ps.setInt(1, id));
        log.info("Day id: {} deleted successfully.", id);
    }

    public void deleteSymbolByDay(int id) {
        update(conn, "DELETE FROM DailyPrep WHERE dailyPrepDateId = ?", ps -> ps.setInt(1, id));
        log.info("Symbol deleted successfully by dateId: {}", id);
    }

    public void deleteSymbol(int id) {
        update(conn, "DELETE FROM DailyPrep WHERE dailyPrepId = ?", ps -> ps.setInt(1, id));
        log.info("Symbol deleted successfully id: {}", id);
    }
}
