package org.iclassq.service.impl;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import okhttp3.CookieJar;
import okhttp3.OkHttpClient;
import okhttp3.Response;
import org.iclassq.model.dto.response.ApiResponse;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;
import java.util.concurrent.TimeUnit;

public abstract class BaseService {
    protected final String baseUrl;
    protected final OkHttpClient client;
    protected final Gson gson;

    protected BaseService(String baseUrl, CookieJar cookieJar) {
        this.baseUrl = baseUrl;
        this.gson = new Gson();

        this.client = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .cookieJar(cookieJar)
                .build();
    }

    protected <T> T parseData(Response response, Type type) throws IOException {
        if (!response.isSuccessful()) {
            throw new IOException("Error Http: " + response.code());
        }

        String json = response.body().string();

        Type apiResponseType = TypeToken.getParameterized(ApiResponse.class, type).getType();
        ApiResponse<T> apiResponse = gson.fromJson(json, apiResponseType);

        return apiResponse.getData();
    }

    protected <T> List<T> parseDataList(Response response, Class<T> clazz) throws IOException {
        Type listType = TypeToken.getParameterized(List.class, clazz).getType();
        return parseData(response, listType);
    }
}
