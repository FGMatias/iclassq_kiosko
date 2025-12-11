package org.iclassq.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UsuarioDTO {
    private Integer iRolEquipo;
    private Integer iSucursal;
    private String vUsuarioUsername;
}
