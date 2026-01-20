package org.iclassq.accessibility.camera;

import lombok.Getter;

import java.awt.image.BufferedImage;
import java.util.*;
import java.util.concurrent.*;
import java.util.logging.Logger;

@Getter
public class CameraService {

    private static final Logger logger = Logger.getLogger(CameraService.class.getName());

    private final CameraConfig config;
    private final Map<Integer, CameraCapture> cameras;
    private final ExecutorService executorService;
    private boolean initialized = false;
    private List<CameraInfo> availableCameras;

    public CameraService(CameraConfig config) {
        this.config = config;
        this.cameras = new ConcurrentHashMap<>();
        this.executorService = Executors.newCachedThreadPool();
        this.availableCameras = new ArrayList<>();
    }

    public CameraService() {
        this(CameraConfig.getDefaultMLConfig());
    }

    public boolean initialize() {
        logger.info("Inicializando CameraService...");

        availableCameras = CameraManager.detectAvailableCameras(config.getMaxCameras());

        if (availableCameras.isEmpty()) {
            logger.severe("No se detectaron cámaras disponibles");
            return false;
        }

        logger.info(String.format("%d cámara(s) detectada(s)", availableCameras.size()));

        List<Integer> indicesToInitialize = getIndicesToInitialize();

        int successCount = 0;
        for (int index : indicesToInitialize) {
            if (initializeCamera(index)) {
                successCount++;
            }
        }

        initialized = successCount > 0;

        if (initialized) {
            logger.info(String.format("CameraService inicializado con %d/%d cámara(s)",
                    successCount, indicesToInitialize.size()));
        } else {
            logger.severe("No se pudo inicializar ninguna cámara");
        }

        return initialized;
    }

    private List<Integer> getIndicesToInitialize() {
        List<Integer> indices = new ArrayList<>();

        if (config.getSpecificCameraIndices() != null && config.getSpecificCameraIndices().length > 0) {
            for (int index : config.getSpecificCameraIndices()) {
                if (isCameraAvailable(index)) {
                    indices.add(index);
                }
            }
        } else {
            for (CameraInfo camera : availableCameras) {
                indices.add(camera.getIndex());
            }
        }

        return indices;
    }

    private boolean initializeCamera(int index) {
        try {
            CameraCapture capture = new CameraCapture(index, config);
            if (capture.initialize()) {
                cameras.put(index, capture);
                return true;
            }
        } catch (Exception e) {
            logger.warning(String.format("Error al inicializar cámara %d: %s", index, e.getMessage()));
        }
        return false;
    }

    public Map<Integer, List<BufferedImage>> captureFromAllCameras() {
        if (!initialized) {
            logger.warning("CameraService no está inicializado");
            return new HashMap<>();
        }

        logger.info("📸 Capturando frames de todas las cámaras...");

        Map<Integer, List<BufferedImage>> allFrames = new ConcurrentHashMap<>();
        List<Future<Void>> futures = new ArrayList<>();

        for (Map.Entry<Integer, CameraCapture> entry : cameras.entrySet()) {
            int cameraIndex = entry.getKey();
            CameraCapture capture = entry.getValue();

            Future<Void> future = executorService.submit(() -> {
                List<BufferedImage> frames = capture.captureFrames(config.getFramesPerCapture());
                allFrames.put(cameraIndex, frames);
                return null;
            });

            futures.add(future);
        }

        for (Future<Void> future : futures) {
            try {
                future.get(config.getInitTimeoutMs(), TimeUnit.MILLISECONDS);
            } catch (Exception e) {
                logger.warning("Error esperando captura de cámara: " + e.getMessage());
            }
        }

        int totalFrames = allFrames.values().stream()
                .mapToInt(List::size)
                .sum();

        logger.info(String.format("%d frames capturados de %d cámara(s)",
                totalFrames, allFrames.size()));

        return allFrames;
    }

    public Map<Integer, List<BufferedImage>> captureFromSpecificCameras(int... cameraIndices) {
        Map<Integer, List<BufferedImage>> allFrames = new ConcurrentHashMap<>();

        for (int index : cameraIndices) {
            CameraCapture capture = cameras.get(index);
            if (capture != null && capture.isInitialized()) {
                List<BufferedImage> frames = capture.captureFrames(config.getFramesPerCapture());
                allFrames.put(index, frames);
            } else {
                logger.warning(String.format("Cámara %d no está disponible", index));
            }
        }

        return allFrames;
    }

    public List<BufferedImage> captureAllFramesAsList() {
        Map<Integer, List<BufferedImage>> framesByCamera = captureFromAllCameras();

        List<BufferedImage> allFrames = new ArrayList<>();

        framesByCamera.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(entry -> allFrames.addAll(entry.getValue()));

        return allFrames;
    }

    public void stopAllCameras() {
        logger.info("Deteniendo todas las cámaras...");

        for (CameraCapture capture : cameras.values()) {
            capture.stop();
        }

        cameras.clear();
        initialized = false;

        logger.info("Todas las cámaras detenidas");
    }

    public void shutdown() {
        logger.info("Apagando CameraService...");

        stopAllCameras();

        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(5, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow();
            Thread.currentThread().interrupt();
        }

        logger.info("CameraService apagado");
    }

    public int getInitializedCameraCount() {
        return cameras.size();
    }

    public boolean isCameraInitialized(int index) {
        CameraCapture capture = cameras.get(index);
        return capture != null && capture.isInitialized();
    }

    private boolean isCameraAvailable(int index) {
        return availableCameras.stream()
                .anyMatch(camera -> camera.getIndex() == index);
    }

    public String getServiceInfo() {
        StringBuilder sb = new StringBuilder();
        sb.append("\n═══════════════════════════════════════\n");
        sb.append("📸 CAMERA SERVICE INFO\n");
        sb.append("═══════════════════════════════════════\n");
        sb.append(String.format("Estado: %s\n", initialized ? "Activo" : "Inactivo"));
        sb.append(String.format("Cámaras detectadas: %d\n", availableCameras.size()));
        sb.append(String.format("Cámaras inicializadas: %d\n", cameras.size()));
        sb.append(String.format("Configuración: %s\n", config));

        if (!availableCameras.isEmpty()) {
            sb.append("\nCámaras disponibles:\n");
            for (CameraInfo camera : availableCameras) {
                String status = cameras.containsKey(camera.getIndex()) ? "✅" : "⏸️";
                sb.append(String.format("  %s [%d] %s - %s\n",
                        status,
                        camera.getIndex(),
                        camera.getName(),
                        camera.getType().getDescription()));
            }
        }

        sb.append("═══════════════════════════════════════\n");
        return sb.toString();
    }

    public List<BufferedImage> captureSingleFrameFromAllCameras() {
        if (!initialized) {
            logger.warning("CameraService no está inicializado");
            return new ArrayList<>();
        }

        logger.info("Capturando 1 frame de cada cámara (optimizado)...");

        List<BufferedImage> frames = new ArrayList<>();

        for (Map.Entry<Integer, CameraCapture> entry : cameras.entrySet()) {
            int cameraIndex = entry.getKey();
            CameraCapture capture = entry.getValue();

            try {
                if (capture.isInitialized()) {
                    BufferedImage frame = capture.captureSingleFrame();

                    if (frame != null) {
                        frames.add(frame);
                        logger.info(String.format("Frame capturado de cámara %d", cameraIndex));
                    } else {
                        logger.warning(String.format("Frame nulo de cámara %d", cameraIndex));
                    }
                } else {
                    logger.warning(String.format("Cámara %d no está inicializada", cameraIndex));
                }
            } catch (Exception e) {
                logger.warning(String.format("Error capturando de cámara %d: %s",
                        cameraIndex, e.getMessage()));
            }
        }

        logger.info(String.format("Captura completa: %d frame(s) de %d cámara(s)",
                frames.size(), cameras.size()));

        return frames;
    }

    public void printServiceInfo() {
        System.out.println(getServiceInfo());
    }
}