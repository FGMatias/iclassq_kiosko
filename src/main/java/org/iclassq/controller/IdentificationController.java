package org.iclassq.controller;

import javafx.application.Platform;
import org.iclassq.KioskoApplication;
import org.iclassq.accessibility.AccessibilityDetectionService;
import org.iclassq.accessibility.DisabilityDetector;
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

    private final VoiceAssistant voiceAssistant;
    private final IdentificationVoiceHelper voiceHelper;

    private boolean isInitialLoad = true;

    public IdentificationController(IdentificationView view) {
        this.view = view;
        this.tipoDocumentoService = ServiceFactory.getTipoDocumentoService();
        view.setOnNext(this::handleNext);
        view.setOnTypeDocumentChange(this::handleTypeDocumentChange);
        view.setOnDelete(this::handleDelete);
        view.setOnDeleteAll(this::handleDeleteAll);

        voiceAssistant = new VoiceAssistant();
        voiceHelper = new IdentificationVoiceHelper(voiceAssistant);

        if (voiceAssistant.isReady()) {
            logger.info("VoiceAssistant READY (servicios preparados en background)");
        } else {
            logger.info("VoiceAssistant no disponible (VoiceManager no inicializado)");
        }

        executeDisabilityDetectionReactive();

        loadDocumentTypes();
    }

    private void executeDisabilityDetectionReactive() {
        logger.info("Ejecutando detección de discapacidad...");

        DisabilityDetector detector = KioskoApplication.getDisabilityDetector();

        if (detector == null) {
            logger.warning("DisabilityDetector no disponible - modo visual");
            return;
        }

        AccessibilityDetectionService detectionService = detector.getDetectionService();

        detectionService.onReady(ready -> {
            logger.info("Sistema de detección listo, iniciando análisis...");

            detectionService.detectAndActivateAsync()
                    .thenAccept(activated -> {
                        Platform.runLater(() -> {
                            if (activated) {
                                logger.info("Persona con discapacidad detectada");
                                activateVoiceServices();

                            } else {
                                logger.info("Persona sin discapacidad detectada");
                                logger.info("Modo visual normal");
                                logger.info("VoiceAssistant permanece READY pero no ACTIVE");
                            }
                        });
                    })
                    .exceptionally(error -> {
                        logger.severe("Error en detección: " + error.getMessage());
                        Platform.runLater(() -> {
                            Message.showError(
                                    "Error de Detección",
                                    "No se pudo completar la detección. Continuando en modo normal."
                            );
                        });
                        return null;
                    });
        });
    }

    private void activateVoiceServices() {
        if (!voiceAssistant.isReady()) {
            logger.warning("No se puede activar voz: servicios no disponibles");
            return;
        }

        if (voiceAssistant.isActive()) {
            logger.info("Servicios de voz ya están activos");
            return;
        }

        logger.info("Activando servicios de voz...");

        boolean activated = voiceAssistant.activate();

        if (activated) {
            logger.info("Servicios de voz ACTIVADOS");
            setupVoiceCommandsIfNeeded();
        } else {
            logger.warning("No se pudieron activar servicios de voz");
        }
    }

    private void setupVoiceCommandsIfNeeded() {
        if (!voiceAssistant.isActive()) {
            logger.fine("Voz no activa, omitiendo configuración de comandos");
            return;
        }

        List<TipoDocumentoDTO> types = view.getTypeDocument().getItems().stream()
                .map(desc -> {
                    Integer id = documentTypesMap.get(desc);
                    return new TipoDocumentoDTO(id, desc);
                })
                .toList();

        if (!types.isEmpty()) {
            setupVoiceCommands(types);
        }
    }

    private void loadDocumentTypes() {
        new Thread(() -> {
            try {
                List<TipoDocumentoDTO> list = tipoDocumentoService.getAll();

                Platform.runLater(() -> {
                    populateDocumentTypes(list);

                    if (voiceAssistant.isActive()) {
                        setupVoiceCommands(list);
                    }

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

            if (voiceAssistant.isActive()) {
                voiceHelper.announceValidationError("Por favor seleccione un tipo de documento");
            }
            return;
        }

        if (!view.isValid()) {
            if (voiceAssistant.isActive()) {
                voiceHelper.announceValidationError("El documento ingresado no es válido");
            }
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

                if (voiceAssistant.isActive()) {
                    voiceHelper.announceValidationError(view.getCurrentConfig().getLengthErrorMessage());
                }
                return;
            }
        }

        if (voiceAssistant.isActive()) {
            voiceAssistant.stopSpeaking();
        }

        SessionData.getInstance().setTipoDocumento(tipoDocId);
        SessionData.getInstance().setTipoDocumentoDescripcion(tipoDocDescripcion);
        SessionData.getInstance().setNumeroDocumento(numeroDoc);

        if (voiceAssistant.isActive()) {
            voiceHelper.announceNavigation();
        }

        new Thread(() -> {
            try {
                Thread.sleep(1500);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            Platform.runLater(() -> {
                if (voiceAssistant.isActive()) {
                    voiceAssistant.deactivate();
                }

                Navigator.navigateToGroups();
            });
        }).start();
    }

    private void handleDelete() {
        String currentText = view.getDocumentNumber().getText();
        if (!currentText.isEmpty()) {
            view.getDocumentNumber().setText(currentText.substring(0, currentText.length() - 1));

            if (voiceAssistant.isActive()) {
                voiceHelper.announcedDeleted();
            }
        }
    }

    private void handleDeleteAll() {
        view.getDocumentNumber().clear();

        if (voiceAssistant.isActive()) {
            voiceHelper.announceDeletedAll();
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

    private void setupVoiceCommands(List<TipoDocumentoDTO> list) {
        if (!voiceAssistant.isActive()) {
            logger.fine("Voz no activa, omitiendo configuración de comandos");
            return;
        }

        logger.info("Configurando comandos de voz...");

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

        logger.info("Comandos de voz configurados");
    }

    private void selectDocumentTypeByVoice(TipoDocumentoDTO docType) {
        view.getTypeDocument().setValue(docType.getDescripcion());
        view.getDocumentNumber().requestFocus();
    }

    private void handleTypeDocumentChange() {
        String tipoDocDescripcion = view.getTypeDocument().getValue();

        if (tipoDocDescripcion == null || tipoDocDescripcion.isEmpty()) {
            return;
        }

        Integer tipoDocId = documentTypesMap.get(tipoDocDescripcion);

        if (tipoDocId != null) {
            view.updateDocumentConfig(tipoDocId);

            if (!isInitialLoad && voiceAssistant.isActive()) {
                voiceHelper.announceDocumentTypeSelected(tipoDocDescripcion);
            }
        }
    }
}