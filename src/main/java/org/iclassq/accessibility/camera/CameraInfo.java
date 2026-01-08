package org.iclassq.accessibility.camera;

import lombok.*;

@Builder
@Value
public class CameraInfo {
    int index;
    String name;
    boolean available;
    CameraType type;

    @Getter
    @AllArgsConstructor
    public enum CameraType {
        INTEGRATED("Cámara Integrada/Nativa"),
        USB("Cámara USB Externa"),
        UNKNOWN("Tipo Desconocido");
        private final String description;
    }

    @Override
    public String toString() {
        return String.format("Camera[%d] %s - %s (%s)",
                index,
                name,
                type.getDescription(),
                available ? "Disponible" : "No Disponible"
        );
    }
}
