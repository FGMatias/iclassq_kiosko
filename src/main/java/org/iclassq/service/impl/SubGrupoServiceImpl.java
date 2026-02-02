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

    @Override
    public SubGrupoDTO getPreferencial(Integer sucursalId, Integer grupoId) throws IOException {
        logger.info("Obteniendo subgrupo preferencial - grupo: " + grupoId + ", sucursal: " + sucursalId);

        String url = baseUrl + "/listarsubgruposxgrupoandsucursal.app?idSucursal=" + sucursalId + "&idGrupo=" + grupoId;

        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();

        try (Response response = client.newCall(request).execute()) {
            List<SubGrupoDTO> subgrupos = parseDataList(response, SubGrupoDTO.class);

            if (subgrupos == null || subgrupos.isEmpty()) {
                logger.warning("No se encontraron subgrupos para el grupo " + grupoId);
                return null;
            }

            for (SubGrupoDTO sg : subgrupos) {
                if (sg.getAgrupador() != null && sg.getAgrupador() == 2) {
                    logger.info(String.format("Subgrupo preferencial encontrado: %s (ID: %d, Prefijo: %s, Agrupador: %d)",
                            sg.getVNombreSubGrupo(),
                            sg.getISubGrupo(),
                            sg.getVPrefijo(),
                            sg.getAgrupador()));
                    return sg;
                }
            }

            SubGrupoDTO primero = subgrupos.get(0);
            logger.warning(String.format("No se encontró subgrupo con agrupador=2, usando primero disponible: %s (ID: %d)",
                    primero.getVNombreSubGrupo(), primero.getISubGrupo()));
            return primero;
        }
    }
}
