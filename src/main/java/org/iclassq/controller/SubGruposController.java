package org.iclassq.controller;

import javafx.application.Platform;
import org.iclassq.config.ServiceFactory;
import org.iclassq.model.domain.SessionData;
import org.iclassq.model.dto.response.SubGrupoDTO;
import org.iclassq.service.SubGrupoService;
import org.iclassq.view.SubGruposView;

import java.util.List;

public class SubGruposController {
    private final SubGruposView view;
    private final SubGrupoService subGrupoService;

    public SubGruposController(SubGruposView view) {
        this.view = view;
        this.subGrupoService = ServiceFactory.getSubGrupoService();
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
        SessionData.getInstance().setSubgrupo(subGrupo);
    }
}
