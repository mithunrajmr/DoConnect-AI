package com.doconnect.backend.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;

/**
 * Reusable Gemini REST client for current and future AI modules.
 */
@Service
public class GeminiClient {

	private final HttpClient httpClient;
	private final ObjectMapper objectMapper;
	private final GeminiProperties properties;

	public GeminiClient(HttpClient geminiHttpClient, ObjectMapper objectMapper, GeminiProperties properties) {
		this.httpClient = geminiHttpClient;
		this.objectMapper = objectMapper;
		this.properties = properties;
	}

	public String generateText(String prompt) {
		requireConfiguredApiKey();
		try {
			HttpRequest request = HttpRequest.newBuilder()
					.uri(generateContentUri())
					.timeout(Duration.ofSeconds(properties.timeoutSeconds()))
					.header("Content-Type", "application/json")
					.POST(HttpRequest.BodyPublishers.ofString(requestBody(prompt)))
					.build();

			HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
			if (response.statusCode() < 200 || response.statusCode() >= 300) {
				throw new GeminiException("Gemini API request failed with status " + response.statusCode(), response.statusCode());
			}
			return extractText(response.body());
		} catch (GeminiException ex) {
			throw ex;
		} catch (Exception ex) {
			throw new GeminiException("Unable to call Gemini API: " + ex.getMessage(), ex);
		}
	}

	private URI generateContentUri() {
		String baseUrl = trimTrailingSlash(properties.baseUrl());
		String model = properties.modelPath();
		String key = URLEncoder.encode(properties.apiKey(), StandardCharsets.UTF_8);
		return URI.create(baseUrl + "/v1beta/" + model + ":generateContent?key=" + key);
	}

	private String requestBody(String prompt) throws Exception {
		Map<String, Object> body = Map.of(
				"contents", List.of(Map.of(
						"role", "user",
						"parts", List.of(Map.of("text", prompt))
				)),
				"generationConfig", Map.of(
						"temperature", properties.temperature(),
						"maxOutputTokens", properties.maxOutputTokens()
				)
		);
		return objectMapper.writeValueAsString(body);
	}

	private String extractText(String responseBody) throws Exception {
		JsonNode root = objectMapper.readTree(responseBody);
		JsonNode candidates = root.path("candidates");
		if (!candidates.isArray() || candidates.isEmpty()) {
			throw new GeminiException("Gemini API returned no candidates");
		}

		JsonNode parts = candidates.get(0).path("content").path("parts");
		if (!parts.isArray() || parts.isEmpty()) {
			throw new GeminiException("Gemini API returned no text parts");
		}

		StringBuilder text = new StringBuilder();
		for (JsonNode part : parts) {
			String value = part.path("text").asText("");
			if (!value.isBlank()) {
				if (!text.isEmpty()) {
					text.append(System.lineSeparator());
				}
				text.append(value);
			}
		}
		if (text.isEmpty()) {
			throw new GeminiException("Gemini API returned an empty text response");
		}
		return text.toString();
	}

	private void requireConfiguredApiKey() {
		if (properties.apiKey() == null || properties.apiKey().isBlank()) {
			throw new GeminiException("Gemini API key is not configured. Set GEMINI_API_KEY.");
		}
	}

	private String trimTrailingSlash(String value) {
		if (value == null || value.isBlank()) {
			return "https://generativelanguage.googleapis.com";
		}
		return value.endsWith("/") ? value.substring(0, value.length() - 1) : value;
	}
}
