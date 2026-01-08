package org.iclassq.accessibility;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum AccessibilityMode {
    NORMAL("Modo Normal", "Usuario sin discapacidad"),
    ACCESSIBLE("Modo Accesible", "Usuario con discapacidad - Servicios activos");

    private final String name;
    private final String description;

    @Override
    public String toString() {
        return name + " - " + description;
    }
}