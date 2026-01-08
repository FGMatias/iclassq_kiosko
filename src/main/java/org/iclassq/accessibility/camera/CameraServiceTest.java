package org.iclassq.accessibility.camera;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

public class CameraServiceTest {

    private static final Logger logger = Logger.getLogger(CameraServiceTest.class.getName());
    private static final String OUTPUT_DIR = "camera_test_output";

    public static void main(String[] args) {
        System.out.println("╔═══════════════════════════════════════════════╗");
        System.out.println("║     CAMERA SERVICE TEST                       ║");
        System.out.println("╚═══════════════════════════════════════════════╝\n");

        try {
            testCameraDetection();
            testServiceInitialization();
            testFrameCapture();

            System.out.println("\n TODOS LOS TESTS COMPLETADOS");

        } catch (Exception e) {
            System.err.println("\n ERROR EN TEST: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void testCameraDetection() {
        System.out.println("\n━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        System.out.println("TEST 1: DETECCIÓN DE CÁMARAS");
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");

        CameraManager.printAvailableCameras();

        int cameraCount = CameraManager.getAvailableCameraCount();

        if (cameraCount == 0) {
            System.out.println("  ADVERTENCIA: No se detectaron cámaras");
            System.out.println("   Verifica que:");
            System.out.println("   - Las cámaras estén conectadas");
            System.out.println("   - Los drivers estén instalados");
            System.out.println("   - Ninguna otra app esté usando las cámaras");
        } else {
            System.out.println(String.format("%d cámara(s) detectada(s)", cameraCount));
        }
    }

    private static void testServiceInitialization() {
        System.out.println("\n━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        System.out.println("TEST 2: INICIALIZACIÓN DEL SERVICIO");
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");

        CameraConfig config = CameraConfig.getDefaultMLConfig();
        config.setFramesPerCapture(2);

        CameraService service = new CameraService(config);

        boolean initialized = service.initialize();

        if (initialized) {
            System.out.println("Servicio inicializado correctamente");
            service.printServiceInfo();
            service.shutdown();
        } else {
            System.out.println("No se pudo inicializar el servicio");
        }
    }

    private static void testFrameCapture() {
        System.out.println("\n━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        System.out.println("TEST 3: CAPTURA Y GUARDADO DE FRAMES");
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");

        File outputDir = new File(OUTPUT_DIR);
        if (!outputDir.exists()) {
            outputDir.mkdirs();
            System.out.println(String.format("Directorio creado: %s", outputDir.getAbsolutePath()));
        }

        CameraConfig config = CameraConfig.getDefaultMLConfig();
        config.setFramesPerCapture(3);

        CameraService service = new CameraService(config);

        if (!service.initialize()) {
            System.out.println("No se pudo inicializar el servicio");
            return;
        }

        try {
            System.out.println("\n📸 Capturando frames de todas las cámaras...");

            Map<Integer, List<BufferedImage>> framesByCamera = service.captureFromAllCameras();

            if (framesByCamera.isEmpty()) {
                System.out.println("No se capturaron frames");
                return;
            }

            String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            int totalSaved = 0;

            for (Map.Entry<Integer, List<BufferedImage>> entry : framesByCamera.entrySet()) {
                int cameraIndex = entry.getKey();
                List<BufferedImage> frames = entry.getValue();

                System.out.println(String.format("\n Cámara %d: %d frames", cameraIndex, frames.size()));

                for (int i = 0; i < frames.size(); i++) {
                    BufferedImage frame = frames.get(i);

                    String filename = String.format("camera_%d_frame_%d_%s.jpg",
                            cameraIndex, i + 1, timestamp);
                    File outputFile = new File(outputDir, filename);

                    try {
                        ImageIO.write(frame, "jpg", outputFile);
                        System.out.println(String.format("    Guardado: %s (%dx%d)",
                                filename, frame.getWidth(), frame.getHeight()));
                        totalSaved++;
                    } catch (IOException e) {
                        System.err.println(String.format("    Error guardando %s: %s",
                                filename, e.getMessage()));
                    }
                }
            }

            System.out.println(String.format("\nTotal de frames guardados: %d", totalSaved));
            System.out.println(String.format("Ubicación: %s", outputDir.getAbsolutePath()));

            System.out.println("\n📸 Test de captura como lista única...");
            List<BufferedImage> allFrames = service.captureAllFramesAsList();
            System.out.println(String.format("%d frames capturados en lista única", allFrames.size()));

        } finally {
            service.shutdown();
        }
    }

    public static void captureAndSaveSingleFrameFromAllCameras(String outputPath) {
        CameraService service = new CameraService();

        try {
            if (!service.initialize()) {
                logger.warning("No se pudo inicializar CameraService");
                return;
            }

            Map<Integer, List<BufferedImage>> frames = service.captureFromAllCameras();

            File dir = new File(outputPath);
            if (!dir.exists()) {
                dir.mkdirs();
            }

            String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());

            for (Map.Entry<Integer, List<BufferedImage>> entry : frames.entrySet()) {
                if (!entry.getValue().isEmpty()) {
                    BufferedImage firstFrame = entry.getValue().get(0);
                    String filename = String.format("camera_%d_%s.jpg", entry.getKey(), timestamp);
                    ImageIO.write(firstFrame, "jpg", new File(dir, filename));
                    logger.info(String.format("Frame guardado: %s", filename));
                }
            }

        } catch (Exception e) {
            logger.severe("Error capturando frames: " + e.getMessage());
        } finally {
            service.shutdown();
        }
    }
}