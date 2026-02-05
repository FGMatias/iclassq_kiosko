package org.iclassq.accessibility.camera;

import org.iclassq.KioskoApplication;
import org.iclassq.accessibility.DisabilityDetector;
import org.iclassq.config.ServiceFactory;
import org.iclassq.model.domain.SessionData;
import org.iclassq.model.dto.response.HorarioDTO;
import org.iclassq.service.HorarioService;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

public class SmartCameraSchedulerDynamic {
    private static final Logger logger = Logger.getLogger(SmartCameraSchedulerDynamic.class.getName());

    private LocalTime activeStartTime = LocalTime.of(6, 0);
    private LocalTime activeEndTime = LocalTime.of(22, 0);

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

        schedulerRunning = true;

        loadHorarios();
        checkAndAdjustCameraState();

        scheduler.scheduleAtFixedRate(
                this::checkAndAdjustCameraState,
                1,
                1,
                TimeUnit.HOURS
        );

        scheduler.scheduleAtFixedRate(
                this::loadHorarios,
                6,
                6,
                TimeUnit.HOURS
        );
    }

    private void loadHorarios() {
        try {
            logger.info("Cargando horarios desde base de datos...");

            SessionData session = SessionData.getInstance();
            Integer idRol = session.getRolEquipoId();

            if (idRol == null) {
                logger.warning("No hay idRol en sesión, usando horarios actuales");
                return;
            }

            HorarioDTO horarios = horarioService.getHorarios(idRol);

            if (horarios == null) {
                logger.warning("No se obtuvieron horarios, usando valores actuales");
                return;
            }

            LocalTime newStartTime = horarios.getHoraInicioAsLocalTime();
            LocalTime newEndTime = horarios.getHoraFinAsLocalTime();

            if (!newStartTime.equals(activeStartTime) || !newEndTime.equals(activeEndTime)) {
                logger.info("Horarios actualizados desde base de datos");
                logger.info(String.format("   Anterior: %s - %s",
                        activeStartTime, activeEndTime));
                logger.info(String.format("   Nuevo:    %s - %s",
                        newStartTime, newEndTime));

                activeStartTime = newStartTime;
                activeEndTime = newEndTime;

                checkAndAdjustCameraState();
            } else {
                logger.info(String.format("Horarios confirmados: %s - %s (sin cambios)",
                        activeStartTime, activeEndTime));
            }

        } catch (Exception e) {
            logger.severe("Error al cargar horarios desde BD: " + e.getMessage());
            logger.warning(String.format("   Usando horarios actuales: %s - %s",
                    activeStartTime, activeEndTime));
            e.printStackTrace();
        }
    }

    private void checkAndAdjustCameraState() {
        try {
            LocalDateTime now = LocalDateTime.now();
            LocalTime currentTime = now.toLocalTime();
            DayOfWeek currentDay = now.getDayOfWeek();

            boolean shouldBeActive = isActiveTime(currentTime, currentDay);

            logger.fine(String.format("Verificación horario: %s %s - Estado deseado: %s",
                    currentDay, currentTime, shouldBeActive ? "ACTIVA" : "INACTIVA"));

            if (shouldBeActive && !cameraActive) {
                activateCamera(currentTime);
            } else if (!shouldBeActive && cameraActive) {
                deactivateCamera(currentTime);
            } else {
                logger.fine(String.format("Cámara ya está en estado correcto: %s",
                        cameraActive ? "ACTIVA" : "INACTIVA"));
            }

        } catch (Exception e) {
            logger.severe("Error en verificación de horario: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private boolean isActiveTime(LocalTime currentTime, DayOfWeek day) {
        return !currentTime.isBefore(activeStartTime) && currentTime.isBefore(activeEndTime);
    }

    private void activateCamera(LocalTime currentTime) {
        logger.info("Activando camara - Horario Activo");
        logger.info(String.format("   Hora actual: %s", currentTime));
        logger.info(String.format("   Horario desde BD: %s - %s",
                activeStartTime, activeEndTime));

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

    private void deactivateCamera(LocalTime currentTime) {
        logger.info("Desactivando camara - Horario Inactivo");
        logger.info(String.format("   Hora actual: %s", currentTime));
        logger.info("   Razón: Fuera del horario de atención");

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
        loadHorarios();
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
            deactivateCamera(LocalTime.now());
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