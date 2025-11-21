package org.iclassq.view;

import atlantafx.base.theme.Styles;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;

public class LoginView extends StackPane {
    private TextField username;
    private PasswordField password;
    private Button login;

    public LoginView() {
        init();
    }

    private void init() {
        HBox container = new HBox(0);
        container.setPadding(new Insets(25));
        container.getStyleClass().add(Styles.BG_ACCENT_EMPHASIS);

        VBox leftPanel = createLeftPanel();
        VBox rightPanel = createRightPanel();

        leftPanel.prefWidthProperty().bind(container.widthProperty().divide(2));
        rightPanel.prefWidthProperty().bind(container.widthProperty().divide(2));

        leftPanel.prefHeightProperty().bind(container.heightProperty());
        rightPanel.prefHeightProperty().bind(container.heightProperty());

        container.getChildren().addAll(leftPanel, rightPanel);

        getChildren().add(container);
    }

    private VBox createLeftPanel() {
        VBox leftPanel = new VBox(40);
        leftPanel.setAlignment(Pos.CENTER);
        leftPanel.setPadding(new Insets(60));

        ImageView illustration = new ImageView();
        try {
            illustration.setImage(new Image(getClass().getResourceAsStream("/images/login.svg")));
            illustration.setPreserveRatio(true);
            // La imagen ocupará máximo el 60% del alto del panel
            illustration.fitHeightProperty().bind(leftPanel.heightProperty().multiply(0.6));
        } catch (Exception e) {
            System.out.println("Ilustración no encontrada");
            // Placeholder si no hay imagen
            Label placeholder = new Label("🏥");
            placeholder.setStyle("-fx-font-size: 120px;");
            leftPanel.getChildren().add(placeholder);
        }

        // Texto descriptivo
        Label descriptionLabel = new Label("Sistema de Gestión de Turnos ICLASSQ");
        descriptionLabel.setStyle(
                "-fx-font-size: 28px; " +
                        "-fx-font-weight: bold; " +
                        "-fx-text-fill: white; " +
                        "-fx-text-alignment: center;"
        );
        descriptionLabel.setWrapText(true);
        descriptionLabel.setMaxWidth(500);
        descriptionLabel.setAlignment(Pos.CENTER);

        leftPanel.getChildren().addAll(illustration, descriptionLabel);

        return leftPanel;
    }

    private VBox createRightPanel() {
        VBox rightPanel = new VBox(30);
        rightPanel.setAlignment(Pos.CENTER);
        rightPanel.setPadding(new Insets(60));
        rightPanel.setStyle(
                "-fx-background-color: -color-light; " +
                        "-fx-background-radius: 10;"
        );

        Label titleLabel = new Label("Ingrese sus credenciales para iniciar sesión");
        titleLabel.setStyle("-fx-font-size: 18px; -fx-text-fill: #4B5563; -fx-font-weight: 500;");
        titleLabel.setWrapText(true);
        titleLabel.setMaxWidth(400);
        titleLabel.setAlignment(Pos.CENTER);

        VBox formContainer = new VBox(20);
        formContainer.setAlignment(Pos.CENTER);
        formContainer.setMaxWidth(400);

        username = new TextField();
        username.setPromptText("Usuario");
        username.setPrefHeight(55);
        username.setStyle(
                "-fx-background-color: #F3F4F6; " +
                        "-fx-background-radius: 8px; " +
                        "-fx-font-size: 16px; " +
                        "-fx-padding: 0 20px;"
        );

        password = new PasswordField();
        password.setPromptText("Contraseña");
        password.setPrefHeight(55);
        password.setStyle(
                "-fx-background-color: #F3F4F6; " +
                        "-fx-background-radius: 8px; " +
                        "-fx-font-size: 16px; " +
                        "-fx-padding: 0 20px;"
        );

        HBox optionsBox = new HBox();
        optionsBox.setAlignment(Pos.CENTER_LEFT);
        optionsBox.setSpacing(20);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        login = new Button("INICIAR SESIÓN");
        login.setPrefHeight(55);
        login.setMaxWidth(Double.MAX_VALUE);
        login.setStyle(
                "-fx-background-color: #9333EA; " +
                        "-fx-text-fill: white; " +
                        "-fx-font-size: 18px; " +
                        "-fx-font-weight: bold; " +
                        "-fx-background-radius: 8px; " +
                        "-fx-cursor: hand;"
        );

        login.setOnMouseEntered(e ->
                login.setStyle(
                        "-fx-background-color: #7C3AED; " +
                                "-fx-text-fill: white; " +
                                "-fx-font-size: 18px; " +
                                "-fx-font-weight: bold; " +
                                "-fx-background-radius: 8px; " +
                                "-fx-cursor: hand;"
                )
        );
        login.setOnMouseExited(e ->
                login.setStyle(
                        "-fx-background-color: #9333EA; " +
                                "-fx-text-fill: white; " +
                                "-fx-font-size: 18px; " +
                                "-fx-font-weight: bold; " +
                                "-fx-background-radius: 8px; " +
                                "-fx-cursor: hand;"
                )
        );

        formContainer.getChildren().addAll(
                username,
                password,
                login
        );

        rightPanel.getChildren().addAll(
                titleLabel,
                formContainer
        );

        return rightPanel;
|               }

    public TextField getUsername() {
        return username;
    }

    public PasswordField getPassword() {
        return password;
    }

    public Button getLogin() {
        return login;
    }
}
