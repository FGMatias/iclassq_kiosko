package org.iclassq.service.impl;

import okhttp3.CookieJar;
import okhttp3.Request;
import okhttp3.Response;
import org.iclassq.model.dto.response.UsuarioRolDTO;
import org.iclassq.service.UsuarioService;

import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;

public class UsuarioServiceImpl extends BaseService implements UsuarioService {
    private final Logger logger = Logger.getLogger(UsuarioServiceImpl.class.getName());

    public UsuarioServiceImpl(String baseUrl, CookieJar cookieJar) {
        super(baseUrl, cookieJar);
    }

    @Override
    public List<UsuarioRolDTO> getCurrentUser(String username) throws IOException {
        String url = baseUrl + "/obtenerrolesbyusername.app?username=" + username;

        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();

        try (Response response = client.newCall(request).execute()) {
            List<UsuarioRolDTO> roles = parseDataList(response, UsuarioRolDTO.class);

            if (roles == null || roles.isEmpty()) {
                throw new IOException("No se encontraron roles para el usuario");
            }

            return roles;
        }
    }
}
