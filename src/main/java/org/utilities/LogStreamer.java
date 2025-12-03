package org.utilities;

import javafx.application.Platform;
import javafx.scene.control.TextArea;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class LogStreamer {
    TextArea logDisplay;

    public LogStreamer(TextArea logDisplay) {
        this.logDisplay = logDisplay;
    }

    private Path logFilePath = Path.of(System.getenv("LOCALAPPDATA"), "TradeStatsMaster", "logs", "app.log");
    private long lastReadPosition = 0;
    private ScheduledExecutorService executorService;

    void startLogMonitoring() {
        executorService = Executors.newSingleThreadScheduledExecutor();
        executorService.scheduleAtFixedRate(this::readNewLogEntries, 0, 1, TimeUnit.SECONDS); // Check every second
    }

    private void readNewLogEntries() {
        if (!Files.exists(logFilePath)) {
            Platform.runLater(() ->
                    logDisplay.appendText("Log file not found at: " + logFilePath + System.lineSeparator())
            );
            return;
        }
        try (RandomAccessFile file = new RandomAccessFile(logFilePath.toFile(), "r")) {
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
