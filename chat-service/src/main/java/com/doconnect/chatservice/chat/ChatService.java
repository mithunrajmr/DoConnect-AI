package com.doconnect.chatservice.chat;

import com.doconnect.chatservice.security.ChatPrincipal;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

/**
 * Coordinates chat message persistence and real-time broadcasting.
 */
@Service
public class ChatService {

	public static final String GLOBAL_TOPIC = "/topic/chat/global";

	private final MessageService messageService;
	private final SimpMessagingTemplate messagingTemplate;
	private final NotificationClient notificationClient;

	public ChatService(
			MessageService messageService,
			SimpMessagingTemplate messagingTemplate,
			NotificationClient notificationClient
	) {
		this.messageService = messageService;
		this.messagingTemplate = messagingTemplate;
		this.notificationClient = notificationClient;
	}

	public ChatMessageResponse publishGlobalMessage(ChatMessageRequest request, ChatPrincipal sender) {
		ChatMessageResponse response = messageService.saveGlobalMessage(request, sender);
		messagingTemplate.convertAndSend(GLOBAL_TOPIC, response);
		notificationClient.sendChatMessageNotification(response);
		return response;
	}
}
