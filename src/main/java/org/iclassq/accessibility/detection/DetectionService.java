package org.iclassq.accessibility.detection;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import lombok.Getter;
import okhttp3.*;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

@Getter
public class DetectionService {

    private static final Logger logger = Logger.getLogger(DetectionService.class.getName());
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");

    private final String apiUrl;
    private final OkHttpClient client;
    private final Gson gson;
    private final double defaultConfidenceThreshold;

    public DetectionService(String apiUrl, int timeoutSeconds, double confidenceThreshold) {
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

        logger.info(String.format("MLDetectionService configurado: %s (timeout: %ds, threshold: %.2f)",
                apiUrl, timeoutSeconds, confidenceThreshold));
    }

    public DetectionService() {
        this("http://localhost:5000/verify-images", 15, 0.5);
    }

    public DetectionResponse detect(List<BufferedImage> images) {
        if (images == null || images.isEmpty()) {
            logger.warning("No hay imágenes para procesar");
            return DetectionResponse.builder()
                    .success(false)
                    .error("No hay imágenes para procesar")
                    .build();
        }

        List<String> base64Images = Base64Utils.toBase64List(images);

        if (base64Images.isEmpty()) {
            logger.severe("No se pudieron convertir las imágenes a Base64");
            return DetectionResponse.builder()
                    .success(false)
                    .error("No se pudieron convertir las imágenes a Base64")
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
            logger.info(String.format("Enviando %d imagen(es) a API ML (%s)...",
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
                    logger.severe(String.format("Error de API: HTTP %d - %s",
                            response.code(), errorBody));

                    return DetectionResponse.builder()
                            .success(false)
                            .error(String.format("HTTP %d: %s", response.code(), errorBody))
                            .processingTimeMs(processingTime)
                            .framesAnalyzed(base64Images.size())
                            .build();
                }

                String responseBody = response.body().string();
                logger.info("Respuesta recibida de API ML");
                logger.fine("Response body: " + responseBody);

                DetectionResponse detectionResponse = parseResponse(responseBody);
                detectionResponse.setProcessingTimeMs(processingTime);
                detectionResponse.setFramesAnalyzed(base64Images.size());
                detectionResponse.setSuccess(true);

                logger.info(String.format("Detección completada: %s (tiempo: %dms)",
                        detectionResponse.getStatus(), processingTime));

                return detectionResponse;
            }

        } catch (IOException e) {
            long processingTime = System.currentTimeMillis() - startTime;
            logger.severe("Error de conexión con API ML: " + e.getMessage());

            return DetectionResponse.builder()
                    .success(false)
                    .error("Error de conexión: " + e.getMessage())
                    .processingTimeMs(processingTime)
                    .framesAnalyzed(base64Images != null ? base64Images.size() : 0)
                    .build();
        }
    }

    private DetectionResponse parseResponse(String jsonResponse) {
        try {
            JsonObject json = gson.fromJson(jsonResponse, JsonObject.class);

            String status = json.has("status") && !json.get("status").isJsonNull()
                    ? json.get("status").getAsString()
                    : "no válido";

            DetectionResponse.DetectionDetails details = null;
            if (json.has("details") && !json.get("details").isJsonNull()) {
                JsonObject detailsJson = json.getAsJsonObject("details");

                Map<String, Integer> totals = new HashMap<>();
                if (detailsJson.has("totals") && !detailsJson.get("totals").isJsonNull()) {
                    JsonObject totalsJson = detailsJson.getAsJsonObject("totals");

                    totals.put("card", getIntValue(totalsJson, "card"));
                    totals.put("crutch", getIntValue(totalsJson, "crutch"));
                    totals.put("sunglasses", getIntValue(totalsJson, "sunglasses"));
                }

                details = DetectionResponse.DetectionDetails.builder()
                        .totals(totals)
                        .build();
            }

            return DetectionResponse.builder()
                    .status(status)
                    .details(details)
                    .build();

        } catch (Exception e) {
            logger.warning("Error parseando respuesta JSON: " + e.getMessage());

            return DetectionResponse.builder()
                    .status("no válido")
                    .error("Error parseando respuesta: " + e.getMessage())
                    .build();
        }
    }

    private int getIntValue(JsonObject json, String key) {
        if (json.has(key) && !json.get(key).isJsonNull()) {
            return json.get(key).getAsInt();
        }
        return 0;
    }

    public boolean isApiAvailable() {
        try {
            Request request = new Request.Builder()
                    .url(apiUrl.replace("/verify-images", "/"))
                    .get()
                    .build();

            try (Response response = client.newCall(request).execute()) {
                boolean available = response.isSuccessful();
                logger.info(String.format("API ML %s: %s",
                        available ? "DISPONIBLE" : "NO DISPONIBLE",
                        apiUrl));
                return available;
            }

        } catch (Exception e) {
            logger.warning("API ML no disponible: " + e.getMessage());
            return false;
        }
    }

    public String testConnection() {
        StringBuilder sb = new StringBuilder();
        sb.append("\n═══════════════════════════════════════\n");
        sb.append("🔌 TEST DE CONEXIÓN API ML\n");
        sb.append("═══════════════════════════════════════\n");
        sb.append(String.format("URL: %s\n", apiUrl));
        sb.append(String.format("Timeout: %ds\n", client.connectTimeoutMillis() / 1000));
        sb.append(String.format("Threshold: %.2f\n", defaultConfidenceThreshold));

        try {
            boolean available = isApiAvailable();
            sb.append(String.format("Estado: %s\n", available ? " CONECTADO" : " DESCONECTADO"));
        } catch (Exception e) {
            sb.append(String.format("Estado:  ERROR - %s\n", e.getMessage()));
        }

        sb.append("═══════════════════════════════════════\n");
        return sb.toString();
    }
}