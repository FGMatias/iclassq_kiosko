package org.iclassq.accessibility;

import lombok.Getter;
import org.iclassq.accessibility.voice.VoiceAssistant;

import java.util.logging.Logger;

/**
 * Manager singleton para coordinar servicios de accesibilidad
 * Activa/desactiva servicios según detección de discapacidad
 */
public class AccessibilityManager {

    private static final Logger logger = Logger.getLogger(AccessibilityManager.class.getName());
    private static AccessibilityManager instance;

    @Getter
    private AccessibilityMode currentMode;

    @Getter
    private VoiceAssistant voiceAssistant;

    @Getter
    private boolean voiceEnabled;

    // Para futuro
    @Getter
    private boolean brailleEnabled;

    /**
     * Constructor privado (Singleton)
     */
    private AccessibilityManager() {
        this.currentMode = AccessibilityMode.NORMAL;
        this.voiceEnabled = false;
        this.brailleEnabled = false;

        logger.info("🎯 AccessibilityManager inicializado");
    }

    /**
     * Obtiene la instancia singleton
     */
    public static synchronized AccessibilityManager getInstance() {
        if (instance == null) {
            instance = new AccessibilityManager();
        }
        return instance;
    }

    /**
     * Activa los servicios de accesibilidad
     */
    public void enableAccessibility() {
        logger.info("🔊 Activando servicios de accesibilidad...");

        currentMode = AccessibilityMode.ACCESSIBLE;

        // Activar voz
        enableVoice();

        // (Futuro) Activar braille
        // enableBraille();

        logger.info("✅ Servicios de accesibilidad ACTIVADOS");
    }

    /**
     * Desactiva los servicios de accesibilidad
     */
    public void disableAccessibility() {
        logger.info("🔇 Desactivando servicios de accesibilidad...");

        currentMode = AccessibilityMode.NORMAL;

        // Desactivar voz
        disableVoice();

        // (Futuro) Desactivar braille
        // disableBraille();

        logger.info("✅ Servicios de accesibilidad DESACTIVADOS");
    }

    /**
     * Activa el servicio de voz
     */
    private void enableVoice() {
        try {
            if (voiceAssistant == null) {
                // Inicializar VoiceAssistant si no existe
                voiceAssistant = VoiceAssistant.getInstance();
            }

            // El VoiceAssistant ya tiene su propio sistema de habilitación
            // Solo necesitamos asegurarnos de que esté disponible
            voiceEnabled = true;

            logger.info("✅ Servicio de VOZ activado");

        } catch (Exception e) {
            logger.severe("❌ Error activando servicio de voz: " + e.getMessage());
            voiceEnabled = false;
        }
    }

    /**
     * Desactiva el servicio de voz
     */
    private void disableVoice() {
        try {
            if (voiceAssistant != null) {
                voiceAssistant.stopSpeaking();
                voiceAssistant.stopListening();
            }

            voiceEnabled = false;

            logger.info("✅ Servicio de VOZ desactivado");

        } catch (Exception e) {
            logger.severe("❌ Error desactivando servicio de voz: " + e.getMessage());
        }
    }

    /**
     * Verifica si los servicios de accesibilidad están activos
     */
    public boolean isAccessibilityEnabled() {
        return currentMode == AccessibilityMode.ACCESSIBLE;
    }

    public String getStatusInfo() {
        StringBuilder sb = new StringBuilder();
        sb.append("\n═══════════════════════════════════════\n");
        sb.append("♿ ACCESSIBILITY MANAGER STATUS\n");
        sb.append("═══════════════════════════════════════\n");
        sb.append(String.format("Modo actual: %s\n", currentMode));
        sb.append(String.format("Voz: %s\n", voiceEnabled ? "✅ Activa" : "❌ Inactiva"));
        sb.append(String.format("Braille: %s (futuro)\n", brailleEnabled ? "✅ Activa" : "❌ Inactiva"));
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