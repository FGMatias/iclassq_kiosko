package org.iclassq.accessibility.detection;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DetectionRequest {
    private List<String> images;
    @Builder.Default
    private double confidence_threshold = 0.5;
    private String output_dir;
}
