package org.iclassq.accessibility.camera;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class CameraConfig {
    private int maxCameras = 2;
    private int captureWidth = 640;
    private int captureHeight = 480;
    private int framesPerCapture = 1;
    private int delayBetweenFrames = 100;
    private int[] specificCameraIndices = null;
    private int cameraInitTimeout = 5;

    public int getInitTimeoutMs() {
        return cameraInitTimeout * 1000;
    }

    public int getFrameDelayMs() {
        return delayBetweenFrames;
    }

    public static CameraConfig getDefaultMLConfig() {
        CameraConfig config = new CameraConfig();
        config.setMaxCameras(2);
        config.setCaptureWidth(640);
        config.setCaptureHeight(480);
        config.setFramesPerCapture(1);
        config.setDelayBetweenFrames(100);
        config.setCameraInitTimeout(5);
        return config;
    }

    public static CameraConfig getHighQualityMLConfig() {
        CameraConfig config = new CameraConfig();
        config.setMaxCameras(2);
        config.setCaptureWidth(1280);
        config.setCaptureHeight(720);
        config.setFramesPerCapture(2);
        config.setDelayBetweenFrames(200);
        config.setCameraInitTimeout(10);
        return config;
    }

    public static CameraConfig getMultiCameraConfig(int numCameras) {
        CameraConfig config = new CameraConfig();
        config.setMaxCameras(numCameras);
        config.setCaptureWidth(640);
        config.setCaptureHeight(480);
        config.setFramesPerCapture(1);
        config.setDelayBetweenFrames(100);
        config.setCameraInitTimeout(5);
        return config;
    }

    public static CameraConfig getSpecificCamerasConfig(int... cameraIndices) {
        CameraConfig config = new CameraConfig();
        config.setMaxCameras(cameraIndices.length);
        config.setSpecificCameraIndices(cameraIndices);
        config.setCaptureWidth(640);
        config.setCaptureHeight(480);
        config.setFramesPerCapture(1);
        config.setDelayBetweenFrames(100);
        config.setCameraInitTimeout(5);
        return config;
    }
}