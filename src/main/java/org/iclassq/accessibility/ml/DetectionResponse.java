package org.iclassq.accessibility.ml;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DetectionResponse {
    private String status;
    private DetectionDetails details;
    private int framesAnalyzed;
    private long processingTimeMs;
    private String error;
    private boolean success;

    public boolean isDisabilityDetected() {
        return success && "persona discapacitada".equalsIgnoreCase(status);
    }

    public boolean isValid() {
        return success && status != null;
    }

    public boolean hasError() {
        return error != null && !error.isEmpty();
    }

    public String getDisabilityType() {
        if (!isDisabilityDetected() || details == null || details.getTotals() == null) {
            return null;
        }

        Map<String, Integer> totals = details.getTotals();

        int crutchCount = totals.getOrDefault("crutch", 0);
        int sunglassesCount = totals.getOrDefault("sunglasses", 0);

        if (crutchCount > 0 && sunglassesCount > 0) {
            return "MULTIPLE";
        } else if (crutchCount > 0) {
            return "MOVILIDAD";
        } else if (sunglassesCount > 0) {
            return "VISUAL";
        }

        return "OTRO";
    }

    public int getTotalDetections(String type) {
        if (details == null || details.getTotals() == null) {
            return 0;
        }
        return details.getTotals().getOrDefault(type, 0);
    }

    public String getSummary() {
        if (hasError()) {
            return "Error: " + error;
        }

        if (!success) {
            return "Detección fallida";
        }

        if (isDisabilityDetected()) {
            String type = getDisabilityType();
            int cardCount = getTotalDetections("card");
            int crutchCount = getTotalDetections("crutch");
            int sunglassesCount = getTotalDetections("sunglasses");

            return String.format("Persona con discapacidad detectada [%s] - Carnet: %d, Muletas: %d, Lentes: %d",
                    type, cardCount, crutchCount, sunglassesCount);
        }

        return "Persona sin discapacidad detectada";
    }

    @Override
    public String toString() {
        return getSummary();
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class DetectionDetails {
        private Map<String, Integer> totals;
    }
}
