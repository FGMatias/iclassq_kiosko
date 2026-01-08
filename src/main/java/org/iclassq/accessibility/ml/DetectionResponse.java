package org.iclassq.accessibility.ml;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DetectionResponse {
    private boolean isDisabled;
    private double confidence;
    private String disabilityType;
    private int framesAnalyzed;
    private long processingTimeMs;
    private String error;
    private boolean success;

    public boolean isConfidentDetection(double threshold) {
        return confidence >= threshold;
    }

    public boolean hasError() {
        return error != null && !error.isEmpty();
    }
}
