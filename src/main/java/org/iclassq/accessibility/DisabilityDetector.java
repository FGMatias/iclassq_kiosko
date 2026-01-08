package org.iclassq.accessibility;

import org.iclassq.accessibility.camera.CameraConfig;
import org.iclassq.accessibility.camera.CameraService;
import org.iclassq.accessibility.ml.DetectionResponse;
import org.iclassq.accessibility.ml.MLDetectionService;

import java.awt.image.BufferedImage;
import java.util.List;
import java.util.logging.Logger;

public class DisabilityDetector {

    private static final Logger logger = Logger.getLogger(DisabilityDetector.class.getName());

    private final CameraService cameraService;
    private final MLDetectionService mlService;
    private final AccessibilityManager accessibilityManager;
    private boolean initialized = false;

    public DisabilityDetector(CameraService cameraService, MLDetectionService mlService) {
        this.cameraService = cameraService;
        this.mlService = mlService;
        this.accessibilityManager = AccessibilityManager.getInstance();

        logger.info("DisabilityDetector creado");
    }

    public DisabilityDetector() {
        this(
                new CameraService(CameraConfig.getDefaultMLConfig()),
                new MLDetectionService()
        );
    }

    public boolean initialize() {
        logger.info("═══════════════════════════════════════");
        logger.info("INICIALIZANDO DISABILITY DETECTOR");
        logger.info("═══════════════════════════════════════");

        logger.info("Verificando API ML...");
        if (!mlService.isApiAvailable()) {
            logger.severe("API ML no está disponible. Verifique que Docker esté corriendo.");
            logger.severe("   Comando: docker ps");
            logger.severe("   URL: " + mlService.getApiUrl());
            return false;
        }
        logger.info("  API ML disponible");

        logger.info("Inicializando cámaras...");
        if (!cameraService.initialize()) {
            logger.severe("No se pudieron inicializar las cámaras");
            return false;
        }
        logger.info(String.format("   %d cámara(s) inicializada(s)",
                cameraService.getInitializedCameraCount()));

        initialized = true;
        logger.info("═══════════════════════════════════════");
        logger.info("DISABILITY DETECTOR INICIALIZADO");
        logger.info("═══════════════════════════════════════\n");

        return true;
    }

    public DetectionResponse detect() {
        if (!initialized) {
            logger.severe("DisabilityDetector no está inicializado. Llame a initialize() primero.");
            return DetectionResponse.builder()
                    .success(false)
                    .error("Detector no inicializado")
                    .build();
        }

        logger.info("\nIniciando proceso de detección...");

        logger.info("Capturando frames de cámaras...");
        List<BufferedImage> frames = cameraService.captureAllFramesAsList();

        if (frames.isEmpty()) {
            logger.severe("No se capturaron frames");
            return DetectionResponse.builder()
                    .success(false)
                    .error("No se capturaron frames de las cámaras")
                    .build();
        }

        logger.info(String.format("   %d frame(s) capturado(s)", frames.size()));

        logger.info("Enviando a API ML...");
        DetectionResponse response = mlService.detect(frames);

        if (response.hasError()) {
            logger.severe(String.format("Error en detección: %s", response.getError()));
            return response;
        }

        if (!response.isValid()) {
            logger.warning("Respuesta no válida de API");
            return response;
        }

        logger.info(String.format("Resultado: %s", response.getSummary()));
        logger.info(String.format("Tiempo de procesamiento: %dms", response.getProcessingTimeMs()));

        return response;
    }

    public boolean detectAndActivate() {
        DetectionResponse response = detect();

        if (response.isDisabilityDetected()) {
            logger.info("Persona con discapacidad detectada");
            logger.info("Activando servicios de accesibilidad...");

            accessibilityManager.enableAccessibility();

            logger.info(String.format("Servicios activados - Tipo: %s",
                    response.getDisabilityType()));

            return true;
        } else {
            logger.info("Persona sin discapacidad detectada");
            logger.info("Manteniendo servicios normales");

            return false;
        }
    }

    public void shutdown() {
        logger.info("Cerrando DisabilityDetector...");

        if (cameraService != null) {
            cameraService.shutdown();
        }

        initialized = false;

        logger.info("DisabilityDetector cerrado");
    }

    public boolean isInitialized() {
        return initialized;
    }

    public CameraService getCameraService() {
        return cameraService;
    }

    public MLDetectionService getMlService() {
        return mlService;
    }

    public AccessibilityManager getAccessibilityManager() {
        return accessibilityManager;
    }

    public String getDetectorInfo() {
        StringBuilder sb = new StringBuilder();
        sb.append("\n═══════════════════════════════════════\n");
        sb.append("DISABILITY DETECTOR INFO\n");
        sb.append("═══════════════════════════════════════\n");
        sb.append(String.format("Estado: %s\n", initialized ? "Inicializado" : "No inicializado"));
        sb.append(String.format("API ML: %s\n", mlService.getApiUrl()));
        sb.append(String.format("Cámaras: %d activa(s)\n", cameraService.getInitializedCameraCount()));
        sb.append(String.format("Accesibilidad: %s\n",
                accessibilityManager.isAccessibilityEnabled() ? "Activa" : "Inactiva"));
        sb.append("═══════════════════════════════════════\n");
        return sb.toString();
    }

    public void printDetectorInfo() {
        System.out.println(getDetectorInfo());
    }
}