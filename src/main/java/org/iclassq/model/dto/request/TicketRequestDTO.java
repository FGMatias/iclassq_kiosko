package org.iclassq.model.dto.request;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class TicketRequestDTO {
    private Integer idSucursal;
    private Integer idSubgrupo;
    private String prefijo;
    private String nombre;
    private String numDoc;
    private Integer tipoDoc;
    private Integer validaDoc;
}
