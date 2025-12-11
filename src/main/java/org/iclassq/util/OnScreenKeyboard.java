package org.iclassq.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.logging.Logger;

public class OnScreenKeyboard {
    private static final Logger logger = Logger.getLogger(OnScreenKeyboard.class.getName());
    private static Process keyboardProcess;
    private static final String OS = System.getProperty("os.name").toLowerCase();

    public static void show() {
        try {
            if (isWindows()) {
                showWindowsKeyboard();
            } else if (isLinux()) {
                showLinuxKeyboard();
            } else {
                logger.warning("Sistema operativo no soportado para teclado virtual: " + OS);
            }
        } catch (Exception e) {
            logger.severe("Error al mostrar teclado virtual: " + e.getMessage());
        }
    }

    public static void hide() {
        try {
            if (keyboardProcess != null && keyboardProcess.isAlive()) {
                keyboardProcess.destroy();
                keyboardProcess = null;
                logger.info("Teclado virtual ocultado");
            }
        } catch (Exception e) {
            logger.severe("Error al ocultar teclado virtual: " + e.getMessage());
        }
    }

    private static void showWindowsKeyboard() throws IOException {
        if (isWindowsKeyboardRunning()) {
            logger.info("Teclado virtual de Windows ya está abierto");
            return;
        }

        ProcessBuilder pb = new ProcessBuilder("cmd.exe", "/c", "osk.exe");
        keyboardProcess = pb.start();
        logger.info("Teclado virtual de Windows iniciado");
    }

    private static void showLinuxKeyboard() throws IOException {
        try {
            ProcessBuilder pb = new ProcessBuilder("onboard");
            keyboardProcess = pb.start();
            logger.info("Teclado virtual onboard iniciado");
            return;
        } catch (IOException e) {
            logger.info("onboard no disponible, intentando con florence");
        }

        try {
            ProcessBuilder pb = new ProcessBuilder("florence");
            keyboardProcess = pb.start();
            logger.info("Teclado virtual florence iniciado");
        } catch (IOException e) {
            logger.warning("No se encontró ningún teclado virtual en Linux");
            throw e;
        }
    }

    private static boolean isWindowsKeyboardRunning() {
        try {
            Process process = Runtime.getRuntime().exec("tasklist");
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream())
            );

            String line;
            while ((line = reader.readLine()) != null) {
                if (line.toLowerCase().contains("osk.exe")) {
                    return true;
                }
            }
        } catch (Exception e) {
            logger.warning("Error al verificar si osk.exe está corriendo: " + e.getMessage());
        }
        return false;
    }

    public static void forceCloseWindowsKeyboard() {
        if (isWindows()) {
            try {
                Runtime.getRuntime().exec("taskkill /F /IM osk.exe");
                logger.info("Teclado virtual de Windows cerrado forzadamente");
            } catch (IOException e) {
                logger.warning("Error al cerrar osk.exe: " + e.getMessage());
            }
        }
    }

    private static boolean isWindows() {
        return OS.contains("win");
    }

    private static boolean isLinux() {
        return OS.contains("nix") || OS.contains("nux") || OS.contains("aix");
    }

    public static String getOSName() {
        return OS;
    }
}