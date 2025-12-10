package org.iclassq.model.domain;

import lombok.Getter;
import lombok.Setter;
import org.iclassq.model.dto.response.GrupoDTO;
import org.iclassq.model.dto.response.SubGrupoDTO;

import java.time.LocalDateTime;

@Getter
@Setter
public class SessionData {
    private static SessionData instance;
    private Integer sucursalId;
    private Integer idRolEquipo;
    private String sessionId;
    private boolean autenticado;
    private Integer kioskId;
    private Integer tipoDocumento;
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

    public boolean puedeGenerarTicket() {
        return numeroDocumento != null &&
                !numeroDocumento.isEmpty() &&
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
}
