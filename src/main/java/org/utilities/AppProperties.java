package org.utilities;

import java.io.IOException;
import java.util.Properties;

public class AppProperties {
    static String VERSION = null;
    static String NAME = null;

    static {
        try {
            Properties props = new Properties();
            props.load(AppProperties.class.getResourceAsStream("/application.properties"));
            VERSION = props.getProperty("app.version");
            NAME = props.getProperty("app.name");
        } catch (IOException | NullPointerException e) {
            // ignore → keeps "DEV"
        }
    }

    public static String getVersion() {
        return VERSION;
    }

    public static String getName() {
        return NAME;
    }

}
