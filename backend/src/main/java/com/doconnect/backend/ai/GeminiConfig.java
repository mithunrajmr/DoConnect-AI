package com.doconnect.backend.ai;

import java.net.http.HttpClient;
import java.time.Duration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(GeminiProperties.class)
public class GeminiConfig {

	@Bean
	public HttpClient geminiHttpClient(GeminiProperties properties) {
		return HttpClient.newBuilder()
				.connectTimeout(Duration.ofSeconds(properties.timeoutSeconds()))
				.build();
	}
}
