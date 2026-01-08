package org.iclassq.controller.voice;

import org.iclassq.util.voice.VoiceAssistant;

public class TicketVoiceHelper {

    private final VoiceAssistant voiceAssistant;

    public TicketVoiceHelper(VoiceAssistant voiceAssistant) {
        this.voiceAssistant = voiceAssistant;
    }

    public void announceTicketGenerated(String codigoTicket) {
        if (!voiceAssistant.isEnabled() || codigoTicket == null || codigoTicket.isEmpty()) {
            return;
        }

        String codigoLegible = formatTicketCode(codigoTicket);

        String message = String.format(
                "Ticket generado exitosamente. Su código es: %s. " +
                codigoLegible
        );

        voiceAssistant.speak(message);
    }

    public void registerCloseCommand(Runnable onClose) {
        if (!voiceAssistant.isEnabled()) {
            return;
        }

        voiceAssistant.registerCommand("cerrar,salir,finalizar,terminar", onClose);
    }

    public void announceClosing() {
        if (!voiceAssistant.isEnabled()) {
            return;
        }

        voiceAssistant.speak("Cerrando. Regresando al inicio.");
    }

    private String formatTicketCode(String code) {
        if (code == null || code.isEmpty()) {
            return "";
        }

        StringBuilder formatted = new StringBuilder();

        for (int i = 0; i < code.length(); i++) {
            char c = code.charAt(i);

            if (Character.isLetter(c)) {
                formatted.append(c).append(" ");
            } else if (Character.isDigit(c)) {
                formatted.append(c).append(" ");
            } else {
                formatted.append(c).append(" ");
            }
        }

        return formatted.toString().trim();
    }
}