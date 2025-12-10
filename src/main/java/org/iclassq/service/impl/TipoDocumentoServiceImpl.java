package org.iclassq.service.impl;

import com.google.gson.Gson;
import okhttp3.CookieJar;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.iclassq.model.dto.response.TipoDocumentoDTO;
import org.iclassq.service.TipoDocumentoService;

import java.io.IOException;
import java.util.List;

public class TipoDocumentoServiceImpl extends BaseService implements TipoDocumentoService {

    public TipoDocumentoServiceImpl(String baseUrl, CookieJar cookieJar) {
        super(baseUrl, cookieJar);
    }

    @Override
    public List<TipoDocumentoDTO> getAll() throws IOException {
        String url = baseUrl + "/listarTipoDocumentoXGeneraTicket.app";

        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();

        try (Response response = client.newCall(request).execute()) {
            return parseDataList(response, TipoDocumentoDTO.class);
        }
    }
}
