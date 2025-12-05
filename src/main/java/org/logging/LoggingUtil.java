package org.logging;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

public class LoggingUtil {

    private static DateRollingFileHandler dateRollingHandler;

    public static void setupLogging() {
        try {
            String logDir = System.getenv("LOCALAPPDATA") + "/TradeStatsMaster/logs";
            Files.createDirectories(Path.of(logDir));

            dateRollingHandler = new DateRollingFileHandler(logDir, "app");
            dateRollingHandler.setLevel(Level.ALL);
            dateRollingHandler.filterAppLogs();

            Logger root = Logger.getLogger("");
            root.addHandler(dateRollingHandler);
            root.setLevel(Level.ALL);

            ConsoleHandler consoleHandler = new ConsoleHandler();
            consoleHandler.setLevel(Level.ALL);
            consoleHandler.setFilter(record -> record.getLoggerName().startsWith("org.app"));
            root.addHandler(consoleHandler);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /** Call periodically (e.g., every minute) to roll logs at midnight */
    public static void rolloverIfNeeded() {
        if (dateRollingHandler != null) {
            dateRollingHandler.rolloverIfNeeded();
        }
    }

}