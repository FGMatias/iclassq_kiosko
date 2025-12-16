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
    private final ProgressIndicator spinner;
    private boolean isShowing = false;

    public Loading() {
        this("Cargando...");
    }

    public Loading(String message) {
        this.setStyle("-fx-background-color: rgba(0, 0, 0, 0.5);");

        VBox loadingBox = new VBox(15);
        loadingBox.setAlignment(Pos.CENTER);
        loadingBox.setPadding(new Insets(30));
        loadingBox.setStyle(
                "-fx-background-color: -color-bg-default;" +
                "-fx-background-radius: 12;" +
                "-fx-border-color: -color-border-default;" +
                "-fx-border-width: 1;" +
                "-fx-border-radius: 12;" +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 15, 0, 0, 5);"
        );
        loadingBox.setMaxWidth(300);

        spinner = new ProgressIndicator();
        spinner.setPrefSize(60, 60);

        messageLabel = new Label(message);
        messageLabel.setFont(Fonts.medium(16));
        messageLabel.setWrapText(true);
        messageLabel.setAlignment(Pos.CENTER);

        loadingBox.getChildren().addAll(spinner, messageLabel);
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

    public void setSpinnerSize(double size) {
        spinner.setPrefSize(size, size);
    }
}