package org.iclassq.model.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.iclassq.model.enums.CharacterType;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DocumentTypeConfig {
    private int maxLength;
    private int minLength;
    private String name;
    private CharacterType characterType;

    public static DocumentTypeConfig getConfig(Integer tipoDocumentoId) {
        if (tipoDocumentoId == null) {
            return null;
        }

        switch (tipoDocumentoId) {
            case 1: // DNI
                return new DocumentTypeConfig(8, 8, "DNI", CharacterType.NUMERIC);

            case 2: // PTP
                return new DocumentTypeConfig(9, 9, "PTP", CharacterType.NUMERIC);

            case 3: // Carnet de Extranjería
                return new DocumentTypeConfig(12, 9, "Carnet de Extranjería", CharacterType.ALPHANUMERIC);

            case 4: // RUC
                return new DocumentTypeConfig(11, 11, "RUC", CharacterType.ALPHANUMERIC);

            default:
                return new DocumentTypeConfig(12, 7, "Documento", CharacterType.ALPHANUMERIC);
        }
    }

    public boolean isCharacterAllowed(char c) {
        switch (characterType) {
            case NUMERIC:
                return Character.isDigit(c);

            case ALPHANUMERIC:
                return Character.isLetterOrDigit(c);

            case ALPHA:
                return Character.isLetter(c);

            default:
                return true;
        }
    }

    public boolean isValidLength(String value) {
        if (value == null) return false;
        int length = value.length();
        return length >= minLength && length <= maxLength;
    }

    public String getLengthErrorMessage() {
        if (minLength == maxLength) {
            return String.format("El %s debe tener %d dígitos", name, maxLength);
        } else {
            return String.format("El %s debe tener entre %d y %d caracteres", name, minLength, maxLength);
        }
    }
}
