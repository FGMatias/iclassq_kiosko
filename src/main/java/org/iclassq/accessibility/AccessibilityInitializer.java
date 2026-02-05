package org.iclassq.accessibility;

import org.iclassq.KioskoApplication;
import org.iclassq.accessibility.camera.SmartCameraSchedulerDynamic;
import org.iclassq.accessibility.proximity.ProximityDetector;
import org.iclassq.config.ServiceFactory;
import org.iclassq.model.domain.SessionData;
import org.iclassq.model.dto.response.HorarioDTO;
import org.iclassq.service.HorarioService;

import java.util.logging.Logger;

public class AccessibilityInitializer {

    private static final Logger logger = Logger.getLogger(AccessibilityInitializer.class.getName());

    public static void initialize() {
        logger.info("═══════════════════════════════════════════");
        logger.info("  INICIALIZANDO SISTEMAS DE ACCESIBILIDAD");
        logger.info("═══════════════════════════════════════════");

        try {
            Integer rolEquipoId = SessionData.getInstance().getRolEquipoId();

            if (rolEquipoId == null) {
                logger.severe("rolEquipoId es null después del login");
                logger.severe("Esto no debería ocurrir. Verificar API de autenticación.");
                return;
            }

            logger.info(String.format("  RolEquipoId obtenido: %d", rolEquipoId));

            initializeProximityDetector();

            HorarioDTO horarios = obtenerHorarios(rolEquipoId);

            if (horarios == null) {
                logger.warning("No se pudieron obtener horarios");
                logger.warning("Sistema continuará sin gestión automática de cámara");
                return;
            }

            logger.info(String.format("  Horarios obtenidos: %s - %s",
                    horarios.getHoraInicio(), horarios.getHoraFin()));

            initializeSmartCameraScheduler();

            logger.info("═══════════════════════════════════════════");
            logger.info("  INICIALIZACIÓN COMPLETADA");
            logger.info("  Scheduler verificará horarios periódicamente");
            logger.info("═══════════════════════════════════════════");

        } catch (Exception e) {
            logger.severe("Error al inicializar sistemas de accesibilidad: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void initializeProximityDetector() {
        try {
            logger.info("  Inicializando Proximity Detector...");

            String[] availablePorts = ProximityDetector.getAvailablePorts();

            if (availablePorts.length == 0) {
                logger.warning("  No se encontraron puertos COM");
                logger.warning("  Verifique que el Arduino esté conectado");
                logger.warning("  Sistema continuará sin detección de proximidad");
                return;
            }

            logger.info("     Puertos COM disponibles:");
            for (String port : availablePorts) {
                logger.info("       - " + port);
            }

            ProximityDetector detector = new ProximityDetector();
            boolean initialized = detector.initialize();

            if (initialized) {
                KioskoApplication.setProximityDetector(detector);
                logger.info(" Proximity Detector inicializado correctamente");
                logger.info("     - Arduino conectado y listo");
                logger.info("     - Sistema esperando señales de proximidad");
            } else {
                logger.warning("      ProximityDetector no se pudo inicializar");
                logger.warning("      Verifique:");
                logger.warning("        1. Arduino conectado al USB");
                logger.warning("        2. Drivers CH340 instalados");
                logger.warning("        3. Puerto COM no usado por otra app");
                logger.warning("     Sistema continuará sin detección de proximidad");
            }

        } catch (Exception e) {
            logger.severe("Error al inicializar Proximity Detector: " + e.getMessage());
            logger.severe("   Sistema continuará sin detección de proximidad");
            e.printStackTrace();
        }
    }

    private static HorarioDTO obtenerHorarios(Integer rolEquipoId) {
        try {
            logger.info("  Obteniendo horarios desde base de datos...");

            HorarioService horarioService = ServiceFactory.getHorarioService();
            HorarioDTO horarios = horarioService.getHorarios(rolEquipoId);

            if (horarios == null) {
                logger.warning("  API retornó horarios null");
                return null;
            }

            return horarios;

        } catch (Exception e) {
            logger.severe("Error al obtener horarios: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    private static void initializeSmartCameraScheduler() {
        try {
            logger.info("  Inicializando Smart Camera Scheduler...");

            SmartCameraSchedulerDynamic existingScheduler = KioskoApplication.getCameraScheduler();

            if (existingScheduler != null && existingScheduler.isSchedulerRunning()) {
                logger.info("  Scheduler ya está activo, recargando horarios...");
                existingScheduler.reloadHorarios();
                return;
            }

            SmartCameraSchedulerDynamic scheduler = new SmartCameraSchedulerDynamic();
            scheduler.start();

            KioskoApplication.setCameraScheduler(scheduler);

            logger.info("  Smart Camera Scheduler inicializado");
            logger.info("     - Verificación cada hora");
            logger.info("     - Recarga horarios cada 6 horas");
            logger.info("     - Activación/desactivación automática");

        } catch (Exception e) {
            logger.severe("Error al inicializar scheduler: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void shutdownSystems() {
        logger.info("Desactivando sistemas de accesibilidad...");

        try {
            ProximityDetector proximityDetector = KioskoApplication.getProximityDetector();
            if (proximityDetector != null) {
                proximityDetector.shutdown();
                KioskoApplication.setProximityDetector(null);
                logger.info("ProximityDetector detenido");
            }

            SmartCameraSchedulerDynamic scheduler = KioskoApplication.getCameraScheduler();
            if (scheduler != null) {
                scheduler.shutdown();
                KioskoApplication.setCameraScheduler(null);
                logger.info("Scheduler detenido");
            }

            DisabilityDetector detector = KioskoApplication.getDisabilityDetector();
            if (detector != null) {
                detector.shutdown();
                KioskoApplication.setDisabilityDetector(null);
                logger.info("Detector desactivado");
            }

        } catch (Exception e) {
            logger.warning("Error al desactivar sistemas: " + e.getMessage());
        }
    }
}