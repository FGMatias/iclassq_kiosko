package org.iclassq.view.components;

import atlantafx.base.util.Animations;
import javafx.animation.PauseTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import org.iclassq.model.enums.MessageType;
import org.kordamp.ikonli.Ikon;
import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.material2.Material2OutlinedAL;
import org.kordamp.ikonli.material2.Material2OutlinedMZ;

import java.util.Map;
import java.util.logging.Logger;

public class Message {
    private static StackPane messageContainer;
    private static final double DEFAULT_DURATION = 3.0;
    private static final Logger logger = Logger.getLogger(Message.class.getName());

    public static void initialize(StackPane container) {
        messageContainer = container;
        messageContainer.setPickOnBounds(false);
    }

    public static void showSuccess(String title, String content) {
        show(
                title,
                content,
                Material2OutlinedAL.CHECK_CIRCLE_OUTLINE,
                MessageType.SUCCESS
        );
    }

    public static void showInformation(String title, String content) {
        show(
                title,
                content,
                Material2OutlinedAL.HELP_OUTLINE,
                MessageType.INFORMATION
        );
    }

    public static void showWarning(String title, String content) {
        show(
                title,
                content,
                Material2OutlinedMZ.WARNING,
                MessageType.WARNING
        );
    }

    public static void showError(String title, String content) {
        show(
                title,
                content,
                Material2OutlinedAL.ERROR_OUTLINE,
                MessageType.ERROR
        );
    }

    public static void showValidationErrors(Map<String, String> errors) {
        if (errors == null || errors.isEmpty()) return;

        if (errors.size() == 1) {
            Map.Entry<String, String> entry = errors.entrySet()
                    .iterator()
                    .next();
            showWarning("Error de validación", entry.getValue());
            return;
        }

        StringBuilder message = new StringBuilder();
        int count = 0;

        for (String error : errors.values()) {
            if (count > 0) {
                message.append("\n");
            }

            message.append("- ").append(error);
            count++;

            if (count >= 5) {
                int remaining = errors.size() - 5;
                if (remaining > 0) {
                    message.append("\n... y ").append(remaining).append(" error(es) más");
                }
                break;
            }
        }

        showWarning("Errores de Validación (" + errors.size() + ")", message.toString());
    }

    public static void showValidationErrors(String title, Map<String, String> errors) {
        if (errors == null || errors.isEmpty()) return;

        if (errors.size() == 1) {
            Map.Entry<String, String> entry = errors.entrySet().iterator().next();
            showWarning(title, entry.getValue());
            return;
        }

        StringBuilder message = new StringBuilder();
        int count = 0;

        for (String error : errors.values()) {
            if (count > 0) {
                message.append("\n");
            }

            message.append("- ").append(error);
            count++;

            if (count >= 3) {
                int remaining = errors.size() - 3;
                if (remaining > 0) {
                    message.append("\n... y ").append(remaining).append(" más");
                }
                break;
            }
        }

        showWarning(title, message.toString());
    }

    public static void showValidationInfo(String title, String content) {
        showInformation(title, content);
    }

    private static void show(
            String title,
            String content,
            Ikon icon,
            MessageType messageType
    ) {
        if (messageContainer == null) {
            logger.severe("Container de notificacion no inicializado");
            return;
        }

        atlantafx.base.controls.Message message = new atlantafx.base.controls.Message(
                title,
                content,
                new FontIcon(icon)
        );
        message.getStyleClass().add(messageType.getStyleClass());

        VBox wrapper = new VBox(message);
        wrapper.setMaxWidth(400);
        wrapper.setMaxHeight(150);
        wrapper.setPrefWidth(400);
        wrapper.setPrefHeight(Region.USE_COMPUTED_SIZE);
        wrapper.setAlignment(Pos.TOP_RIGHT);

        VBox.setVgrow(message, Priority.NEVER);
        message.setMaxWidth(400);
        message.setMaxHeight(150);

        StackPane.setAlignment(wrapper, Pos.TOP_RIGHT);
        StackPane.setMargin(wrapper, new Insets(20, 20, 0, 0));

        messageContainer.getChildren().add(wrapper);

        Animations.slideInDown(wrapper, Duration.millis(300)).playFromStart();

        PauseTransition autoClose = new PauseTransition(Duration.seconds(DEFAULT_DURATION));

        message.setOnClose(evt -> {
            autoClose.stop();
            var slideOut = Animations.slideOutUp(wrapper, Duration.millis(250));
            slideOut.setOnFinished(e -> messageContainer.getChildren().remove(wrapper));
            slideOut.playFromStart();
        });

        autoClose.setOnFinished(e -> {
            var slideOut = Animations.slideOutUp(wrapper, Duration.millis(250));
            slideOut.setOnFinished(ev -> messageContainer.getChildren().remove(wrapper));
            slideOut.playFromStart();
        });

        autoClose.play();
    }
}
