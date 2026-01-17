package org.iclassq.controller;

import javafx.application.Platform;
import org.iclassq.accessibility.adapter.GruposVoiceAdapter;
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

    private final GruposVoiceAdapter voiceAdapter;

    private boolean isInitialLoad = true;
    private List<GrupoDTO> allGroups;

    public GruposController(GruposView view) {
        this.view = view;
        this.grupoService = ServiceFactory.getGrupoService();

        this.voiceAdapter = new GruposVoiceAdapter(view);

        view.setOnGroupSelected(this::handleGrupoSelected);
        view.setOnNextPage(this::handleNextPage);
        view.setOnPreviousPage(this::handlePreviousPage);
        view.setOnBack(this::handleBack);

        voiceAdapter.registerNavigationCommands(
                view::goToPreviousPage,
                view::goToNextPage,
                this::handleBackVoice
        );

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

                    this.allGroups = groups;

                    voiceAdapter.onGroupsLoaded(groups, this::selectGroupByVoice);

                    isInitialLoad = false;
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

    private void selectGroupByVoice(GrupoDTO grupo) {
        voiceAdapter.onGroupSelectedByVoice(grupo, this::handleGrupoSelected);
    }

    private void handleGrupoSelected(GrupoDTO grupo) {
        try {
            SessionData.getInstance().setGrupo(grupo);

            voiceAdapter.onNavigating();
            voiceAdapter.cleanup();

            new Thread(() -> {
                try {
                    Thread.sleep(1500);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }

                Platform.runLater(() -> Navigator.navigateToSubGroups());
            }).start();

        } catch (Exception e) {
            logger.severe("Error al seleccionar grupo: " + e.getMessage());
            Message.showError(
                    "Error",
                    "No se pudo seleccionar el grupo. Por favor intente nuevamente."
            );
        }
    }

    private void handleNextPage() {
        voiceAdapter.onPageChanged();
    }

    private void handlePreviousPage() {
        voiceAdapter.onPageChanged();
    }

    private void handleBack() {
        handleBackVoice();
    }

    private void handleBackVoice() {
        voiceAdapter.onGoingBack();
        voiceAdapter.cleanup();

        new Thread(() -> {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            Platform.runLater(() -> Navigator.navigateToIdentification());
        }).start();
    }

    public void shutdown() {
        voiceAdapter.cleanup();

        if (currentTask != null && !currentTask.isDone()) {
            currentTask.cancel(true);
        }
        executor.shutdown();
    }
}