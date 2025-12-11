package org.iclassq.service;

import org.iclassq.model.dto.response.UsuarioRolDTO;

import java.io.IOException;
import java.util.List;

public interface UsuarioService {
    List<UsuarioRolDTO> getCurrentUser(String username) throws IOException;
}
