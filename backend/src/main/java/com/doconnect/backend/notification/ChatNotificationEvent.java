package com.doconnect.backend.notification;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ChatNotificationEvent(
		@NotNull(message = "senderId is required")
		Long senderId,
		@NotBlank(message = "senderName is required")
		String senderName,
		@NotNull(message = "messageId is required")
		Long messageId,
		@NotBlank(message = "content is required")
		String content,
		@NotBlank(message = "roomId is required")
		String roomId
) {
}
