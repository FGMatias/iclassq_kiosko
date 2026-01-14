package org.iclassq.accessibility;

import org.iclassq.accessibility.voice.VoiceManager;
import org.iclassq.util.voice.VoiceAssistant;

import java.util.logging.Logger;

public class AccessibilityManager {

    private static final Logger logger = Logger.getLogger(AccessibilityManager.class.getName());
    private static AccessibilityManager instance;

    private boolean accessibilityEnabled = false;

    private VoiceAssistant voiceAssistant = null;

    private AccessibilityManager() {
        logger.info("AccessibilityManager inicializado");
    }

    public static synchronized AccessibilityManager getInstance() {
        if (instance == null) {
            instance = new AccessibilityManager();
        }
        return instance;
    }

    public void enableAccessibility() {
        if (accessibilityEnabled) {
            logger.info("Servicios de accesibilidad ya están habilitados");
            return;
        }

        logger.info("Habilitando servicios de accesibilidad...");

        if (voiceAssistant == null) {
            voiceAssistant = new VoiceAssistant();
        }

        if (voiceAssistant.isReady()) {
            boolean activated = voiceAssistant.activate();

            if (activated) {
                accessibilityEnabled = true;
                logger.info("Servicios de accesibilidad HABILITADOS");
                logger.info("   VoiceAssistant compartido ACTIVO");
            } else {
                logger.warning("No se pudo activar VoiceAssistant");
                voiceAssistant = null;
                accessibilityEnabled = false;
            }
        } else {
            logger.warning("VoiceAssistant no está ready");
            voiceAssistant = null;
            accessibilityEnabled = false;
        }
    }

    public void disableAccessibility() {
        if (!accessibilityEnabled) {
            logger.info("Servicios de accesibilidad ya están deshabilitados");
            return;
        }

        logger.info("Desactivando servicios de accesibilidad...");

        if (voiceAssistant != null && voiceAssistant.isActive()) {
            voiceAssistant.deactivate();
        }

        accessibilityEnabled = false;

        logger.info("Servicios de accesibilidad DESACTIVADOS");
    }

    public boolean isAccessibilityEnabled() {
        return accessibilityEnabled;
    }

    public VoiceAssistant getVoiceAssistant() {
        return voiceAssistant;
    }

    public boolean isVoiceActive() {
        return voiceAssistant != null && voiceAssistant.isActive();
    }

    public void reset() {
        if (voiceAssistant != null && voiceAssistant.isActive()) {
            voiceAssistant.deactivate();
        }

        voiceAssistant = null;
        accessibilityEnabled = false;

        logger.info("AccessibilityManager reseteado");
    }

    public String getStatusInfo() {
        StringBuilder sb = new StringBuilder();
        sb.append("\n═══════════════════════════════════════\n");
        sb.append("ACCESSIBILITY MANAGER STATUS\n");
        sb.append("═══════════════════════════════════════\n");
        sb.append(String.format("Accesibilidad: %s\n",
                accessibilityEnabled ? "HABILITADA" : "DESHABILITADA"));
        sb.append(String.format("VoiceAssistant: %s\n",
                voiceAssistant != null ? "DISPONIBLE" : "NULL"));
        sb.append(String.format("Voz Activa: %s\n",
                isVoiceActive() ? "SÍ" : "NO"));
        sb.append("═══════════════════════════════════════\n");
        return sb.toString();
    }

    public void printStatus() {
        System.out.println(getStatusInfo());
    }
}