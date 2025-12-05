package org.logging;

import javafx.application.Platform;
import javafx.scene.control.TextArea;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class LogStreamer {
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final String LOG_FILE_BASE = "app";
    TextArea logDisplay;
    private Path currentLogFile;
    private long lastReadPosition = 0;
    private ScheduledExecutorService executorService;

    public LogStreamer(TextArea logDisplay) {
        this.logDisplay = logDisplay;
    }


    void startLogMonitoring() {
        executorService = Executors.newSingleThreadScheduledExecutor();
        executorService.scheduleAtFixedRate(this::readNewLogEntries, 0, 1, TimeUnit.SECONDS); // Check every second
    }

    private Path getTodayLogFile() {
        String logDirStr = System.getenv("LOCALAPPDATA") + "/TradeStatsMaster/logs";
        Path logDir = Path.of(logDirStr);
        String fileName = LOG_FILE_BASE + "-" + LocalDate.now().format(DATE_FORMAT) + ".log";
        return logDir.resolve(fileName);
    }


    private void readNewLogEntries() {
        Path todayLog = getTodayLogFile();

        if (!todayLog.equals(currentLogFile)) {
            currentLogFile = todayLog;
            lastReadPosition = 0;
        }

        if (!Files.exists(currentLogFile)) {
            Platform.runLater(() ->
                    logDisplay.appendText("Log file not found at: " + currentLogFile + System.lineSeparator())
            );
            return;
        }
        try (RandomAccessFile file = new RandomAccessFile(currentLogFile.toFile(), "r")) {
            file.seek(lastReadPosition);
            String line;
            while ((line = file.readLine()) != null) {
                String finalLine = line;
                Platform.runLater(() -> logDisplay.appendText(finalLine + System.lineSeparator()));
            }
            lastReadPosition = file.getFilePointer();
        } catch (IOException e) {
            Platform.runLater(() -> logDisplay.appendText("Error reading log: " + e.getMessage() + System.lineSeparator()));
        }
    }

    public void stop() {
        if (executorService != null) {
            executorService.shutdownNow();
        }
    }
}
