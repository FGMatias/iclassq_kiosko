package org.iclassq.accessibility.proximity;

import java.util.logging.Logger;

public class ProximityDetector {

    private static final Logger logger = Logger.getLogger(ProximityDetector.class.getName());

    private final ProximityDetectionService detectionService;
    private final String portName;

    public ProximityDetector(String portName) {
        this.portName = portName;
        this.detectionService = new ProximityDetectionService();
    }

    public ProximityDetector() {
        this(null);
    }

    public boolean initialize() {
        try {
            logger.info("Inicializando ProximityDetector...");

            boolean success = detectionService.initialize(portName);

            if (success) {
                logger.info("ProximityDetector inicializado correctamente");
            } else {
                logger.warning("ProximityDetector no se pudo inicializar");
            }

            return success;

        } catch (Exception e) {
            logger.severe("Error al inicializar ProximityDetector: " + e.getMessage());
            return false;
        }
    }

    public ProximityDetectionService getDetectionService() {
        return detectionService;
    }

    public boolean isReady() {
        return detectionService.isReady();
    }

    public boolean isConnected() {
        return detectionService.isConnected();
    }

    public void shutdown() {
        logger.info("Cerrando ProximityDetector...");
        detectionService.shutdown();
    }

     */
    public static String[] getAvailablePorts() {
        return ProximityDetectionService.getAvailablePorts();
    }
}