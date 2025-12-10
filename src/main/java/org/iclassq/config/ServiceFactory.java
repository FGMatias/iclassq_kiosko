package org.iclassq.config;

import okhttp3.Cookie;
import okhttp3.CookieJar;
import okhttp3.HttpUrl;
import org.iclassq.service.*;
import org.iclassq.service.impl.*;

import java.util.ArrayList;
import java.util.List;

public class ServiceFactory {
    private static AuthService authService;
    private static GrupoService grupoService;
    private static SubGrupoService subGrupoService;
    private static TipoDocumentoService tipoDocumentoService;
    private static TicketService ticketService;
    private static CookieJar cookieJar;

    public static void init(String backendUrl) {
        cookieJar = new CookieJar() {
            private List<Cookie> cookies = new ArrayList<>();

            @Override
            public void saveFromResponse(HttpUrl httpUrl, List<Cookie> cookies) {
                this.cookies = cookies;
            }

            @Override
            public List<Cookie> loadForRequest(HttpUrl httpUrl) {
                return cookies;
            }
        };

        authService = new AuthServiceImpl(backendUrl, cookieJar);
        grupoService = new GrupoServiceImpl(backendUrl, cookieJar);
        subGrupoService = new SubGrupoServiceImpl(backendUrl, cookieJar);
        tipoDocumentoService = new TipoDocumentoServiceImpl(backendUrl, cookieJar);
        ticketService = new TicketServiceImpl(backendUrl, cookieJar);
    }

    public static AuthService getAuthService() {
        checkInitialization();
        return authService;
    }

    public static GrupoService getGrupoService() {
        checkInitialization();
        return grupoService;
    }

    public static SubGrupoService getSubGrupoService() {
        checkInitialization();
        return subGrupoService;
    }

    public static TipoDocumentoService getTipoDocumentoService() {
        checkInitialization();
        return tipoDocumentoService;
    }

    public static TicketService getTicketService() {
        checkInitialization();
        return ticketService;
    }

    private static void checkInitialization() {
        if (authService == null) {
            throw new IllegalStateException("ServiceFactory no ha sido inicializado");
        }
    }
}
