package org.iclassq.controller;

import javafx.application.Platform;
import org.iclassq.accessibility.AccessibilityManager;
import org.iclassq.accessibility.adapter.DisabilityDetectionAdapter;
import org.iclassq.accessibility.adapter.IdentificationVoiceAdapter;
import org.iclassq.accessibility.adapter.ProximityDetectionAdapter;
import org.iclassq.config.ServiceFactory;
import org.iclassq.model.domain.SessionData;
import org.iclassq.model.dto.response.TipoDocumentoDTO;
import org.iclassq.navigation.Navigator;
import org.iclassq.service.TipoDocumentoService;
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

    private DisabilityDetectionAdapter detectionAdapter;
    private final ProximityDetectionAdapter proximityAdapter;
    private final IdentificationVoiceAdapter voiceAdapter;

    private boolean isInitialLoad = true;

    public IdentificationController(IdentificationView view) {
        this.view = view;
        this.tipoDocumentoService = ServiceFactory.getTipoDocumentoService();

        view.setOnNext(this::handleNext);
        view.setOnTypeDocumentChange(this::handleTypeDocumentChange);
        view.setOnDelete(this::handleDelete);
        view.setOnDeleteAll(this::handleDeleteAll);

        AccessibilityManager.getInstance().enableAccessibility();
        this.detectionAdapter = new DisabilityDetectionAdapter();
        this.voiceAdapter = new IdentificationVoiceAdapter();
        this.proximityAdapter = null;
//        this.proximityAdapter = new ProximityDetectionAdapter();
//        this.proximityAdapter.onDetectionCompleted(proximityDetected -> {
//            if (proximityDetected) {
//                logger.info("Arduino detectó presencia - iniciando detección por cámara");
//
//                this.detectionAdapter = new DisabilityDetectionAdapter();
//
//            } else {
//                logger.info("Arduino no detectó presencia continua");
//                logger.info("   Usuario se retiró antes de completar 5 segundos");
//                logger.info("   Continuando en modo visual normal (sin accesibilidad)");
//
//                AccessibilityManager.getInstance().disableAccessibility();
//            }
//        });

        loadDocumentTypes();
//        initializeProximityDetection();
    }

    private void initializeProximityDetection() {
        logger.info("Preparando inicio de detección de proximidad");

        new Thread(() -> {
            try {
                Thread.sleep(500);
                Platform.runLater(() -> {
                    logger.info("Iniciando detección de proximidad en IdentificationView");
                    proximityAdapter.start();
                });
            } catch (InterruptedException e) {
                logger.warning("Thread de inicialización interrumpido");
                Thread.currentThread().interrupt();
            }
        }, "ProximityInitThread").start();
    }

    public void cleanup() {
        logger.info("Limpiando recursos de IdentificationController");

        proximityAdapter.stop();

        if (detectionAdapter != null) {
            logger.info("   Limpiando DisabilityDetectionAdapter");
        }

        logger.info("   Limpiando servicios de voz");
        logger.info("Recursos limpiados correctamente");
    }

    public void resetProximityDetection() {
        logger.info("Reseteando detección de proximidad para nueva sesión");
        proximityAdapter.reset();
        initializeProximityDetection();
    }

    private void loadDocumentTypes() {
        new Thread(() -> {
            try {
                List<TipoDocumentoDTO> list = tipoDocumentoService.getAll();

                Platform.runLater(() -> {
                    populateDocumentTypes(list);
                    voiceAdapter.onDocumentTypesLoaded(list);
                    registerVoiceCommands(list);
                    isInitialLoad = false;
                });

            } catch (Exception e) {
                logger.severe("Error al cargar tipos de documento: " + e.getMessage());
                Platform.runLater(() ->
                        Message.showError(
                                "Error al cargar datos",
                                "No se pudieron cargar los tipos de documento"
                        )
                );
            }
        }).start();
    }

    private void handleNext() {
        String tipoDocDescripcion = view.getTypeDocument().getValue();
        String numeroDoc = view.getDocumentNumber().getText().trim();

        if (tipoDocDescripcion == null || tipoDocDescripcion.isEmpty()) {
            Message.showWarning(
                    "Tipo de documento requerido",
                    "Por favor seleccione un tipo de documento"
            );
            voiceAdapter.onValidationError("Por favor seleccione un tipo de documento");
            return;
        }

        if (!view.isValid()) {
            voiceAdapter.onValidationError("El documento ingresado no es válido");
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
                voiceAdapter.onValidationError(view.getCurrentConfig().getLengthErrorMessage());
                return;
            }
        }

        SessionData.getInstance().setTipoDocumento(tipoDocId);
        SessionData.getInstance().setTipoDocumentoDescripcion(tipoDocDescripcion);
        SessionData.getInstance().setNumeroDocumento(numeroDoc);

        voiceAdapter.onNavigating();

        new Thread(() -> {
            try {
                Thread.sleep(1500);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            Platform.runLater(() -> Navigator.navigateToGroups());
        }).start();
    }

    private void handleDelete() {
        String currentText = view.getDocumentNumber().getText();
        if (!currentText.isEmpty()) {
            view.getDocumentNumber().setText(currentText.substring(0, currentText.length() - 1));
            voiceAdapter.onCharacterDeleted();
        }
    }

    private void handleDeleteAll() {
        view.getDocumentNumber().clear();
        voiceAdapter.onFieldCleared();
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
                voiceAdapter.onDocumentTypeSelected(tipoDocDescripcion);
            }
        }
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

    private void registerVoiceCommands(List<TipoDocumentoDTO> list) {
        voiceAdapter.registerCommands(
                list,
                this::selectDocumentTypeByVoice,
                this::handleNumbersRecognized,
                this::handleNextVoice,
                this::handleDelete,
                this::handleDeleteAll
        );
    }

    private void selectDocumentTypeByVoice(TipoDocumentoDTO docType) {
        view.getTypeDocument().setValue(docType.getDescripcion());
        view.getDocumentNumber().requestFocus();
    }

    private void handleNumbersRecognized(String numbers) {
        String currentText = view.getDocumentNumber().getText();
        view.getDocumentNumber().setText(currentText + numbers);
    }

    private void handleNextVoice() {
        if (view.isValid()) {
            handleNext();
        } else {
            voiceAdapter.onValidationError("Por favor completa todos los campos correctamente");
        }
    }
}