package org.iclassq.service;

import org.iclassq.model.dto.response.HorarioDTO;

import java.io.IOException;

public interface HorarioService {
    HorarioDTO getHorarios(Integer idRol) throws IOException;
}
