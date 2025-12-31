package org.iclassq.controller.voice;

import org.iclassq.model.dto.response.GrupoDTO;
import org.iclassq.util.voice.KeywordGenerator;
import org.iclassq.util.voice.VoiceAssistant;

import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class GruposVoiceHelper {

    private final VoiceAssistant voiceAssistant;

    public GruposVoiceHelper(VoiceAssistant voiceAssistant) {
        this.voiceAssistant = voiceAssistant;
    }

    public void announceGroups(
            String numeroDocumento,
            List<GrupoDTO> groups,
            int currentPage,
            int totalPages
    ) {
        if (!voiceAssistant.isEnabled() || groups == null || groups.isEmpty()) {
            return;
        }

        StringBuilder message = new StringBuilder();

        message.append("Número de documento ingresado: ");
        for (char c : numeroDocumento.toCharArray()) {
            message.append(c).append(", ");
        }

        message.append(". Selecciona una opción de atención.");

        if (totalPages > 1) {
            message.append("Página ")
                    .append(currentPage)
                    .append(" de ")
                    .append(totalPages)
                    .append(". ");
        }

        message.append("Las opciones son: ");

        for (int i = 0; i < groups.size(); i++) {
            message.append(groups.get(i).getNombre());
            if (i < groups.size() - 1) {
                message.append(", ");
            }
        }

        message.append(". También puedes usar el teclado para seleccionar un grupo.");

        voiceAssistant.speak(message.toString());
    }

    public void registerGroupCommands(
            List<GrupoDTO> groups,
            Consumer<GrupoDTO> onSelected
    ) {
        if (!voiceAssistant.isEnabled()) {
            return;
        }

        for (GrupoDTO group : groups) {
            String nombre = group.getNombre().toLowerCase();

            String keywords = KeywordGenerator.generateKeywords(nombre);

            voiceAssistant.registerCommand(keywords, () -> {
                onSelected.accept(group);
            });
        }
    }

    public void registerPreviousPageCommand(Runnable onPrevious) {
        if (!voiceAssistant.isEnabled()) {
            return;
        }

        voiceAssistant.registerCommand("anterior,atras,pagina anterior", onPrevious);
    }

    public void registerNextPageCommand(Runnable onNext) {
        if (!voiceAssistant.isEnabled()) {
            return;
        }

        voiceAssistant.registerCommand("siguiente,adelante,pagina siguiente", onNext);
    }

    public void registerBackCommand(Runnable onBack) {
        if (!voiceAssistant.isEnabled()) {
            return;
        }

        voiceAssistant.registerCommand("regresar,volver,atras", onBack);
    }

    public void announceGroupSelected(String nombreGrupo) {
        if (!voiceAssistant.isEnabled()) {
            return;
        }

        String message = String.format(
                "Has seleccionado %s.",
                nombreGrupo
        );

        voiceAssistant.speak(message);
    }

    public void announcePageChange(int currentPage, int totalPages) {
        if (!voiceAssistant.isEnabled()) {
            return;
        }

        String message = String.format(
                "Página %d de %d.",
                currentPage,
                totalPages
        );

        voiceAssistant.speak(message);
    }

    public void announceNoMorePages(String direction) {
        if (!voiceAssistant.isEnabled()) {
            return;
        }

        if ("next".equals(direction)) {
            voiceAssistant.speak("No hay más páginas siguientes.");
        } else {
            voiceAssistant.speak("Ya estás en la primera página.");
        }
    }

    public void announceNavigation() {
        if (!voiceAssistant.isEnabled()) {
            return;
        }

        voiceAssistant.speak("Avanzando a la siguiente pantalla.");
    }

    public void announceBack() {
        if (!voiceAssistant.isEnabled()) {
            return;
        }

        voiceAssistant.speak("Regresando a la pantalla anterior.");
    }



    private String formatDocumentNumber(String numero) {
        if (numero == null || numero.isEmpty()) {
            return "sin número";
        }

        StringBuilder formatted = new StringBuilder();
        for (int i = 0; i < numero.length(); i++) {
            formatted.append(numero.charAt(i));
            if (i < numero.length() - 1) {
                formatted.append(" ");
            }
        }

        return formatted.toString();
    }
}