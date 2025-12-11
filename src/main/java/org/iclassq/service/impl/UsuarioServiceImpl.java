package org.iclassq.service.impl;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import okhttp3.CookieJar;
import okhttp3.Request;
import okhttp3.Response;
import org.iclassq.model.dto.response.LoginResponseDTO;
import org.iclassq.model.dto.response.RolDTO;
import org.iclassq.model.dto.response.UsuarioDTO;
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

            if (!roles.isEmpty()) {
                UsuarioRolDTO primerRol = roles.get(0);
                if (primerRol.getUsuario() != null) {
                    logger.info("   - Sucursal ID: " + primerRol.getUsuario().getISucursal());
                    logger.info("   - Rol Equipo ID: " + primerRol.getUsuario().getIRolEquipo());
                }
                if (primerRol.getRol() != null) {
                    logger.info("   - Rol: " + primerRol.getRol().getVRolNombre());
                }
            }

            return roles;
        }
    }
}
