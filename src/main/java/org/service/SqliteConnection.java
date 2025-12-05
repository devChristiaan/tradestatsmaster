package org.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Objects;

public class SqliteConnection {
    private static final Logger log = LoggerFactory.getLogger(SqliteConnection.class);
    static Path mainDB = Path.of(System.getenv("LOCALAPPDATA"), "TradeStatsMaster", "master.db");
    static final String mainDBName = "/org/app/data/master.sqlite";

    public static Connection getConnection() throws IOException {

        if (!Files.isRegularFile(mainDB)) {
//            Files.createDirectories(mainDB.getParent()); // if using subdir for app
            try (var in = SqliteConnection.class.getResourceAsStream(mainDBName)) {
                if (in == null) {
                    return createConnection();
                }
                Objects.requireNonNull(in, () -> "Not found resource: " + mainDBName);
                Files.copy(in, mainDB);
            } catch (IOException e) {
                log.error("Failed to copy temp db to main folder", e);
                throw e;
            }
        }

        return createConnection();
    }

    private static Connection createConnection() {
        try {
            Class.forName("org.sqlite.JDBC");
            Connection connection = DriverManager.getConnection("jdbc:sqlite:" + mainDB.toAbsolutePath());
            log.info("DB Connection is successful");
            return connection;
        } catch (Exception e) {
            log.error("DB Connection is failed", e);
            return null;
        }
    }
}
