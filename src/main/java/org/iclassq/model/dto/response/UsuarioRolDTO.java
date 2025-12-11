package org.iclassq.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UsuarioRolDTO {
    private RolDTO rol;
    private UsuarioDTO usuario;
}
