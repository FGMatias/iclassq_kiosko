package org.iclassq.service.impl;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import okhttp3.*;
import org.iclassq.model.dto.request.TicketRequestDTO;
import org.iclassq.model.dto.response.TicketResponseDTO;
import org.iclassq.service.TicketService;

import java.io.IOException;

public class TicketServiceImpl extends BaseService implements TicketService {

    public TicketServiceImpl(String baseUrl, CookieJar cookieJar) {
        super(baseUrl, cookieJar);
    }

    @Override
    public TicketResponseDTO generateTicket(TicketRequestDTO dto) throws IOException {
        String url = baseUrl + "/generarticketatencion.app";

        FormBody.Builder body = new FormBody.Builder();
        body.add("idSubgrupo", String.valueOf(dto.getIdSubgrupo()));
        body.add("idSucursal", String.valueOf(dto.getIdSucursal()));
        body.add("prefijo", dto.getPrefijo());
        body.add("nombre", dto.getNombre());
        body.add("numDoc", dto.getNumDoc());
        body.add("tipoDoc", String.valueOf(dto.getTipoDoc()));
        body.add("validaDoc", String.valueOf(dto.getValidaDoc()));

        RequestBody request = body.build();

        Request http = new Request.Builder()
                .url(url)
                .post(request)
                .build();

        try (Response response = client.newCall(http).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Error al generar el ticket: " + response);
            }

            String responseBody = response.body().string();
            JsonObject jsonResponse = gson.fromJson(responseBody, JsonObject.class);
            boolean success = jsonResponse.has("success") && jsonResponse.get("success").getAsBoolean();

            if (!success) {
                String errorMessage = jsonResponse.get("message").getAsString();
                throw new IOException(errorMessage);
            }

            JsonObject data = jsonResponse.getAsJsonObject("data");
            TicketResponseDTO ticket = gson.fromJson(data, TicketResponseDTO.class);

            return ticket;
        }
    }
}
