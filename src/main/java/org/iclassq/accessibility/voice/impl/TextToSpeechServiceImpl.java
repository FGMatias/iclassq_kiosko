package org.iclassq.accessibility.voice.impl;

import com.sun.jna.Platform;
import org.iclassq.accessibility.voice.TextToSpeechService;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TextToSpeechServiceImpl implements TextToSpeechService {
    private static final Logger logger = Logger.getLogger(TextToSpeechServiceImpl.class.getName());
    private final ExecutorService executor;
    private Future<?> currentSpeechTask;
    private float rate = 0f;
    private int volume = 100;
    private String voice = "es-ES";
    private boolean available = false;
    private volatile boolean speaking = false;

    public TextToSpeechServiceImpl() {
        this.executor = Executors.newSingleThreadExecutor(r -> {
            Thread thread = new Thread(r, "WindowsTTS-Thread");
            thread.setDaemon(true);
            return thread;
        });
    }

    @Override
    public void init() throws Exception {
        if (!Platform.isWindows()) {
            throw new Exception("Windows SAPI solo funciona en Windows");
        }

        try {
            ProcessBuilder pb = new ProcessBuilder("powershell", "-Command", "Get-Command Add-Type");
            Process process = pb.start();
            int exitCode = process.waitFor();

            if (exitCode == 0) {
                available = true;
                logger.info("Servicio TTS Windows SAPI inicializado correctamente");
            } else {
                throw new Exception("PowerShell no está disponible");
            }
        } catch (Exception e) {
            logger.severe("Error al inicializar Windows SAPI: " + e.getMessage());
            throw new Exception("No se pudo inicializar el servicio TTS de Windows", e);
        }
    }

    @Override
    public void speak(String text) {
        if (!available) {
            logger.warning("TTS no disponible. No se puede reproducir: " + text);
            return;
        }

        if (text == null || text.trim().isEmpty()) {
            return;
        }

        executor.submit(() -> speakInternal(text));
    }

    @Override
    public void speakUrgent(String text) {
        if (!available) {
            logger.warning("TTS no disponible. No se puede reproducir: " + text);
            return;
        }

        if (text == null || text.trim().isEmpty()) {
            return;
        }

        stop();

        currentSpeechTask = executor.submit(() -> speakInternal(text));
    }

    private void speakInternal(String text) {
        try {
            speaking = true;

            String escapedText = text.replace("\"", "`\"").replace("'", "''");

            String psScript = String.format(
                    "$speak = New-Object -ComObject SAPI.SpVoice; " +
                    "$speak.Rate = %d; " +
                    "$speak.Volume = %d; " +
                    "$speak.Speak('%s', 0) | Out-Null",
                    (int)rate,
                    volume,
                    escapedText
            );

            ProcessBuilder pb = new ProcessBuilder(
                    "powershell",
                    "-NoProfile",
                    "-NonInteractive",
                    "-Command",
                    psScript
            );

            pb.redirectErrorStream(true);

            Process process = pb.start();
            int exitCode = process.waitFor();

            if (exitCode != 0) {
                logger.warning("PowerShell TTS terminó con código: " + exitCode);
            }

        } catch (InterruptedException e) {
            logger.info("Reproducción de voz interrumpida");
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error al ejecutar Windows SAPI", e);
        } finally {
            speaking = false;
        }
    }

    @Override
    public void stop() {
        if (currentSpeechTask != null && !currentSpeechTask.isDone()) {
            currentSpeechTask.cancel(true);
        }

        try {
            ProcessBuilder pb = new ProcessBuilder(
                    "powershell",
                    "-Command",
                    "Get-Process | Where-Object {$_.ProcessName -like '*powershell*'} | Stop-Process -Force"
            );
            pb.start();
        } catch (Exception e) {
            logger.log(Level.WARNING, "No se pudo detener PowerShell TTS", e);
        }

        speaking = false;
    }

    @Override
    public boolean isSpeaking() {
        return speaking;
    }

    @Override
    public void setRate(float rate) {
        if (rate < 0.5f) rate = 0.5f;
        if (rate > 2.0f) rate = 2.0f;

        this.rate = (rate - 1.0f) * 10f;
    }

    @Override
    public void setVolume(float volume) {
        this.volume = (int)(volume * 100);
        this.volume = Math.max(0, Math.min(100, this.volume));
    }

    public void setVoice(String voiceId) {
        this.voice = voiceId;
    }

    @Override
    public void shutdown() {
        stop();
        executor.shutdown();
        logger.info("Servicio TTS Windows apagado");
    }

    @Override
    public boolean isAvailable() {
        return available;
    }

    public String listInstalledVoices() {
        try {
            String psScript =
                    "Add-Type -AssemblyName System.Speech; " +
                    "[System.Speech.Synthesis.SpeechSynthesizer]::new().GetInstalledVoices() | " +
                    "ForEach-Object { $_.VoiceInfo.Name + ' (' + $_.VoiceInfo.Culture + ')' }";

            ProcessBuilder pb = new ProcessBuilder(
                    "powershell",
                    "-NoProfile",
                    "-Command",
                    psScript
            );

            Process process = pb.start();
            java.util.Scanner scanner = new java.util.Scanner(process.getInputStream());
            StringBuilder result = new StringBuilder();

            while (scanner.hasNextLine()) {
                result.append(scanner.nextLine()).append("\n");
            }

            return result.toString();

        } catch (Exception e) {
            return "Error al listar voces: " + e.getMessage();
        }
    }
}