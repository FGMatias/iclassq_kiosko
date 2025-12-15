package org.iclassq.controller;

import javafx.application.Platform;
import org.iclassq.config.ServiceFactory;
import org.iclassq.model.domain.SessionData;
import org.iclassq.model.dto.request.TicketRequestDTO;
import org.iclassq.model.dto.response.SubGrupoDTO;
import org.iclassq.model.dto.response.TicketResponseDTO;
import org.iclassq.navigation.Navigator;
import org.iclassq.service.SubGrupoService;
import org.iclassq.service.TicketService;
import org.iclassq.view.SubGruposView;
import org.iclassq.view.components.Message;

import java.util.List;
import java.util.logging.Logger;

public class SubGruposController {
    private final SubGruposView view;
    private final SubGrupoService subGrupoService;
    private final TicketService ticketService;
    private final Logger logger = Logger.getLogger(SubGruposController.class.getName());

    public SubGruposController(SubGruposView view) {
        this.view = view;
        this.subGrupoService = ServiceFactory.getSubGrupoService();
        this.ticketService = ServiceFactory.getTicketService();
        view.setOnSubGroupSelected(this::handleSubGroupSelected);
        loadSubGroups();
    }

    private void loadSubGroups() {
        new Thread(() -> {
            try {
                Integer sucursalId = SessionData.getInstance().getSucursalId();
                Integer grupoId = SessionData.getInstance().getGrupo().getId();
                List<SubGrupoDTO> subGroups = subGrupoService.getByGrupo(sucursalId, grupoId);

                Platform.runLater(() -> view.setSubGroups(subGroups));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void handleSubGroupSelected(SubGrupoDTO subGrupo) {
        SessionData session = SessionData.getInstance();

        TicketRequestDTO request = new TicketRequestDTO();
        request.setIdSucursal(session.getSucursalId());
        request.setIdSubgrupo(subGrupo.getIdSubgrupo());
        request.setPrefijo(subGrupo.getVPrefijo());
        request.setNombre(subGrupo.getVNombreSubGrupo());
        request.setNumDoc(session.getNumeroDocumento());
        request.setTipoDoc(session.getTipoDocumento());
        request.setValidaDoc(0);

        new Thread(() -> {
            try {
                TicketResponseDTO ticket = ticketService.generateTicket(request);

                Platform.runLater(() -> {
                    Navigator.navigatoToTicket(ticket);
                });
            } catch (Exception e) {
                logger.severe("Error al generar ticket: " + e.getMessage());
                e.printStackTrace();

                Platform.runLater(() -> {
                    Message.showError(
                            "Error",
                            "No se pudo generar el ticket"
                    );
                });
            }
        }).start();
    }

}
