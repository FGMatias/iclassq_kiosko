package org.iclassq.printer;

import org.iclassq.model.dto.response.TicketResponseDTO;

import java.awt.*;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class TicketPrinter implements Printable {

    private final TicketResponseDTO ticket;

    // Fuentes
    private static final Font FONT_TITLE = new Font("Monospaced", Font.BOLD, 16);
    private static final Font FONT_HEADER = new Font("Monospaced", Font.BOLD, 12);
    private static final Font FONT_NORMAL = new Font("Monospaced", Font.PLAIN, 10);
    private static final Font FONT_SMALL = new Font("Monospaced", Font.PLAIN, 8);
    private static final Font FONT_TICKET_NUMBER = new Font("Monospaced", Font.BOLD, 24);

    // Formatters
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");

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

        int y = 20;
        int lineHeight = 15;

        // ========== HEADER ==========
        g2d.setFont(FONT_HEADER);
        y = printCentered(g2d, "SISTEMA ICLASSQ", y, pageFormat);
        y += lineHeight;

        g2d.setFont(FONT_SMALL);
        y = printCentered(g2d, "Gestion de Turnos", y, pageFormat);
        y += lineHeight;

        y += 10;
        y = printLine(g2d, y, pageFormat);
        y += 10;

        // ========== NÚMERO DE TICKET ==========
        g2d.setFont(FONT_TICKET_NUMBER);
        String ticketNumber = ticket.getCodigo() != null ? ticket.getCodigo() : "N/A";
        y = printCentered(g2d, ticketNumber, y, pageFormat);
        y += 30;

        y = printLine(g2d, y, pageFormat);
        y += 10;

        // ========== INFORMACIÓN DEL SERVICIO ==========
        g2d.setFont(FONT_NORMAL);

        if (ticket.getNombreSubgrupo() != null) {
            y = printLeftRight(g2d, "Servicio:", ticket.getNombreSubgrupo(), y, pageFormat);
            y += lineHeight;
        }

        y += 5;
        y = printLine(g2d, y, pageFormat);
        y += 10;

        // ========== INFORMACIÓN DEL USUARIO ==========
        g2d.setFont(FONT_SMALL);

        if (ticket.getTipoIdentificacion() != null && ticket.getNumeroIdentificacion() != null) {
            String tipoDoc = getTipoDocumentoDescripcion(ticket.getTipoIdentificacion());
            String docInfo = tipoDoc + ": " + ticket.getNumeroIdentificacion();
            y = printLeft(g2d, docInfo, y, pageFormat);
            y += lineHeight;
        }

        // ========== FECHA Y HORA ==========
        String fecha = LocalDate.now().format(DATE_FORMATTER);
        String hora = ticket.getHoraEmision() != null ?
                ticket.getHoraEmision() : LocalTime.now().format(TIME_FORMATTER);

        y = printLeftRight(g2d, "Fecha:", fecha, y, pageFormat);
        y += lineHeight;
        y = printLeftRight(g2d, "Hora:", hora, y, pageFormat);
        y += lineHeight;

        // ========== MENSAJE ==========
        y += 10;
        y = printLine(g2d, y, pageFormat);
        y += 10;

        g2d.setFont(FONT_SMALL);
        y = printCentered(g2d, "Por favor espere su turno", y, pageFormat);
        y += lineHeight;

        // ========== FOOTER ==========
        y += 15;
        y = printLine(g2d, y, pageFormat);
        y += 10;

        g2d.setFont(FONT_SMALL);
        y = printCentered(g2d, "Gracias por su preferencia", y, pageFormat);

        return PAGE_EXISTS;
    }

    /**
     * Obtiene la descripción del tipo de documento
     */
    private String getTipoDocumentoDescripcion(String tipoIdentificacion) {
        if (tipoIdentificacion == null) return "DOC";

        switch (tipoIdentificacion.toUpperCase()) {
            case "1": return "DNI";
            case "2": return "CE";
            case "3": return "PASAPORTE";
            case "4": return "RUC";
            default: return "DOC";
        }
    }

    /**
     * Imprime texto centrado
     */
    private int printCentered(Graphics2D g2d, String text, int y, PageFormat pageFormat) {
        FontMetrics metrics = g2d.getFontMetrics();
        int x = (int) ((pageFormat.getImageableWidth() - metrics.stringWidth(text)) / 2);
        g2d.drawString(text, x, y);
        return y;
    }

    /**
     * Imprime texto alineado a la izquierda
     */
    private int printLeft(Graphics2D g2d, String text, int y, PageFormat pageFormat) {
        g2d.drawString(text, 10, y);
        return y;
    }

    /**
     * Imprime texto con formato "Label: Value"
     */
    private int printLeftRight(Graphics2D g2d, String label, String value,
                               int y, PageFormat pageFormat) {
        FontMetrics metrics = g2d.getFontMetrics();

        // Label a la izquierda
        g2d.drawString(label, 10, y);

        // Value a la derecha
        int valueWidth = metrics.stringWidth(value);
        int x = (int) (pageFormat.getImageableWidth() - valueWidth - 10);
        g2d.drawString(value, x, y);

        return y;
    }

    /**
     * Imprime una línea separadora
     */
    private int printLine(Graphics2D g2d, int y, PageFormat pageFormat) {
        int width = (int) pageFormat.getImageableWidth() - 20;
        g2d.drawLine(10, y, 10 + width, y);
        return y;
    }
}