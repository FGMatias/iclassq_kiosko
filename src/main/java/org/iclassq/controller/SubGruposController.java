package org.iclassq.controller;

import javafx.application.Platform;
import org.iclassq.accessibility.adapter.SubGruposVoiceAdapter;
import org.iclassq.config.ServiceFactory;
import org.iclassq.model.domain.SessionData;
import org.iclassq.model.dto.request.TicketRequestDTO;
import org.iclassq.model.dto.response.SubGrupoDTO;
import org.iclassq.model.dto.response.TicketResponseDTO;
import org.iclassq.navigation.Navigator;
import org.iclassq.service.SubGrupoService;
import org.iclassq.service.TicketService;
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

    private final SubGruposVoiceAdapter voiceAdapter;

    private boolean isInitialLoad = true;
    private List<SubGrupoDTO> allSubGroups;

    public SubGruposController(SubGruposView view) {
        this.view = view;
        this.subGrupoService = ServiceFactory.getSubGrupoService();
        this.ticketService = ServiceFactory.getTicketService();

        this.voiceAdapter = new SubGruposVoiceAdapter(view);

        view.setOnSubGroupSelected(this::handleSubGroupSelected);
        view.setOnNextPage(this::handleNextPage);
        view.setOnPreviousPage(this::handlePreviousPage);
        view.setOnBack(this::handleBack);

        voiceAdapter.registerNavigationCommands(
                view::goToPreviousPage,
                view::goToNextPage,
                this::handleBackVoice
        );

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

                    voiceAdapter.onSubGroupsLoaded(subGroups, this::selectSubGroupByVoice);

                    isInitialLoad = false;
                });

            } catch (IOException e) {
                logger.severe("Error de conexión: " + e.getMessage());
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

    private void selectSubGroupByVoice(SubGrupoDTO subGrupo) {
        voiceAdapter.onSubGroupSelectedByVoice(subGrupo, this::handleSubGroupSelected);
    }

    private void handleSubGroupSelected(SubGrupoDTO subGrupo) {
        if (currentGenerateTask != null && !currentGenerateTask.isDone()) {
            return;
        }

        view.showLoading();

        voiceAdapter.onGeneratingTicket();

        currentGenerateTask = executor.submit(() -> {
            try {
                TicketRequestDTO request = buildTicketRequest(subGrupo);
                TicketResponseDTO ticket = ticketService.generateTicket(request);

                Platform.runLater(() -> {
                    view.hideLoading();
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

        new Thread(() -> {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            Platform.runLater(() -> Navigator.navigateToGroups());
        }).start();
    }

    public void shutdown() {
        if (currentLoadTask != null && !currentLoadTask.isDone()) {
            currentLoadTask.cancel(true);
        }
        if (currentGenerateTask != null && !currentGenerateTask.isDone()) {
            currentGenerateTask.cancel(true);
        }
        executor.shutdown();
    }
}