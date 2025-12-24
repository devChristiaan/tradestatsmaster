package org.context;

import javafx.application.Platform;
import org.service.ZipFiles;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class AppLauncher {
    private static final Logger log = LoggerFactory.getLogger(AppLauncher.class);

    private static List<String> launchCommand;

    public static void init(String[] args) {
        launchCommand = new ArrayList<>();

        // Native image or packaged app
        String executable = ProcessHandle.current()
                .info()
                .command()
                .orElse(null);

        if (executable != null) {
            launchCommand.add(executable);
            launchCommand.addAll(Arrays.asList(args));
        }
    }

    public static void restart() {
        try {
            new ProcessBuilder(launchCommand).start();
        } catch (IOException e) {
            log.error("App failed to relaunch {}", e.getMessage());
        }

        Platform.exit();
        System.exit(0);
    }
}
