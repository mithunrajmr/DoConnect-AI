package com.doconnect.chatservice.chat;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class NotificationClient {

	private static final Logger log = LoggerFactory.getLogger(NotificationClient.class);

	private final RestClient restClient;
	private final String internalToken;

	public NotificationClient(
			RestClient.Builder restClientBuilder,
			@Value("${app.notifications.backend-url:http://localhost:8080}") String backendUrl,
			@Value("${app.notifications.internal-token:change-this-notification-token}") String internalToken
	) {
		this.restClient = restClientBuilder
				.baseUrl(backendUrl)
				.defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
				.build();
		this.internalToken = internalToken;
	}

	public void sendChatMessageNotification(ChatMessageResponse response) {
		try {
			restClient.post()
					.uri("/internal/notifications/chat-message")
					.header("X-Internal-Token", internalToken)
					.body(ChatNotificationEvent.from(response))
					.retrieve()
					.toBodilessEntity();
			log.info("Notification sent successfully to backend. messageId={}", response.getId());
		} catch (org.springframework.web.client.RestClientResponseException ex) {
			log.warn("Unexpected response from backend. messageId={}, error={}", response.getId(), ex.getMessage());
		} catch (Exception ex) {
			log.error("Inter-service communication failures. messageId={}, error={}", response.getId(), ex.getMessage());
		}
	}
}
