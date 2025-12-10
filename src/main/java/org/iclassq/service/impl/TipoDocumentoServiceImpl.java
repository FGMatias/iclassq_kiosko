package org.iclassq.service.impl;

import com.google.gson.Gson;
import okhttp3.CookieJar;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.iclassq.model.dto.response.TipoDocumentoDTO;
import org.iclassq.service.TipoDocumentoService;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class TipoDocumentoServiceImpl implements TipoDocumentoService {
    private final String baseUrl;
    private final OkHttpClient client;
    private final Gson gson;

    public TipoDocumentoServiceImpl(String baseUrl, CookieJar cookieJar) {
        this.baseUrl = baseUrl;
        this.gson = new Gson();

        this.client = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .cookieJar(cookieJar)
                .build();
    }

    @Override
    public List<TipoDocumentoDTO> getAll() throws IOException {
        String url = baseUrl + "/listarTipoDocumentoXGeneraTicket.app";

        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful()) {
                String json = response.body().string();
                TipoDocumentoDTO[] tipos = gson.fromJson(json, TipoDocumentoDTO[].class);
                return Arrays.asList(tipos);
            }
            throw new IOException("Error al obtener los tipos de documento: " + response.code());
        }
    }
}
