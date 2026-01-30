package org.iclassq.controller;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import org.iclassq.accessibility.AccessibilityInitializer;
import org.iclassq.config.ServiceFactory;
import org.iclassq.model.dto.request.LoginRequestDTO;
import org.iclassq.model.dto.response.LoginResponseDTO;
import org.iclassq.navigation.Navigator;
import org.iclassq.service.AuthService;
import org.iclassq.util.Constants;
import org.iclassq.util.OnScreenKeyboard;
import org.iclassq.view.LoginView;

import java.util.logging.Logger;

public class LoginController {
    private final Logger logger = Logger.getLogger(LoginController.class.getName());
    private final LoginView view;
    private final AuthService authService;

    public LoginController(LoginView view) {
        this.view = view;
        this.authService = ServiceFactory.getAuthService();

        OnScreenKeyboard.show();
        view.setOnLogin(this::handleLogin);
    }

    private void handleLogin() {
        String username = view.getUsername().getText();
        String password = view.getPassword().getText();

        if (username.isEmpty() || password.isEmpty()) {
            showError("Por favor ingrese usuario y contraseña");
            return;
        }

        view.getBtnLogin().setDisable(true);

        new Thread(() -> {
            try {
                LoginRequestDTO request = new LoginRequestDTO(username, password, Constants.ROL_KIOSKO);
                LoginResponseDTO response = authService.login(request);

                Platform.runLater(() -> {
                    if (response.isSuccess()) {
                        logger.info("Autenticación exitosa");
                        view.showLoading(
                                "Preparando sistema",
                                "Esto tomará solo unos segundos..."
                        );
                        OnScreenKeyboard.hide();

                        new Thread(() -> {
                            try {
                                logger.info("Iniciando inicialización de sistemas de accesibilidad...");
                                AccessibilityInitializer.initializer();

                                logger.info("Inicialización completada exitosamente");
                                Platform.runLater(() -> {
                                    view.hideLoading();
                                    Navigator.navigateToIdentification();
                                });
                            } catch (Exception e) {
                                logger.severe("Error en inicialización: " + e.getMessage());
                                e.printStackTrace();

                                Platform.runLater(() -> {
                                    view.hideLoading();
                                    showError("Error al iniciar el sistema: " + e.getMessage());
                                    view.getBtnLogin().setDisable(false);
                                });
                            }
                        }, "AccessibilityInitThread").start();
                    } else {
                        logger.warning("Autenticación fallida: " + response.getMessage());
                        view.getBtnLogin().setDisable(false);
                        showError(response.getMessage());
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> {
                    view.getBtnLogin().setDisable(false);
                    showError("Error de conexión: " + e.getMessage());
                });
            }
        }, "AuthenticationThread").start();
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error de Autenticación");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
