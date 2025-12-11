package org.iclassq.model.dto.response;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponseDTO {
    private boolean success;
    private String message;
    private String sessionId;
    private UsuarioData usuario;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UsuarioData {
        private Integer iRolEquipo;
        private Integer iSucursal;
        private Integer iRol;
    }
}
