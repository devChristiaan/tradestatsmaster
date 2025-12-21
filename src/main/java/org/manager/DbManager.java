package org.manager;

import org.model.goal.ETimeHorizon;
import org.model.goal.Goal;
import org.service.SqliteConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.sql.*;
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
