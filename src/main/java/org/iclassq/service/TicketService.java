package org.iclassq.service;

import org.iclassq.model.dto.request.TicketRequestDTO;
import org.iclassq.model.dto.response.TicketResponseDTO;

import java.io.IOException;

public interface TicketService {
    TicketResponseDTO generateTicket(TicketRequestDTO dto) throws IOException;
}
