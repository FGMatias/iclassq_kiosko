package org.iclassq.accessibility.camera;

import org.bytedeco.javacv.OpenCVFrameGrabber;
import org.bytedeco.javacv.Frame;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class CameraManager {

    private static final Logger logger = Logger.getLogger(CameraManager.class.getName());

    public static List<CameraInfo> detectAvailableCameras(int maxCamerasToCheck) {
        logger.info(String.format("Detectando cámaras disponibles (max: %d) [PARALELO]...", maxCamerasToCheck));

        ExecutorService executor = Executors.newFixedThreadPool(Math.min(maxCamerasToCheck, 4));
        List<Future<CameraInfo>> futures = new ArrayList<>();

        for (int i = 0; i < maxCamerasToCheck; i++) {
            final int index = i;
            futures.add(executor.submit(() -> testCamera(index)));
        }

        List<CameraInfo> cameras = new ArrayList<>();
        for (Future<CameraInfo> future : futures) {
            try {
                CameraInfo info = future.get(5, TimeUnit.SECONDS);
                if (info != null) {
                    cameras.add(info);
                    logger.info(String.format("Cámara detectada: %s", info));
                }
            } catch (TimeoutException e) {
                logger.fine("Timeout detectando cámara");
            } catch (Exception e) {
                logger.fine("Error detectando cámara: " + e.getMessage());
            }
        }

        executor.shutdown();
        try {
            executor.awaitTermination(10, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }

        cameras.sort((a, b) -> Integer.compare(a.getIndex(), b.getIndex()));

        logger.info(String.format("Total de cámaras detectadas: %d", cameras.size()));
        return cameras;
    }

    public static List<CameraInfo> detectAvailableCameras() {
        return detectAvailableCameras(10);
    }

    private static CameraInfo testCamera(int index) {
        OpenCVFrameGrabber grabber = null;

        try {
            grabber = new OpenCVFrameGrabber(index);
            grabber.setImageWidth(640);
            grabber.setImageHeight(480);

            grabber.start();

            Frame testFrame = grabber.grab();

            if (testFrame != null && testFrame.image != null) {
                String cameraName = getCameraName(index, grabber);
                CameraInfo.CameraType type = determineCameraType(index, cameraName);

                return CameraInfo.builder()
                        .index(index)
                        .name(cameraName)
                        .available(true)
                        .type(type)
                        .build();
            }

        } catch (Exception e) {
            logger.fine(String.format("Cámara %d no disponible: %s", index, e.getMessage()));
        } finally {
            if (grabber != null) {
                try {
                    grabber.stop();
                    grabber.release();
                } catch (Exception e) {
                    logger.fine(String.format("Error al liberar cámara %d: %s", index, e.getMessage()));
                }
            }
        }

        return null;
    }

    private static String getCameraName(int index, OpenCVFrameGrabber grabber) {
        try {
            String format = grabber.getFormat();
            if (format != null && !format.isEmpty()) {
                return String.format("Camera %d (%s)", index, format);
            }
        } catch (Exception e) {
        }

        return String.format("Camera %d", index);
    }

    private static CameraInfo.CameraType determineCameraType(int index, String name) {
        String nameLower = name.toLowerCase();

        if (nameLower.contains("usb") || nameLower.contains("external")) {
            return CameraInfo.CameraType.USB;
        }

        if (index == 0) {
            return CameraInfo.CameraType.INTEGRATED;
        }

        if (index > 0) {
            return CameraInfo.CameraType.USB;
        }

        return CameraInfo.CameraType.UNKNOWN;
    }

    public static boolean isCameraAvailable(int index) {
        return testCamera(index) != null;
    }

    public static int getAvailableCameraCount() {
        return detectAvailableCameras().size();
    }

    public static CameraInfo getCameraInfo(int index) {
        return testCamera(index);
    }

    public static void printAvailableCameras() {
        List<CameraInfo> cameras = detectAvailableCameras();

        System.out.println("\n═══════════════════════════════════════");
        System.out.println("CÁMARAS DISPONIBLES");
        System.out.println("═══════════════════════════════════════");

        if (cameras.isEmpty()) {
            System.out.println("No se detectaron cámaras disponibles");
        } else {
            for (CameraInfo camera : cameras) {
                System.out.println(String.format(
                        "[%d] %s - %s",
                        camera.getIndex(),
                        camera.getName(),
                        camera.getType().getDescription()
                ));
            }
            System.out.println(String.format("\nTotal: %d cámara(s)", cameras.size()));
        }

        System.out.println("═══════════════════════════════════════\n");
    }
}