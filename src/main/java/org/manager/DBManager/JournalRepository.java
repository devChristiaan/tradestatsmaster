package org.manager.DBManager;

import org.model.journal.Journal;
import org.model.transaction.Transaction;
import org.service.SqliteConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.Date;
import java.time.LocalDate;
import java.util.List;

import static org.manager.DBManager.RepositoryHelper.queryList;
import static org.manager.DBManager.RepositoryHelper.update;
import static org.service.DataObjectService.deserializeObject;
import static org.service.DataObjectService.serializeObject;

public class JournalRepository {
    private final Logger log = LoggerFactory.getLogger(JournalRepository.class);
    private final Connection conn;

    public JournalRepository(SqliteConnection db) {
        this.conn = db.getConnection();
    }

    public List<Journal> getAllJournalEntries() {
        String sql = "select * from Journal ORDER BY date ASC";

        return queryList(conn, sql, null, rs -> new Journal(
                rs.getInt("id"),
                rs.getDate("date").toLocalDate(),
                rs.getString("symbol"),
                deserializeObject(rs.getBytes("document"))
        ));
    }

    public void addJournalEntry(Journal journal) {
        String sql = "insert into Journal(date, symbol, document) VALUES(?,?,?)";

        update(conn, sql, ps -> {
            ps.setDate(1, Date.valueOf(journal.getDate()));
            ps.setString(2, journal.getSymbol());
            ps.setBytes(3, serializeObject(journal.getDocument()));
        });
        log.info("Journal entry {} symbol {} added successfully.", Date.valueOf(journal.getDate()), journal.getSymbol());
    }

    public void deleteJournalDay(LocalDate date) {
        update(conn, "DELETE FROM Journal WHERE date = ?", ps -> ps.setDate(1, Date.valueOf(date)));
        log.info("Day: {} deleted successfully.", Date.valueOf(date));
    }

    public void deleteJourneyEntryBySymbol(int id) {
        update(conn, "DELETE FROM Journal WHERE id = ?", ps -> ps.setInt(1, id));
        log.info("Journal entry with symbol id {} deleted successfully.", id);
    }

    public void updateJournalEntrySymbol(
            Journal entry) {
        String sql = "update Journal set document = ? WHERE id = ?";
        update(conn, sql, ps -> {
            ps.setBytes(1, serializeObject(entry.getDocument()));
            ps.setInt(2, entry.getId());
        });
        log.info("Journal entry id:{} updates successfully", entry.getId());
    }
}
