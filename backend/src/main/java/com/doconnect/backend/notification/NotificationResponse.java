package com.doconnect.backend.notification;

import java.time.Instant;

public record NotificationResponse(
		Long id,
		NotificationType type,
		String title,
		String message,
		String targetPath,
		Long referenceId,
		boolean read,
		int occurrenceCount,
		Instant createdAt,
		Instant updatedAt
) {
	public static NotificationResponse from(Notification notification) {
		return new NotificationResponse(
				notification.getId(),
				notification.getType(),
				notification.getTitle(),
				notification.getMessage(),
				notification.getTargetPath(),
				notification.getReferenceId(),
				notification.isRead(),
				notification.getOccurrenceCount(),
				notification.getCreatedAt(),
				notification.getUpdatedAt()
		);
	}
}
