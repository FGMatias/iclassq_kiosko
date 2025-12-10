package org.iclassq.service;

import org.iclassq.model.dto.response.TipoDocumentoDTO;

import java.io.IOException;
import java.util.List;

public interface TipoDocumentoService {
    List<TipoDocumentoDTO> getAll() throws IOException;
}
