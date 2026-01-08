package org.iclassq.accessibility;

import lombok.Getter;
import org.iclassq.util.voice.VoiceAssistant;

import java.util.logging.Logger;

public class AccessibilityManager {

    private static final Logger logger = Logger.getLogger(AccessibilityManager.class.getName());
    private static AccessibilityManager instance;

    @Getter
    private AccessibilityMode currentMode;

    @Getter
    private VoiceAssistant voiceAssistant;

    @Getter
    private boolean voiceEnabled;

    @Getter
    private boolean brailleEnabled;

    private AccessibilityManager() {
        this.currentMode = AccessibilityMode.NORMAL;
        this.voiceEnabled = false;
        this.brailleEnabled = false;

        logger.info("AccessibilityManager inicializado");
    }

    public static synchronized AccessibilityManager getInstance() {
        if (instance == null) {
            instance = new AccessibilityManager();
        }
        return instance;
    }

    public void enableAccessibility() {
        logger.info("Activando servicios de accesibilidad...");

        currentMode = AccessibilityMode.ACCESSIBLE;

        enableVoice();

        logger.info("Servicios de accesibilidad ACTIVADOS");
    }

    public void disableAccessibility() {
        logger.info("Desactivando servicios de accesibilidad...");

        currentMode = AccessibilityMode.NORMAL;

        disableVoice();

        logger.info("Servicios de accesibilidad DESACTIVADOS");
    }

    private void enableVoice() {
        try {
            if (voiceAssistant == null) {
                voiceAssistant = new VoiceAssistant();
            }

            voiceEnabled = true;

            logger.info("Servicio de VOZ activado");

        } catch (Exception e) {
            logger.severe("Error activando servicio de voz: " + e.getMessage());
            voiceEnabled = false;
        }
    }

    private void disableVoice() {
        try {
            if (voiceAssistant != null) {
                voiceAssistant.stopSpeaking();
                voiceAssistant.cleanup();
            }

            voiceEnabled = false;

            logger.info("Servicio de VOZ desactivado");

        } catch (Exception e) {
            logger.severe("Error desactivando servicio de voz: " + e.getMessage());
        }
    }

    public boolean isAccessibilityEnabled() {
        return currentMode == AccessibilityMode.ACCESSIBLE;
    }

    public String getStatusInfo() {
        StringBuilder sb = new StringBuilder();
        sb.append("\n═══════════════════════════════════════\n");
        sb.append("ACCESSIBILITY MANAGER STATUS\n");
        sb.append("═══════════════════════════════════════\n");
        sb.append(String.format("Modo actual: %s\n", currentMode));
        sb.append(String.format("Voz: %s\n", voiceEnabled ? "Activa" : "Inactiva"));
        sb.append(String.format("Braille: %s (futuro)\n", brailleEnabled ? "Activa" : "Inactiva"));
        sb.append("═══════════════════════════════════════\n");
        return sb.toString();
    }

    public void printStatus() {
        System.out.println(getStatusInfo());
    }

    public static synchronized void reset() {
        if (instance != null) {
            instance.disableAccessibility();
            instance = null;
        }
    }
}