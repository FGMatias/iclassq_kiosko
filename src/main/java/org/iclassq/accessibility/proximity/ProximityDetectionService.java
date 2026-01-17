package org.iclassq.accessibility.proximity;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public interface ProximityDetectionService {
    boolean initialize(String portName);
    CompletableFuture<Boolean> detectAsync();
    CompletableFuture<Boolean> detectAndActivateAsync();
    void shutdown();
    void onReady(Consumer<Boolean> callback);
    void onProximityUpdate(Consumer<ProximityData> callback);
    boolean isReady();
    boolean isDetecting();
    boolean isConnected();
    String[] getAvailablePorts();
}
