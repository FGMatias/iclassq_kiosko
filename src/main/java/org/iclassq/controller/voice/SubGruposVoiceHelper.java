package org.iclassq.controller.voice;

import org.iclassq.model.dto.response.SubGrupoDTO;
import org.iclassq.util.voice.KeywordGenerator;
import org.iclassq.util.voice.VoiceAssistant;

import java.util.List;
import java.util.function.Consumer;

public class SubGruposVoiceHelper {
    private final VoiceAssistant voiceAssistant;

    public SubGruposVoiceHelper(VoiceAssistant voiceAssistant) {
        this.voiceAssistant = voiceAssistant;
    }

    public void announceSubGroups(String groupName, List<SubGrupoDTO> subgrupos, int currentPage, int totalPages) {
        if (!voiceAssistant.isEnabled() || subgrupos.isEmpty()) {
            return;
        }

        StringBuilder message = new StringBuilder();

        message.append("Grupo seleccionado: ").append(groupName).append(". ");
        message.append("Selecciona un servicio. ");

        if (totalPages > 1) {
            message.append("Página ").append(currentPage).append(" de ").append(totalPages).append(". ");
        }

        message.append("Las opciones son: ");

        for (int i = 0; i < subgrupos.size(); i++) {
            message.append(subgrupos.get(i).getVNombreSubGrupo());
            if (i < subgrupos.size() - 1) {
                message.append(", ");
            }
        }

        voiceAssistant.speak(message.toString());
    }

    public void registerSubGroupCommands(List<SubGrupoDTO> subgrupos, Consumer<SubGrupoDTO> onSelect) {
        if (!voiceAssistant.isEnabled()) {
            return;
        }

        for (SubGrupoDTO subgrupo : subgrupos) {
            String keywords = KeywordGenerator.generateKeywords(subgrupo.getVNombreSubGrupo());
            voiceAssistant.registerCommand(keywords, () -> onSelect.accept(subgrupo));
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

    public void announceSubGroupSelected(String subGroupName) {
        if (!voiceAssistant.isEnabled()) {
            return;
        }

        voiceAssistant.speak(String.format("Seleccionaste %s. Generando tu ticket.", subGroupName));
    }

    public void announcePageChanged(int currentPage, int totalPages) {
        if (!voiceAssistant.isEnabled()) {
            return;
        }

        voiceAssistant.speak(String.format("Página %d de %d", currentPage, totalPages));
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

    public void announceBack() {
        if (!voiceAssistant.isEnabled()) {
            return;
        }

        voiceAssistant.speak("Regresando a grupos.");
    }
}