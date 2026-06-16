package com.doconnect.backend.notification;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.HttpStatus;

@RestController
@RequestMapping("/internal/notifications")
public class InternalNotificationController {

	private final NotificationService notificationService;
	private final String internalToken;

	public InternalNotificationController(
			NotificationService notificationService,
			@Value("${app.notifications.internal-token:change-this-notification-token}") String internalToken
	) {
		this.notificationService = notificationService;
		this.internalToken = internalToken;
	}

	@PostMapping("/chat-message")
	@ResponseStatus(HttpStatus.ACCEPTED)
	public void chatMessage(
			@RequestHeader(name = "X-Internal-Token", required = false) String providedToken,
			@Valid @RequestBody ChatNotificationEvent event
	) {
		if (providedToken == null || !providedToken.equals(internalToken)) {
			throw new AccessDeniedException("Invalid internal notification token");
		}
		notificationService.notifyChatMessage(event);
	}
}
