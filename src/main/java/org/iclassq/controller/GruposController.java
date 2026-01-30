package org.iclassq.controller;

import javafx.application.Platform;
import javafx.scene.input.KeyEvent;
import org.iclassq.accessibility.AccessibilityManager;
import org.iclassq.accessibility.adapter.GruposBrailleAdapter;
import org.iclassq.accessibility.adapter.GruposVoiceAdapter;
import org.iclassq.config.ServiceFactory;
import org.iclassq.model.domain.SessionData;
import org.iclassq.model.dto.request.TicketRequestDTO;
import org.iclassq.model.dto.response.GrupoDTO;
import org.iclassq.model.dto.response.SubGrupoDTO;
import org.iclassq.model.dto.response.TicketResponseDTO;
import org.iclassq.navigation.Navigator;
import org.iclassq.service.GrupoService;
import org.iclassq.service.SubGrupoService;
import org.iclassq.service.TicketService;
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
    private final SubGrupoService subGrupoService;
    private final TicketService ticketService;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private Future<?> currentTask;
    private final Logger logger = Logger.getLogger(GruposController.class.getName());

    private final GruposVoiceAdapter voiceAdapter;
    private final GruposBrailleAdapter brailleAdapter;

    private boolean isInitialLoad = true;
    private List<GrupoDTO> allGroups;

    public GruposController(GruposView view) {
        this.view = view;
        this.grupoService = ServiceFactory.getGrupoService();
        this.subGrupoService = ServiceFactory.getSubGrupoService();
        this.ticketService = ServiceFactory.getTicketService();

        this.voiceAdapter = new GruposVoiceAdapter(view);
        this.brailleAdapter = new GruposBrailleAdapter();

        view.setOnGroupSelected(this::handleGrupoSelected);
        view.setOnNextPage(this::handleNextPage);
        view.setOnPreviousPage(this::handlePreviousPage);
        view.setOnBack(this::handleBack);

        view.getRoot().setOnKeyPressed(this::handleKeyPressed);

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

                    if (AccessibilityManager.getInstance().isBrailleActive() && groups.size() > 5) {
                        logger.info(String.format("Limitando a 5 grupos para Braille (había %d)", groups.size()));
                        view.setGroups(groups.subList(0, 5));
                        this.allGroups = groups.subList(0, 5);
                    } else {
                        view.setGroups(groups);
                        this.allGroups = groups;
                    }

                    voiceAdapter.onGroupsLoaded(this.allGroups, this::selectGroupByVoice);

                    if (AccessibilityManager.getInstance().isBrailleActive()) {
                        brailleAdapter.onGroupsLoaded(this.allGroups, this::selectGroupByBraille);
                    }

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

    private void handleKeyPressed(KeyEvent event) {
        if (brailleAdapter != null) {
            boolean handled = brailleAdapter.handleKeyEvent(event);
            if (handled) {
                logger.fine("Tecla manejada por Braille: " + event.getCode());
            }
        }
    }

    private void selectGroupByVoice(GrupoDTO grupo) {
        voiceAdapter.onGroupSelectedByVoice(grupo, this::handleGrupoSelected);
    }

    private void selectGroupByBraille(GrupoDTO grupo) {
        handleGrupoSelectedWithDisability(grupo);
    }

    private void handleGrupoSelected(GrupoDTO grupo) {
        try {
            SessionData.getInstance().setGrupo(grupo);

            boolean hasDisability = AccessibilityManager.getInstance().isAccessibilityEnabled();

            if (hasDisability) {
                logger.info("🔲 Discapacidad detectada - Generando ticket preferencial directo");
                handleGrupoSelectedWithDisability(grupo);
            } else {
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
            }

        } catch (Exception e) {
            logger.severe("Error al seleccionar grupo: " + e.getMessage());
            Message.showError(
                    "Error",
                    "No se pudo seleccionar el grupo. Por favor intente nuevamente."
            );
        }
    }

    private void handleGrupoSelectedWithDisability(GrupoDTO grupo) {
        SessionData.getInstance().setGrupo(grupo);

        view.showLoading();
        voiceAdapter.cleanup();
        brailleAdapter.cleanup();

        executor.submit(() -> {
            try {
                SessionData session = SessionData.getInstance();
                Integer sucursalId = session.getSucursalId();
                Integer grupoId = grupo.getId();

                logger.info(String.format("Obteniendo subgrupo preferencial para grupo: %s (ID: %d)",
                        grupo.getNombre(), grupoId));

                SubGrupoDTO subgrupoPreferencial = subGrupoService.getPreferencial(sucursalId, grupoId);

                if (subgrupoPreferencial == null) {
                    throw new Exception("No se encontró subgrupo preferencial para el grupo " + grupo.getNombre());
                }

                logger.info(String.format("Subgrupo preferencial obtenido: %s (ID: %d)",
                        subgrupoPreferencial.getVNombreSubGrupo(),
                        subgrupoPreferencial.getISubGrupo()));

                TicketRequestDTO request = buildTicketRequest(subgrupoPreferencial);

                logger.info("Generando ticket preferencial...");
                TicketResponseDTO ticket = ticketService.generateTicket(request);

                logger.info(String.format("Ticket generado: %s", ticket.getCodigo()));

                Platform.runLater(() -> {
                    view.hideLoading();
                    Navigator.navigatoToTicket(ticket);
                });

            } catch (IOException e) {
                logger.severe("Error de conexión: " + e.getMessage());
                Platform.runLater(() -> {
                    view.hideLoading();
                    Message.showError(
                            "Error de Conexión",
                            "No se pudo generar el ticket preferencial. Verifique su conexión."
                    );
                });
            } catch (Exception e) {
                logger.severe("Error al generar ticket preferencial: " + e.getMessage());
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
        voiceAdapter.cleanup();
        brailleAdapter.cleanup();

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
        brailleAdapter.cleanup();

        if (currentTask != null && !currentTask.isDone()) {
            currentTask.cancel(true);
        }
        executor.shutdown();
    }
}