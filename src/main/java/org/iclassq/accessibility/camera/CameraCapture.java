package org.iclassq.accessibility.camera;

import lombok.Getter;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.Java2DFrameConverter;
import org.bytedeco.javacv.OpenCVFrameGrabber;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

@Getter
public class CameraCapture {
    private static final Logger logger = Logger.getLogger(CameraCapture.class.getName());

    private final int cameraIndex;
    private final CameraConfig config;
    private OpenCVFrameGrabber grabber;
    private Java2DFrameConverter converter;
    private boolean initialized = false;
    private boolean capturing = false;

    public CameraCapture(int cameraIndex, CameraConfig config) {
        this.cameraIndex = cameraIndex;
        this.config = config;
        this.converter = new Java2DFrameConverter();
    }

    public boolean initialize() {
        try {
            logger.info(String.format("Inicializando cámara %d...", cameraIndex));

            grabber = new OpenCVFrameGrabber(cameraIndex);

            grabber.setImageWidth(config.getFrameWidth());
            grabber.setImageHeight(config.getFrameHeight());
            grabber.start();

            Frame testFrame = grabber.grab();
            if (testFrame == null) {
                logger.warning(String.format("Cámara %d no pudo capturar frame de prueba", cameraIndex));
                stop();
                return false;
            }

            initialized = true;
            logger.info(String.format("Cámara %d inicializada correctamente", cameraIndex));
            return true;

        } catch (Exception e) {
            logger.severe(String.format("Error al inicializar cámara %d: %s", cameraIndex, e.getMessage()));
            initialized = false;
            return false;
        }
    }

    public List<BufferedImage> captureFrames(int frameCount) {
        List<BufferedImage> frames = new ArrayList<>();

        if (!initialized) {
            logger.warning(String.format("Cámara %d no está inicializada", cameraIndex));
            return frames;
        }

        try {
            capturing = true;
            logger.info(String.format("Capturando %d frames de cámara %d...", frameCount, cameraIndex));

            for (int i = 0; i < frameCount; i++) {
                Frame frame = grabber.grab();

                if (frame != null && frame.image != null) {
                    BufferedImage bufferedImage = converter.convert(frame);

                    if (bufferedImage != null) {
                        frames.add(bufferedImage);
                        logger.fine(String.format("Frame %d/%d capturado de cámara %d",
                                i + 1, frameCount, cameraIndex));
                    } else {
                        logger.warning(String.format("Frame %d de cámara %d es null después de conversión",
                                i + 1, cameraIndex));
                    }
                } else {
                    logger.warning(String.format("Frame %d de cámara %d es null", i + 1, cameraIndex));
                }

                if (i < frameCount - 1 && config.getFrameDelayMs() > 0) {
                    Thread.sleep(config.getFrameDelayMs());
                }
            }

            logger.info(String.format("%d frames capturados de cámara %d", frames.size(), cameraIndex));

        } catch (InterruptedException e) {
            logger.warning(String.format("Captura interrumpida en cámara %d", cameraIndex));
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            logger.severe(String.format("Error capturando frames de cámara %d: %s",
                    cameraIndex, e.getMessage()));
        } finally {
            capturing = false;
        }

        return frames;
    }

    public BufferedImage captureSingleFrame() {
        List<BufferedImage> frames = captureFrames(1);
        return frames.isEmpty() ? null : frames.get(0);
    }

    public void stop() {
        try {
            if (grabber != null) {
                grabber.stop();
                grabber.release();
                logger.info(String.format("Cámara %d detenida", cameraIndex));
            }
        } catch (Exception e) {
            logger.warning(String.format("Error al detener cámara %d: %s", cameraIndex, e.getMessage()));
        } finally {
            initialized = false;
            capturing = false;
        }
    }

    public void release() {
        stop();
        if (converter != null) {
            converter.close();
            converter = null;
        }
    }
}
