package org.logging;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Set;
import java.util.logging.*;

public class DateRollingFileHandler extends Handler {
    private static final Set<String> ALLOWED_PACKAGES = Set.of(
            "org.app",
            "org.context",
            "org.controller",
            "org.logging",
            "org.manager",
            "org.model",
            "org.service"
    );

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private final String baseDir;
    private final String baseFileName;
    private FileHandler currentHandler;
    private LocalDate currentDate;

    public DateRollingFileHandler(String baseDir,
                                  String baseFileName) throws IOException {
        this.baseDir = baseDir;
        this.baseFileName = baseFileName;
        this.currentDate = LocalDate.now();


        Files.createDirectories(Path.of(baseDir));
        openNewHandler();
    }

    private void openNewHandler() throws IOException {
        if (currentHandler != null) {
            currentHandler.close();
        }

        String datedFileName = baseFileName + "-" + currentDate.format(DATE_FORMAT) + ".log";
        Path fullPath = Path.of(baseDir, datedFileName);

        currentHandler = new FileHandler(fullPath.toString(), true); // append = true
        currentHandler.setFormatter(new java.util.logging.SimpleFormatter());
        currentHandler.setLevel(Level.ALL);
    }

    /**
     * Call this method periodically to roll over the log if the date has changed.
     */
    public void rolloverIfNeeded() {
        LocalDate today = LocalDate.now();
        if (!today.equals(currentDate)) {
            currentDate = today;
            try {
                openNewHandler();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void publish(LogRecord record) {
        if (currentHandler == null) return;
        rolloverIfNeeded();

        if (isLoggable(record)) {
            currentHandler.publish(record);
        }
    }

    @Override
    public void flush() {
        if (currentHandler != null) {
            currentHandler.flush();
        }
    }

    @Override
    public void close() throws SecurityException {
        if (currentHandler != null) {
            currentHandler.close();
        }
    }

    public void attachToRootLogger() {
        Logger root = Logger.getLogger("");
        root.addHandler(this);
        root.setLevel(Level.ALL);
    }

    public void filterAppLogs() {
        setFilter(record -> {
            String name = record.getLoggerName();
            if (name == null) return false;

            return ALLOWED_PACKAGES.stream().anyMatch(name::startsWith);
        });
    }
}