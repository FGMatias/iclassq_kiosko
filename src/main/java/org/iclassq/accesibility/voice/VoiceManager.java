package org.iclassq.accesibility.voice;

import org.iclassq.accesibility.voice.impl.SpeechToTextServiceImpl;
import org.iclassq.accesibility.voice.impl.TextToSpeechServiceImpl;

import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

public class VoiceManager {
    private static final Logger logger = Logger.getLogger(VoiceManager.class.getName());
    private static volatile VoiceManager instance;
    private TextToSpeechService textToSpeechService;
    private SpeechToTextService speechToTextService;
    private boolean voiceServicesEnabled = false;
    private boolean autoSpeak = true;
    private boolean autoListen = false;

    private VoiceManager() { }

    public static VoiceManager getInstance() {
        if (instance == null) {
            synchronized (VoiceManager.class) {
                if (instance == null) {
                    instance = new VoiceManager();
                }
            }
        }
        return instance;
    }

    public synchronized void enableVoiceServices() {
        if (voiceServicesEnabled) {
            logger.info("Los servicios de voz ya estan habilitados");
            return;
        }

        logger.info("Habilitando servicios de voz para Windows");

        try {
            logger.info("Inicializando Windows SAPI...");
            textToSpeechService = new TextToSpeechServiceImpl();
            textToSpeechService.init();
            logger.info("Servicio TTS habilitado");
        } catch (Exception e) {
            logger.severe("No se pudo habilitar TTS");
            logger.severe("La aplicacion continuará sin sintesis de voz");
            textToSpeechService = null;
        }

        try {
            logger.info("Inicializando Vosk");
            speechToTextService = new SpeechToTextServiceImpl();
            speechToTextService.init();
            logger.info("Servicio STT habilitado");
        } catch (Exception e) {
            logger.log(Level.WARNING, "No se pudo habilitar STT. " + e);
            logger.warning("La aplicacion continuara sin reconocimiento de voz");
            speechToTextService = null;
        }

        voiceServicesEnabled = (textToSpeechService != null && textToSpeechService.isAvailable()) ||
                                (speechToTextService != null && speechToTextService.isAvailable());

        if (voiceServicesEnabled) {
            logger.info("Servicios de voz habilitados correctamente");
            logServiceStatus();
        } else {
            logger.warning("No se pudo habilitar ningun servicio de voz");
            logger.warning("La aplicación funcionará normalmente sin servicios de voz");
        }
    }

    public synchronized void disableVoiceServices() {
        if (!voiceServicesEnabled) {
            return;
        }

        logger.info("Desabilitando servicios de voz");

        if (textToSpeechService != null) {
            textToSpeechService.shutdown();
            textToSpeechService = null;
        }

        if (speechToTextService != null) {
            speechToTextService.shutdown();
            speechToTextService = null;
        }

        voiceServicesEnabled = false;
        logger.info("Servicios de voz deshabilitados");
    }

    private void logServiceStatus() {
        logger.info("═══════════════════════════════════════");
        logger.info("  ESTADO DE SERVICIOS DE VOZ");
        logger.info("═══════════════════════════════════════");
        logger.info("  TTS (Síntesis):      " + (isTTSAvailable() ? "Disponible (Windows SAPI)" : "No disponible"));
        logger.info("  STT (Reconocimiento): " + (isSTTAvailable() ? "Disponible (Vosk)" : "No disponible"));
        logger.info("  Auto-hablar:         " + (autoSpeak ? "Activo" : "Inactivo"));
        logger.info("  Auto-escuchar:       " + (autoListen ? "Activo" : "Inactivo"));
        logger.info("═══════════════════════════════════════");
    }

    public void speak(String text) {
        if (textToSpeechService != null && textToSpeechService.isAvailable() && autoSpeak) {
            textToSpeechService.speak(text);
        }
    }

    public void speakUrgent(String text) {
        if (textToSpeechService != null && textToSpeechService.isAvailable()) {
            textToSpeechService.speakUrgent(text);
        }
    }

    public void stopSpeaking() {
        if (textToSpeechService != null) {
            textToSpeechService.stop();
        }
    }

    public boolean isSpeaking() {
        return textToSpeechService != null && textToSpeechService.isSpeaking();
    }

    public void startListening() {
        if (speechToTextService != null && speechToTextService.isAvailable()) {
            speechToTextService.startListening();
        } else {
            logger.warning("No se puede iniciar escucha: STT no disponible");
        }
    }

    public void stopListening() {
        if (speechToTextService != null) {
            speechToTextService.stopListening();
        }
    }

    public boolean isListening() {
        return speechToTextService != null && speechToTextService.isListening();
    }

    public void addTextRecognizedListener(Consumer<String> listener) {
        if (speechToTextService != null) {
            speechToTextService.addTextRecognizedListener(listener);
        } else {
            logger.warning("No se puede agregar listener: STT no disponible");
        }
    }

    public void removeTextRecognizedListener(Consumer<String> listener) {
        if (speechToTextService != null) {
            speechToTextService.removeTextRecognizedListener(listener);
        }
    }

    public void addErrorListener(Consumer<String> listener) {
        if (speechToTextService != null) {
            speechToTextService.addErrorListener(listener);
        }
    }

    public void clearListener() {
        if (speechToTextService != null) {
            speechToTextService.clearListeners();
        }
    }

    public void setAutoSpeak(boolean autoSpeak) {
        this.autoSpeak = autoSpeak;
        logger.info("Auto-hablar: " + (autoSpeak ? "activado" : "desactivado"));
    }

    public void setAutoListen(boolean autoListen) {
        this.autoListen = autoListen;
        logger.info("Auto-escuchar: " + (autoListen ? "activado" : "desactivado"));
    }

    public void setSpeechRate(float rate) {
        if (textToSpeechService != null) {
            textToSpeechService.setRate(rate);
        }
    }

    public void setVolume(float volume) {
        if (textToSpeechService != null) {
            textToSpeechService.setVolume(volume);
        }
    }

    public void setLanguage(String languageCode) {
        if (speechToTextService != null) {
            speechToTextService.setLanguage(languageCode);
        }
    }

    public void setConfidenceThreshold(float threshold) {
        if (speechToTextService != null) {
            speechToTextService.setConfidenceThreshold(threshold);
        }
    }

    public boolean isVoiceServicesEnabled() {
        return voiceServicesEnabled;
    }

    public boolean isTTSAvailable() {
        return textToSpeechService != null && textToSpeechService.isAvailable();
    }

    public boolean isSTTAvailable() {
        return speechToTextService != null && speechToTextService.isAvailable();
    }

    public boolean isAutoSpeak() {
        return autoSpeak;
    }

    public boolean isAutoListen() {
        return autoListen;
    }

    public TextToSpeechService getTtsService() {
        return textToSpeechService;
    }

    public SpeechToTextService getSttService() {
        return speechToTextService;
    }
}
