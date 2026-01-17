package org.iclassq.accessibility.proximity.impl;

import com.fazecast.jSerialComm.SerialPort;
import org.iclassq.accessibility.proximity.ArduinoSerialService;
import org.iclassq.accessibility.proximity.ProximityData;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.function.Consumer;
import java.util.logging.Logger;

public class ArduinoSerialServiceImpl implements ArduinoSerialService {
    private static final Logger logger = Logger.getLogger(ArduinoSerialServiceImpl.class.getName());

    private SerialPort serialPort;
    private BufferedReader reader;
    private Thread listenerThread;
    private volatile boolean listening = false;

    private Consumer<Boolean> onReady;
    private Consumer<ProximityData> onProximityUpdate;
    private Runnable onDetectionComplete;
    private Runnable onDetectionCancelled;
    private Runnable onReset;

    public boolean connect(String portName) {
        try {
            logger.info("Intentando conectar a Arduino en puerto: " + portName);

            serialPort = SerialPort.getCommPort(portName);
            serialPort.setBaudRate(9600);
            serialPort.setComPortTimeouts(SerialPort.TIMEOUT_READ_SEMI_BLOCKING, 0, 0);

            if (serialPort.openPort()) {
                logger.info("Puerto serial abierto: " + portName);

                Thread.sleep(2000);

                startListening();

                return true;
            } else {
                logger.severe("No se pudo abrir el puerto: " + portName);
                return false;
            }

        } catch (Exception e) {
            logger.severe("Error al conectar con Arduino: " + e.getMessage());
            return false;
        }
    }

    public void disconnect() {
        listening = false;

        if (listenerThread != null && listenerThread.isAlive()) {
            try {
                listenerThread.interrupt();
                listenerThread.join(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        if (serialPort != null && serialPort.isOpen()) {
            serialPort.closePort();
            logger.info("🔌 Puerto serial cerrado");
        }
    }

    private void startListening() {
        listening = true;

        listenerThread = new Thread(() -> {
            try {
                reader = new BufferedReader(new InputStreamReader(serialPort.getInputStream()));
                logger.info("Escuchando mensajes de Arduino...");

                while (listening && !Thread.interrupted()) {
                    if (reader.ready()) {
                        String line = reader.readLine();
                        if (line != null && !line.isEmpty()) {
                            processMessage(line.trim());
                        }
                    }
                    Thread.sleep(50);
                }

            } catch (InterruptedException e) {
                logger.info("Escucha de Arduino interrumpida");
                Thread.currentThread().interrupt();
            } catch (Exception e) {
                logger.severe("Error al leer del puerto serial: " + e.getMessage());
            }
        });

        listenerThread.setDaemon(true);
        listenerThread.setName("Arduino-Listener");
        listenerThread.start();
    }

    private void processMessage(String message) {
        logger.info("Arduino: " + message);

        try {
            if (message.equals("READY")) {
                handleReady();
            } else if (message.startsWith("DISTANCE:")) {
                handleProximityUpdate(message);
            } else if (message.equals("COMPLETE")) {
                handleComplete();
            } else if (message.equals("CANCELLED")) {
                handleCancelled();
            } else if (message.equals("RESET")) {
                handleReset();
            }
        } catch (Exception e) {
            logger.warning("Error al procesar mensaje: " + e.getMessage());
        }
    }

    private void handleReady() {
        logger.info("Arduino READY");
        if (onReady != null) {
            onReady.accept(true);
        }
    }

    private void handleProximityUpdate(String message) {
        try {
            String[] parts = message.split(",");
            float distance = Float.parseFloat(parts[0].split(":")[1]);
            int time = Integer.parseInt(parts[1].split(":")[1]);

            ProximityData data = new ProximityData(distance, time);

            if (onProximityUpdate != null) {
                onProximityUpdate.accept(data);
            }
        } catch (Exception e) {
            logger.warning("Error al parsear datos de proximidad: " + e.getMessage());
        }
    }

    private void handleComplete() {
        logger.info("Detección COMPLETA (5 segundos)");
        if (onDetectionComplete != null) {
            onDetectionComplete.run();
        }
    }

    private void handleCancelled() {
        logger.info("Detección CANCELADA (persona se retiró)");
        if (onDetectionCancelled != null) {
            onDetectionCancelled.run();
        }
    }

    private void handleReset() {
        logger.info("Arduino RESET");
        if (onReset != null) {
            onReset.run();
        }
    }

    public void onReady(Consumer<Boolean> callback) {
        this.onReady = callback;
    }

    public void onProximityUpdate(Consumer<ProximityData> callback) {
        this.onProximityUpdate = callback;
    }

    public void onDetectionComplete(Runnable callback) {
        this.onDetectionComplete = callback;
    }

    public void onDetectionCancelled(Runnable callback) {
        this.onDetectionCancelled = callback;
    }

    public void onReset(Runnable callback) {
        this.onReset = callback;
    }

    public boolean isConnected() {
        return serialPort != null && serialPort.isOpen();
    }

    public static String[] getAvailablePorts() {
        SerialPort[] ports = SerialPort.getCommPorts();
        String[] portNames = new String[ports.length];

        for (int i = 0; i < ports.length; i++) {
            portNames[i] = ports[i].getSystemPortName();
        }

        return portNames;
    }

    public static String findArduinoPort() {
        SerialPort[] ports = SerialPort.getCommPorts();

        for (SerialPort port : ports) {
            String portName = port.getSystemPortName();
            String description = port.getDescriptivePortName().toLowerCase();

            if (description.contains("usb") || description.contains("arduino")) {
                logger.info("Puerto Arduino encontrado: " + portName + " (" + description + ")");
                return portName;
            }
        }

        logger.warning("No se encontró puerto Arduino automáticamente");
        return null;
    }
}