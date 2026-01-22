package org.iclassq.service.impl;

import okhttp3.CookieJar;
import okhttp3.Request;
import okhttp3.Response;
import org.iclassq.model.dto.response.HorarioDTO;
import org.iclassq.service.HorarioService;

import java.io.IOException;
import java.util.logging.Logger;

public class HorarioServiceImpl extends BaseService implements HorarioService {
    private static final Logger logger = Logger.getLogger(HorarioServiceImpl.class.getName());

    public HorarioServiceImpl(String baseUrl, CookieJar cookieJar) {
        super(baseUrl, cookieJar);
    }

    @Override
    public HorarioDTO getHorarios(Integer idRol) throws IOException {
        String url = baseUrl + "/obtenerhorariosgrupos.app?idRol=" + idRol;

        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Error al obtener horarios: " + response);
            }

            HorarioDTO horarios = parseData(response, HorarioDTO.class);

            return horarios;
        }
    }
}
