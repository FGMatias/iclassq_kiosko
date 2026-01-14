package org.iclassq.accessibility.adapter;

import org.iclassq.accessibility.AccessibilityManager;
import org.iclassq.model.domain.SessionData;
import org.iclassq.model.dto.response.SubGrupoDTO;
import org.iclassq.util.voice.KeywordGenerator;
import org.iclassq.util.voice.VoiceAssistant;
import org.iclassq.view.SubGruposView;

import java.util.List;
import java.util.function.Consumer;
import java.util.logging.Logger;

public class SubGruposVoiceAdapter {

    private static final Logger logger = Logger.getLogger(SubGruposVoiceAdapter.class.getName());

    private final SubGruposView view;
    private VoiceAssistant voice;

    public SubGruposVoiceAdapter(SubGruposView view) {
        this.view = view;
        this.voice = AccessibilityManager.getInstance().getVoiceAssistant();
    }

    public void onSubGroupsLoaded(List<SubGrupoDTO> subGroups) {
        if (!isVoiceActive() || subGroups == null || subGroups.isEmpty()) {
            return;
        }

        announceSubGroups(subGroups);
        registerCommands(subGroups);
        voice.enableGrammar();

        logger.info("Comandos de voz configurados para subgrupos");
    }

    public void onSubGroupSelectedByVoice(SubGrupoDTO subGrupo, Consumer<SubGrupoDTO> callback) {
        if (!isVoiceActive()) {
            callback.accept(subGrupo);
            return;
        }

        voice.stopSpeaking();
        voice.speak("Seleccionaste " + subGrupo.getVNombreSubGrupo() + ". Generando tu ticket.");

        new Thread(() -> {
            try {
                Thread.sleep(800);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            callback.accept(subGrupo);
        }).start();
    }

    public void onGeneratingTicket() {
        if (!isVoiceActive()) return;

        voice.stopSpeaking();
    }

    public void onPageChanged() {
        if (!isVoiceActive()) return;

        int currentPage = view.getCurrentPage();
        int totalPages = view.getTotalPages();

        voice.speak(String.format("Página %d de %d.", currentPage, totalPages));
    }

    public void onGoingBack() {
        if (!isVoiceActive()) return;

        voice.stopSpeaking();
        voice.speak("Regresando a la pantalla anterior.");
    }

    public void registerNavigationCommands(
            Runnable onPrevious,
            Runnable onNext,
            Runnable onBack
    ) {
        if (!isVoiceActive()) return;

        voice.registerCommand("anterior,atras,pagina anterior", onPrevious);
        voice.registerCommand("siguiente,adelante,pagina siguiente", onNext);
        voice.registerCommand("regresar,volver,atras", onBack);
    }

    private void announceSubGroups(List<SubGrupoDTO> subGroups) {
        String nombreGrupo = SessionData.getInstance().getGrupo().getNombre();
        int currentPage = view.getCurrentPage();
        int totalPages = view.getTotalPages();

        StringBuilder message = new StringBuilder();
        message.append("Grupo seleccionado: ").append(nombreGrupo).append(". ");
        message.append("Selecciona un servicio. ");

        if (totalPages > 1) {
            message.append("Página ").append(currentPage).append(" de ").append(totalPages).append(". ");
        }

        message.append("Las opciones son: ");
        for (int i = 0; i < subGroups.size(); i++) {
            message.append(subGroups.get(i).getVNombreSubGrupo());
            if (i < subGroups.size() - 1) {
                message.append(", ");
            }
        }

        voice.speak(message.toString());
    }

    private void registerCommands(List<SubGrupoDTO> subGroups) {
        for (SubGrupoDTO subGrupo : subGroups) {
            String keywords = KeywordGenerator.generateKeywords(subGrupo.getVNombreSubGrupo());
            voice.registerCommand(keywords, () -> {
            });
        }
    }

    private boolean isVoiceActive() {
        return voice != null && voice.isActive();
    }
}