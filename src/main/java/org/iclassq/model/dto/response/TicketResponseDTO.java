package org.iclassq.model.dto.response;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
@ToString
public class TicketResponseDTO {
    private Integer id;
    private String codigo;
    private Date horaEmision;
    private Date horaInicioAtencion;
    private Date horaFinAtencion;
    private Integer idSubgrupo;
    private String numeroIdentificacion;
    private Date fechaHoraProgAlg2Tiempo;
    private Integer estado;
    private Integer secuenciaAlgo3Proporcion;
    private Integer idTicketDerivado;
    private String nombreSubgrupo;
    private String prefijo;
    private Integer activaAudioTexto;
    private String tipoIdentificacion;
    private Integer idVentanillaDerivacion;
    private Integer ventanillaIdUsuario;
    private Integer ticketSecuencia;
    private Integer enviaAudio;
    private Integer ventanillaCaja;
    private Integer evaluacion;
    private Date fechaAtencion;
    private String persona;
    private String prioridad;
}
