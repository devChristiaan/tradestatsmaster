package org.manager;

import org.model.goal.ETimeHorizon;
import org.model.goal.Goal;
import org.model.journal.Journal;
import org.service.SqliteConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.service.DataObjectService.deserializeObject;
import static org.service.DataObjectService.serializeObject;

public class DbManager {
    private static final Logger log = LoggerFactory.getLogger(DbManager.class);
    Connection bdConnection;

    public void setBdConnection() throws IOException {
        bdConnection = SqliteConnection.getConnection();
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
                            deserializeObject(rs.getBytes("document"))
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
        return null;
    }

    public void addJournalEntry(Journal journal) throws SQLException {
        if (isDbConnected()) {
            PreparedStatement ps = null;
            String query = "insert into Journal(date, symbol, document) VALUES(?,?,?)";
            try {
                ps = bdConnection.prepareStatement(query);
                ps.setDate(1, Date.valueOf(journal.getDate()));
                ps.setString(2, journal.getSymbol());
                ps.setBytes(3, serializeObject(journal.getDocument()));
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
        String query = "update Journal set document = ? WHERE id = ?";
        try {
            ps = bdConnection.prepareStatement(query);
            ps.setBytes(1, serializeObject(entry.getDocument()));
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
                            deserializeObject(rs.getBytes("document")),
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
        String query = "insert into Goal(date, timeHorizon, document, achieved) VALUES(?,?,?,?)";
        try {
            ps = bdConnection.prepareStatement(query);
            ps.setDate(1, Date.valueOf(goal.getDate()));
            ps.setString(2, String.valueOf(goal.getTimeHorizon()));
            ps.setBytes(3, serializeObject(goal.getDocument()));
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
                log.info("Goal id: {} deleted successfully", id);
            } catch (SQLException e) {
                log.error("Failed to delete goal witg id:{} : {}", id, e.getMessage());
            }
        }
    }

    public void updateGoal(
            Goal goal) throws SQLException {
        PreparedStatement ps = null;
        String query = "update Goal set document = ?, achieved = ? WHERE id = ?";
        try {
            ps = bdConnection.prepareStatement(query);
            ps.setBytes(1, serializeObject(goal.getDocument()));
            ps.setBoolean(2, goal.getAchieved());
            ps.setInt(3, goal.getId());
            ps.executeUpdate();
            ps.close();
            log.info("Goal id: {} updated successfully", goal.getId());
        } catch (Exception e) {
            log.error("Failed to update goal id:{} : {}", goal.getId(), e.getMessage());
        }
    }

}
