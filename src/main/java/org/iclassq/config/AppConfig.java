package org.iclassq.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Logger;

public class AppConfig {
    private static final Logger logger = Logger.getLogger(AppConfig.class.getName());
    private static final Properties properties = new Properties();

    static {
        try (InputStream input = AppConfig.class.getResourceAsStream("/application.properties")) {
            if (input != null) {
                properties.load(input);
            }
        } catch (IOException e) {
            logger.severe("Error cargando application properties: " + e.getMessage());
        }
    }

    public static String getBackendUrl() {
        return properties.getProperty("app.backend.url", "http://localhost:8080/iclassq");
    }
}
