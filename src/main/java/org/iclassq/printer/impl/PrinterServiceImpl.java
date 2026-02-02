package org.iclassq.printer.impl;

import org.iclassq.model.dto.response.TicketResponseDTO;
import org.iclassq.printer.PrinterManager;
import org.iclassq.printer.PrinterService;
import org.iclassq.printer.TicketPrinter;

import javax.print.PrintService;
import java.awt.print.Book;
import java.awt.print.PageFormat;
import java.awt.print.Paper;
import java.awt.print.PrinterJob;
import java.util.logging.Logger;

public class PrinterServiceImpl implements PrinterService {
    private static final Logger logger = Logger.getLogger(PrinterServiceImpl.class.getName());
    private final PrinterManager printerManager;

    public PrinterServiceImpl() {
        this.printerManager = PrinterManager.getInstance();
        logPrinterInfo();
    }

    private void logPrinterInfo() {
        if (printerManager.hasAvailablePrinter()) {
            logger.info("Impresora detectada: " + printerManager.getSelectedPrinterName());
        } else {
            logger.warning("No se detectó ninguna impresora");
        }
    }

    @Override
    public boolean printTicket(TicketResponseDTO ticket) {
        if (!isAvailable()) {
            logger.severe("No hay impresora disponible");
            return false;
        }

        logger.info("Imprimiendo ticket: " + ticket.getCodigo());

        try {
            PrintService printService = printerManager.getSelectedPrinter();

            PrinterJob printerJob = PrinterJob.getPrinterJob();
            printerJob.setPrintService(printService);

            PageFormat pageFormat = createThermalPageFormat();

            TicketPrinter ticketPrinter = new TicketPrinter(ticket);

            Book book = new Book();
            book.append(ticketPrinter, pageFormat);
            printerJob.setPageable(book);

            printerJob.print();

            logger.info("Ticket impreso exitosamente");
            return true;
        } catch (Exception e) {
            logger.severe("Error al imprimir ticket: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    private PageFormat createThermalPageFormat() {
        PageFormat pageFormat = new PageFormat();
        pageFormat.setOrientation(PageFormat.PORTRAIT);

        Paper paper = new Paper();

        double width = 226;
        double height = 800;

        paper.setSize(width, height);

        double margin = 5;
        paper.setImageableArea(
                margin,
                margin,
                width - 2 * margin,
                height - 2 * margin
        );

        pageFormat.setPaper(paper);
        return pageFormat;
    }

    @Override
    public boolean isAvailable() {
        return printerManager.hasAvailablePrinter();
    }

    @Override
    public String getPrinterName() {
        return printerManager.getSelectedPrinterName();
    }

    @Override
    public void refresh() {
        printerManager.refresh();
        logPrinterInfo();
    }
}
