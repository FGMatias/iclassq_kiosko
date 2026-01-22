package org.iclassq.model.dto.response;

import lombok.*;

import java.time.LocalTime;

@Data
public class HorarioDTO {
    private String horaInicio;
    private String horaFin;

    public LocalTime getHoraInicioAsLocalTime() {
        if (horaInicio == null || horaInicio.isEmpty()) {
            return LocalTime.of(6, 0);
        }
        return LocalTime.parse(horaInicio);
    }

    public LocalTime getHoraFinAsLocalTime() {
        if (horaFin == null || horaFin.isEmpty()) {
            return LocalTime.of(22, 0);
        }
        return LocalTime.parse(horaFin);
    }

    public int getHoraInicioAsInt() {
        return getHoraInicioAsLocalTime().getHour();
    }

    public int getHoraFinAsInt() {
        return getHoraFinAsLocalTime().getHour();
    }
}
