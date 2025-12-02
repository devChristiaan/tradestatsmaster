package org.utilities;

import java.nio.file.Files;
import java.nio.file.Path;

public class InitLogging {
    public static String init() {
        String localAppData = System.getenv("LOCALAPPDATA") + "\\TradeStatsMaster\\logs";
        Path path = Path.of(localAppData);

        try {
            Files.createDirectories(path);
        } catch (Exception e) {
            System.err.println("Could not create appdata log directory: " + e.getMessage());
        }

        return localAppData;
    }
}
