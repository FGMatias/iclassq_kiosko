package org.iclassq.util;

import javafx.application.Platform;
import org.iclassq.accessibility.voice.VoiceManager;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.logging.Logger;

public class VoiceAssistant {

    private static final Logger logger = Logger.getLogger(VoiceAssistant.class.getName());
    private final VoiceManager voiceManager;
    private final Map<String, Runnable> commandHandlers = new HashMap<>();
    private boolean enabled = false;

    public VoiceAssistant() {
        this.voiceManager = VoiceManager.getInstance();
        initialize();
    }

    private void initialize() {
        try {
            if (voiceManager != null && voiceManager.isVoiceServicesEnabled()) {
                enabled = true;
                voiceManager.startListening();

                voiceManager.addTextRecognizedListener(this::handleRecognizedText);

                logger.info("VoiceAssistant activado");
            } else {
                logger.info("VoiceAssistant desactivado (VoiceManager no disponible)");
            }
        } catch (Exception e) {
            logger.warning("No se pudo activar VoiceAssistant: " + e.getMessage());
            enabled = false;
        }
    }

    public void speak(String message) {
        if (!enabled || message == null || message.isEmpty()) {
            return;
        }

        try {
            voiceManager.speak(message);
        } catch (Exception e) {
            logger.warning("Error al hablar: " + e.getMessage());
        }
    }

    public void registerCommand(String keywords, Runnable action) {
        if (!enabled) return;

        String[] keywordArray = keywords.toLowerCase().split(",");
        for (String keyword : keywordArray) {
            commandHandlers.put(keyword.trim(), action);
        }
    }

    public void onNumberRecognized(Consumer<String> handler) {
        registerCommand("__NUMBERS__", () -> {});
        commandHandlers.put("__NUMBERS__", () -> {});
    }

    public void clearCommands() {
        commandHandlers.clear();
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void cleanup() {
        if (!enabled) return;

        try {
            voiceManager.stopListening();
            voiceManager.clearListener();
            commandHandlers.clear();
        } catch (Exception e) {
            logger.warning("Error en cleanup: " + e.getMessage());
        }
    }

    private void handleRecognizedText(String text) {
        if (text == null || text.isEmpty()) return;

        String normalized = normalizeText(text);
        logger.info("Texto reconocido: " + text + " (normalizado: " + normalized + ")");

        for (Map.Entry<String, Runnable> entry : commandHandlers.entrySet()) {
            String keyword = entry.getKey();

            if (keyword.equals("__NUMBERS__")) {
                String numbers = extractNumbers(text);
                if (!numbers.isEmpty()) {
                    logger.info("Números extraídos: " + numbers);
                }
                continue;
            }

            if (normalized.contains(keyword)) {
                logger.info("Comando detectado: " + keyword);
                Platform.runLater(entry.getValue());
                return;
            }
        }
    }

    private String normalizeText(String text) {
        if (text == null) return "";
        return text.toLowerCase()
                .replaceAll("[áàäâ]", "a")
                .replaceAll("[éèëê]", "e")
                .replaceAll("[íìïî]", "i")
                .replaceAll("[óòöô]", "o")
                .replaceAll("[úùüû]", "u")
                .trim();
    }

    private String extractNumbers(String text) {
        StringBuilder numbers = new StringBuilder();

        Map<String, String> numberWords = new HashMap<>();
        numberWords.put("cero", "0");
        numberWords.put("uno", "1");
        numberWords.put("dos", "2");
        numberWords.put("tres", "3");
        numberWords.put("cuatro", "4");
        numberWords.put("cinco", "5");
        numberWords.put("seis", "6");
        numberWords.put("siete", "7");
        numberWords.put("ocho", "8");
        numberWords.put("nueve", "9");

        String normalized = normalizeText(text);
        String[] words = normalized.split("\\s+");

        for (String word : words) {
            if (numberWords.containsKey(word)) {
                numbers.append(numberWords.get(word));
            } else if (word.matches("\\d+")) {
                numbers.append(word);
            }
        }

        return numbers.toString();
    }
}