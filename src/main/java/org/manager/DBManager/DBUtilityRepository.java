package org.manager.DBManager;

import org.service.SqliteConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;

import static org.manager.DBManager.RepositoryHelper.create;

public class DBUtilityRepository {
    private final Logger log = LoggerFactory.getLogger(DBUtilityRepository.class);
    private final Connection conn;

    public DBUtilityRepository(SqliteConnection db) {
        this.conn = db.getConnection();
    }

    public void createBack(String name) {
        create(conn, "VACUUM INTO '" + name + "'");
    }
}
