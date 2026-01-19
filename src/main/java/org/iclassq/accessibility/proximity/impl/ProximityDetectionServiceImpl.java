package org.iclassq.accessibility.proximity.impl;

import javafx.application.Platform;
import org.iclassq.accessibility.proximity.ArduinoSerialService;
import org.iclassq.accessibility.proximity.ProximityData;
import org.iclassq.accessibility.proximity.ProximityDetectionService;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.logging.Logger;

public class ProximityDetectionServiceImpl implements ProximityDetectionService {
    private static final Logger logger = Logger.getLogger(ProximityDetectionService.class.getName());

    private final ArduinoSerialService arduinoService;
    private Consumer<Boolean> readyCallback;
    private Consumer<ProximityData> updateCallback;
    private boolean ready = false;
    private boolean detecting = false;

    public ProximityDetectionServiceImpl() {
        this.arduinoService = new ArduinoSerialServiceImpl();
    }

    @Override
    public boolean initialize(String portName) {
        try {
            logger.info("Inicializando servicio de detección por proximidad...");

            if (portName == null || portName.isEmpty()) {
                portName = ArduinoSerialServiceImpl.findArduinoPort();

                if (portName == null) {
                    logger.warning("No se pudo auto-detectar puerto Arduino");
                    return false;
                }
            }

            boolean connected = arduinoService.connect(portName);

            if (!connected) {
                logger.severe("No se pudo conectar con Arduino");
                return false;
            }

            setupCallbacks();

            logger.info("Servicio de detección por proximidad inicializado");
            return true;

        } catch (Exception e) {
            logger.severe("Error al inicializar servicio: " + e.getMessage());
            return false;
        }
    }

    private void setupCallbacks() {
        arduinoService.onReady(isReady -> {
            this.ready = isReady;
            logger.info("Arduino READY - Sistema listo para detectar");

            if (readyCallback != null) {
                Platform.runLater(() -> readyCallback.accept(isReady));
            }
        });

        arduinoService.onProximityUpdate(data -> {
            logger.fine("Actualización: " + data);

            if (updateCallback != null) {
                Platform.runLater(() -> updateCallback.accept(data));
            }
        });
    }

    @Override
    public CompletableFuture<Boolean> detectAsync() {
        CompletableFuture<Boolean> future = new CompletableFuture<>();

        if (!ready) {
            logger.warning("Arduino no está listo");
            future.complete(false);
            return future;
        }

        if (detecting) {
            logger.warning("Ya hay una detección en progreso");
            future.complete(false);
            return future;
        }

        detecting = true;
        logger.info("Iniciando detección por proximidad...");
        logger.info("Esperando que alguien se acerque al sensor...");

        arduinoService.onDetectionComplete(() -> {
            logger.info("Persona detectada (5 segundos de presencia)");
            detecting = false;
            Platform.runLater(() -> future.complete(true));
        });

        arduinoService.onDetectionCancelled(() -> {
            logger.info("Detección cancelada (persona se retiró)");
            detecting = false;
            Platform.runLater(() -> future.complete(false));
        });

        return future;
    }

    @Override
    public CompletableFuture<Boolean> detectAndActivateAsync() {
        return detectAsync()
                .thenApply(detected -> {
                    if (detected) {
                        logger.info("Presencia detectada → Activar accesibilidad");
                        return true;
                    } else {
                        logger.info("No se detectó presencia → No activar accesibilidad");
                        return false;
                    }
                });
    }

    @Override
    public void shutdown() {
        logger.info("Cerrando servicio de detección por proximidad...");
        detecting = false;
        arduinoService.disconnect();
    }

    @Override
    public void onReady(Consumer<Boolean> callback) {
        this.readyCallback = callback;
    }

    @Override
    public void onProximityUpdate(Consumer<ProximityData> callback) {
        this.updateCallback = callback;
    }

    @Override
    public boolean isReady() {
        return ready;
    }

    @Override
    public boolean isDetecting() {
        return detecting;
    }

    @Override
    public boolean isConnected() {
        return arduinoService.isConnected();
    }

    @Override
    public String[] getAvailablePorts() {
        return ArduinoSerialServiceImpl.getAvailablePorts();
    }
}