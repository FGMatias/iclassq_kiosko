package org.iclassq.util.voice;

import javafx.application.Platform;
import org.iclassq.accessibility.voice.VoiceManager;

import java.lang.reflect.Array;
import java.util.*;
import java.util.function.Consumer;
import java.util.logging.Logger;

public class VoiceAssistant {

    private static final Logger logger = Logger.getLogger(VoiceAssistant.class.getName());
    private final VoiceManager voiceManager;
    private final Map<String, Runnable> commandHandlers = new HashMap<>();
    private Consumer<String> numberHandler = null;
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

    public void stopSpeaking() {
        if (!enabled) return;

        try {
            voiceManager.stopSpeaking();
            logger.info("TTS detenido");
        } catch (Exception e) {
            logger.warning("Error al detener TTS: " + e.getMessage());
        }
    }

    public void registerCommand(String keywords, Runnable action) {
        if (!enabled) return;

        String[] keywordArray = keywords.toLowerCase().split(",");
        for (String keyword : keywordArray) {
            commandHandlers.put(keyword.trim(), action);
        }
    }

    public void enableGrammar() {
        if (!enabled || commandHandlers.isEmpty()) return;

        Set<String> allWords = new LinkedHashSet<>();

        for (String keyword : commandHandlers.keySet()) {
            String[] words = keyword.split("\\s+");
            for (String word : words) {
                if (word.length() >= 2) {
                    allWords.add(word.trim());
                }
            }

            allWords.add(keyword.trim());
        }

        if (numberHandler != null) {
            allWords.addAll(Arrays.asList(
                    "cero", "uno", "dos", "tres", "cuatro", "cinco",
                    "seis", "siete", "ocho", "nueve"
            ));
        }

        List<String> wordList = new ArrayList<>(allWords);
        voiceManager.setExpectedWords(wordList);

        logger.info("GRAMATICA ACTIVADA - Vosk solo reconocera entre " + wordList.size() + " palabras");
    }

    public void disableGrammar() {
        if (!enabled) return;

        voiceManager.clearExpectedWords();
        logger.info("GRAMATICA DESACTIVADA - reconocimiento general");
    }

    public void onNumberRecognized(Consumer<String> handler) {
        if (!enabled) return;
        this.numberHandler = handler;
        logger.info("Handler de numeros registrado");
    }

    public void clearCommands() {
        commandHandlers.clear();
        numberHandler = null;
        disableGrammar();
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void cleanup() {
        if (!enabled) return;

        try {
            disableGrammar();
            voiceManager.stopListening();
            voiceManager.clearListener();
            commandHandlers.clear();
            numberHandler = null;
        } catch (Exception e) {
            logger.warning("Error en cleanup: " + e.getMessage());
        }
    }

    private void handleRecognizedText(String text) {
        if (text == null || text.isEmpty()) return;

        String normalized = normalizeText(text);
        logger.info("Texto reconocido: '" + text + "' (normalizado: '" + normalized + "')");

        boolean commandFound = false;
        for (Map.Entry<String, Runnable> entry : commandHandlers.entrySet()) {
            String keyword = entry.getKey();

            if (normalized.contains(keyword)) {
                logger.info("Comando detectado: '" + keyword + "'");
                Platform.runLater(entry.getValue());
                commandFound = true;
                return;
            }
        }

        if (!commandFound && numberHandler != null) {
            String numbers = extractNumbers(text);
            if (!numbers.isEmpty()) {
                logger.info("Numeros extraidos: '" + numbers + "'");
                String finalNumbers = numbers;
                Platform.runLater(() -> numberHandler.accept(finalNumbers));
                return;
            }
        }

        if (!commandFound) {
            logger.info("No se encontro comando ni numeros en: '" + normalized + "'");
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