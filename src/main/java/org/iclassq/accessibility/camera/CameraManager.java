package org.iclassq.accessibility.camera;

import org.bytedeco.javacv.OpenCVFrameGrabber;
import org.bytedeco.javacv.Frame;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class CameraManager {

    private static final Logger logger = Logger.getLogger(CameraManager.class.getName());

    public static List<CameraInfo> detectAvailableCameras(int maxCamerasToCheck) {
        List<CameraInfo> cameras = new ArrayList<>();

        logger.info(String.format("🔍 Detectando cámaras disponibles (max: %d)...", maxCamerasToCheck));

        for (int i = 0; i < maxCamerasToCheck; i++) {
            CameraInfo cameraInfo = testCamera(i);
            if (cameraInfo != null) {
                cameras.add(cameraInfo);
                logger.info(String.format("Cámara detectada: %s", cameraInfo));
            }
        }

        logger.info(String.format("📸 Total de cámaras detectadas: %d", cameras.size()));
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
        System.out.println("📸 CÁMARAS DISPONIBLES");
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