package org.iclassq.controller;

import org.iclassq.config.ServiceFactory;
import org.iclassq.service.SubGrupoService;
import org.iclassq.view.SubGruposView;

public class SubGruposController {
    private final SubGruposView view;
    private final SubGrupoService subGrupoService;

    public SubGruposController(SubGruposView view) {
        this.view = view;
        this.subGrupoService = ServiceFactory.getSubGrupoService();

    }
}
