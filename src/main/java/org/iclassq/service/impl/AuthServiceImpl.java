package org.iclassq.service.impl;

import okhttp3.*;
import org.iclassq.model.domain.SessionData;
import org.iclassq.model.dto.request.LoginRequestDTO;
import org.iclassq.model.dto.response.LoginResponseDTO;
import org.iclassq.service.AuthService;
import org.iclassq.util.Constants;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class AuthServiceImpl implements AuthService {
    private final String baseUrl;
    private final OkHttpClient client;
    private final CookieJar cookieJar;

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
                String sessionId = null;

                for (Cookie cookie : cookieJar.loadForRequest(HttpUrl.parse(baseUrl))) {
                    if ("JSESSIONID".equals(cookie.name())) {
                        sessionId = cookie.value();
                        break;
                    }
                }

                login.setSuccess(true);
                login.setSessionId(sessionId);
                login.setMessage("Inicio de sesión exitoso");

                SessionData.getInstance().setSessionId(sessionId);
                SessionData.getInstance().setAutenticado(true);

                return login;
            } else {
                login.setSuccess(false);
                login.setMessage("Credenciales inválidas");
                return login;
            }
        }
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
