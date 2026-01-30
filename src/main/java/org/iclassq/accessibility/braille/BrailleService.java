package org.iclassq.accessibility.braille;

import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.logging.Logger;

public class BrailleService {
    private static final Logger logger = Logger.getLogger(BrailleService.class.getName());

    private boolean enabled = false;
    private Consumer<Integer> onButtonPressed;

    private static final Map<KeyCode, Integer> KEY_MAPPING = new HashMap<>();

    static {
        KEY_MAPPING.put(KeyCode.F12, 0);
        KEY_MAPPING.put(KeyCode.F11, 1);
        KEY_MAPPING.put(KeyCode.F10, 2);
        KEY_MAPPING.put(KeyCode.F9, 3);
        KEY_MAPPING.put(KeyCode.F8, 4);
    }

    public void enable() {
        this.enabled = true;
        logger.info("🔲 Servicio Braille ACTIVADO (F12-F8)");
        logger.info("   F12 (Verde)   → Grupo 1");
        logger.info("   F11 (Azul)    → Grupo 2");
        logger.info("   F10 (Amarillo)→ Grupo 3");
        logger.info("   F9  (Rojo)    → Grupo 4");
        logger.info("   F8  (Blanco)  → Grupo 5");
    }

    public void disable() {
        this.enabled = false;
        this.onButtonPressed = null;
        logger.info("Servicio Braille DESACTIVADO");
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void onButtonPressed(Consumer<Integer> callback) {
        this.onButtonPressed = callback;
    }

    public boolean handleKeyPressed(KeyEvent event) {
        if (!enabled) {
            return false;
        }

        KeyCode code = event.getCode();

        if (KEY_MAPPING.containsKey(code)) {
            Integer groupIndex = KEY_MAPPING.get(code);

            logger.info(String.format("Braille: Tecla %s presionada → Grupo %d (%s)",
                    code, groupIndex + 1, getButtonName(groupIndex)));

            if (onButtonPressed != null) {
                onButtonPressed.accept(groupIndex);
            }

            event.consume();
            return true;
        }

        return false;
    }

    public String getKeyName(int groupIndex) {
        switch (groupIndex) {
            case 0: return "F12";
            case 1: return "F11";
            case 2: return "F10";
            case 3: return "F9";
            case 4: return "F8";
            default: return "UNKNOWN";
        }
    }

    public String getButtonName(int groupIndex) {
        switch (groupIndex) {
            case 0: return "Verde";
            case 1: return "Azul";
            case 2: return "Amarillo";
            case 3: return "Rojo";
            case 4: return "Blanco";
            default: return "Desconocido";
        }
    }
}