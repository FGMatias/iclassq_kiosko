package org.iclassq.navigation;

import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.util.Duration;
import lombok.extern.java.Log;
import org.iclassq.controller.*;
import org.iclassq.model.domain.SessionData;
import org.iclassq.model.dto.response.TicketResponseDTO;
import org.iclassq.view.*;

import java.util.Stack;

public class Navigator {
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

        IdentificationView view = new IdentificationView();
        IdentificationController controller = new IdentificationController(view);

        currentController = controller;

        navigateTo(view);
    }

    public static void navigateToGroups() {
        cleanupCurrentController();

        GruposView view = new GruposView();
        GruposController controller = new GruposController(view);

        currentController = controller;

        navigateTo(view.getRoot());
    }

    public static void navigateToSubGroups() {
        cleanupCurrentController();

        SubGruposView view = new SubGruposView();
        SubGruposController controller = new SubGruposController(view);

        currentController = controller;

        navigateTo(view.getRoot());
    }

    public static void navigatoToTicket(TicketResponseDTO ticket) {
        cleanupCurrentController();

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

    private static void cleanupCurrentController() {
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
            System.err.println("Error al limpiar controller: " + e.getMessage());
        }
    }
}
