package org.iclassq;

import atlantafx.base.theme.*;
import javafx.application.Application;
import javafx.scene.input.KeyCombination;
import javafx.stage.Stage;
import org.iclassq.accessibility.DisabilityDetector;
import org.iclassq.accessibility.proximity.ProximityDetector;
import org.iclassq.accessibility.voice.VoiceManager;
import org.iclassq.config.AppConfig;
import org.iclassq.config.ServiceFactory;
import org.iclassq.navigation.Navigator;
import org.iclassq.util.Constants;
import org.iclassq.util.Fonts;

import java.util.logging.Logger;

public class KioskoApplication extends Application {
    private static final Logger logger = Logger.getLogger(KioskoApplication.class.getName());
    private static DisabilityDetector disabilityDetector;
    private static ProximityDetector proximityDetector;

    @Override
    public void start(Stage stage) throws Exception {
        Fonts.loadFonts();
        Application.setUserAgentStylesheet(new CupertinoLight().getUserAgentStylesheet());

        ServiceFactory.init(AppConfig.getBackendUrl());

        initializeProximityDetector(null);
        initializeDisabilityDetector();
        initializeVoiceServices();

        Navigator.init(stage);

        stage.setTitle(Constants.APP_TITLE);
        stage.setFullScreen(true);
        stage.setFullScreenExitHint("");
        stage.setFullScreenExitKeyCombination(KeyCombination.NO_MATCH);

        Navigator.navigateToLogin();

        stage.show();
    }

    private void initializeVoiceServices() {
        try {
            logger.info("Inicializando servicios de voz");
            VoiceManager voiceManager = VoiceManager.getInstance();
            voiceManager.enableVoiceServices();

            if (voiceManager.isVoiceServicesEnabled()) {
                logger.info("Servicios de voz habilitados correctamente");
            } else {
                logger.warning("Servicios de voz no disponibles, la aplicacion continuará sin voz");
            }
        } catch (Exception e) {
            logger.warning("Error al inicializar los servicios de voz: " + e.getMessage());
            logger.warning("La aplicación continuara sin servicios de voz");
        }
    }

    private void initializeDisabilityDetector() {
        logger.info("Inicializando sistema de detección de discapacidad...");

        new Thread(() -> {
            try {
                disabilityDetector = new DisabilityDetector();

                boolean initialized = disabilityDetector.initialize();

                if (initialized) {
                    logger.info("Sistema de detección de discapacidad LISTO");
                    disabilityDetector.printDetectorInfo();
                } else {
                    logger.warning("Sistema de detección no disponible");
                    logger.warning("   Verifique:");
                    logger.warning("   1. Docker Desktop está corriendo");
                    logger.warning("   2. API ML activa: docker ps");
                    logger.warning("   3. Cámaras conectadas");
                    disabilityDetector = null;
                }

            } catch (Exception e) {
                logger.severe("Error al inicializar sistema de detección: " + e.getMessage());
                disabilityDetector = null;
            }

        }, "DisabilityDetector-Init").start();
    }

    public static boolean initializeProximityDetector(String portName) {
        try {
            logger.info("Inicializando detector de proximidad...");

            proximityDetector = new ProximityDetector(portName);
            boolean success = proximityDetector.initialize();

            if (success) {
                logger.info("Detector de proximidad inicializado");
                return true;
            } else {
                logger.warning("No se pudo inicializar detector de proximidad");
                proximityDetector = null;
                return false;
            }

        } catch (Exception e) {
            logger.severe("Error al inicializar detector de proximidad: " + e.getMessage());
            proximityDetector = null;
            return false;
        }
    }

    public static DisabilityDetector getDisabilityDetector() {
        return disabilityDetector;
    }

    public static boolean isDisabilityDetectorAvailable() {
        return disabilityDetector != null && disabilityDetector.isInitialized();
    }

    public static ProximityDetector getProximityDetector() {
        return proximityDetector;
    }

    @Override
    public void stop() throws Exception {
        try {
            VoiceManager voiceManager = VoiceManager.getInstance();
            if (voiceManager.isVoiceServicesEnabled()) {
                voiceManager.disableVoiceServices();
                logger.info("Servicios de voz deshabilitados");
            }
        } catch (Exception e) {
            logger.warning("Error al deshabilitar servicios de voz: " + e.getMessage());
        }

        try {
            if (disabilityDetector != null) {
                disabilityDetector.shutdown();
                logger.info("Sistema de detección cerrado");
            }
        } catch (Exception e) {
            logger.warning("Error al cerrar sistema de detección: " + e.getMessage());
        }

        try {
            logger.info("Cerrando aplicación...");

            if (proximityDetector != null) {
                proximityDetector.shutdown();
            }
        } catch (Exception e) {
            logger.severe("Error al cerrar aplicación: " + e.getMessage());
        }

        super.stop();
    }

    public static void main(String[] args) {
        launch(args);
    }
}