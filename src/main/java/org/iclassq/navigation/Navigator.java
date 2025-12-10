package org.iclassq.navigation;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import org.iclassq.controller.GruposController;
import org.iclassq.controller.LoginController;
import org.iclassq.controller.SubGruposController;
import org.iclassq.controller.TecladoController;
import org.iclassq.model.domain.SessionData;
import org.iclassq.view.GruposView;
import org.iclassq.view.LoginView;
import org.iclassq.view.SubGruposView;
import org.iclassq.view.TecladoView;

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
        navigateToKeyboard();
    }

    public static void clearHistory() {
        history.clear();
    }

    public static void navigateToLogin() {
        LoginView view = new LoginView();
        new LoginController(view);
        navigateTo(view);
    }

    public static void navigateToKeyboard() {
        TecladoView view = new TecladoView();
        new TecladoController(view);
        navigateTo(view);
    }

    public static void navigateToGroups() {
        GruposView view = new GruposView();
        new GruposController(view);
        navigateTo(view);
    }

    public static void navigateToSubGroups() {
        SubGruposView view = new SubGruposView();
        new SubGruposController(view);
//        navigateTo(view);
    }
}
