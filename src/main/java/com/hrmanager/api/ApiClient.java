package com.hrmanager.api;

import java.net.CookieManager;
import java.net.http.HttpClient;
import java.time.Duration;

public class ApiClient {
    public static final String BASE_URL = "http://84.8.221.29:8001/api";
    private static final ApiClient instance = new ApiClient();
    private final HttpClient client;
    private String accessToken;
    private String refreshToken;

    private ApiClient() {
        this.client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(30))
                .build();
    }

    public static ApiClient getInstance() {
        return instance;
    }

    public HttpClient getClient() {
        return client;
    }

    public String getBaseUrl() {
        return BASE_URL;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setTokens(String accessToken, String refreshToken) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
    }

    public void clearTokens() {
        this.accessToken = null;
        this.refreshToken = null;
    }

    public boolean isAuthenticated() {
        return accessToken != null && !accessToken.isEmpty();
    }

    public java.net.http.HttpRequest.Builder request(String endpoint) {
        java.net.http.HttpRequest.Builder builder = java.net.http.HttpRequest.newBuilder()
                .uri(java.net.URI.create(BASE_URL + endpoint));
        if (isAuthenticated()) {
            builder.header("Authorization", "Bearer " + accessToken);
        }
        builder.header("Content-Type", "application/json");
        return builder;
    }

    public java.net.http.HttpRequest.Builder requestWithFormData(String endpoint) {
        java.net.http.HttpRequest.Builder builder = java.net.http.HttpRequest.newBuilder()
                .uri(java.net.URI.create(BASE_URL + endpoint));
        if (isAuthenticated()) {
            builder.header("Authorization", "Bearer " + accessToken);
        }
        return builder;
    }
}