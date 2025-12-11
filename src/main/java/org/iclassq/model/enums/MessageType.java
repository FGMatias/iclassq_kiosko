package org.iclassq.model.enums;

import atlantafx.base.theme.Styles;

public enum MessageType {
    SUCCESS(Styles.SUCCESS),
    ERROR(Styles.DANGER),
    INFORMATION(Styles.ACCENT),
    WARNING(Styles.WARNING);

    private final String styleClass;

    MessageType(String styleClass) {
        this.styleClass = styleClass;
    }

    public String getStyleClass() {
        return styleClass;
    }
}
