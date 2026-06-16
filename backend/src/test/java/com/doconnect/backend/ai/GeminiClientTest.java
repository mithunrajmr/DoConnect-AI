package com.doconnect.backend.ai;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.http.HttpClient;
import java.net.http.HttpResponse;
import org.junit.jupiter.api.Test;

class GeminiClientTest {

	@Test
	void generateTextExtractsTextFromSuccessfulResponse() throws Exception {
		HttpClient httpClient = mock(HttpClient.class);
		@SuppressWarnings("unchecked")
		HttpResponse<String> httpResponse = mock(HttpResponse.class);
		GeminiClient client = new GeminiClient(
				httpClient,
				new ObjectMapper(),
				properties("valid-test-api-key-value")
		);

		when(httpResponse.statusCode()).thenReturn(200);
		when(httpResponse.body()).thenReturn("""
				{
				  "candidates": [
				    {
				      "content": {
				        "parts": [
				          { "text": "Generated text" }
				        ]
				      }
				    }
				  ]
				}
				""");
		when(httpClient.send(any(), any(HttpResponse.BodyHandler.class))).thenReturn(httpResponse);

		String result = client.generateText("prompt");

		assertThat(result).isEqualTo("Generated text");
	}

	@Test
	void generateTextRejectsMissingApiKeyBeforeCallingApi() {
		GeminiClient client = new GeminiClient(mock(HttpClient.class), new ObjectMapper(), properties(""));

		assertThatThrownBy(() -> client.generateText("prompt"))
				.isInstanceOf(GeminiException.class)
				.hasMessage("Gemini API key is not configured. Set GEMINI_API_KEY.");
	}

	@Test
	void generateTextReportsInvalidApiStatus() throws Exception {
		HttpClient httpClient = mock(HttpClient.class);
		@SuppressWarnings("unchecked")
		HttpResponse<String> httpResponse = mock(HttpResponse.class);
		GeminiClient client = new GeminiClient(
				httpClient,
				new ObjectMapper(),
				properties("invalid-test-api-key-value")
		);

		when(httpResponse.statusCode()).thenReturn(400);
		when(httpResponse.body()).thenReturn("{\"error\":{\"message\":\"API key not valid\"}}");
		when(httpClient.send(any(), any(HttpResponse.BodyHandler.class))).thenReturn(httpResponse);

		assertThatThrownBy(() -> client.generateText("prompt"))
				.isInstanceOf(GeminiException.class)
				.hasMessage("Gemini API request failed with status 400");
	}

	private GeminiProperties properties(String apiKey) {
		return new GeminiProperties(
				apiKey,
				"gemini-2.0-flash",
				"https://generativelanguage.googleapis.com",
				30,
				0.35,
				1200
		);
	}
}
