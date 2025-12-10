package org.iclassq.service.impl;

import com.google.gson.Gson;
import okhttp3.CookieJar;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.iclassq.model.dto.response.GrupoDTO;
import org.iclassq.service.GrupoService;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class GrupoServiceImpl implements GrupoService {
    private final String baseUrl;
    private final OkHttpClient client;
    private final Gson gson;

    public GrupoServiceImpl(String baseUrl, CookieJar cookieJar) {
        this.baseUrl = baseUrl;
        this.gson = new Gson();
        this.client = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .cookieJar(cookieJar)
                .build();
    }

    @Override
    public List<GrupoDTO> getAllByTime(Integer rolEquipoId) throws IOException {
        Request request = new Request.Builder()
                .url(baseUrl + "/listargruposxtiempo.app?idRol=" + rolEquipoId)
                .get()
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful()) {
                String json = response.body().string();
                GrupoDTO[] grupos = gson.fromJson(json, GrupoDTO[].class);
                return Arrays.asList(grupos);
            }
            throw new IOException("Error al obtener grupos: " + response.code());
        }
    }
}
