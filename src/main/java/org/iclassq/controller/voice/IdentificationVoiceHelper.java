package org.iclassq.controller.voice;

import org.iclassq.model.dto.response.TipoDocumentoDTO;
import org.iclassq.util.voice.KeywordGenerator;
import org.iclassq.util.voice.VoiceAssistant;

import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class IdentificationVoiceHelper {

    private final VoiceAssistant voiceAssistant;

    public IdentificationVoiceHelper(VoiceAssistant voiceAssistant) {
        this.voiceAssistant = voiceAssistant;
    }

    public void announceDocumentTypes(List<TipoDocumentoDTO> documentTypes) {
        if (!voiceAssistant.isActive() || documentTypes == null || documentTypes.isEmpty()) {
            return;
        }

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

        voiceAssistant.speak(message.toString());
    }

    public void registerDocumentTypeCommands(
            List<TipoDocumentoDTO> documentTypes,
            Consumer<TipoDocumentoDTO> onDocumentSelected
    ) {
        if (!voiceAssistant.isActive() || documentTypes == null) {
            return;
        }

        for (TipoDocumentoDTO docType : documentTypes) {
            String descripcion = docType.getDescripcion().toLowerCase();
            String keywords = KeywordGenerator.generateKeywords(descripcion);

            voiceAssistant.registerCommand(keywords, () -> {
                onDocumentSelected.accept(docType);
            });
        }
    }

    public void announceDocumentTypeSelected(String descripcion) {
        if (!voiceAssistant.isActive()) {
            return;
        }

        String message = String.format(
                "Has seleccionado %s. Ahora ingresa tu número de documento. " +
                        "Puedes dictarlo número por número o usar el teclado en pantalla.",
                descripcion
        );

        voiceAssistant.speak(message);
    }

    public void registerNumberInput(Consumer<String> onNumbersRecognized) {
        if (!voiceAssistant.isActive()) {
            return;
        }

        voiceAssistant.onNumberRecognized(onNumbersRecognized);
    }

    public void announceValidationSuccess() {
        if (!voiceAssistant.isActive()) {
            return;
        }

        voiceAssistant.speak("Datos correctos. Puedes presionar el botón siguiente o decir 'siguiente'.");
    }

    public void announceValidationError(String errorMessage) {
        if (!voiceAssistant.isActive()) {
            return;
        }

        voiceAssistant.speak(errorMessage);
    }

    public void announceNavigation() {
        if (!voiceAssistant.isActive()) {
            return;
        }

        voiceAssistant.speak("Avanzando a la siguiente pantalla.");
    }

    public void announcedDeleted() {
        if (!voiceAssistant.isActive()) {
            return;
        }

        voiceAssistant.speak("Carácter eliminado.");
    }

    public void announceDeletedAll() {
        if (!voiceAssistant.isActive()) {
            return;
        }

        voiceAssistant.speak("Campo limpiado.");
    }

    public void registerNextCommand(Runnable onNext) {
        if (!voiceAssistant.isActive()) {
            return;
        }

        voiceAssistant.registerCommand("siguiente,continuar,adelante", onNext);
    }

    public void registerDeleteCommand(Runnable onDelete) {
        if (!voiceAssistant.isActive()) {
            return;
        }

        voiceAssistant.registerCommand("borrar,eliminar,quitar", onDelete);
    }

    public void registerDeleteAllCommand(Runnable onDeleteAll) {
        if (!voiceAssistant.isActive()) {
            return;
        }

        voiceAssistant.registerCommand("borrar todo,limpiar,limpiar todo,eliminar todo", onDeleteAll);
    }
}