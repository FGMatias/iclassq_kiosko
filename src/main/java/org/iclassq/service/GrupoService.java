package org.iclassq.service;

import org.iclassq.model.dto.response.GrupoDTO;

import java.io.IOException;
import java.util.List;

public interface GrupoService {
    List<GrupoDTO> getAllByTime(Integer rolEquipoId) throws IOException;
}
