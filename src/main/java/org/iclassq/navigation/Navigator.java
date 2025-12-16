package org.iclassq.navigation;

import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.iclassq.controller.GruposController;
import org.iclassq.controller.LoginController;
import org.iclassq.controller.SubGruposController;
import org.iclassq.controller.IdentificationController;
import org.iclassq.model.domain.SessionData;
import org.iclassq.model.dto.response.TicketResponseDTO;
import org.iclassq.view.*;

import java.util.Stack;

public class Navigator {
    private static Stage primaryStage;
    private static Scene mainScene;
    private static Stack<Parent> history = new Stack<>();

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
        SessionData.getInstance().limpiarFlujoAtencion();
        history.clear();
        navigateToIdentification();
    }

    public static void clearHistory() {
        history.clear();
    }

    public static void navigateToLogin() {
        LoginView view = new LoginView();
        new LoginController(view);
        navigateTo(view);
    }

    public static void navigateToIdentification() {
        IdentificationView view = new IdentificationView();
        new IdentificationController(view);
        navigateTo(view);
    }

    public static void navigateToGroups() {
        GruposView view = new GruposView();
        new GruposController(view);
        navigateTo(view.getRoot());
    }

    public static void navigateToSubGroups() {
        SubGruposView view = new SubGruposView();
        new SubGruposController(view);
        navigateTo(view.getRoot());
    }

    public static void navigatoToTicket(TicketResponseDTO ticket) {
        TicketView view = new TicketView(ticket);

        view.setOnClose(() -> {
            navigateToIdentification();
        });

        navigateTo(view.getRoot());

        PauseTransition delay = new PauseTransition(Duration.seconds(5));
        delay.setOnFinished(evt -> navigateToIdentification());
        delay.play();
    }
}
