package org.iclassq.printer;

import org.iclassq.model.domain.SessionData;
import org.iclassq.model.dto.response.TicketResponseDTO;
import org.iclassq.util.DateFormatter;

import java.awt.*;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;

public class TicketPrinter implements Printable {

    private final TicketResponseDTO ticket;

    private static final Font FONT_TITLE = new Font("Monospaced",Font.BOLD, 14);
    private static final Font FONT_DOC = new Font("Monospaced", Font.BOLD, 12);
    private static final Font FONT_TICKET_CODE = new Font("Monospaced", Font.BOLD, 44);
    private static final Font FONT_SERVICE = new Font("Monospaced", Font.BOLD, 16);
    private static final Font FONT_DATE_TIME = new Font("Monospaced", Font.BOLD, 11);

    public TicketPrinter(TicketResponseDTO ticket) {
        this.ticket = ticket;
    }

    @Override
    public int print(Graphics graphics, PageFormat pageFormat, int pageIndex)
            throws PrinterException {

        if (pageIndex > 0) {
            return NO_SUCH_PAGE;
        }

        Graphics2D g2d = (Graphics2D) graphics;
        g2d.translate(pageFormat.getImageableX(), pageFormat.getImageableY());

        int y = 40;
        int lineHeight = 16;

        g2d.setFont(FONT_TITLE);
        y = printCentered(g2d, "BIENVENIDO", y, pageFormat);
        y += lineHeight + 20;

        if (ticket.getNumeroIdentificacion() != null) {
            g2d.setFont(FONT_DOC);
            String tipoDoc = SessionData.getInstance().getTipoDocumentoDescripcion();
            String docText = tipoDoc + ": " + ticket.getNumeroIdentificacion();
            y = printCentered(g2d, docText, y, pageFormat);
            y += lineHeight + 25;
        }

        g2d.setFont(FONT_TICKET_CODE);
        String ticketNumber = ticket.getCodigo() != null ? ticket.getCodigo() : "N/A";
        y = printCentered(g2d, ticketNumber, y, pageFormat);
        y += 50;

        String serviceName = SessionData.getInstance().getGrupo().getNombre();

        if (serviceName != null) {
            g2d.setFont(FONT_SERVICE);
            y = printCenteredWrapped(g2d, serviceName, y, pageFormat, 18);
            y += 25;
        }

        g2d.setFont(FONT_DATE_TIME);
        String fecha = DateFormatter.formatDate(ticket.getFechaAtencion());
        y = printCentered(g2d, "Fecha: " + fecha, y, pageFormat);
        y += lineHeight + 5;

        return PAGE_EXISTS;
    }

    private int printCentered(Graphics2D g2d, String text, int y, PageFormat pageFormat) {
        FontMetrics metrics = g2d.getFontMetrics();
        int x = (int) ((pageFormat.getImageableWidth() - metrics.stringWidth(text)) / 2);
        g2d.drawString(text, x, y);
        return y;
    }

    private int printCenteredWrapped(Graphics2D g2d, String text, int y, PageFormat pageFormat, int lineHeight) {
        FontMetrics metrics = g2d.getFontMetrics();
        int maxWidth = (int) pageFormat.getImageableWidth() - 30;

        if (metrics.stringWidth(text) <= maxWidth) {
            return printCentered(g2d, text, y, pageFormat);
        }

        String[] words = text.split(" ");
        StringBuilder line = new StringBuilder();

        for (String word : words) {
            String testLine = line.length() == 0 ? word : line + " " + word;
            int testWidth = metrics.stringWidth(testLine);

            if (testWidth > maxWidth && line.length() > 0) {
                y = printCentered(g2d, line.toString(), y, pageFormat);
                y += lineHeight;
                line = new StringBuilder(word);
            } else {
                line = new StringBuilder(testLine);
            }
        }

        if (line.length() > 0) {
            y = printCentered(g2d, line.toString(), y, pageFormat);
        }

        return y;
    }
}