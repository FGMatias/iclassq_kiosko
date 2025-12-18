package org.iclassq.controller.voice;

import org.iclassq.model.dto.response.TipoDocumentoDTO;
import org.iclassq.util.VoiceAssistant;

import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class IdentificationVoiceHelper {

    private final VoiceAssistant voiceAssistant;

    public IdentificationVoiceHelper(VoiceAssistant voiceAssistant) {
        this.voiceAssistant = voiceAssistant;
    }

    public void announceDocumentTypes(List<TipoDocumentoDTO> documentTypes) {
        if (!voiceAssistant.isEnabled() || documentTypes == null || documentTypes.isEmpty()) {
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
        if (!voiceAssistant.isEnabled() || documentTypes == null) {
            return;
        }

        for (TipoDocumentoDTO docType : documentTypes) {
            String descripcion = docType.getDescripcion().toLowerCase();

            String keywords = generateKeywords(descripcion);

            voiceAssistant.registerCommand(keywords, () -> {
                onDocumentSelected.accept(docType);
            });
        }
    }

    public void announceDocumentTypeSelected(String descripcion) {
        if (!voiceAssistant.isEnabled()) {
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
        if (!voiceAssistant.isEnabled()) {
            return;
        }

        voiceAssistant.onNumberRecognized(onNumbersRecognized);
    }

    public void announceValidationSuccess() {
        if (!voiceAssistant.isEnabled()) {
            return;
        }

        voiceAssistant.speak("Datos correctos. Puedes presionar el botón siguiente o decir 'siguiente'.");
    }

    public void announceValidationError(String errorMessage) {
        if (!voiceAssistant.isEnabled()) {
            return;
        }

        voiceAssistant.speak(errorMessage);
    }

    public void announceNavigation() {
        if (!voiceAssistant.isEnabled()) {
            return;
        }

        voiceAssistant.speak("Avanzando a la siguiente pantalla.");
    }

    public void registerNextCommand(Runnable onNext) {
        if (!voiceAssistant.isEnabled()) {
            return;
        }

        voiceAssistant.registerCommand("siguiente,continuar,adelante", onNext);
    }

    private String generateKeywords(String descripcion) {
        StringBuilder keywords = new StringBuilder(descripcion);

        if (descripcion.contains("dni") || descripcion.contains("documento nacional")) {
            keywords.append(",dni,de ene i,documento nacional");
        } else if (descripcion.contains("pasaporte")) {
            keywords.append(",pasaporte");
        } else if (descripcion.contains("carnet") || descripcion.contains("extranjeria") || descripcion.contains("extranjería")) {
            keywords.append(",carnet,extranjeria,carnet de extranjeria");
        }

        return keywords.toString();
    }
}