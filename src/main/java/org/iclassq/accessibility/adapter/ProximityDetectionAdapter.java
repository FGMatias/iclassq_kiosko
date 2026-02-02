package org.iclassq.accessibility.adapter;

import javafx.application.Platform;
import org.iclassq.KioskoApplication;
import org.iclassq.accessibility.AccessibilityManager;
import org.iclassq.accessibility.proximity.ProximityDetectionService;
import org.iclassq.accessibility.proximity.ProximityDetector;
import org.iclassq.view.components.Message;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.logging.Logger;

public class ProximityDetectionAdapter {
    private static final Logger logger = Logger.getLogger(ProximityDetectionAdapter.class.getName());

    private static ProximityDetectionAdapter instance;

    private Consumer<Boolean> onDetectionCompleted;
    private final AtomicBoolean detectionExecuted = new AtomicBoolean(false);
    private ProximityDetectionService detectionService;

    private ProximityDetectionAdapter() {
        logger.info("ProximityDetectionAdapter creado (Singleton - modo espera)");
    }

    public static synchronized ProximityDetectionAdapter getInstance() {
        if (instance == null) {
            instance = new ProximityDetectionAdapter();
        }
        return instance;
    }

    public void onDetectionCompleted(Consumer<Boolean> callback) {
        this.onDetectionCompleted = callback;
    }

    public void start() {
        if (detectionExecuted.get()) {
            logger.warning("Detección ya fue ejecutada, ignorando llamada a start");
            logger.warning("Usa reset() si necesitas reiniciar la detección");
            return;
        }

        logger.info("Iniciando detección de proximidad");
        executeDetection();
    }

    public void stop() {
        logger.info("Deteniendo detección de proximidad");
        detectionExecuted.set(true);
    }

    public void reset() {
        logger.info("═══════════════════════════════════════════");
        logger.info("RESETEANDO DETECTOR DE PROXIMIDAD");
        logger.info("═══════════════════════════════════════════");
        logger.info("   Estado anterior: detectionExecuted = " + detectionExecuted.get());

        detectionExecuted.set(false);

        logger.info("   Estado nuevo: detectionExecuted = " + detectionExecuted.get());
        logger.info("   Sistema listo para detectar nueva persona");
        logger.info("═══════════════════════════════════════════");
    }

    public boolean isDetectionExecuted() {
        return detectionExecuted.get();
    }

    private void executeDetection() {
        logger.info("Iniciando detección de proximidad...");

        ProximityDetector detector = KioskoApplication.getProximityDetector();

        if (detector == null) {
            handleDetectionUnavailable();
            return;
        }

        detectionService = detector.getDetectionService();

        detectionService.onReady(ready -> {
            logger.info("Sistema de proximidad listo, esperando presencia...");
            executeDetectionAsync();
        });
    }

    private void executeDetectionAsync() {
        detectionService.detectAndActivateAsync()
                .thenAccept(this::handleDetectionSuccess)
                .exceptionally(error -> {
                    handleDetectionError(error);
                    return null;
                });
    }

    private void handleDetectionSuccess(boolean hasPresence) {
        if (!detectionExecuted.compareAndSet(false, true)) {
            logger.info("Señal COMPLETE ignorada - detección ya procesada anteriormente");
            logger.fine("   detectionExecuted = true, ignorando llamada subsecuente");
            return;
        }

        Platform.runLater(() -> {
            logger.info("PRIMERA señal de proximidad recibida - procesando...");

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
        logger.info("Presencia detectada (usuario cerca del sensor)");
        logger.info("   Notificando a IdentificationController para iniciar cámara");
    }

    private void handleNoPresenceDetected() {
        logger.info("No se detectó presencia continua");
        logger.info("   Usuario se acercó pero se retiró antes de 5 segundos");

        AccessibilityManager.getInstance().disableAccessibility();
    }

    private void handleDetectionUnavailable() {
        logger.warning("Sistema de proximidad no disponible");
        logger.warning("   Arduino no está conectado o no responde");
        logger.warning("   Continuando en modo visual normal");

        detectionExecuted.set(true);

        Platform.runLater(() -> {
            AccessibilityManager.getInstance().disableAccessibility();

            if (onDetectionCompleted != null) {
                onDetectionCompleted.accept(false);
            }
        });
    }

    private void handleDetectionError(Throwable error) {
        logger.severe("Error en detección de proximidad: " + error.getMessage());
        error.printStackTrace();

        detectionExecuted.set(true);

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