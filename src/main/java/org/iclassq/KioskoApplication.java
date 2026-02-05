package org.iclassq;

import atlantafx.base.theme.*;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.input.KeyCombination;
import javafx.stage.Stage;
import org.iclassq.accessibility.AccessibilityInitializer;
import org.iclassq.accessibility.DisabilityDetector;
import org.iclassq.accessibility.camera.SmartCameraSchedulerDynamic;
import org.iclassq.accessibility.proximity.ProximityDetector;
import org.iclassq.accessibility.voice.VoiceManager;
import org.iclassq.config.AppConfig;
import org.iclassq.config.ServiceFactory;
import org.iclassq.navigation.Navigator;
import org.iclassq.util.Constants;
import org.iclassq.util.Fonts;

import java.io.InputStream;
import java.util.Optional;
import java.util.logging.LogManager;
import java.util.logging.Logger;

public class KioskoApplication extends Application {
    private static final Logger logger = Logger.getLogger(KioskoApplication.class.getName());
    private static DisabilityDetector disabilityDetector;
    private static ProximityDetector proximityDetector;
    private static SmartCameraSchedulerDynamic cameraScheduler;

    private int globalTapCount = 0;
    private long lastTapTime = 0;
    private static final int REQUIRED_TAPS = 5;
    private static final long TAP_TIMEOUT_MS = 3000;
    private static final double ADMIN_ZONE_SIZE = 100.0;

    @Override
    public void start(Stage stage) throws Exception {
        Fonts.loadFonts();
        Application.setUserAgentStylesheet(new CupertinoLight().getUserAgentStylesheet());

        ServiceFactory.init(AppConfig.getBackendUrl());
//        ServiceFactory.init(AppConfig.getDetectionUrl());

        initializeVoiceServices();

        Navigator.init(stage);

        stage.setTitle(Constants.APP_TITLE);
        stage.setFullScreen(true);
        stage.setFullScreenExitHint("");
        stage.setFullScreenExitKeyCombination(KeyCombination.NO_MATCH);

        Navigator.navigateToLogin();

        Platform.runLater(() -> {
            Scene scene = stage.getScene();
            if (scene != null) {
                setupGlobalAdminGesture(scene);
                logger.info("Gesto de administrador configurado (5 toques en esquina superior izquierda)");
            }
        });

        stage.show();
    }

    private void setupGlobalAdminGesture(Scene scene) {
        scene.setOnMouseClicked(event -> {
            long now = System.currentTimeMillis();
            double x = event.getSceneX();
            double y = event.getSceneY();

            boolean isInAdminZone = (x < ADMIN_ZONE_SIZE && y < ADMIN_ZONE_SIZE);

            if (!isInAdminZone) {
                return;
            }

            if (now - lastTapTime < TAP_TIMEOUT_MS) {
                globalTapCount++;
            } else {
                globalTapCount = 1;
            }

            lastTapTime = now;

            logger.fine(String.format("Admin tap detectado: %d/%d", globalTapCount, REQUIRED_TAPS));

            if (globalTapCount >= REQUIRED_TAPS) {
                globalTapCount = 0; // Reset
                Platform.runLater(this::showGlobalShutdownDialog);
            }
        });
    }

    private void showGlobalShutdownDialog() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Modo Administrador");
        alert.setHeaderText("¿Apagar Sistema iClassQ Kiosko?");
        alert.setContentText("Esta acción cerrará la aplicación completamente.");

        alert.getDialogPane().setMinWidth(600);
        alert.getDialogPane().setMinHeight(300);

        alert.getDialogPane().lookupButton(ButtonType.OK).setStyle(
                "-fx-min-width: 150px; " +
                "-fx-min-height: 60px; " +
                "-fx-font-size: 16px;"
        );
        alert.getDialogPane().lookupButton(ButtonType.CANCEL).setStyle(
                "-fx-min-width: 150px; " +
                "-fx-min-height: 60px; " +
                "-fx-font-size: 16px;"
        );

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            shutdownApplication();
        } else {
            logger.info("Apagado cancelado por el administrador");
        }
    }

    private void shutdownApplication() {
        try {
            logger.info("Cerrando sistemas de accesibilidad...");
            AccessibilityInitializer.shutdownSystems();
            logger.info("Sistemas de accesibilidad cerrados");
        } catch (Exception e) {
            logger.severe("Error al cerrar sistemas de accesibilidad: " + e.getMessage());
            e.printStackTrace();
        }

        try {
            logger.info("Deshabilitando servicios de voz...");
            VoiceManager voiceManager = VoiceManager.getInstance();
            if (voiceManager.isVoiceServicesEnabled()) {
                voiceManager.disableVoiceServices();
                logger.info("Servicios de voz deshabilitados");
            }
        } catch (Exception e) {
            logger.warning("Error al deshabilitar servicios de voz: " + e.getMessage());
        }

        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        Platform.exit();

        System.exit(0);
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

    public static DisabilityDetector getDisabilityDetector() {
        return disabilityDetector;
    }

    public static void setDisabilityDetector(DisabilityDetector detector) {
        disabilityDetector = detector;
    }

    public static boolean isDisabilityDetectorAvailable() {
        return disabilityDetector != null && disabilityDetector.isInitialized();
    }

    public static ProximityDetector getProximityDetector() {
        return proximityDetector;
    }

    public static void setProximityDetector(ProximityDetector detector) {
        proximityDetector = detector;
    }

    public static SmartCameraSchedulerDynamic getCameraScheduler() {
        return cameraScheduler;
    }

    public static void setCameraScheduler(SmartCameraSchedulerDynamic scheduler) {
        cameraScheduler = scheduler;
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
            if (cameraScheduler != null) {
                cameraScheduler.shutdown();
                logger.info("Smart Camera Scheduler detenido");
            }
        } catch (Exception e) {
            logger.warning("Error al detener scheduler: " + e.getMessage());
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
        try {
            InputStream configFile = KioskoApplication.class
                    .getResourceAsStream("/logging.properties");
            if (configFile != null) {
                LogManager.getLogManager().readConfiguration(configFile);
            }
        } catch (Exception e) {
            System.err.println("No se pudo cargar logging.properties");
        }
        launch(args);
    }
}