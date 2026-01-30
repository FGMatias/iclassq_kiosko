package org.iclassq.service;

import org.iclassq.model.dto.response.SubGrupoDTO;

import java.io.IOException;
import java.util.List;

public interface SubGrupoService {
    List<SubGrupoDTO> getByGrupo(Integer sucursalId, Integer grupoId) throws IOException;
    SubGrupoDTO getPreferencial(Integer sucursalId, Integer grupoId) throws IOException;
}
