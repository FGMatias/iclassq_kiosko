package org.iclassq.printer;

import org.iclassq.model.dto.response.TicketResponseDTO;

public interface PrinterService {
    boolean printTicket(TicketResponseDTO ticket);
    boolean isAvailable();
    String getPrinterName();
    void refresh();
}
