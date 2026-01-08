package org.iclassq.controller;

import javafx.application.Platform;
import org.iclassq.config.ServiceFactory;
import org.iclassq.controller.voice.IdentificationVoiceHelper;
import org.iclassq.model.domain.SessionData;
import org.iclassq.model.dto.response.TipoDocumentoDTO;
import org.iclassq.navigation.Navigator;
import org.iclassq.service.TipoDocumentoService;
import org.iclassq.util.voice.VoiceAssistant;
import org.iclassq.view.IdentificationView;
import org.iclassq.view.components.Message;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

public class IdentificationController {
    private final IdentificationView view;
    private final TipoDocumentoService tipoDocumentoService;

    private final Logger logger = Logger.getLogger(IdentificationController.class.getName());
    private Map<String, Integer> documentTypesMap = new HashMap<>();

    private final VoiceAssistant voiceAssistant = new VoiceAssistant();
    private final IdentificationVoiceHelper voiceHelper = new IdentificationVoiceHelper(voiceAssistant);
    private boolean isInitialLoad = true;

    public IdentificationController(IdentificationView view) {
        this.view = view;
        this.tipoDocumentoService = ServiceFactory.getTipoDocumentoService();
        view.setOnNext(this::handleNext);
        view.setOnTypeDocumentChange(this::handleTypeDocumentChange);
        view.setOnDelete(this::handleDelete);
        view.setOnDeleteAll(this::handleDeleteAll);
        loadDocumentTypes();
    }

    private void loadDocumentTypes() {
        view.getTypeDocument().setDisable(true);
        view.getBtnNext().setDisable(true);

        new Thread(() -> {
            try {
                List<TipoDocumentoDTO> list = tipoDocumentoService.getAll();

                Platform.runLater(() -> {
                    populateDocumentTypes(list);
                    view.getTypeDocument().setDisable(false);
                    view.getBtnNext().setDisable(false);

                    setupVoiceCommands(list);
                    isInitialLoad = false;
                });
            } catch (Exception e) {
                logger.severe(e.getMessage());
                Platform.runLater(() -> {
                    Message.showError("Error de conexión",
                            "No se pudieron cargar los tipos de documento.");
                    view.getTypeDocument().setDisable(false);
                    view.getBtnNext().setDisable(false);
                });
            }
        }).start();
    }

    private void handleTypeDocumentChange() {
        String tipoDocDescripcion = view.getTypeDocument().getValue();

        if (tipoDocDescripcion == null || tipoDocDescripcion.isEmpty()) {
            return;
        }

        Integer tipoDocId = documentTypesMap.get(tipoDocDescripcion);

        if (tipoDocId != null) {
            view.updateDocumentConfig(tipoDocId);

            if (!isInitialLoad) {
                voiceHelper.announceDocumentTypeSelected(tipoDocDescripcion);
            }
        }
    }

    private void handleNext() {
        String tipoDocDescripcion = view.getTypeDocument().getValue();
        String numeroDoc = view.getDocumentNumber().getText().trim();

        if (tipoDocDescripcion == null || tipoDocDescripcion.isEmpty()) {
            Message.showWarning(
                    "Tipo de documento requerido",
                    "Por favor seleccione un tipo de documento"
            );
            voiceHelper.announceValidationError("Por favor seleccione un tipo de documento");
            return;
        }

        if (!view.isValid()) {
            voiceHelper.announceValidationError("El documento ingresado no es válido");
            return;
        }

        Integer tipoDocId = documentTypesMap.get(tipoDocDescripcion);

        if (tipoDocId == null) {
            Message.showError(
                    "Error interno",
                    "Tipo de documento no válido. Por favor intente nuevamente."
            );
            return;
        }

        if (view.getCurrentConfig() != null) {
            if (!view.getCurrentConfig().isValidLength(numeroDoc)) {
                Message.showWarning(
                        "Documento inválido",
                        view.getCurrentConfig().getLengthErrorMessage()
                );
                voiceHelper.announceValidationError(view.getCurrentConfig().getLengthErrorMessage());
                return;
            }
        }

        voiceAssistant.stopSpeaking();

        SessionData.getInstance().setTipoDocumento(tipoDocId);
        SessionData.getInstance().setTipoDocumentoDescripcion(tipoDocDescripcion);
        SessionData.getInstance().setNumeroDocumento(numeroDoc);

        voiceHelper.announceNavigation();

        new Thread(() -> {
            try {
                Thread.sleep(1500);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            Platform.runLater(() -> {
                voiceAssistant.cleanup();
                Navigator.navigateToGroups();
            });
        }).start();
    }

    private void handleDelete() {
        String currentText = view.getDocumentNumber().getText();
        if (!currentText.isEmpty()) {
            view.getDocumentNumber().setText(currentText.substring(0, currentText.length() - 1));
            voiceHelper.announcedDeleted();
        }
    }

    private void handleDeleteAll() {
        view.getDocumentNumber().clear();
        voiceHelper.announceDeletedAll();
    }

    private void populateDocumentTypes(List<TipoDocumentoDTO> list) {
        view.getTypeDocument().getItems().clear();
        documentTypesMap.clear();

        for (TipoDocumentoDTO type : list) {
            String descripcion = type.getDescripcion();
            view.getTypeDocument().getItems().add(descripcion);
            documentTypesMap.put(descripcion, type.getId());
        }

        if (!list.isEmpty()) {
            view.getTypeDocument().setValue(list.get(0).getDescripcion());
        }
    }

    private void setupVoiceCommands(List<TipoDocumentoDTO> list) {
        if (!voiceAssistant.isEnabled()) {
            return;
        }

        voiceHelper.announceDocumentTypes(list);
        voiceHelper.registerDocumentTypeCommands(list, this::selectDocumentTypeByVoice);
        voiceHelper.registerNumberInput(numbers -> {
            String currentText = view.getDocumentNumber().getText();
            view.getDocumentNumber().setText(currentText + numbers);
        });
        voiceHelper.registerNextCommand(() -> {
            if (view.isValid()) {
                handleNext();
            } else {
                voiceHelper.announceValidationError("Por favor completa todos los campos correctamente");
            }
        });

        voiceHelper.registerDeleteCommand(this::handleDelete);
        voiceHelper.registerDeleteAllCommand(this::handleDeleteAll);
        voiceAssistant.enableGrammar();
    }

    private void selectDocumentTypeByVoice(TipoDocumentoDTO docType) {
        view.getTypeDocument().setValue(docType.getDescripcion());
        view.getDocumentNumber().requestFocus();
    }
}
