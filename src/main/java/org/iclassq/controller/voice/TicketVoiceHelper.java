package org.iclassq.controller.voice;

import org.iclassq.model.dto.response.TicketResponseDTO;
import org.iclassq.util.voice.VoiceAssistant;

public class TicketVoiceHelper {

    private final VoiceAssistant voiceAssistant;

    public TicketVoiceHelper(VoiceAssistant voiceAssistant) {
        this.voiceAssistant = voiceAssistant;
    }

    public void announceTicket(TicketResponseDTO ticket) {
        if (!voiceAssistant.isEnabled() || ticket == null) {
            return;
        }

        StringBuilder message = new StringBuilder();

        message.append("Tu ticket ha sido generado exitosamente. ");
        message.append("Tu número es: ");
        message.append(formatTicketNumber(ticket.getCodigo()));
        message.append(". ");

//        if (ticket.getVGrupo() != null && !ticket.getVGrupo().isEmpty()) {
//            message.append("Grupo: ");
//            message.append(ticket.getVGrupo());
//            message.append(". ");
//        }
//
//        if (ticket.getVSubGrupo() != null && !ticket.getVSubGrupo().isEmpty()) {
//            message.append("Servicio: ");
//            message.append(ticket.getVSubGrupo());
//            message.append(". ");
//        }

        message.append("Por favor espera a que se llame tu número en la pantalla. Gracias.");

        voiceAssistant.speak(message.toString());
    }

    private String formatTicketNumber(String ticket) {
        if (ticket == null || ticket.isEmpty()) {
            return "sin número";
        }

        StringBuilder formatted = new StringBuilder();

        for (int i = 0; i < ticket.length(); i++) {
            char c = ticket.charAt(i);

            if (Character.isLetter(c)) {
                formatted.append(c).append(" ");
            } else if (Character.isDigit(c)) {
                formatted.append(c).append(" ");
            }
        }

        return formatted.toString().trim();
    }
}