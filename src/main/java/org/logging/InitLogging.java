package org.logging;

import org.app.App;

import java.util.logging.*;

public class InitLogging {
    public static void configureLogging() {
        try (var in = App.class.getResourceAsStream("/logging.properties")) {
            if (in != null) {
                LogManager.getLogManager().readConfiguration(in);
            } else {
                System.err.println("logging.properties not found!");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        Logger root = Logger.getLogger("");
        for (Handler h : root.getHandlers()) {
            if (h instanceof FileHandler) {
                root.removeHandler(h);
                h.close();
            }
        }
        // Add DateRollingFileHandler
        LoggingUtil.setupLogging();

        Logger log = Logger.getLogger(App.class.getName());
        log.setLevel(Level.ALL);
        log.setUseParentHandlers(true);
    }
}
