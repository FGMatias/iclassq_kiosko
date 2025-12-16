package org.iclassq.model.dto.response;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDate;
import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
@ToString
public class TicketResponseDTO {
    private Integer id;
    private String codigo;
    private String horaEmision;
    private Integer idSubgrupo;
    private String numeroIdentificacion;
    private Integer estado;
    private String nombreSubgrupo;
    private String prefijo;
    private String tipoIdentificacion;
    private Integer ticketSecuencia;
    private String fechaAtencion;
}
