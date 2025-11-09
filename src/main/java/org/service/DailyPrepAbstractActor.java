package org.service;

import org.model.dailyPrep.DailyPrep;
import org.model.dailyPrep.DailyPrepDate;
import org.model.dailyPrep.DailyPrepItems;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class DailyPrepAbstractActor {
//    public List<DailyPrep> getAllDailyPrepData() throws SQLException {
//        if (isDbConnected()) {
//            PreparedStatement ps = null;
//            ResultSet rs = null;
//            String query = "select * from DailyPrepDate ORDER BY date ASC";
//            try {
//                ps = bdConnection.prepareStatement(query);
//                rs = ps.executeQuery();
//                List<DailyPrep> dailyPreps = new ArrayList<>();
//                List<DailyPrepDate> dailyPrepDates = new ArrayList<>();
//                while (rs.next()) {
//                    dailyPrepDates.add(new DailyPrepDate(
//                            rs.getInt("dailyPrepDateId"),
//                            rs.getDate("date").toLocalDate()
//                    ));
//                }
//
//                for (DailyPrepDate dailyPrepDate : dailyPrepDates) {
//                    List<DailyPrepItems> dailyPrepItems = getDailyPrepItem(dailyPrepDate.getDailyPrepDateId());
//                    dailyPreps.add(new DailyPrep(dailyPrepDate.getDailyPrepDateId(), dailyPrepDate.getDate(), dailyPrepItems));
//                }
//                return dailyPreps;
//            } catch (Exception e) {
//                throw new RuntimeException(e);
//            } finally {
//                ps.close();
//                rs.close();
//            }
//        }
//        System.out.println("Get All Transactions failed! DB is not connected");
//        return null;
//    }
//
//    public List<DailyPrepItems> getDailyPrepItem(int id) throws SQLException {
//        if (isDbConnected()) {
//            PreparedStatement ps = null;
//            ResultSet rs = null;
//            String query = "SELECT *" +
//                    "FROM DailyPrep \n" +
//                    "WHERE foreign_key_column = ?;";
//            try {
//                ps = bdConnection.prepareStatement(query);
//                ps.setInt(1, id);
//                rs = ps.executeQuery();
//                List<DailyPrepItems> dailyPrepItems = new ArrayList<>();
//                while (rs.next()) {
//                    dailyPrepItems.add(new DailyPrepItems(
//                            rs.getInt("dailyPrepId"),
//                            rs.getInt("dailyPrepDateId"),
//                            rs.getString("dailyEvents"),
//                            rs.getString("symbol"),
//                            rs.getString("hourlyTrend"),
//                            rs.getString("halfHourlyTrend"),
//                            rs.getString("dailyTrend"),
//                            rs.getDouble("hh_ll_3_bars_high"),
//                            rs.getDouble("hh_ll_3_bars_low"),
//                            rs.getDouble("hh_ll_any_high"),
//                            rs.getDouble("hh_ll_any_low")));
//                }
//                return dailyPrepItems;
//            } catch (Exception e) {
//                throw new RuntimeException(e);
//            } finally {
//                ps.close();
//                rs.close();
//            }
//        }
//        return null;
//    }
//
//    public DailyPrep addDailyPrep(LocalDate date) throws SQLException {
//        PreparedStatement ps = null;
//        String query = "insert into DailyPrepDate(date) VALUES(?)";
//        try {
//            ps = bdConnection.prepareStatement(query);
//            ps.setDate(1, Date.valueOf(date));
//            ps.executeUpdate();
//            ps.close();
//        } catch (Exception e) {
//            throw new RuntimeException(e);
//        }
//    }
}
