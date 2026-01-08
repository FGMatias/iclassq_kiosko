package org.iclassq.controller;

import javafx.application.Platform;
import org.iclassq.config.ServiceFactory;
import org.iclassq.controller.voice.GruposVoiceHelper;
import org.iclassq.model.domain.SessionData;
import org.iclassq.model.dto.response.GrupoDTO;
import org.iclassq.navigation.Navigator;
import org.iclassq.service.GrupoService;
import org.iclassq.util.voice.VoiceAssistant;
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

    private final VoiceAssistant voiceAssistant = new VoiceAssistant();
    private final GruposVoiceHelper voiceHelper = new GruposVoiceHelper(voiceAssistant);
    private boolean isInitialLoad = true;

    private List<GrupoDTO> allGroups;

    public GruposController(GruposView view) {
        this.view = view;
        this.grupoService = ServiceFactory.getGrupoService();

        view.setOnGroupSelected(this::handleGrupoSelected);
        view.setOnNextPage(this::handleNextPage);
        view.setOnPreviousPage(this::handlePreviousPage);
        view.setOnBack(this::handleBack);
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
                    setupVoiceCommands(groups);
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

    private void setupVoiceCommands(List<GrupoDTO> groups) {
        if (!voiceAssistant.isEnabled() || groups == null || groups.isEmpty()) {
            return;
        }

        String numeroDocumento = SessionData.getInstance().getNumeroDocumento();
        int currentPage = view.getCurrentPage();
        int totalPages = view.getTotalPages();

        voiceHelper.announceGroups(numeroDocumento, groups, currentPage, totalPages);

        voiceHelper.registerGroupCommands(groups, this::selectGroupByVoice);
        voiceHelper.registerPreviousPageCommand(this::handlePreviousPageVoice);
        voiceHelper.registerNextPageCommand(this::handleNextPageVoice);
        voiceHelper.registerBackCommand(this::handleBackVoice);
        voiceAssistant.enableGrammar();

        logger.info("Gramática activada para grupos");
    }

    private void selectGroupByVoice(GrupoDTO grupo) {
        voiceAssistant.stopSpeaking();
        voiceHelper.announceGroupSelected(grupo.getNombre());

        new Thread(() -> {
            try {
                Thread.sleep(800);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            Platform.runLater(() -> {
                handleGrupoSelected(grupo);
            });
        }).start();
    }

    private void handleGrupoSelected(GrupoDTO grupo) {
        try {
            voiceAssistant.stopSpeaking();
            voiceHelper.announceNavigation();

            SessionData.getInstance().setGrupo(grupo);

            new Thread(() -> {
                try {
                    Thread.sleep(1500);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }

                Platform.runLater(() -> {
                    voiceAssistant.cleanup();
                    Navigator.navigateToSubGroups();
                });
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
        voiceHelper.announcePageChange(view.getCurrentPage(), view.getTotalPages());
    }

    private void handlePreviousPage() {
        voiceHelper.announcePageChange(view.getCurrentPage(), view.getTotalPages());
    }

    private void handleNextPageVoice() {
        view.goToNextPage();
    }

    private void handlePreviousPageVoice() {
        view.goToPreviousPage();
    }

    private void handleBack() {
        handleBackVoice();
    }

    private void handleBackVoice() {
        voiceAssistant.stopSpeaking();
        voiceHelper.announceBack();

        new Thread(() -> {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            Platform.runLater(() -> {
                voiceAssistant.cleanup();
                Navigator.navigateToIdentification();
            });
        }).start();
    }

    public void shutdown() {
        if (currentTask != null && !currentTask.isDone()) {
            currentTask.cancel(true);
        }
        voiceAssistant.cleanup();
        executor.shutdown();
    }
}