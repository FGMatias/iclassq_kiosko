package org.iclassq.accessibility.voice;

public interface TextToSpeechService {
    void speak(String text);
    void speakUrgent(String text);
    void stop();
    boolean isSpeaking();
    void setRate(float rate);
    void setVolume(float volume);
    void init() throws Exception;
    void shutdown();
    boolean isAvailable();
}
