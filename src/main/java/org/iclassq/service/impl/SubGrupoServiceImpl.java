package org.iclassq.service.impl;

import com.google.gson.Gson;
import okhttp3.CookieJar;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.iclassq.model.dto.response.SubGrupoDTO;
import org.iclassq.service.SubGrupoService;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class SubGrupoServiceImpl implements SubGrupoService {
    private final String baseUrl;
    private final OkHttpClient client;
    private final Gson gson;

    public SubGrupoServiceImpl(String baseUrl, CookieJar cookieJar) {
        this.baseUrl = baseUrl;
        this.gson = new Gson();
        this.client = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .cookieJar(cookieJar)
                .build();
    }


    @Override
    public List<SubGrupoDTO> getByGrupo(Integer sucursalId, Integer grupoId) throws IOException {
        String url = baseUrl + "/listarsubgruposxgrupoandsucursal.app?idSucursal=" + sucursalId + "&idGrupo=" + grupoId;

        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful()) {
                String json = response.body().string();
                SubGrupoDTO[] subGrupos = gson.fromJson(json, SubGrupoDTO[].class);
                return Arrays.asList(subGrupos);
            }
            throw new IOException("Error al obtener los subgrupos: " + response.code());
        }
    }
}
