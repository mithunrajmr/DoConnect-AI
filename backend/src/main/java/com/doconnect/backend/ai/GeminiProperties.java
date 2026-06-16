package com.doconnect.backend.ai;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.gemini")
public record GeminiProperties(
		String apiKey,
		String model,
		String baseUrl,
		int timeoutSeconds,
		double temperature,
		int maxOutputTokens
) {

	public String modelPath() {
		if (model == null || model.isBlank()) {
			return "models/gemini-2.0-flash";
		}
		return model.startsWith("models/") ? model : "models/" + model;
	}
}
