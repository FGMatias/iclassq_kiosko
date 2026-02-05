package org.iclassq.accessibility;

import org.iclassq.accessibility.braille.BrailleService;
import org.iclassq.accessibility.voice.VoiceManager;
import org.iclassq.util.voice.VoiceAssistant;

import java.util.logging.Logger;

public class AccessibilityManager {

    private static final Logger logger = Logger.getLogger(AccessibilityManager.class.getName());
    private static AccessibilityManager instance;

    private boolean accessibilityEnabled = false;

    private VoiceAssistant voiceAssistant = null;
    private BrailleService brailleService = null;

    private AccessibilityManager() {
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

        if (brailleService == null) {
            brailleService = new BrailleService();
        }

        if (voiceAssistant.isReady()) {
            boolean activated = voiceAssistant.activate();

            if (activated) {
                brailleService.enable();
                accessibilityEnabled = true;
                logger.info("Servicios de accesibilidad habilitados");
                logger.info("   VoiceAssistant compartido activo");
            } else {
                logger.warning("No se pudo activar VoiceAssistant");
                voiceAssistant = null;
                brailleService.disable();
                brailleService = null;
                accessibilityEnabled = false;
            }
        } else {
            logger.warning("VoiceAssistant no está ready");
            voiceAssistant = null;
            brailleService = null;
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

        if (brailleService != null && brailleService.isEnabled()) {
            brailleService.disable();
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

    public BrailleService getBrailleService() {
        return brailleService;
    }

    public boolean isBrailleActive() {
        return brailleService != null && brailleService.isEnabled();
    }

    public void reset() {
        if (voiceAssistant != null && voiceAssistant.isActive()) {
            voiceAssistant.deactivate();
        }

        if (brailleService != null && brailleService.isEnabled()) {
            brailleService.disable();
        }

        voiceAssistant = null;
        brailleService = null;
        accessibilityEnabled = false;

        logger.info("AccessibilityManager reseteado");
    }

}