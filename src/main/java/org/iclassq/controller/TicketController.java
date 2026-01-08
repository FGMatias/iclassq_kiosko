package org.iclassq.controller;

import javafx.application.Platform;
import org.iclassq.controller.voice.TicketVoiceHelper;
import org.iclassq.navigation.Navigator;
import org.iclassq.util.voice.VoiceAssistant;
import org.iclassq.view.TicketView;

import java.util.logging.Logger;

public class TicketController {
    private final TicketView view;
    private final Logger logger = Logger.getLogger(TicketController.class.getName());

    private final VoiceAssistant voiceAssistant = new VoiceAssistant();
    private final TicketVoiceHelper voiceHelper = new TicketVoiceHelper(voiceAssistant);

    public TicketController(TicketView view) {
        this.view = view;
        view.setOnClose(this::handleClose);

        setupVoiceCommands();
    }

    private void setupVoiceCommands() {
        if (!voiceAssistant.isEnabled()) {
            return;
        }

        String codigoTicket = view.getTicketCode();
        voiceHelper.announceTicketGenerated(codigoTicket);
        voiceHelper.registerCloseCommand(this::handleCloseVoice);
        voiceAssistant.enableGrammar();

        logger.info("Gramática activada en Ticket - Código: " + codigoTicket);
    }

    private void handleClose() {
        handleCloseVoice();
    }

    private void handleCloseVoice() {
        voiceAssistant.stopSpeaking();
        voiceHelper.announceClosing();

        new Thread(() -> {
            try {
                Thread.sleep(1200);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            Platform.runLater(() -> {
                voiceAssistant.cleanup();
                Navigator.navigateToIdentification();
            });
        }).start();
    }
}