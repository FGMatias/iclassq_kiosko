package org.iclassq.accessibility.adapter;

import javafx.application.Platform;
import org.iclassq.KioskoApplication;
import org.iclassq.accessibility.AccessibilityDetectionService;
import org.iclassq.accessibility.AccessibilityManager;
import org.iclassq.accessibility.DisabilityDetector;
import org.iclassq.view.components.Message;

import java.util.function.Consumer;
import java.util.logging.Logger;

public class DisabilityDetectionAdapter {

    private static final Logger logger = Logger.getLogger(DisabilityDetectionAdapter.class.getName());

    private Consumer<Boolean> onDetectionCompleted;

    public DisabilityDetectionAdapter() {
        executeDetection();
    }

    public void onDetectionCompleted(Consumer<Boolean> callback) {
        this.onDetectionCompleted = callback;
    }

    private void executeDetection() {
        logger.info("Iniciando detección de discapacidad");

        DisabilityDetector detector = KioskoApplication.getDisabilityDetector();

        if (detector == null) {
            handleDetectionUnavailable();
            return;
        }

        AccessibilityDetectionService detectionService = detector.getDetectionService();

        detectionService.onReady(ready -> {
            logger.info("Sistema de detección listo, iniciando análisis");
            executeDetectionAsync(detectionService);
        });
    }

    private void executeDetectionAsync(AccessibilityDetectionService detectionService) {
        detectionService.detectAndActivateAsync()
                .thenAccept(this::handleDetectionSuccess)
                .exceptionally(error -> {
                    handleDetectionError(error);
                    return null;
                });
    }

    private void handleDetectionSuccess(boolean hasDisability) {
        Platform.runLater(() -> {
            if (hasDisability) {
                handleDisabilityDetected();
            } else {
                handleNoDisabilityDetected();
            }

            if (onDetectionCompleted != null) {
                onDetectionCompleted.accept(hasDisability);
            }
        });
    }

    private void handleDisabilityDetected() {
        logger.info("Persona con discapacidad detectada");

        AccessibilityManager.getInstance().enableAccessibility();

        logger.info("Sistema de accesibilidad activado globalmente");
    }

    private void handleNoDisabilityDetected() {
        logger.info("Persona sin discapacidad detectada");

        AccessibilityManager.getInstance().disableAccessibility();

        logger.info("Modo visual normal activado");
    }

    private void handleDetectionUnavailable() {
        logger.warning("Sistema de detección no disponible - continuando en modo normal");

        Platform.runLater(() -> {
            AccessibilityManager.getInstance().disableAccessibility();

            if (onDetectionCompleted != null) {
                onDetectionCompleted.accept(false);
            }
        });
    }

    private void handleDetectionError(Throwable error) {
        logger.severe("Error en detección de discapacidad: " + error.getMessage());

        Platform.runLater(() -> {
            Message.showError(
                    "Error de Detección",
                    "No se pudo completar la detección de accesibilidad. Continuando en modo normal."
            );

            AccessibilityManager.getInstance().disableAccessibility();

            if (onDetectionCompleted != null) {
                onDetectionCompleted.accept(false);
            }
        });
    }
}