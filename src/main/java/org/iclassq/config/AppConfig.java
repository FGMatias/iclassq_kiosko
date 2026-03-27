package org.iclassq.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Logger;

public class AppConfig {
    private static final Logger logger = Logger.getLogger(AppConfig.class.getName());
    private static final Properties properties = new Properties();

    static {
        loadProperties();
    }

    private static void loadProperties() {
        File runtimeConfig = new File("../runtime/conf/application.properties");
        if (runtimeConfig.exists()) {
            try (FileInputStream fis = new FileInputStream(runtimeConfig)) {
                properties.load(fis);
                logger.info("Configuración cargada desde: " + runtimeConfig.getAbsolutePath());
                return;
            } catch (Exception e) {
                logger.warning("Error al cargar config desde runtime/conf: " + e.getMessage());
            }
        }

        try {
            String jarPath = AppConfig.class
                    .getProtectionDomain()
                    .getCodeSource()
                    .getLocation()
                    .toURI()
                    .getPath();

            if (jarPath.startsWith("/") && jarPath.contains(":")) {
                jarPath = jarPath.substring(1);
            }

            File jarFile = new File(jarPath);
            File appDir = jarFile.getParentFile();
            File installDir = appDir != null ? appDir.getParentFile() : null;

            if (installDir != null) {
                File configFile = new File(installDir, "runtime/conf/application.properties");
                if (configFile.exists()) {
                    try (FileInputStream fis = new FileInputStream(configFile)) {
                        properties.load(fis);
                        logger.info("Configuración cargada desde: " + configFile.getAbsolutePath());
                        return;
                    } catch (Exception e) {
                        logger.warning("Error: " + e.getMessage());
                    }
                }
            }
        } catch (Exception e) {
            logger.warning("Error buscando config por ubicación JAR: " + e.getMessage());
        }

        try (InputStream is = AppConfig.class.getResourceAsStream("/application.properties")) {
            if (is != null) {
                properties.load(is);
                logger.info("Configuración cargada desde JAR (resources)");
            } else {
                logger.warning("application.properties no encontrado");
            }
        } catch (Exception e) {
            logger.severe("Error al cargar application.properties: " + e.getMessage());
        }
    }

    public static String getProperty(String key) {
        return properties.getProperty(key);
    }

    public static String getProperty(String key, String defaultValue) {
        return properties.getProperty(key, defaultValue);
    }

    public static String getBackendUrl() {
        return properties.getProperty("app.backend.url", "http://localhost:8080/iclassq");
    }

    public static String getDetectionUrl() {
        return properties.getProperty("app.detection.url", "http://localhost:5000/verify-images");
    }
}
