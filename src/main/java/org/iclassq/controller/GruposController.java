package org.iclassq.controller;

import javafx.application.Platform;
import org.iclassq.config.ServiceFactory;
import org.iclassq.model.domain.SessionData;
import org.iclassq.model.dto.response.GrupoDTO;
import org.iclassq.navigation.Navigator;
import org.iclassq.service.GrupoService;
import org.iclassq.view.GruposView;
import org.iclassq.view.components.Message;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.logging.Logger;

public class GruposController {
    private final GruposView view;
    private final GrupoService grupoService;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private Future<?> currentTask;
    private final Logger logger = Logger.getLogger(GruposController.class.getName());

    public GruposController(GruposView view) {
        this.view = view;
        this.grupoService = ServiceFactory.getGrupoService();
        view.setOnGroupSelected(this::handleGrupoSelected);
        loadGroups();
    }

    private void loadGroups() {
        if (currentTask != null && !currentTask.isDone()) {
            currentTask.cancel(true);
        }

        view.showLoading();

        currentTask = executor.submit(() -> {
            try {
                SessionData session = SessionData.getInstance();
                Integer rolEquipoId = session.getRolEquipoId();

                List<GrupoDTO> groups = grupoService.getAllByTime(rolEquipoId);

                Platform.runLater(() -> {
                    view.hideLoading();
                    view.setGroups(groups);
                });

            } catch (IOException e) {
                logger.severe("Error de conexión al cargar grupos: " + e.getMessage());
                Platform.runLater(() -> {
                    view.hideLoading();
                    Message.showError(
                            "Error de Conexión",
                            "No se pudo conectar con el servidor. Verifique su conexión e intente nuevamente."
                    );
                    Navigator.navigateBack();
                });
            } catch (Exception e) {
                logger.severe("Error inesperado al cargar grupos: " + e.getMessage());
                Platform.runLater(() -> {
                    view.hideLoading();
                    Message.showError(
                            "Error Inesperado",
                            "Ocurrió un error al cargar los grupos: " + e.getClass().getSimpleName()
                    );
                    Navigator.navigateBack();
                });
            }
        });
    }

    private void handleGrupoSelected(GrupoDTO grupo) {
        try {
            SessionData.getInstance().setGrupo(grupo);
            Navigator.navigateToSubGroups();
        } catch (Exception e) {
            logger.severe("Error al seleccionar grupo: " + e.getMessage());
            Message.showError(
                    "Error",
                    "No se pudo seleccionar el grupo. Por favor intente nuevamente."
            );
        }
    }

    public void shutdown() {
        if (currentTask != null && !currentTask.isDone()) {
            currentTask.cancel(true);
        }
        executor.shutdown();
    }
}