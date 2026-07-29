package com.example.taskable.service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class ClaudeService {

    private static final String MODEL = "claude-sonnet-4-6";
    private static final int MAX_TOKENS = 1024;

    private final HttpClient httpClient;
    private final String apiUrl;
    private final String apiKey;
    private final String apiVersion;

    public ClaudeService(
            @Value("${anthropic.api.url:}") String apiUrl,
            @Value("${anthropic.api.key:}") String apiKey,
            @Value("${anthropic.api.version:}") String apiVersion) {
        this.httpClient = HttpClient.newHttpClient();
        this.apiUrl = requireConfigured(apiUrl, "anthropic.api.url");
        this.apiKey = requireConfigured(apiKey, "anthropic.api.key");
        this.apiVersion = requireConfigured(apiVersion, "anthropic.api.version");
    }

    public String sendMessage(String prompt) {
        String requestBody = """
                {
                  "model": "%s",
                  "max_tokens": %d,
                  "messages": [
                    {
                      "role": "user",
                      "content": "%s"
                    }
                  ]
                }
                """.formatted(MODEL, MAX_TOKENS, escapeJson(prompt));

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(apiUrl))
                .header("x-api-key", apiKey)
                .header("anthropic-version", apiVersion)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            return response.body();
        } catch (IOException e) {
            throw new IllegalStateException("Failed to call Anthropic API", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Anthropic API call was interrupted", e);
        }
    }

    private String escapeJson(String value) {
        if (value == null) {
            return "";
        }

        return value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\b", "\\b")
                .replace("\f", "\\f")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    private String requireConfigured(String value, String propertyName) {
        if (value == null || value.isBlank()) {
            throw new IllegalStateException("Missing required Anthropic configuration property: " + propertyName);
        }
        return value;
    }
}
