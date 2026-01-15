package org.iclassq.controller;

import javafx.application.Platform;
import org.iclassq.accessibility.adapter.TicketVoiceAdapter;
import org.iclassq.navigation.Navigator;
import org.iclassq.view.TicketView;

import java.util.logging.Logger;

public class TicketController {
    private final TicketView view;
    private final Logger logger = Logger.getLogger(TicketController.class.getName());

    private final TicketVoiceAdapter voiceAdapter;

    public TicketController(TicketView view) {
        this.view = view;

        this.voiceAdapter = new TicketVoiceAdapter();
        view.setOnClose(this::handleClose);

        String codigoTicket = view.getTicketCode();
        voiceAdapter.onTicketGenerated(codigoTicket, this::handleCloseVoice);
    }

    private void handleClose() {
        handleCloseVoice();
    }

    private void handleCloseVoice() {
        voiceAdapter.onClosing(() -> {
            Platform.runLater(() -> {
                Navigator.navigateToIdentification();
            });
        });
    }
}