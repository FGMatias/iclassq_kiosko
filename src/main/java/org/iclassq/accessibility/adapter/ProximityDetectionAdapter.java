package org.iclassq.accessibility.adapter;

import javafx.application.Platform;
import org.iclassq.KioskoApplication;
import org.iclassq.accessibility.AccessibilityManager;
import org.iclassq.accessibility.proximity.ProximityDetectionService;
import org.iclassq.accessibility.proximity.ProximityDetector;
import org.iclassq.view.components.Message;

import java.util.function.Consumer;
import java.util.logging.Logger;

public class ProximityDetectionAdapter {
    private static final Logger logger = Logger.getLogger(ProximityDetectionAdapter.class.getName());

    private Consumer<Boolean> onDetectionCompleted;

    public ProximityDetectionAdapter() {
        executeDetection();
    }

    public void onDetectionCompleted(Consumer<Boolean> callback) {
        this.onDetectionCompleted = callback;
    }

    private void executeDetection() {
        logger.info("Iniciando detección de proximidad...");

        ProximityDetector detector = KioskoApplication.getProximityDetector();

        if (detector == null) {
            handleDetectionUnavailable();
            return;
        }

        ProximityDetectionService detectionService = detector.getDetectionService();

        detectionService.onReady(ready -> {
            logger.info("📡 Sistema de proximidad listo, esperando presencia...");
            executeDetectionAsync(detectionService);
        });
    }

    private void executeDetectionAsync(ProximityDetectionService detectionService) {
        detectionService.detectAndActivateAsync()
                .thenAccept(this::handleDetectionSuccess)
                .exceptionally(error -> {
                    handleDetectionError(error);
                    return null;
                });
    }

    private void handleDetectionSuccess(boolean hasPresence) {
        Platform.runLater(() -> {
            if (hasPresence) {
                handlePresenceDetected();
            } else {
                handleNoPresenceDetected();
            }

            if (onDetectionCompleted != null) {
                onDetectionCompleted.accept(hasPresence);
            }
        });
    }

    private void handlePresenceDetected() {
        logger.info("Presencia detectada (5 segundos)");

        AccessibilityManager.getInstance().enableAccessibility();

        logger.info("Sistema de accesibilidad activado");
    }

    private void handleNoPresenceDetected() {
        logger.info("No se detectó presencia continua");

        AccessibilityManager.getInstance().disableAccessibility();

        logger.info("Modo visual normal activado");
    }

    private void handleDetectionUnavailable() {
        logger.warning("Sistema de proximidad no disponible - continuando en modo visual");

        Platform.runLater(() -> {
            AccessibilityManager.getInstance().disableAccessibility();

            if (onDetectionCompleted != null) {
                onDetectionCompleted.accept(false);
            }
        });
    }

    private void handleDetectionError(Throwable error) {
        logger.severe("Error en detección de proximidad: " + error.getMessage());

        Platform.runLater(() -> {
            Message.showError(
                    "Error de Detección",
                    "No se pudo completar la detección de proximidad. Continuando en modo normal."
            );

            AccessibilityManager.getInstance().disableAccessibility();

            if (onDetectionCompleted != null) {
                onDetectionCompleted.accept(false);
            }
        });
    }
}