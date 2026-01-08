package org.iclassq.controller;

import javafx.application.Platform;
import org.iclassq.config.ServiceFactory;
import org.iclassq.controller.voice.SubGruposVoiceHelper;
import org.iclassq.controller.voice.TicketVoiceHelper;
import org.iclassq.model.domain.SessionData;
import org.iclassq.model.dto.request.TicketRequestDTO;
import org.iclassq.model.dto.response.SubGrupoDTO;
import org.iclassq.model.dto.response.TicketResponseDTO;
import org.iclassq.navigation.Navigator;
import org.iclassq.service.SubGrupoService;
import org.iclassq.service.TicketService;
import org.iclassq.util.voice.VoiceAssistant;
import org.iclassq.view.SubGruposView;
import org.iclassq.view.components.Message;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.logging.Logger;

public class SubGruposController {
    private final SubGruposView view;
    private final SubGrupoService subGrupoService;
    private final TicketService ticketService;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private Future<?> currentLoadTask;
    private Future<?> currentGenerateTask;
    private final Logger logger = Logger.getLogger(SubGruposController.class.getName());

    private final VoiceAssistant voiceAssistant = new VoiceAssistant();
    private final SubGruposVoiceHelper voiceHelper = new SubGruposVoiceHelper(voiceAssistant);
    private final TicketVoiceHelper ticketVoiceHelper = new TicketVoiceHelper(voiceAssistant);
    private boolean isInitialLoad = true;

    private List<SubGrupoDTO> allSubGroups;

    public SubGruposController(SubGruposView view) {
        this.view = view;
        this.subGrupoService = ServiceFactory.getSubGrupoService();
        this.ticketService = ServiceFactory.getTicketService();

        view.setOnSubGroupSelected(this::handleSubGroupSelected);
        view.setOnNextPage(this::handleNextPage);
        view.setOnPreviousPage(this::handlePreviousPage);
        view.setOnBack(this::handleBack);
        loadSubGroups();
    }

    private void loadSubGroups() {
        if (currentLoadTask != null && !currentLoadTask.isDone()) {
            currentLoadTask.cancel(true);
        }

        view.showLoading();

        currentLoadTask = executor.submit(() -> {
            try {
                SessionData session = SessionData.getInstance();
                Integer sucursalId = session.getSucursalId();
                Integer grupoId = session.getGrupo().getId();
                List<SubGrupoDTO> subGroups = subGrupoService.getByGrupo(sucursalId, grupoId);

                Platform.runLater(() -> {
                    view.hideLoading();
                    view.setSubGroups(subGroups);

                    this.allSubGroups = subGroups;
                    setupVoiceCommands(subGroups);
                    isInitialLoad = false;
                });
            } catch (IOException e) {
                logger.severe("Error de conexión al cargar los subgrupos" + e);
                Platform.runLater(() -> {
                    view.hideLoading();
                    Message.showError(
                            "Error de Conexión",
                            "No se pudo conectar con el servidor. Verifique su conexión"
                    );
                });
            } catch (Exception e) {
                logger.severe("Error inesperado: " + e.getMessage());
                Platform.runLater(() -> {
                    view.hideLoading();
                    Message.showError(
                            "Error",
                            "Ocurrió un error inesperado. Por favor intente nuevamente"
                    );
                });
            }
        });
    }

    private void setupVoiceCommands(List<SubGrupoDTO> subGroups) {
        if (!voiceAssistant.isEnabled() || subGroups == null || subGroups.isEmpty()) {
            return;
        }

        String nombreGrupo = SessionData.getInstance().getGrupo().getNombre();
        int currentPage = view.getCurrentPage();
        int totalPages = view.getTotalPages();

        voiceHelper.announceSubGroups(nombreGrupo, subGroups, currentPage, totalPages);

        voiceHelper.registerSubGroupCommands(subGroups, this::selectSubGroupByVoice);
        voiceHelper.registerPreviousPageCommand(this::handlePreviousPageVoice);
        voiceHelper.registerNextPageCommand(this::handleNextPageVoice);
        voiceHelper.registerBackCommand(this::handleBackVoice);
        voiceAssistant.enableGrammar();

        logger.info("Gramática activada para subgrupos");
    }

    private void selectSubGroupByVoice(SubGrupoDTO subGrupo) {
        voiceAssistant.stopSpeaking();

        voiceHelper.announceSubGroupSelected(subGrupo.getVNombreSubGrupo());

        new Thread(() -> {
            try {
                Thread.sleep(800);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            Platform.runLater(() -> {
                handleSubGroupSelected(subGrupo);
            });
        }).start();
    }

    private void handleSubGroupSelected(SubGrupoDTO subGrupo) {
        if (currentGenerateTask != null && !currentGenerateTask.isDone()) {
            return;
        }

        view.showLoading();

        voiceAssistant.stopSpeaking();

        currentGenerateTask = executor.submit(() -> {
            try {
                TicketRequestDTO request = buildTicketRequest(subGrupo);
                TicketResponseDTO ticket = ticketService.generateTicket(request);

                Platform.runLater(() -> {
                    view.hideLoading();

                    voiceAssistant.cleanup();

                    Navigator.navigatoToTicket(ticket);
                });

            } catch (IOException e) {
                logger.severe("Error de conexión al generar ticket: " + e);
                Platform.runLater(() -> {
                    view.hideLoading();
                    Message.showError(
                            "Error de Conexión",
                            "No se pudo generar el ticket. Verifique su conexión."
                    );
                });
            } catch (Exception e) {
                logger.severe("Error al generar ticket: " + e.getMessage());
                Platform.runLater(() -> {
                    view.hideLoading();
                    Message.showError(
                            "Error",
                            "No se pudo generar el ticket: " + e.getMessage()
                    );
                });
            }
        });
    }

    private TicketRequestDTO buildTicketRequest(SubGrupoDTO subGrupo) {
        SessionData session = SessionData.getInstance();

        TicketRequestDTO request = new TicketRequestDTO();
        request.setIdSucursal(session.getSucursalId());
        request.setIdSubgrupo(subGrupo.getISubGrupo());
        request.setPrefijo(subGrupo.getVPrefijo());
        request.setNombre(subGrupo.getVNombreSubGrupo());
        request.setNumDoc(session.getNumeroDocumento());
        request.setTipoDoc(session.getTipoDocumento());
        request.setValidaDoc(0);

        return request;
    }

    private void handleNextPage() {
        voiceHelper.announcePageChanged(view.getCurrentPage(), view.getTotalPages());
    }

    private void handlePreviousPage() {
        voiceHelper.announcePageChanged(view.getCurrentPage(), view.getTotalPages());
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
                Navigator.navigateToGroups();
            });
        }).start();
    }

    public void shutdown() {
        if (currentLoadTask != null && !currentLoadTask.isDone()) {
            currentLoadTask.cancel(true);
        }
        if (currentGenerateTask != null && !currentGenerateTask.isDone()) {
            currentGenerateTask.cancel(true);
        }
        voiceAssistant.cleanup();
        executor.shutdown();
    }
}