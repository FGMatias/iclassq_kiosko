package org.iclassq.controller;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import org.iclassq.config.ServiceFactory;
import org.iclassq.model.domain.SessionData;
import org.iclassq.model.dto.response.TipoDocumentoDTO;
import org.iclassq.navigation.Navigator;
import org.iclassq.service.TipoDocumentoService;
import org.iclassq.view.IdentificationView;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

public class IdentificationController {
    private final IdentificationView view;
    private final TipoDocumentoService tipoDocumentoService;

    private final Logger logger = Logger.getLogger(IdentificationController.class.getName());
    private Map<String, Integer> documentTypesMap = new HashMap<>();

    public IdentificationController(IdentificationView view) {
        this.view = view;
        this.tipoDocumentoService = ServiceFactory.getTipoDocumentoService();
        view.setOnNext(this::handleNext);
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
                });
            } catch (Exception e) {
                logger.severe(e.getMessage());
                Platform.runLater(() -> {
                    showError("Error de conexión",
                            "No se pudieron cargar los tipos de documento.");
                    view.getTypeDocument().setDisable(false);
                    view.getBtnNext().setDisable(false);
                });
            }
        }).start();
    }

    private void handleNext() {
        String tipoDocumento = view.getTypeDocument().getValue();
        String numeroDocumento = view.getDocumentNumber().getText();

        if (tipoDocumento == null || tipoDocumento.isEmpty()) {
            showError("Tipo de documento requerido",
                    "Por favor seleccione un tipo de documento.");
            return;
        }

        if (numeroDocumento.isEmpty()) {
            showError("Número de documento requerido",
                    "Por favor ingrese su número de documento.");
            return;
        }

        if (numeroDocumento.length() < 8) {
            showError("Documento inválido",
                    "El número de documento debe tener al menos 8 dígitos.");
            return;
        }

        Integer tipoDocumentoId = documentTypesMap.get(tipoDocumento);

        if (tipoDocumentoId == null) {
            showError("Error", "Tipo de documento no válido.");
            return;
        }

        SessionData.getInstance().setTipoDocumento(tipoDocumentoId);
        SessionData.getInstance().setTipoDocumentoDescripcion(tipoDocumento);
        SessionData.getInstance().setNumeroDocumento(numeroDocumento);
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

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
