package org.iclassq.util.voice;

import javafx.application.Platform;
import org.iclassq.accessibility.voice.VoiceManager;

import java.util.*;
import java.util.function.Consumer;
import java.util.logging.Logger;

public class VoiceAssistant {

    private static final Logger logger = Logger.getLogger(VoiceAssistant.class.getName());
    private final VoiceManager voiceManager;
    private final Map<String, Runnable> commandHandlers = new HashMap<>();
    private Consumer<String> numberHandler = null;
    private boolean ready = false;
    private boolean active = false;

    public VoiceAssistant() {
        this.voiceManager = VoiceManager.getInstance();

        if (voiceManager != null && voiceManager.isVoiceServicesEnabled()) {
            ready = true;
            logger.info("VoiceAssistant READY (servicios disponibles pero inactivos");
        } else {
            ready = false;
            logger.info("VoiceAssistant NOT READY (VoiceManager no disponible");
        }
    }

    public boolean activate() {
        if (!ready) {
            logger.warning("No se puede activar VoiceAssistant: servicios no disponibles");
            return false;
        }

        if (active) {
            logger.info("VoiceAssistant ya está activo");
            return true;
        }

        try {
            logger.info("Activando VoiceAssistant...");

            voiceManager.startListening();
            voiceManager.addTextRecognizedListener(this::handleRecognizedText);

            active = true;

            logger.info("VoiceAssistant ACTIVADO (hablando y escuchando)");
            return true;

        } catch (Exception e) {
            logger.warning("Error al activar VoiceAssistant: " + e.getMessage());
            active = false;
            return false;
        }
    }

    public void deactivate() {
        if (!active) {
            logger.fine("VoiceAssistant ya está inactivo");
            return;
        }

        try {
            logger.info("🔇 Desactivando VoiceAssistant...");

            disableGrammar();
            voiceManager.stopListening();
            voiceManager.clearListener();
            commandHandlers.clear();
            numberHandler = null;

            active = false;

            logger.info("VoiceAssistant DESACTIVADO (servicios siguen disponibles)");

        } catch (Exception e) {
            logger.warning("Error al desactivar VoiceAssistant: " + e.getMessage());
        }
    }


    public void speak(String message) {
        if (!active || message == null || message.isEmpty()) {
            return;
        }

        try {
            voiceManager.speak(message);
        } catch (Exception e) {
            logger.warning("Error al hablar: " + e.getMessage());
        }
    }

    public void stopSpeaking() {
        if (!active) return;

        try {
            voiceManager.stopSpeaking();
            logger.info("TTS detenido");
        } catch (Exception e) {
            logger.warning("Error al detener TTS: " + e.getMessage());
        }
    }

    public void registerCommand(String keywords, Runnable action) {
        if (!ready) return;

        String[] keywordArray = keywords.toLowerCase().split(",");
        for (String keyword : keywordArray) {
            commandHandlers.put(keyword.trim(), action);
        }
    }

    public void enableGrammar() {
        if (!active || commandHandlers.isEmpty()) return;

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
        if (!active) return;

        voiceManager.clearExpectedWords();
        logger.info("GRAMATICA DESACTIVADA - reconocimiento general");
    }

    public void onNumberRecognized(Consumer<String> handler) {
        if (!ready) return;
        this.numberHandler = handler;
        logger.info("Handler de numeros registrado");
    }

    public void clearCommands() {
        commandHandlers.clear();
        numberHandler = null;
        disableGrammar();
    }

    public boolean isReady() {
        return ready;
    }

    public boolean isActive() {
        return active;
    }

    public void cleanup() {
        deactivate();
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
                logger.info("Números extraídos: '" + numbers + "'");
                Platform.runLater(() -> numberHandler.accept(numbers));
                return;
            }
        }

        if (!commandFound) {
            logger.fine("No se encontró comando para: '" + normalized + "'");
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
        String normalized = normalizeText(text);
        String[] words = normalized.split("\\s+");
        StringBuilder numbers = new StringBuilder();

        Map<String, String> numberMap = new HashMap<>();
        numberMap.put("cero", "0");
        numberMap.put("uno", "1");
        numberMap.put("dos", "2");
        numberMap.put("tres", "3");
        numberMap.put("cuatro", "4");
        numberMap.put("cinco", "5");
        numberMap.put("seis", "6");
        numberMap.put("siete", "7");
        numberMap.put("ocho", "8");
        numberMap.put("nueve", "9");

        for (String word : words) {
            if (numberMap.containsKey(word)) {
                numbers.append(numberMap.get(word));
            }
        }

        return numbers.toString();
    }
}