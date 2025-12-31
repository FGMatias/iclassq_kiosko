package org.iclassq.accessibility.voice;

import java.util.List;
import java.util.function.Consumer;

public interface SpeechToTextService {
    void init() throws Exception;
    void startListening();
    void stopListening();
    boolean isListening();
    void addTextRecognizedListener(Consumer<String> listener);
    void removeTextRecognizedListener(Consumer<String> listener);
    void addErrorListener(Consumer<String> listener);
    void setLanguage(String language);
    void setConfidenceThreshold(float threshold);
    void shutdown();
    boolean isAvailable();
    void clearListeners();
    void setExpectedWords(List<String> words);
    void clearExpectedWords();
}
