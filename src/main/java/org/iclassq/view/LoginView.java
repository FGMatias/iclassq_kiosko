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
import org.iclassq.util.Fonts;
import org.iclassq.view.components.Loading;

public class LoginView extends StackPane {
    private TextField username;
    private PasswordField password;
    private Button btnLogin;

    private Runnable onLogin;

    private Loading loading;

    public LoginView() {
        init();
        setupEventHandler();
        createLoading();
    }

    private void setupEventHandler() {
        btnLogin.setOnAction(evt -> {
            if (onLogin != null) onLogin.run();
        });
    }

    private void init() {
        HBox container = new HBox(0);
        container.setPadding(new Insets(25));
        container.getStyleClass().add(Styles.BG_ACCENT_EMPHASIS);

        VBox leftPanel = createLeftPanel();
        VBox rightPanel = createRightPanel();

        leftPanel.prefWidthProperty().bind(container.widthProperty().divide(1.5));
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
        illustration.setImage(new Image(getClass().getResourceAsStream("/images/login.png")));
        illustration.setPreserveRatio(true);
        illustration.fitHeightProperty().bind(leftPanel.heightProperty().multiply(0.6));

        Label description = new Label("Sistema de Gestión de Turnos ICLASSQ");
        description.setFont(Fonts.bold(30));
        description.setAlignment(Pos.CENTER);
        description.setStyle(
                    "-fx-text-fill: white; " +
                    "-fx-text-alignment: center;"
        );
        description.setWrapText(true);
        description.setMaxWidth(500);
        description.setAlignment(Pos.CENTER);

        leftPanel.getChildren().addAll(illustration, description);

        return leftPanel;
    }

    private VBox createRightPanel() {
        VBox rightPanel = new VBox(30);
        rightPanel.setAlignment(Pos.CENTER);
        rightPanel.setPadding(new Insets(60));
        rightPanel.setStyle(
                "-fx-background-color: -color-light;" +
                "-fx-background-radius: 30; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 20, 0, 0, 5);"
        );

        VBox form = formLogin();
        rightPanel.getChildren().add(form);

        return rightPanel;
    }

    private VBox formLogin() {
        VBox container = new VBox(40);
        container.setAlignment(Pos.CENTER);

        VBox header = new VBox(20);
        Label title = new Label("Bienvenidos");
        title.getStyleClass().add(Styles.TITLE_1);
        title.setFont(Fonts.bold(48));
        title.setAlignment(Pos.CENTER_LEFT);

        Label subTitle = new Label("Ingrese sus credenciales para iniciar sesión");
        subTitle.getStyleClass().add(Styles.TEXT_SUBTLE);
        subTitle.setFont(Fonts.regular(20));
        subTitle.setWrapText(true);
        subTitle.setAlignment(Pos.CENTER_LEFT);

        header.getChildren().addAll(title, subTitle);

        VBox usernameBox = new VBox(15);
        Label usernameLabel = new Label("Usuario");
        usernameLabel.setFont(Fonts.regular(24));

        username = new TextField();
        username.setPromptText("Ingrese el nombre de usuario");
        username.setFont(Fonts.regular(20));
        username.setPrefHeight(55);
        username.getStyleClass().add(Styles.BG_DEFAULT);

        usernameBox.getChildren().addAll(usernameLabel, username);

        VBox passwordBox = new VBox(15);
        Label passwordLabel = new Label("Contraseña");
        passwordLabel.setFont(Fonts.regular(24));

        password = new PasswordField();
        password.setPromptText("Ingrese su contraseña");
        password.setFont(Fonts.regular(20));
        password.setPrefHeight(55);
        password.getStyleClass().add(Styles.LARGE);

        passwordBox.getChildren().addAll(passwordLabel, password);

        btnLogin = new Button("INICIAR SESIÓN");
        btnLogin.setPrefHeight(55);
        btnLogin.setMaxWidth(Double.MAX_VALUE);
        btnLogin.getStyleClass().addAll(Styles.LARGE, Styles.ACCENT);
        btnLogin.setFont(Fonts.bold(24));
        btnLogin.setStyle("-fx-background-radius: 10px;");

        container.getChildren().addAll(
                header,
                usernameBox,
                passwordBox,
                btnLogin
        );
        return container;
    }

    private void createLoading() {
        loading = new Loading(
                "Preparando sistema",
                "Esto tomará solo unos segundos..."
        );
        loading.setSpinnerSize(60);
        loading.hide();
        getChildren().add(loading);
    }

    public void showLoading() {
        loading.setMessages(
                "Preparando sistema",
                "Esto tomará solo unos segundos..."
        );
        loading.show();
        loading.toFront();

        btnLogin.setDisable(true);
        username.setDisable(true);
        password.setDisable(true);
    }

    public void showLoading(String message, String subMessage) {
        loading.setMessages(message, subMessage);
        loading.show();
        loading.toFront();
        btnLogin.setDisable(true);
        username.setDisable(true);
        password.setDisable(true);
    }

    public void hideLoading() {
        loading.hide();
        btnLogin.setDisable(false);
        username.setDisable(false);
        password.setDisable(false);
    }

    public TextField getUsername() {
        return username;
    }

    public PasswordField getPassword() {
        return password;
    }

    public Button getBtnLogin() {
        return btnLogin;
    }

    public void setOnLogin(Runnable callback) {
        this.onLogin = callback;
    }
}
