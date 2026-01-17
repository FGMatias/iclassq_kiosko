package org.iclassq.accessibility;

import org.iclassq.accessibility.camera.CameraConfig;
import org.iclassq.accessibility.camera.CameraService;
import org.iclassq.accessibility.detection.DetectionResponse;
import org.iclassq.accessibility.detection.DetectionService;

import java.util.logging.Logger;

public class DisabilityDetector {

    private static final Logger logger = Logger.getLogger(DisabilityDetector.class.getName());

    private final CameraService cameraService;
    private final DetectionService mlService;
    private final AccessibilityManager accessibilityManager;
    private final AccessibilityDetectionService detectionService;

    private boolean initialized = false;

    public DisabilityDetector(CameraService cameraService, DetectionService mlService) {
        this.cameraService = cameraService;
        this.mlService = mlService;
        this.accessibilityManager = AccessibilityManager.getInstance();

        this.detectionService = new AccessibilityDetectionService(
                cameraService,
                mlService,
                accessibilityManager
        );

        logger.info("DisabilityDetector creado");
    }

    public DisabilityDetector() {
        this(
                new CameraService(CameraConfig.getDefaultMLConfig()),
                new DetectionService()
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
        logger.info("API ML disponible");

        logger.info("Inicializando cámaras...");
        if (!cameraService.initialize()) {
            logger.severe("No se pudieron inicializar las cámaras");
            return false;
        }
        logger.info(String.format("%d cámara(s) inicializada(s)",
                cameraService.getInitializedCameraCount()));

        initialized = true;
        detectionService.markAsReady();

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

        try {
            return detectionService.detectAsync().get();
        } catch (Exception e) {
            logger.severe("Error en detección: " + e.getMessage());
            return DetectionResponse.builder()
                    .success(false)
                    .error("Error en detección: " + e.getMessage())
                    .build();
        }
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

    public AccessibilityDetectionService getDetectionService() {
        return detectionService;
    }

    public void shutdown() {
        logger.info("Cerrando DisabilityDetector...");

        if (cameraService != null) {
            cameraService.shutdown();
        }

        if (detectionService != null) {
            detectionService.shutdown();
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

    public DetectionService getMlService() {
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
        sb.append(String.format("Servicio de Detección: %s\n",
                detectionService.isReady() ? "Listo" : "No listo"));
        sb.append("═══════════════════════════════════════\n");
        return sb.toString();
    }

    public void printDetectorInfo() {
        System.out.println(getDetectorInfo());
    }
}