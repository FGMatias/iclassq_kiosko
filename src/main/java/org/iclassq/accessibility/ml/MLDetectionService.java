package org.iclassq.accessibility.ml;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.Getter;
import okhttp3.*;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

@Getter
public class MLDetectionService {

    private static final Logger logger = Logger.getLogger(MLDetectionService.class.getName());
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");

    private final String apiUrl;
    private final OkHttpClient client;
    private final Gson gson;
    private final double defaultConfidenceThreshold;

    public MLDetectionService(String apiUrl, int timeoutSeconds, double confidenceThreshold) {
        this.apiUrl = apiUrl;
        this.defaultConfidenceThreshold = confidenceThreshold;

        this.client = new OkHttpClient.Builder()
                .connectTimeout(timeoutSeconds, TimeUnit.SECONDS)
                .readTimeout(timeoutSeconds, TimeUnit.SECONDS)
                .writeTimeout(timeoutSeconds, TimeUnit.SECONDS)
                .build();

        this.gson = new GsonBuilder()
                .setPrettyPrinting()
                .create();

        logger.info(String.format("MLDetectionService configurado: %s (timeout: %ds)",
                apiUrl, timeoutSeconds));
    }

    public MLDetectionService() {
        this("https://j54pl9m6-5000.brs.devtunnels.ms/detect", 10, 0.5);
    }

    public DetectionResponse detect(List<BufferedImage> images) {
        List<String> base64Images = Base64Utils.toBase64List(images);

        if (base64Images.isEmpty()) {
            logger.severe("No hay imágenes válidas para enviar");
            return DetectionResponse.builder()
                    .success(false)
                    .error("No hay imágenes válidas para procesar")
                    .build();
        }

        return detectFromBase64(base64Images, defaultConfidenceThreshold);
    }

    public DetectionResponse detectFromBase64(List<String> base64Images, double confidenceThreshold) {
        long startTime = System.currentTimeMillis();

        try {
            DetectionRequest request = DetectionRequest.builder()
                    .images(base64Images)
                    .confidence_threshold(confidenceThreshold)
                    .build();

            String jsonRequest = gson.toJson(request);

            int totalSize = Base64Utils.getTotalBase64Size(base64Images);
            logger.info(String.format("Enviando %d imágenes a API ML (%s)...",
                    base64Images.size(), Base64Utils.formatBytes(totalSize)));

            RequestBody body = RequestBody.create(jsonRequest, JSON);
            Request httpRequest = new Request.Builder()
                    .url(apiUrl)
                    .post(body)
                    .build();

            try (Response response = client.newCall(httpRequest).execute()) {
                long processingTime = System.currentTimeMillis() - startTime;

                if (!response.isSuccessful()) {
                    String errorBody = response.body() != null ? response.body().string() : "Unknown error";
                    logger.severe(String.format("Error de API: %d - %s",
                            response.code(), errorBody));

                    return DetectionResponse.builder()
                            .success(false)
                            .error(String.format("HTTP %d: %s", response.code(), errorBody))
                            .processingTimeMs(processingTime)
                            .build();
                }

                String responseBody = response.body().string();
                logger.info("Respuesta recibida de API ML");
                logger.fine("Response body: " + responseBody);

                DetectionResponse detectionResponse = parseResponse(responseBody);
                detectionResponse.setProcessingTimeMs(processingTime);
                detectionResponse.setFramesAnalyzed(base64Images.size());
                detectionResponse.setSuccess(true);

                logger.info(String.format("Detección completada: %s (confianza: %.2f%%, tiempo: %dms)",
                        detectionResponse.isDisabled() ? "DISCAPACITADO" : "NORMAL",
                        detectionResponse.getConfidence() * 100,
                        processingTime));

                return detectionResponse;
            }

        } catch (IOException e) {
            long processingTime = System.currentTimeMillis() - startTime;
            logger.severe("Error de conexión con API ML: " + e.getMessage());

            return DetectionResponse.builder()
                    .success(false)
                    .error("Error de conexión: " + e.getMessage())
                    .processingTimeMs(processingTime)
                    .build();
        }
    }

    private DetectionResponse parseResponse(String jsonResponse) {
        try {
            com.google.gson.JsonObject json = gson.fromJson(jsonResponse, com.google.gson.JsonObject.class);

            boolean isDisabled = getBoolean(json, "is_disabled", "isDisabled", "disabled");
            double confidence = getDouble(json, "confidence", "score", "probability");
            String disabilityType = getString(json, "disability_type", "disabilityType", "type");

            return DetectionResponse.builder()
                    .isDisabled(isDisabled)
                    .confidence(confidence)
                    .disabilityType(disabilityType)
                    .build();

        } catch (Exception e) {
            logger.warning("No se pudo parsear respuesta JSON: " + e.getMessage());

            return DetectionResponse.builder()
                    .isDisabled(false)
                    .confidence(0.0)
                    .error("Error parseando respuesta: " + e.getMessage())
                    .build();
        }
    }

    private boolean getBoolean(com.google.gson.JsonObject json, String... keys) {
        for (String key : keys) {
            if (json.has(key) && !json.get(key).isJsonNull()) {
                return json.get(key).getAsBoolean();
            }
        }
        return false;
    }

    private double getDouble(com.google.gson.JsonObject json, String... keys) {
        for (String key : keys) {
            if (json.has(key) && !json.get(key).isJsonNull()) {
                return json.get(key).getAsDouble();
            }
        }
        return 0.0;
    }

    private String getString(com.google.gson.JsonObject json, String... keys) {
        for (String key : keys) {
            if (json.has(key) && !json.get(key).isJsonNull()) {
                return json.get(key).getAsString();
            }
        }
        return null;
    }

    public boolean isApiAvailable() {
        try {
            String healthUrl = apiUrl.replace("/detect", "/health");

            Request request = new Request.Builder()
                    .url(healthUrl)
                    .get()
                    .build();

            try (Response response = client.newCall(request).execute()) {
                boolean available = response.isSuccessful();
                logger.info(String.format("API ML %s: %s",
                        available ? "DISPONIBLE" : "NO DISPONIBLE",
                        healthUrl));
                return available;
            }

        } catch (Exception e) {
            logger.warning("API ML no disponible: " + e.getMessage());
            return false;
        }
    }
}