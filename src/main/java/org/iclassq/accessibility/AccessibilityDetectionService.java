package org.iclassq.accessibility;

import org.iclassq.accessibility.camera.CameraService;
import org.iclassq.accessibility.detection.DetectionResponse;
import org.iclassq.accessibility.detection.DetectionService;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class AccessibilityDetectionService {

    private static final Logger logger = Logger.getLogger(AccessibilityDetectionService.class.getName());

    private final CameraService cameraService;
    private final DetectionService mlService;
    private final AccessibilityManager accessibilityManager;
    private final ExecutorService executor;

    private volatile boolean ready = false;
    private final List<Consumer<Boolean>> readyListeners = new ArrayList<>();

    public AccessibilityDetectionService(
            CameraService cameraService,
            DetectionService mlService,
            AccessibilityManager accessibilityManager) {
        this.cameraService = cameraService;
        this.mlService = mlService;
        this.accessibilityManager = accessibilityManager;
        this.executor = Executors.newCachedThreadPool();

        logger.info("AccessibilityDetectionService creado");
    }

    public synchronized void markAsReady() {
        if (ready) {
            return;
        }

        ready = true;
        logger.info("AccessibilityDetectionService LISTO");

        for (Consumer<Boolean> listener : readyListeners) {
            try {
                listener.accept(true);
            } catch (Exception e) {
                logger.warning("Error notificando listener: " + e.getMessage());
            }
        }

        readyListeners.clear();
    }

    public synchronized void onReady(Consumer<Boolean> listener) {
        if (ready) {
            logger.info("Servicio ya listo, ejecutando callback inmediatamente");
            listener.accept(true);
        } else {
            logger.info("Servicio no listo, registrando callback para cuando esté disponible");
            readyListeners.add(listener);
        }
    }

    public CompletableFuture<List<BufferedImage>> captureFramesAsync() {
        return CompletableFuture.supplyAsync(() -> {
            if (!ready) {
                throw new IllegalStateException("Servicio no está listo. Espera a que se inicialice.");
            }

            logger.info("Capturando frames de cámaras...");
            List<BufferedImage> frames = cameraService.captureAllFramesAsList();

            if (frames.isEmpty()) {
                logger.warning("No se capturaron frames");
                throw new RuntimeException("No se pudieron capturar frames");
            }

            logger.info(String.format("%d frame(s) capturado(s)", frames.size()));
            return frames;

        }, executor);
    }

    public List<String> encodeFramesToBase64(List<BufferedImage> frames) {
        logger.info("Codificando frames a Base64...");

        List<String> encodedFrames = frames.stream()
                .map(this::imageToBase64)
                .collect(Collectors.toList());

        logger.info(String.format("%d frame(s) codificado(s)", encodedFrames.size()));
        return encodedFrames;
    }

    public CompletableFuture<DetectionResponse> sendToMLApiAsync(List<BufferedImage> frames) {
        return CompletableFuture.supplyAsync(() -> {
            logger.info("Enviando frames a API ML...");

            DetectionResponse response = mlService.detect(frames);

            if (response.hasError()) {
                logger.severe("Error en API ML: " + response.getError());
                throw new RuntimeException("Error en detección ML: " + response.getError());
            }

            logger.info(String.format("Respuesta recibida: %s", response.getSummary()));
            return response;

        }, executor);
    }

    public CompletableFuture<DetectionResponse> detectAsync() {
        logger.info("Iniciando detección completa (captura → API → respuesta)");

        return captureFramesAsync()
                .thenCompose(this::sendToMLApiAsync)
                .whenComplete((response, error) -> {
                    if (error != null) {
                        logger.severe("Error en detección: " + error.getMessage());
                    } else {
                        logger.info("Detección completada exitosamente");
                    }
                });
    }

    public CompletableFuture<Boolean> detectAndActivateAsync() {
        logger.info("Iniciando detección con activación automática");

        return detectAsync()
                .thenApply(response -> {
                    if (response.isDisabilityDetected()) {
                        logger.info("♿ Persona con discapacidad detectada");
                        logger.info("   Tipo: " + response.getDisabilityType());
                        logger.info("🔊 Activando servicios de accesibilidad...");

                        accessibilityManager.enableAccessibility();

                        logger.info("Servicios de accesibilidad ACTIVADOS");
                        return true;

                    } else {
                        logger.info("Persona sin discapacidad detectada");
                        logger.info("Manteniendo modo visual normal");
                        return false;
                    }
                })
                .exceptionally(error -> {
                    logger.severe("Error en detección y activación: " + error.getMessage());
                    return false;
                });
    }

    public boolean detectAndActivateSync() {
        try {
            return detectAndActivateAsync().get();
        } catch (Exception e) {
            logger.severe("Error en detección síncrona: " + e.getMessage());
            return false;
        }
    }

    private String imageToBase64(BufferedImage image) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(image, "jpg", baos);
            byte[] imageBytes = baos.toByteArray();
            return Base64.getEncoder().encodeToString(imageBytes);
        } catch (Exception e) {
            logger.severe("Error convirtiendo imagen a Base64: " + e.getMessage());
            throw new RuntimeException("Error en codificación Base64", e);
        }
    }

    public boolean isReady() {
        return ready;
    }

    public void shutdown() {
        logger.info("Cerrando AccessibilityDetectionService...");
        executor.shutdown();
        logger.info("AccessibilityDetectionService cerrado");
    }
}