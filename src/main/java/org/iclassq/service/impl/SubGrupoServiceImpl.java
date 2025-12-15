package org.iclassq.service.impl;

import okhttp3.CookieJar;
import okhttp3.Request;
import okhttp3.Response;
import org.iclassq.model.dto.response.SubGrupoDTO;
import org.iclassq.service.SubGrupoService;

import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;

public class SubGrupoServiceImpl extends BaseService implements SubGrupoService {
    private final Logger logger = Logger.getLogger(AuthServiceImpl.class.getName());

    public SubGrupoServiceImpl(String baseUrl, CookieJar cookieJar) {
        super(baseUrl, cookieJar);
    }

    @Override
    public List<SubGrupoDTO> getByGrupo(Integer sucursalId, Integer grupoId) throws IOException {
        logger.info("id sucursal: " + sucursalId);
        logger.info("id grupo: " + grupoId);
        String url = baseUrl + "/listarsubgruposxgrupoandsucursal.app?idSucursal=" + sucursalId + "&idGrupo=" + grupoId;

        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();

        try (Response response = client.newCall(request).execute()) {
            return parseDataList(response, SubGrupoDTO.class);
        }
    }
}
