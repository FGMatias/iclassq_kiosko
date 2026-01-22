package org.iclassq.accessibility;

import org.iclassq.KioskoApplication;
import org.iclassq.accessibility.camera.SmartCameraSchedulerDynamic;
import org.iclassq.config.ServiceFactory;
import org.iclassq.model.domain.SessionData;
import org.iclassq.model.dto.response.HorarioDTO;
import org.iclassq.service.HorarioService;

import java.util.logging.Logger;

/**
 * Inicializador de Sistemas de Accesibilidad
 *
 * Responsabilidad:
 * - Obtener horarios de atención desde la BD
 * - Inicializar el SmartCameraScheduler con esos horarios
 * - El scheduler se encargará de activar/desactivar la cámara periódicamente
 *
 * Se ejecuta UNA VEZ después del login exitoso.
 *
 * @author ICLASSQ Team
 */
public class AccessibilityInitializer {

    private static final Logger logger = Logger.getLogger(AccessibilityInitializer.class.getName());

    public static void initializer() {
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

    private static HorarioDTO obtenerHorarios(Integer rolEquipoId) {
        try {
            logger.info("  Obteniendo horarios desde base de datos...");

            HorarioService horarioService = ServiceFactory.getHorarioService();
            HorarioDTO horarios = horarioService.getHorarios(rolEquipoId);

            if (horarios == null) {
                logger.warning(" API retornó horarios null");
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

            logger.info(" Smart Camera Scheduler inicializado");
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