package org.iclassq.service.impl;

import okhttp3.CookieJar;
import okhttp3.Request;
import okhttp3.Response;
import org.iclassq.model.dto.response.GrupoDTO;
import org.iclassq.service.GrupoService;

import java.io.IOException;
import java.util.List;

public class GrupoServiceImpl extends BaseService implements GrupoService {

    public GrupoServiceImpl(String baseUrl, CookieJar cookieJar) {
        super(baseUrl, cookieJar);
    }

    @Override
    public List<GrupoDTO> getAllByTime(Integer rolEquipoId) throws IOException {
        Request request = new Request.Builder()
                .url(baseUrl + "/listargruposxtiempo.app?idRol=" + rolEquipoId)
                .get()
                .build();

        try (Response response = client.newCall(request).execute()) {
            return parseDataList(response, GrupoDTO.class);
        }
    }
}
