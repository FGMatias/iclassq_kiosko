package org.iclassq.controller;

import javafx.application.Platform;
import org.iclassq.accessibility.AccessibilityManager;
import org.iclassq.accessibility.adapter.TicketVoiceAdapter;
import org.iclassq.model.dto.response.TicketResponseDTO;
import org.iclassq.navigation.Navigator;
import org.iclassq.printer.PrinterService;
import org.iclassq.printer.impl.PrinterServiceImpl;
import org.iclassq.util.voice.VoiceAssistant;
import org.iclassq.view.TicketView;

import java.util.logging.Logger;

public class TicketController {
    private final TicketView view;
    private final TicketResponseDTO ticket;
    private final Logger logger = Logger.getLogger(TicketController.class.getName());

    private final TicketVoiceAdapter voiceAdapter;
    private final PrinterService printerService;

    public TicketController(TicketView view) {
        this.view = view;
        this.ticket = view.getTicket();

        this.voiceAdapter = new TicketVoiceAdapter();
        view.setOnClose(this::handleClose);

        String codigoTicket = view.getTicketCode();
        voiceAdapter.onTicketGenerated(codigoTicket, this::handleCloseVoice);

        this.printerService = new PrinterServiceImpl();
        printTicketAutomatically();
    }

    private void printTicketAutomatically() {
        new Thread(() -> {
            try {
                if (!printerService.isAvailable()) {
                    logger.warning("No hay impresora disponible");
                    logger.warning("Verifique que la impresora esté conectada e instalada");
                    return;
                }

                logger.info("Imprimiendo ticket: " + ticket.getCodigo());

                boolean success = printerService.printTicket(ticket);

                if (success) {
                    logger.info("Ticket impreso exitosamente");
                } else {
                    logger.warning("No se pudo imprimir el ticket");
                }
            } catch (Exception e) {
                logger.severe("Error al imprimir el ticket: " + e.getMessage());
                e.printStackTrace();
            }
        }, "PrinterThread").start();
    }

    private void handleClose() {
        handleCloseVoice();
    }

    private void handleCloseVoice() {
        logger.info("Usuario solicitó salir, deteniendo voz inmediatamente");

        VoiceAssistant voice = AccessibilityManager.getInstance().getVoiceAssistant();
        if (voice != null && voice.isActive()) {
            voice.stopSpeaking();
        }

        Platform.runLater(() -> {
            logger.info("Navegando a IdentificationView");
            Navigator.navigateToIdentification();
        });
    }
}