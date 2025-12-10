package org.iclassq.service.impl;

import com.google.gson.Gson;
import okhttp3.*;
import org.iclassq.model.dto.request.TicketRequestDTO;
import org.iclassq.model.dto.response.TicketResponseDTO;
import org.iclassq.service.TicketService;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class TicketServiceImpl implements TicketService {
    private final String baseUrl;
    private final OkHttpClient client;
    private final Gson gson;

    public TicketServiceImpl(String baseUrl, CookieJar cookieJar) {
        this.baseUrl = baseUrl;
        this.gson = new Gson();

        this.client = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .cookieJar(cookieJar)
                .build();
    }

    @Override
    public TicketResponseDTO generateTicket(TicketRequestDTO dto) throws IOException {
        String url = baseUrl + "/generarticketatencion.app";
        String json = gson.toJson(dto);

        RequestBody body = RequestBody.create(
                json,
                MediaType.parse("application/json; charset=utf-8")
        );

        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful()) {
                String jsonRes = response.body().string();
                return gson.fromJson(jsonRes, TicketResponseDTO.class);
            }
            throw new IOException("Error al generar ticket: " + response.code());
        }
    }
}
