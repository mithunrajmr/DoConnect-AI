package com.doconnect.backend.notification;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
public class NotificationRealtimeService {

	private final SimpMessagingTemplate messagingTemplate;

	public NotificationRealtimeService(SimpMessagingTemplate messagingTemplate) {
		this.messagingTemplate = messagingTemplate;
	}

	public void sendToUser(String email, NotificationResponse notification, long unreadCount) {
		messagingTemplate.convertAndSendToUser(email, "/queue/notifications", notification);
		messagingTemplate.convertAndSendToUser(email, "/queue/notifications/unread-count", new UnreadCountResponse(unreadCount));
	}

	public void unreadCountChanged(String email, long unreadCount) {
		messagingTemplate.convertAndSendToUser(email, "/queue/notifications/unread-count", new UnreadCountResponse(unreadCount));
	}
}
