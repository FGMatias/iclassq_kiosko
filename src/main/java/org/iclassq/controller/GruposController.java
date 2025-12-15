package org.iclassq.controller;

import javafx.application.Platform;
import org.iclassq.config.ServiceFactory;
import org.iclassq.model.domain.SessionData;
import org.iclassq.model.dto.response.GrupoDTO;
import org.iclassq.navigation.Navigator;
import org.iclassq.service.GrupoService;
import org.iclassq.view.GruposView;

import java.util.List;

public class GruposController {
    private final GruposView view;
    private final GrupoService grupoService;

    public GruposController(GruposView view) {
        this.view = view;
        this.grupoService = ServiceFactory.getGrupoService();
        view.setOnGroupSelected(this::handleGrupoSelected);
        loadGroups();
    }

    private void loadGroups() {
        new Thread(() -> {
            try {
                Integer rolEquipoId = SessionData.getInstance().getRolEquipoId();
                List<GrupoDTO> groups = grupoService.getAllByTime(rolEquipoId);

                Platform.runLater(() -> view.setGroups(groups));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void handleGrupoSelected(GrupoDTO grupo) {
        SessionData.getInstance().setGrupo(grupo);
        Navigator.navigateToSubGroups();
    }
}
