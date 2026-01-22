package org.iclassq.accessibility.camera;

import org.iclassq.KioskoApplication;
import org.iclassq.accessibility.DisabilityDetector;
import org.iclassq.config.ServiceFactory;
import org.iclassq.model.domain.SessionData;
import org.iclassq.model.dto.response.HorarioDTO;
import org.iclassq.service.HorarioService;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

public class SmartCameraSchedulerDynamic {

    private static final Logger logger = Logger.getLogger(SmartCameraSchedulerDynamic.class.getName());

    private int activeStartHour = 6;
    private int activeEndHour = 22;

    private final ScheduledExecutorService scheduler;
    private final HorarioService horarioService;
    private boolean cameraActive = false;
    private boolean schedulerRunning = false;

    public SmartCameraSchedulerDynamic() {
        this.scheduler = Executors.newScheduledThreadPool(1);
        this.horarioService = ServiceFactory.getHorarioService();
    }

    public void start() {
        if (schedulerRunning) {
            logger.warning("Scheduler ya está corriendo");
            return;
        }

        logger.info("═══════════════════════════════════════════");
        logger.info("  SMART CAMERA SCHEDULER - DINÁMICO");
        logger.info("═══════════════════════════════════════════");

        schedulerRunning = true;

        loadHorariosFromBD();

        logger.info(String.format("  Horario actual: %02d:00 - %02d:00",
                activeStartHour, activeEndHour));
        logger.info("  Fuente: Base de datos (tbl_grupo)");
        logger.info("  Verificación estado: Cada hora");
        logger.info("  Actualización horarios: Cada 6 horas");
        logger.info("═══════════════════════════════════════════");

        checkAndAdjustCameraState();

        // 3. Verificar cámara cada hora
        scheduler.scheduleAtFixedRate(
                this::checkAndAdjustCameraState,
                1,
                1,
                TimeUnit.HOURS
        );

        scheduler.scheduleAtFixedRate(
                this::loadHorariosFromBD,
                6,
                6,
                TimeUnit.HOURS
        );
    }

    private void loadHorariosFromBD() {
        try {
            logger.info("Cargando horarios desde base de datos...");

            SessionData session = SessionData.getInstance();
            Integer idRol = session.getRolEquipoId();

            if (idRol == null) {
                logger.warning("No hay idRol en sesión, usando horarios por defecto");
                return;
            }

            HorarioDTO horarios = horarioService.getHorarios(idRol);

            if (horarios == null) {
                logger.warning("⚠️ No se obtuvieron horarios, usando valores por defecto");
                return;
            }

            int newStartHour = horarios.getHoraInicioAsInt();
            int newEndHour = horarios.getHoraFinAsInt();

            if (newStartHour != activeStartHour || newEndHour != activeEndHour) {
                logger.info("═══════════════════════════════════════════");
                logger.info("HORARIOS ACTUALIZADOS DESDE BD");
                logger.info(String.format("   Anterior: %02d:00 - %02d:00",
                        activeStartHour, activeEndHour));
                logger.info(String.format("   Nuevo:    %02d:00 - %02d:00",
                        newStartHour, newEndHour));
                logger.info("═══════════════════════════════════════════");

                activeStartHour = newStartHour;
                activeEndHour = newEndHour;

                checkAndAdjustCameraState();
            } else {
                logger.info(String.format("Horarios confirmados: %02d:00 - %02d:00 (sin cambios)",
                        activeStartHour, activeEndHour));
            }

        } catch (Exception e) {
            logger.severe("Error al cargar horarios desde BD: " + e.getMessage());
            logger.warning(String.format("   Usando horarios actuales: %02d:00 - %02d:00",
                    activeStartHour, activeEndHour));
            e.printStackTrace();
        }
    }

    private void checkAndAdjustCameraState() {
        try {
            LocalDateTime now = LocalDateTime.now();
            int currentHour = now.getHour();
            DayOfWeek currentDay = now.getDayOfWeek();

            boolean shouldBeActive = isActiveHours(currentHour, currentDay);

            logger.fine(String.format("Verificación horario: %s %02d:00 - Estado deseado: %s",
                    currentDay, currentHour, shouldBeActive ? "ACTIVA" : "INACTIVA"));

            if (shouldBeActive && !cameraActive) {
                activateCamera(currentHour);
            } else if (!shouldBeActive && cameraActive) {
                deactivateCamera(currentHour);
            } else {
                logger.fine(String.format("Cámara ya está en estado correcto: %s",
                        cameraActive ? "ACTIVA" : "INACTIVA"));
            }

        } catch (Exception e) {
            logger.severe("Error en verificación de horario: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private boolean isActiveHours(int hour, DayOfWeek day) {
        return hour >= activeStartHour && hour < activeEndHour;
    }

    private void activateCamera(int currentHour) {
        logger.info("═══════════════════════════════════════════");
        logger.info("ACTIVANDO CÁMARA - Horario Activo");
        logger.info(String.format("   Hora actual: %02d:00", currentHour));
        logger.info(String.format("   Horario desde BD: %02d:00 - %02d:00",
                activeStartHour, activeEndHour));
        logger.info("═══════════════════════════════════════════");

        try {
            DisabilityDetector detector = new DisabilityDetector();
            boolean initialized = detector.initialize();

            if (initialized) {
                KioskoApplication.setDisabilityDetector(detector);
                cameraActive = true;

                logger.info("Cámara activada exitosamente");
                logger.info("   Sistema listo para detectar pacientes");
            } else {
                logger.severe("Error al activar cámara");
                cameraActive = false;
            }

        } catch (Exception e) {
            logger.severe("Error al activar cámara: " + e.getMessage());
            e.printStackTrace();
            cameraActive = false;
        }
    }

    private void deactivateCamera(int currentHour) {
        logger.info("═══════════════════════════════════════════");
        logger.info("DESACTIVANDO CÁMARA - Horario Inactivo");
        logger.info(String.format("   Hora actual: %02d:00", currentHour));
        logger.info("   Razón: Fuera del horario de atención");
        logger.info("═══════════════════════════════════════════");

        try {
            DisabilityDetector detector = KioskoApplication.getDisabilityDetector();

            if (detector != null) {
                detector.shutdown();
                KioskoApplication.setDisabilityDetector(null);

                logger.info("Cámara desactivada exitosamente");
                logger.info("   Recursos liberados");
            } else {
                logger.warning("No hay detector activo para desactivar");
            }

            cameraActive = false;

        } catch (Exception e) {
            logger.severe("Error al desactivar cámara: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void reloadHorarios() {
        logger.info("Recarga manual de horarios solicitada");
        loadHorariosFromBD();
    }

    public String getHorariosActuales() {
        return String.format("%02d:00 - %02d:00", activeStartHour, activeEndHour);
    }

    public void printStatus() {
        LocalDateTime now = LocalDateTime.now();

        logger.info("═══════════════════════════════════════════");
        logger.info("  SMART CAMERA SCHEDULER - STATUS");
        logger.info("═══════════════════════════════════════════");
        logger.info(String.format("  Fecha/Hora: %s %02d:%02d",
                now.getDayOfWeek(), now.getHour(), now.getMinute()));
        logger.info(String.format("  Horario desde BD: %02d:00 - %02d:00",
                activeStartHour, activeEndHour));
        logger.info(String.format("  Estado actual: %s",
                cameraActive ? "ACTIVA 💡" : "INACTIVA 🔴"));
        logger.info(String.format("  Debe estar activa: %s",
                isActiveHours(now.getHour(), now.getDayOfWeek()) ? "SÍ" : "NO"));
        logger.info(String.format("  Scheduler corriendo: %s",
                schedulerRunning ? "SÍ" : "NO"));
        logger.info("  Fuente horarios: Base de datos (tbl_grupo)");
        logger.info("═══════════════════════════════════════════");
    }

    public void shutdown() {
        logger.info("Deteniendo Smart Camera Scheduler...");

        schedulerRunning = false;

        if (scheduler != null && !scheduler.isShutdown()) {
            scheduler.shutdown();

            try {
                if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                    scheduler.shutdownNow();
                }
            } catch (InterruptedException e) {
                scheduler.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }

        if (cameraActive) {
            deactivateCamera(LocalDateTime.now().getHour());
        }

        logger.info("Smart Camera Scheduler detenido");
    }

    public boolean isCameraActive() {
        return cameraActive;
    }

    public boolean isSchedulerRunning() {
        return schedulerRunning;
    }
}