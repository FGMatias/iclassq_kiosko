package org.iclassq.controller;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import org.iclassq.config.ServiceFactory;
import org.iclassq.controller.voice.IdentificationVoiceHelper;
import org.iclassq.model.domain.SessionData;
import org.iclassq.model.dto.response.TipoDocumentoDTO;
import org.iclassq.navigation.Navigator;
import org.iclassq.service.TipoDocumentoService;
import org.iclassq.util.VoiceAssistant;
import org.iclassq.view.IdentificationView;
import org.iclassq.view.components.Message;

import java.awt.event.ActionEvent;
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

    public IdentificationController(IdentificationView view) {
        this.view = view;
        this.tipoDocumentoService = ServiceFactory.getTipoDocumentoService();
        view.setOnNext(this::handleNext);
        view.setOnTypeDocumentChange(this::handleTypeDocumentChange);
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
                    if (!list.isEmpty()) {
                        handleTypeDocumentChange();
                    }

                    setupVoiceCommands(list);
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
            voiceHelper.announceDocumentTypeSelected(tipoDocDescripcion);
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

        voiceHelper.announceNavigation();

        SessionData.getInstance().setTipoDocumento(tipoDocId);
        SessionData.getInstance().setTipoDocumentoDescripcion(tipoDocDescripcion);
        SessionData.getInstance().setNumeroDocumento(numeroDoc);

        voiceAssistant.cleanup();

        Navigator.navigateToGroups();
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
    }

    private void selectDocumentTypeByVoice(TipoDocumentoDTO docType) {
        view.getTypeDocument().setValue(docType.getDescripcion());
        voiceHelper.announceDocumentTypeSelected(docType.getDescripcion());
        view.getDocumentNumber().requestFocus();
    }
}
