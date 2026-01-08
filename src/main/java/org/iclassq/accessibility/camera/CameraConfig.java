package org.iclassq.accessibility.camera;

import lombok.Data;

@Data
public class CameraConfig {
    private int frameWidth = 640;
    private int frameHeight = 480;
    private int fps = 30;
    private int framesPerCapture = 3;
    private int frameDelayMs = 100;
    private int initTimeoutMs = 5000;
    private boolean keepCamerasActive = true;
    private int jpegQuality = 85;
    private int maxCameras = 10;
    private int[] specificCameraIndices = null;

    public void setJpegQuality(int jpegQuality) {
        this.jpegQuality = Math.max(0, Math.min(100, jpegQuality));
    }

    public static CameraConfig getDefaultMLConfig() {
        CameraConfig config = new CameraConfig();
        config.setFrameWidth(640);
        config.setFrameWidth(480);
        config.setFramesPerCapture(3);
        config.setFrameDelayMs(100);
        config.setKeepCamerasActive(true);
        config.setJpegQuality(80);
        return config;
    }

    public static CameraConfig getHighQualityConfig() {
        CameraConfig config = new CameraConfig();
        config.setFrameWidth(1280);
        config.setFrameHeight(720);
        config.setFramesPerCapture(5);
        config.setFrameDelayMs(150);
        config.setJpegQuality(95);
        return config;
    }

    public static CameraConfig getLowQualityConfig() {
        CameraConfig config = new CameraConfig();
        config.setFrameWidth(320);
        config.setFrameHeight(240);
        config.setFramesPerCapture(2);
        config.setFrameDelayMs(50);
        config.setJpegQuality(70);
        return config;
    }

    @Override
    public String toString() {
        return String.format("CameraConfig[%dx%d, %d frames, %dms delay, quality=%d]",
                frameWidth, frameHeight, framesPerCapture, frameDelayMs, jpegQuality);
    }
}
