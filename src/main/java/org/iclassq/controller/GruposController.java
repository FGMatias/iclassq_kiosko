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

    private List<GrupoDTO> allGroups;

    public GruposController(GruposView view) {
        this.view = view;
        this.grupoService = ServiceFactory.getGrupoService();
        view.setOnGroupSelected(this::handleGrupoSelected);
        voiceHelper.registerBackCommand(this::handleBack);
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
        if (!voiceAssistant.isEnabled()) {
            return;
        }

        String numeroDocumento = SessionData.getInstance().getNumeroDocumento();

        voiceHelper.announceGroups(numeroDocumento, groups);
        voiceHelper.registerGroupCommands(groups, this::selectGroupByVoice);
    }

    private void selectGroupByVoice(GrupoDTO grupo) {
        voiceHelper.announceGroupSelected(grupo.getNombre());
        handleGrupoSelected(grupo);
    }

    private void handleGrupoSelected(GrupoDTO grupo) {
        try {
            voiceAssistant.stopSpeaking();
            SessionData.getInstance().setGrupo(grupo);
            voiceAssistant.cleanup();
            Navigator.navigateToSubGroups();
        } catch (Exception e) {
            logger.severe("Error al seleccionar grupo: " + e.getMessage());
            Message.showError(
                    "Error",
                    "No se pudo seleccionar el grupo. Por favor intente nuevamente."
            );
        }
    }

    private void handleBack() {
        voiceAssistant.stopSpeaking();
        voiceHelper.announceBack();
        voiceAssistant.stopSpeaking();
        voiceAssistant.cleanup();
        Navigator.navigateToIdentification();
    }

    public void shutdown() {
        if (currentTask != null && !currentTask.isDone()) {
            currentTask.cancel(true);
        }
        voiceAssistant.cleanup();
        executor.shutdown();
    }
}