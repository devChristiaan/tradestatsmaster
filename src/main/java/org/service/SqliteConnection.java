package org.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Objects;

public class SqliteConnection {
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
                System.out.println("Failed to copy temp db to main folder");
                e.printStackTrace();
                throw e;
            }
        }

        return createConnection();
    }

    private static Connection createConnection() {
        try {
            Class.forName("org.sqlite.JDBC");
            Connection connection = DriverManager.getConnection("jdbc:sqlite:" + mainDB.toAbsolutePath());
            System.out.println("DB Connection is successful");
            return connection;
        } catch (Exception e) {
            System.out.println("DB Connection is failed");
            e.printStackTrace();
            return null;
        }
    }
}
