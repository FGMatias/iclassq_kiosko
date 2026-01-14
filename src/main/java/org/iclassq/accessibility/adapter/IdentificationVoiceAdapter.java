package org.iclassq.accessibility.adapter;

import org.iclassq.accessibility.AccessibilityManager;
import org.iclassq.model.dto.response.TipoDocumentoDTO;
import org.iclassq.util.voice.KeywordGenerator;
import org.iclassq.util.voice.VoiceAssistant;

import java.util.List;
import java.util.function.Consumer;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class IdentificationVoiceAdapter {
    private static final Logger logger = Logger.getLogger(IdentificationVoiceAdapter.class.getName());

    private VoiceAssistant voice;

    public IdentificationVoiceAdapter() {
        this.voice = AccessibilityManager.getInstance().getVoiceAssistant();
    }

    public void onDocumentTypesLoaded(List<TipoDocumentoDTO> documentTypes) {
        if (!isVoiceActive() || documentTypes == null || documentTypes.isEmpty()) {
            return;
        }

        announceWelcomeAndDocumentTypes(documentTypes);
        logger.info("Tipos de documento anunciados");
    }

    public void registerCommands(
            List<TipoDocumentoDTO> documentTypes,
            Consumer<TipoDocumentoDTO> onDocumentTypeSelected,
            Consumer<String> onNumbersRecognized,
            Runnable onNext,
            Runnable onDelete,
            Runnable onDeleteAll
    ) {
        if (!isVoiceActive()) {
            return;
        }

        registerDocumentTypeCommands(documentTypes, onDocumentTypeSelected);

        voice.onNumberRecognized(onNumbersRecognized);
        voice.registerCommand("siguiente,continuar,adelante", onNext);
        voice.registerCommand("borrar,eliminar,quitar", onDelete);
        voice.registerCommand("borrar todo,limpiar,limpiar todo,eliminar todo", onDeleteAll);

        voice.enableGrammar();

        logger.info("Comandos de voz configurados para identificación");
    }

    public void onDocumentTypeSelected(String descripcion) {
        if (!isVoiceActive()) return;

        String message = String.format(
                "Has seleccionado %s. Ahora ingresa tu número de documento. " +
                "Puedes dictarlo número por número o usar el teclado en pantalla.",
                descripcion
        );

        voice.speak(message);
    }

    public void onValidationError(String errorMessage) {
        if (!isVoiceActive()) return;

        voice.speak(errorMessage);
    }

    public void onCharacterDeleted() {
        if (!isVoiceActive()) return;

        voice.speak("Carácter eliminado.");
    }

    public void onFieldCleared() {
        if (!isVoiceActive()) return;

        voice.speak("Campo limpiado.");
    }

    public void onNavigating() {
        if (!isVoiceActive()) return;

        voice.stopSpeaking();
        voice.speak("Avanzando a la siguiente pantalla.");
    }

    private void announceWelcomeAndDocumentTypes(List<TipoDocumentoDTO> documentTypes) {
        StringBuilder message = new StringBuilder();
        message.append("Bienvenido. Ingresa tus datos para generar un ticket. ");
        message.append("Elige el tipo de documento. Las opciones son: ");

        List<String> nombres = documentTypes.stream()
                .map(TipoDocumentoDTO::getDescripcion)
                .collect(Collectors.toList());

        for (int i = 0; i < nombres.size(); i++) {
            message.append(nombres.get(i));
            if (i < nombres.size() - 1) {
                message.append(", ");
            }
        }

        message.append(". También puedes usar el teclado para digitar tu número de documento.");

        voice.speak(message.toString());
    }

    private void registerDocumentTypeCommands(
            List<TipoDocumentoDTO> documentTypes,
            Consumer<TipoDocumentoDTO> onDocumentSelected
    ) {
        for (TipoDocumentoDTO docType : documentTypes) {
            String descripcion = docType.getDescripcion().toLowerCase();
            String keywords = KeywordGenerator.generateKeywords(descripcion);

            voice.registerCommand(keywords, () -> {
                onDocumentSelected.accept(docType);
            });
        }
    }

    private boolean isVoiceActive() {
        return voice != null && voice.isActive();
    }
}