package org.iclassq.view.components;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import org.iclassq.util.Fonts;

public class Loading extends StackPane {

    private final Label messageLabel;
    private final Label subMessageLabel;
    private final ProgressIndicator spinner;
    private boolean isShowing = false;

    public Loading() {
        this("Cargando...", "Por favor espere");
    }

    public Loading(String message) {
        this(message, "Por favor espere");
    }

    public Loading(String message, String subMessage) {
        this.setStyle("-fx-background-color: rgba(0, 0, 0, 0.6);");

        VBox loadingBox = new VBox(20);
        loadingBox.setAlignment(Pos.CENTER);
        loadingBox.setPadding(new Insets(40));
        loadingBox.setStyle(
                "-fx-background-color: -color-bg-default;" +
                "-fx-background-radius: 15;" +
                "-fx-border-color: -color-border-default;" +
                "-fx-border-width: 1;" +
                "-fx-border-radius: 15;" +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 15, 0, 0, 5);"
        );
        loadingBox.setMaxWidth(400);
        loadingBox.setMaxHeight(280);

        spinner = new ProgressIndicator();
        spinner.setPrefSize(60, 60);
        spinner.setStyle("-fx-progress-color: -color-accent-emphasis;");

        messageLabel = new Label(message);
        messageLabel.setFont(Fonts.bold(20));
        messageLabel.setWrapText(true);
        messageLabel.setAlignment(Pos.CENTER);
        messageLabel.setMaxWidth(350);
        messageLabel.setStyle(
                "-fx-text-alignment: center;"
        );

        subMessageLabel = new Label(subMessage);
        subMessageLabel.setFont(Fonts.regular(14));
        subMessageLabel.setWrapText(true);
        subMessageLabel.setAlignment(Pos.CENTER);
        subMessageLabel.setMaxWidth(350);
        subMessageLabel.setStyle(
                "-fx-text-fill: -color-fg-subtle;" +
                "-fx-text-alignment: center;"
        );

        loadingBox.getChildren().addAll(spinner, messageLabel, subMessageLabel);
        this.getChildren().add(loadingBox);

        this.setVisible(false);
    }

    public void show() {
        this.setVisible(true);
        isShowing = true;
    }

    public void hide() {
        this.setVisible(false);
        isShowing = false;
    }

    public boolean isShowing() {
        return isShowing;
    }

    public void setMessage(String message) {
        messageLabel.setText(message);
    }

    public String getMessage() {
        return messageLabel.getText();
    }

    public String getSubMessage() {
        return subMessageLabel.getText();
    }

    public void setSubMessage(String subMessage) {
        subMessageLabel.setText(subMessage);
    }

    public void setMessages(String message, String subMessage) {
        messageLabel.setText(message);
        subMessageLabel.setText(subMessage);
    }

    public void setSpinnerSize(double size) {
        spinner.setPrefSize(size, size);
    }
}