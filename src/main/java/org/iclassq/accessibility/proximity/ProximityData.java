package org.iclassq.accessibility.proximity;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class ProximityData {
    private final float distance;
    private final int timeElapsed;

    @Override
    public String toString() {
        return String.format("ProximityData{distance=%.1fcm, time=%ds}", distance, timeElapsed);
    }
}
