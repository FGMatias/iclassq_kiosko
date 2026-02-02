package org.iclassq.navigation;

import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.iclassq.accessibility.AccessibilityManager;
import org.iclassq.accessibility.adapter.ProximityDetectionAdapter;
import org.iclassq.controller.*;
import org.iclassq.model.domain.SessionData;
import org.iclassq.model.dto.response.TicketResponseDTO;
import org.iclassq.util.voice.VoiceAssistant;
import org.iclassq.view.*;

import java.util.Stack;
import java.util.logging.Logger;

public class Navigator {
    private static final Logger logger = Logger.getLogger(Navigator.class.getName());

    private static final int TTS_CLEANUP_DELAY_MS = 200;
    private static Stage primaryStage;
    private static Scene mainScene;
    private static Stack<Parent> history = new Stack<>();
    private static Object currentController;

    public static void init(Stage stage) {
        primaryStage = stage;
    }

    public static void navigateTo(Parent view) {
        if (Platform.isFxApplicationThread()) {
            changeScene(view);
        } else {
            Platform.runLater(() -> changeScene(view));
        }
    }

    private static void changeScene(Parent view) {
        if (mainScene == null) {
            mainScene = new Scene(view);
            primaryStage.setScene(mainScene);
        } else {
            mainScene.setRoot(view);
        }
        history.push(view);
    }

    public static void navigateBack() {
        Platform.runLater(() -> {
            if (history.size() > 1) {
                history.pop();
                Parent prevView = history.peek();

                if (mainScene != null) {
                    mainScene.setRoot(prevView);
                }
            }
        });
    }

    public static void reboot() {
        cleanupCurrentController();

        SessionData.getInstance().limpiarFlujoAtencion();
        history.clear();
        navigateToIdentification();
    }

    public static void clearHistory() {
        history.clear();
    }

    public static void navigateToLogin() {
        cleanupCurrentController();

        LoginView view = new LoginView();
        LoginController controller = new LoginController(view);

        currentController = controller;

        navigateTo(view);
    }

    public static void navigateToIdentification() {
        cleanupCurrentController();
        waitForTTSCleanup();
        resetProximitySystem();

        IdentificationView view = new IdentificationView();
        IdentificationController controller = new IdentificationController(view);

        currentController = controller;

        navigateTo(view);
    }

    public static void navigateToGroups() {
        cleanupCurrentController();
        waitForTTSCleanup();

        GruposView view = new GruposView();
        GruposController controller = new GruposController(view);

        currentController = controller;

        navigateTo(view.getRoot());
    }

    public static void navigateToSubGroups() {
        cleanupCurrentController();
        waitForTTSCleanup();

        SubGruposView view = new SubGruposView();
        SubGruposController controller = new SubGruposController(view);

        currentController = controller;

        navigateTo(view.getRoot());
    }

    public static void navigatoToTicket(TicketResponseDTO ticket) {
        cleanupCurrentController();
        waitForTTSCleanup();

        TicketView view = new TicketView(ticket);
        TicketController controller = new TicketController(view);

        currentController = controller;

        view.setOnClose(() -> {
            navigateToIdentification();
        });

        navigateTo(view.getRoot());

        PauseTransition delay = new PauseTransition(Duration.seconds(5));
        delay.setOnFinished(evt -> navigateToIdentification());
        delay.play();
    }

    private static void waitForTTSCleanup() {
        try {
            VoiceAssistant voice = AccessibilityManager.getInstance().getVoiceAssistant();
            if (voice != null && voice.wasRecentlyStopped()) {
                logger.fine("Esperando " + TTS_CLEANUP_DELAY_MS + "ms para limpieza de TTS");
                Thread.sleep(TTS_CLEANUP_DELAY_MS);
            }
        } catch (InterruptedException e) {
            logger.warning("Delay de TTS interrumpido: " + e.getMessage());
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            logger.fine("Error al verificar estado TTS: " + e.getMessage());
        }
    }

    private static void resetProximitySystem() {
        try {
            ProximityDetectionAdapter proximityAdapter = ProximityDetectionAdapter.getInstance();
            proximityAdapter.reset();

            logger.info("Sistema de proximidad reseteado - Listo para siguiente usuario");
        } catch (Exception e) {
            logger.warning("Error al resetear proximidad: " + e.getMessage());
        }
    }

    private static void stopAllVoice() {
        try {
            VoiceAssistant voice = AccessibilityManager.getInstance().getVoiceAssistant();
            if (voice != null && voice.isActive()) {
                voice.stopSpeaking();
                logger.info("Voz detenida antes de cambio de vista");
            }
        } catch (Exception e) {
            logger.warning("Error al detener voz: " + e.getMessage());
        }
    }

    private static void cleanupCurrentController() {
        stopAllVoice();

        if (currentController == null) {
            return;
        }

        try {
            if (currentController instanceof IdentificationController) {
                ((IdentificationController) currentController).cleanup();
            } else if (currentController instanceof GruposController) {
                ((GruposController) currentController).shutdown();
            } else if (currentController instanceof SubGruposController) {
                ((SubGruposController) currentController).shutdown();
            }
        } catch (Exception e) {
            logger.warning("Error al limpiar controller: " + e.getMessage());
        }
    }
}