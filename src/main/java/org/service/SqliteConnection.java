package org.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;

public class SqliteConnection {
    private static final Logger log = LoggerFactory.getLogger(SqliteConnection.class);
    Path mainDB = Path.of(System.getenv("LOCALAPPDATA"), "TradeStatsMaster", "master.db");
    final String mainDBName = "/org/app/data/master.sqlite";
    private Connection connection;

    public synchronized Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                initDatabaseFile();
                connection = createConnection();
            }
            return connection;
        } catch (Exception e) {
            log.error("Failed to get DB connection", e);
            return null;
        }
    }

    private void initDatabaseFile() throws IOException {
        if (Files.isRegularFile(mainDB)) return;

        //            Files.createDirectories(mainDB.getParent()); // if using subdir for app

        try (var in = SqliteConnection.class.getResourceAsStream(mainDBName)) {
            if (in == null) {
                log.warn("Embedded DB not found, creating empty DB");
                return;
            }
            Files.copy(in, mainDB);
        }
    }

    private Connection createConnection() throws Exception {
        Class.forName("org.sqlite.JDBC");
        log.info("Opening SQLite DB at {}", mainDB);
        return DriverManager.getConnection("jdbc:sqlite:" + mainDB.toAbsolutePath());
    }

    public boolean isOpen() {
        try {
            return connection != null && !connection.isClosed();
        } catch (Exception e) {
            return false;
        }
    }

    public void close() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                log.info("SQLite connection closed");
            }
        } catch (Exception e) {
            log.error("Failed to close DB connection", e);
        }
    }
}
