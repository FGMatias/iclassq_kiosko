package org.iclassq.accessibility.adapter;

import org.iclassq.accessibility.AccessibilityManager;
import org.iclassq.model.domain.SessionData;
import org.iclassq.model.dto.response.GrupoDTO;
import org.iclassq.util.voice.KeywordGenerator;
import org.iclassq.util.voice.VoiceAssistant;
import org.iclassq.view.GruposView;

import java.util.List;
import java.util.function.Consumer;
import java.util.logging.Logger;

public class GruposVoiceAdapter {
    private static final Logger logger = Logger.getLogger(GruposVoiceAdapter.class.getName());

    private final GruposView view;
    private VoiceAssistant voice;

    public GruposVoiceAdapter(GruposView view) {
        this.view = view;
        this.voice = AccessibilityManager.getInstance().getVoiceAssistant();
    }

    public void onGroupsLoaded(List<GrupoDTO> groups, Consumer<GrupoDTO> onGroupSelected) {
        if (!isVoiceActive() || groups == null || groups.isEmpty()) {
            return;
        }

        announceGroups(groups);
        registerCommands(groups, onGroupSelected);
        voice.enableGrammar();

        logger.info("Comandos de voz configurados para grupos");
    }

    public void onGroupSelectedByVoice(GrupoDTO grupo, Consumer<GrupoDTO> callback) {
        if (!isVoiceActive()) {
            callback.accept(grupo);
            return;
        }

        voice.stopSpeaking();
        voice.speak("Has seleccionado " + grupo.getNombre() + ".");

        new Thread(() -> {
            try {
                Thread.sleep(800);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            callback.accept(grupo);
        }).start();
    }

    public void onNavigating() {
        if (!isVoiceActive()) return;

        voice.stopSpeaking();
        voice.speak("Avanzando a la siguiente pantalla.");
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

    public void cleanup() {
        if (!isVoiceActive()) return;

        logger.info("Limpiando comandos de grupos");
        voice.clearCommands();
    }

    private void announceGroups(List<GrupoDTO> groups) {
        String numeroDocumento = SessionData.getInstance().getNumeroDocumento();
        int currentPage = view.getCurrentPage();
        int totalPages = view.getTotalPages();

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

        voice.speak(message.toString());
    }

    private void registerCommands(List<GrupoDTO> groups, Consumer<GrupoDTO> onGroupSelected) {
        for (GrupoDTO group : groups) {
            String keywords = KeywordGenerator.generateKeywords(group.getNombre().toLowerCase());
            voice.registerCommand(keywords, () -> {
                onGroupSelected.accept(group);
            });
        }
    }

    private boolean isVoiceActive() {
        return voice != null && voice.isActive();
    }
}