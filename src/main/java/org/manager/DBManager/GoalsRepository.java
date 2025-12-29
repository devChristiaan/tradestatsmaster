package org.manager.DBManager;

import org.model.goal.ETimeHorizon;
import org.model.goal.Goal;
import org.service.SqliteConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.Date;
import java.util.List;

import static org.manager.DBManager.RepositoryHelper.queryList;
import static org.manager.DBManager.RepositoryHelper.update;
import static org.service.DataObjectService.deserializeObject;
import static org.service.DataObjectService.serializeObject;

public class GoalsRepository {
    private final Logger log = LoggerFactory.getLogger(GoalsRepository.class);
    private final Connection conn;

    public GoalsRepository(SqliteConnection db) {
        this.conn = db.getConnection();
    }

    public List<Goal> getAllGoals() {
        String sql = "select * from Goal ORDER BY date ASC";

        return queryList(conn, sql, null, rs -> new Goal(
                rs.getInt("id"),
                rs.getDate("date").toLocalDate(),
                ETimeHorizon.valueOf(rs.getString("timeHorizon")),
                deserializeObject(rs.getBytes("document")),
                rs.getBoolean("achieved")
        ));
    }

    public void addGoal(
            Goal goal) {
        String sql = "insert into Goal(date, timeHorizon, document, achieved) VALUES(?,?,?,?)";

        update(conn, sql, ps -> {
            ps.setDate(1, Date.valueOf(goal.getDate()));
            ps.setString(2, String.valueOf(goal.getTimeHorizon()));
            ps.setBytes(3, serializeObject(goal.getDocument()));
            ps.setBoolean(4, goal.getAchieved());
        });
        log.info("Goal added successfully");
    }

    public void deleteGoal(int id) {
        update(conn, "DELETE FROM Goal WHERE id = ?", ps -> ps.setInt(1, id));
        log.info("Goal id: {} deleted successfully", id);
    }

    public void updateGoal(
            Goal goal) {
        String sql = "update Goal set document = ?, achieved = ? WHERE id = ?";
        update(conn, sql, ps -> {
            ps.setBytes(1, serializeObject(goal.getDocument()));
            ps.setBoolean(2, goal.getAchieved());
            ps.setInt(3, goal.getId());
        });
        log.info("Goal id: {} updated successfully", goal.getId());
    }
}
