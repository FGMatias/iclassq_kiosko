package org.iclassq.accessibility.adapter;

import javafx.scene.input.KeyEvent;
import org.iclassq.accessibility.AccessibilityManager;
import org.iclassq.accessibility.braille.BrailleService;
import org.iclassq.model.dto.response.GrupoDTO;
import org.iclassq.util.voice.VoiceAssistant;

import java.util.List;
import java.util.function.Consumer;
import java.util.logging.Logger;

public class GruposBrailleAdapter {

    private static final Logger logger = Logger.getLogger(GruposBrailleAdapter.class.getName());

    private final BrailleService brailleService;
    private final VoiceAssistant voiceAssistant;

    private List<GrupoDTO> grupos;
    private Consumer<GrupoDTO> onGroupSelected;

    public GruposBrailleAdapter() {
        AccessibilityManager manager = AccessibilityManager.getInstance();
        this.brailleService = manager.getBrailleService();
        this.voiceAssistant = manager.getVoiceAssistant();
    }

    public void onGroupsLoaded(List<GrupoDTO> grupos, Consumer<GrupoDTO> onGroupSelected) {
        if (!isBrailleActive()) {
            logger.info("Braille no está activo, omitiendo configuración");
            return;
        }

        if (grupos.size() > 5) {
            logger.warning(String.format("Se recibieron %d grupos, limitando a 5 para Braille", grupos.size()));
            this.grupos = grupos.subList(0, 5);
        } else {
            this.grupos = grupos;
        }

        this.onGroupSelected = onGroupSelected;

        brailleService.onButtonPressed(this::handleButtonPressed);

        logger.info(String.format("Braille configurado con %d grupos", this.grupos.size()));
    }

    public boolean handleKeyEvent(KeyEvent event) {
        if (!isBrailleActive()) {
            return false;
        }

        return brailleService.handleKeyPressed(event);
    }

    private void handleButtonPressed(int groupIndex) {
        if (grupos == null || grupos.isEmpty()) {
            logger.warning("No hay grupos cargados");
            return;
        }

        if (groupIndex >= grupos.size()) {
            logger.warning(String.format("Índice %d fuera de rango (grupos disponibles: %d)",
                    groupIndex, grupos.size()));

            if (voiceAssistant != null && voiceAssistant.isActive()) {
                voiceAssistant.speak("Opción no disponible.");
            }
            return;
        }

        GrupoDTO selectedGroup = grupos.get(groupIndex);

        logger.info(String.format("Grupo seleccionado con Braille: %s (índice %d)",
                selectedGroup.getNombre(), groupIndex));

        if (voiceAssistant != null && voiceAssistant.isActive()) {
            String message = String.format("Has seleccionado %s. Generando ticket.",
                    selectedGroup.getNombre());
            voiceAssistant.stopSpeaking();
            voiceAssistant.speak(message);
        }

        new Thread(() -> {
            try {
                Thread.sleep(800);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            if (onGroupSelected != null) {
                onGroupSelected.accept(selectedGroup);
            }
        }).start();
    }

    public void cleanup() {
        if (brailleService != null) {
            brailleService.onButtonPressed(null);
        }

        grupos = null;
        onGroupSelected = null;

        logger.info("GruposBrailleAdapter limpiado");
    }

    private boolean isBrailleActive() {
        return brailleService != null && brailleService.isEnabled();
    }
}