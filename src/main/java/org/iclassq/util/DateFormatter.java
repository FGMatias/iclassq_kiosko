package org.iclassq.util;

import java.text.SimpleDateFormat;
import java.util.Date;

public class DateFormatter {
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");

    public static String formatDate(String fecha) {
        if (fecha == null || fecha.isEmpty()) {
            return "";
        }

        if (fecha.matches("\\d{13}")) {
            try {
                long timestamp = Long.parseLong(fecha);
                Date date = new Date(timestamp);
                return DATE_FORMAT.format(date);
            } catch (Exception e) {
                return fecha;
            }
        }

        if (fecha.matches("\\d{4}-\\d{2}-\\d{2}")) {
            return fecha;
        }

        if (fecha.contains(" ")) {
            return fecha.split(" ")[0];
        }

        if (fecha.contains("T")) {
            return fecha.split("T")[0];
        }

        return fecha;
    }
}
