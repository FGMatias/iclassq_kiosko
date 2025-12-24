package org.iclassq.accessibility.voice.impl;


import org.iclassq.accessibility.voice.SpeechToTextService;
import org.vosk.Model;
import org.vosk.Recognizer;

import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.logging.Logger;

public class SpeechToTextServiceImpl implements SpeechToTextService {
    private static final Logger logger = Logger.getLogger(SpeechToTextServiceImpl.class.getName());
    private static final String MODEL_PATH = "vosk-model-es";
    private Model model;
    private Recognizer recognizer;
    private TargetDataLine microphone;
    private final ExecutorService executor;
    private final AtomicBoolean listening = new AtomicBoolean(false);
    private final List<Consumer<String>> textListeners = new ArrayList<>();
    private final List<Consumer<String>> errorListeners = new ArrayList<>();
    private String languageCode = "es-ES";
    private float confidenceThreshold = 0.5f;
    private boolean available = false;
    private long totalBytesRead = 0;
    private int waveFormAccepted = 0;
    private int waveFormRejected = 0;

    public SpeechToTextServiceImpl() {
        this.executor = Executors.newSingleThreadExecutor(run -> {
            Thread thread = new Thread(run,"VoskSTT-Thread");
            thread.setDaemon(true);
            return thread;
        });
    }

    @Override
    public void init() throws Exception {
        try {
            try {
                Class<?> libVoskClass = Class.forName("org.vosk.LibVosk");
                Method setLogLevel = libVoskClass.getMethod("setLogLevel", int.class);
                setLogLevel.invoke(null, -1);
                logger.fine("Vosk log level configurado");
            } catch (Exception e) {
                logger.fine("LibVosk.setLogLevel no disponible: " + e.getMessage());
            }

            String modelPath = getModelPath();
            logger.info("Cargando modelo desde: " + modelPath);

            model = new Model(modelPath);

            initMicrophone();

            recognizer = new Recognizer(model, 16000);

            available = true;
            logger.info("Servicio STT Vosk inicializado correctamente");
        } catch (IOException e) {
            logger.severe("Error al cargar el modelo Vosk: " + e.getMessage());
            throw new Exception("No se pudo cargar el modelo Vosk. " +
                    "Verifique que el modelo esté en: " + MODEL_PATH, e);
        } catch (LineUnavailableException e) {
            logger.severe("Error al acceder al microfono: " + e.getMessage());
            throw new Exception("No se pudo acceder al microfono. " +
                    "Verifique que el microfono esté conectado y los permisos esten habilitados. " + e.getMessage());
        }
    }

    private void initMicrophone() throws Exception {
        try {
            AudioFormat format = new AudioFormat(16000, 16, 1, true, false);
            DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);

            if (!AudioSystem.isLineSupported(info)) {
                throw new Exception("Formato de audio no soportado");
            }

            logger.info("=== BUSCANDO MICRÓFONOS ===");
            Mixer.Info[] mixers = AudioSystem.getMixerInfo();
            for (Mixer.Info mixerInfo : mixers) {
                Mixer mixer = AudioSystem.getMixer(mixerInfo);
                Line.Info[] targetLineInfo = mixer.getTargetLineInfo();
                if (targetLineInfo.length > 0) {
                    logger.info("  - " + mixerInfo.getName());
                }
            }

            microphone = (TargetDataLine) AudioSystem.getLine(info);

            int bufferSize = 16000 * 2;
            microphone.open(format, bufferSize);

            logger.info("Micrófono abierto: " + microphone.getLineInfo());
            logger.info("   Buffer size: " + microphone.getBufferSize() + " bytes");
            logger.info("   Formato: " + microphone.getFormat());

        } catch (LineUnavailableException e) {
            logger.severe("❌ Error al acceder al micrófono: " + e.getMessage());

            logger.severe("Posibles causas:");
            logger.severe("  1. Otro programa está usando el micrófono (Discord, Zoom, Teams, etc.)");
            logger.severe("  2. Permisos de Windows bloqueados");
            logger.severe("  3. El micrófono no está conectado correctamente");

            throw new Exception("No se pudo acceder al micrófono: " + e.getMessage());
        }
    }

    private String getModelPath() throws IOException {
        try {
            java.net.URL resourceUrl = getClass().getClassLoader().getResource(MODEL_PATH);
            if (resourceUrl != null) {
                String path = resourceUrl.getPath();
                if (path.startsWith("/") && path.contains(":")) {
                    path = path.substring(1);
                }
                File modelDir = new File(path);
                if (modelDir.exists() && modelDir.isDirectory()) {
                    return modelDir.getAbsolutePath();
                }
            }
        } catch (Exception e) {
            logger.warning("No se pudo cargar modelo desde resources: " + e.getMessage());
        }

        File localModel = new File("src/main/resources/" + MODEL_PATH);
        if (localModel.exists() && localModel.isDirectory()) {
            return localModel.getAbsolutePath();
        }

        File workingModel = new File(MODEL_PATH);
        if (workingModel.exists() && workingModel.isDirectory()) {
            return workingModel.getAbsolutePath();
        }

        throw new IOException("Modelo Vosk no encontrado.");
    }

    @Override
    public void startListening() {
        if (!available) {
            notifyError("Servicio de reconocimiento de voz no disponible");
            return;
        }

        if (listening.get()) {
            logger.warning("El servicio ya está escuchando");
            return;
        }

        try {
            if (microphone != null && microphone.isOpen()) {
                logger.info("Cerrando micrófono anterior para reiniciarlo...");
                if (microphone.isActive()) {
                    microphone.stop();
                }
                microphone.close();
            }

            logger.info("Reabriendo micrófono...");
            initMicrophone();

        } catch (Exception e) {
            logger.severe("No se pudo reinicializar el micrófono: " + e.getMessage());
            notifyError("No se pudo iniciar el micrófono: " + e.getMessage());
            return;
        }

        listening.set(true);

        try {
            microphone.start();
            Thread.sleep(300);

            int available = microphone.available();

            logger.info("Micrófono activo: " + microphone.isActive());
            logger.info("Micrófono corriendo: " + microphone.isRunning());
            logger.info("Bytes disponibles: " + available);

            if (!microphone.isActive() && available == 0) {
                logger.severe("El micrófono no está capturando audio");
                listening.set(false);
                notifyError("El micrófono no se pudo activar");
                return;
            }

            if (!microphone.isActive()) {
                logger.warning("isActive() es false pero hay bytes disponibles");
                logger.warning("   Esto es normal con Intel Smart Sound Technology");
                logger.warning("   El micrófono debería funcionar de todas formas");
            }

            logger.info("Micrófono listo para capturar audio");

        } catch (Exception e) {
            logger.severe("Error al iniciar micrófono: " + e.getMessage());
            e.printStackTrace();
            listening.set(false);
            notifyError("Error al iniciar el micrófono: " + e.getMessage());
            return;
        }

        executor.submit(this::listenLoop);
        logger.info("Thread de escucha enviado al executor");
        logger.info("   Executor shutdown: " + executor.isShutdown());
        logger.info("   Executor terminated: " + executor.isTerminated());
        logger.info("Iniciando escucha de voz...");
    }

    private void listenLoop() {
        logger.info("ListenLoop iniciado");
        byte[] buffer = new byte[4096];
        int iterationCount = 0;
        long lastLogTime = System.currentTimeMillis();

        while (listening.get()) {
            try {
                int bytesRead = microphone.read(buffer, 0, buffer.length);

                iterationCount++;
                totalBytesRead += bytesRead;

                if (System.currentTimeMillis() - lastLogTime > 5000) {
                    logger.info(String.format(
                            "📊 Stats: iterations=%d, totalBytes=%d, accepted=%d, rejected=%d",
                            iterationCount, totalBytesRead, waveFormAccepted, waveFormRejected
                    ));
                    lastLogTime = System.currentTimeMillis();
                }

                if (bytesRead > 0) {
                    if (recognizer.acceptWaveForm(buffer, bytesRead)) {
                        waveFormAccepted++;
                        String result = recognizer.getResult();
                        logger.info("Wave form accepted, result: " + result);
                        processResult(result);
                    } else {
                        waveFormRejected++;
                        String partialResult = recognizer.getPartialResult();
                        if (partialResult != null && !partialResult.contains("\"partial\" : \"\"")) {
                            logger.info("Partial: " + partialResult);
                        }
                    }
                } else {
                    logger.warning("bytesRead = 0, el micrófono no está capturando audio");
                }
            } catch (Exception e) {
                if (listening.get()) {
                    logger.severe("Error durante el reconocimiento de voz: " + e);
                    notifyError("Error en el reconocimiento: " + e.getMessage());
                }
            }
        }
        logger.info("ListenLoop terminado");
    }

    private void processResult(String jsonResult) {
        try {
            String text = extractTextFromJson(jsonResult);

            if (text != null && !text.trim().isEmpty()) {
                logger.info("Texto reconocido: " + text);
                notifyTextRecognized(text);
            }
        } catch (Exception e) {
            logger.severe("Error al procesar el resultado: " + e);
        }
    }

    private String extractTextFromJson(String json) {
        int textStart = json.indexOf("\"text\"");
        if (textStart == -1) return null;

        int colonPos = json.indexOf(":", textStart);
        if (colonPos == -1) return null;

        int quoteStart = json.indexOf("\"", colonPos);
        if (quoteStart == -1) return null;

        int quoteEnd = json.indexOf("\"", quoteStart + 1);
        if (quoteEnd == -1) return null;

        return json.substring(quoteStart + 1, quoteEnd).trim();
    }

    @Override
    public void stopListening() {
        if (listening.get()) {
            listening.set(false);

            if (microphone != null && microphone.isActive()) {
                microphone.stop();
            }

            logger.info("Escucha de voz detenida");
        }
    }

    @Override
    public boolean isListening() {
        return listening.get();
    }

    @Override
    public void addTextRecognizedListener(Consumer<String> listener) {
        if (listener != null && !textListeners.contains(listener)) {
            textListeners.add(listener);
        }
    }

    @Override
    public void removeTextRecognizedListener(Consumer<String> listener) {
        textListeners.remove(listener);
    }

    @Override
    public void addErrorListener(Consumer<String> listener) {
        if (listener != null && !errorListeners.contains(listener)) {
            errorListeners.add(listener);
        }
    }

    @Override
    public void setLanguage(String language) {
        this.languageCode = language;
    }

    @Override
    public void setConfidenceThreshold(float threshold) {
        this.confidenceThreshold = Math.max(0.0f, Math.min(1.0f, threshold));
        logger.info("Umbral de confianza configurado a: " + confidenceThreshold);
    }

    @Override
    public void shutdown() {
        stopListening();

        if (microphone != null) {
            microphone.close();
        }

        if (recognizer != null) {
            recognizer.close();
        }

        if (model != null) {
            model.close();
        }

        executor.shutdown();
        logger.info("Servicio STT Vosk apagado");
    }

    @Override
    public boolean isAvailable() {
        return available;
    }

    @Override
    public void clearListeners() {
        textListeners.clear();
        errorListeners.clear();
        logger.info("Listeners limpiados");
    }

    private void notifyTextRecognized(String text) {
        logger.info("Notificando a " + textListeners.size() + " listeners: " + text);
        for (Consumer<String> listener : new ArrayList<>(textListeners)) {
            try {
                listener.accept(text);
            } catch (Exception e) {
                logger.warning("Error al notificar texto reconocido: " + e);
            }
        }
    }

    private void notifyError(String error) {
        for (Consumer<String> listener : new ArrayList<>(errorListeners)) {
            try {
                listener.accept(error);
            } catch (Exception e) {
                logger.warning("Error al notificar error: " + e);
            }
        }
    }
}
