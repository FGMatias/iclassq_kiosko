package org.iclassq.accessibility.adapter;

import org.iclassq.accessibility.AccessibilityManager;
import org.iclassq.util.voice.VoiceAssistant;

import java.util.logging.Logger;

public class TicketVoiceAdapter {
    private static final Logger logger = Logger.getLogger(TicketVoiceAdapter.class.getName());

    private VoiceAssistant voice;

    public TicketVoiceAdapter() {
        this.voice = AccessibilityManager.getInstance().getVoiceAssistant();
    }

    public void onTicketGenerated(String codigoTicket, Runnable onClose) {
        if (!isVoiceActive() || codigoTicket == null || codigoTicket.isEmpty()) {
            return;
        }

        announceTicket(codigoTicket);
        registerCommands(onClose);
        voice.enableGrammar();

        logger.info("Comandos de voz configurados - Código: " + codigoTicket);
    }

    public void onClosing(Runnable callback) {
        if (!isVoiceActive()) {
            callback.run();
            return;
        }

        voice.stopSpeaking();
        voice.speak("Cerrando. Regresando al inicio.");

        new Thread(() -> {
            try {
                Thread.sleep(1200);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            callback.run();
        }).start();
    }

    private void announceTicket(String codigoTicket) {
        StringBuilder codigoLegible = new StringBuilder();
        for (int i = 0; i < codigoTicket.length(); i++) {
            codigoLegible.append(codigoTicket.charAt(i)).append(" ");
        }

        String message = String.format(
                "Ticket generado exitosamente. Su código es: %s",
                codigoLegible.toString().trim()
        );

        voice.speak(message);
    }

    private void registerCommands(Runnable onClose) {
        voice.registerCommand("cerrar,salir,finalizar,terminar", () -> {
            onClose.run();
        });
    }

    private boolean isVoiceActive() {
        return voice != null && voice.isActive();
    }
}