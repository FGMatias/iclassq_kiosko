package org.iclassq.accessibility.proximity;

import java.util.function.Consumer;

public interface ArduinoSerialService {
    boolean connect(String portName);
    void disconnect();
    void onReady(Consumer<Boolean> callback);
    void onProximityUpdate(Consumer<ProximityData> callback);
    void onDetectionComplete(Runnable callback);
    void onReset(Runnable callback);
    boolean isConnected();
    String[] getAvailablePorts();
    String findArduinoPort();
}