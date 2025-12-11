package org.iclassq.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RolDTO {
    private Integer iRolId;
    private String vRolNombre;
    private String vPrefijo;
}
