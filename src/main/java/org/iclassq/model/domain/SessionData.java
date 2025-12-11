package org.iclassq.model.domain;

import lombok.Getter;
import lombok.Setter;
import org.iclassq.model.dto.response.*;

import java.time.LocalDateTime;

@Getter
@Setter
public class SessionData {
    private static SessionData instance;
    private String sessionId;
    private boolean autenticado;
    private String username;
    private Integer sucursalId;
    private Integer rolEquipoId;
    private Integer rolId;
    private Integer kioskId;
    private Integer tipoDocumento;
    private String tipoDocumentoDescripcion;
    private String numeroDocumento;
    private GrupoDTO grupo;
    private SubGrupoDTO subgrupo;
    private MLDetectionResult mlResult;
    private boolean esPreferencial;
    private LocalDateTime inicioSesion;

    private SessionData() {
        this.inicioSesion = LocalDateTime.now();
    }

    public static SessionData getInstance() {
        if (instance == null) {
            instance = new SessionData();
        }
        return instance;
    }

    public static void reset() {
        instance = new SessionData();
    }

    public void setUsuarioData(UsuarioRolDTO usuarioRol) {
        if (usuarioRol == null) {
            return;
        }

        UsuarioDTO usuario = usuarioRol.getUsuario();
        if (usuario != null) {
            this.username = usuario.getVUsuarioUsername();
            this.sucursalId = usuario.getISucursal();
            this.rolEquipoId = usuario.getIRolEquipo();
        }

        RolDTO rol = usuarioRol.getRol();
        if (rol != null) {
            this.rolId = rol.getIRolId();
        }
    }

    public boolean puedeGenerarTicket() {
        return numeroDocumento != null &&
                !numeroDocumento.isEmpty() &&
                tipoDocumento != null &&
                grupo != null &&
                subgrupo != null;
    }

    public void limpiarFlujoAtencion() {
        this.tipoDocumento = null;
        this.numeroDocumento = null;
        this.grupo = null;
        this.subgrupo = null;
        this.mlResult = null;
        this.esPreferencial = false;
        this.inicioSesion = LocalDateTime.now();
    }

    public void limpiarSesion() {
        this.sessionId = null;
        this.autenticado = false;
        this.username = null;
        this.sucursalId = null;
        this.rolEquipoId = null;
        this.rolId = null;
        this.kioskId = null;
        limpiarFlujoAtencion();
    }
}
