package org.iclassq.service.impl;

import okhttp3.*;
import org.iclassq.model.domain.SessionData;
import org.iclassq.model.dto.request.LoginRequestDTO;
import org.iclassq.model.dto.response.LoginResponseDTO;
import org.iclassq.model.dto.response.UsuarioRolDTO;
import org.iclassq.service.AuthService;
import org.iclassq.service.UsuarioService;
import org.iclassq.util.Constants;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

public class AuthServiceImpl implements AuthService {
    private final String baseUrl;
    private final OkHttpClient client;
    private final CookieJar cookieJar;
    private final UsuarioServiceImpl usuarioService;
    private final Logger logger = Logger.getLogger(AuthServiceImpl.class.getName());

    public AuthServiceImpl(String baseUrl, CookieJar cookieJar) {
        this.baseUrl = baseUrl;
        this.cookieJar = cookieJar;
        this.client = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .cookieJar(cookieJar)
                .followRedirects(true)
                .followSslRedirects(true)
                .build();
        this.usuarioService = new UsuarioServiceImpl(baseUrl, cookieJar);
    }

    @Override
    public LoginResponseDTO login(LoginRequestDTO dto) throws IOException {
        RequestBody form = new FormBody.Builder()
                .add("j_username", dto.getUsername())
                .add("j_password", dto.getPassword())
                .add("rol", String.valueOf(Constants.ROL_KIOSKO))
                .build();

        Request request = new Request.Builder()
                .url(baseUrl + "/j_security_check_for_standar")
                .post(form)
                .build();

        try (Response response = client.newCall(request).execute()) {
            LoginResponseDTO login = new LoginResponseDTO();
            String finalUrl = response.request().url().toString();

            if (finalUrl.contains("/main.app") || response.isSuccessful()) {
                String sessionId = extractSessionId();

                login.setSuccess(true);
                login.setSessionId(sessionId);
                login.setMessage("Inicio de sesión exitoso");

                SessionData.getInstance().setSessionId(sessionId);
                SessionData.getInstance().setAutenticado(true);

                try {
                    List<UsuarioRolDTO> roles = usuarioService.getCurrentUser(dto.getUsername());

                    if (roles != null && !roles.isEmpty()) {
                        UsuarioRolDTO usuarioRol = roles.get(0);

                        SessionData.getInstance().setUsuarioData(usuarioRol);

                        logger.info("Datos del usuario cargados en SessionData");
                        logger.info("   - Username: " + usuarioRol.getUsuario().getVUsuarioUsername());
                        logger.info("   - Sucursal ID: " + usuarioRol.getUsuario().getISucursal());
                        logger.info("   - Rol Equipo ID: " + usuarioRol.getUsuario().getIRolEquipo());
                        logger.info("   - Rol: " + usuarioRol.getRol().getVRolNombre());
                    }
                } catch (IOException e) {
                    logger.warning("No se pudieron obtener datos del usuario: " + e.getMessage());
                }

                return login;
            } else {
                login.setSuccess(false);
                login.setMessage("Credenciales inválidas");
                return login;
            }
        }
    }

    private String extractSessionId() {
        for (Cookie cookie : cookieJar.loadForRequest(HttpUrl.parse(baseUrl))) {
            if ("JSESSIONID".equals(cookie.name())) {
                return cookie.value();
            }
        }
        return null;
    }

    @Override
    public void logout() throws IOException {
        Request request = new Request.Builder()
                .url(baseUrl + "/j_spring_logout_standar")
                .get()
                .build();

        try (Response response = client.newCall(request).execute()) {
            SessionData.getInstance().setAutenticado(false);
            SessionData.getInstance().setSessionId(null);
        }
    }

    @Override
    public boolean isAuthenticated() {
        return SessionData.getInstance().isAutenticado() &&
                SessionData.getInstance().getSessionId() != null;
    }
}
