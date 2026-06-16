package com.doconnect.chatservice.chat;

import com.doconnect.chatservice.security.ChatPrincipal;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Factory for creating persistable chat messages from authenticated user input.
 */
@Component
public class ChatMessageFactory {

	private final String globalRoomId;

	public ChatMessageFactory(@Value("${app.chat.global-room-id}") String globalRoomId) {
		this.globalRoomId = globalRoomId;
	}

	public ChatMessage createGlobalMessage(
			ChatMessageRequest request,
			ChatPrincipal sender,
			ModerationDecision moderationDecision
	) {
		ChatMessage message = new ChatMessage();
		message.setSenderId(requireUserId(sender));
		message.setUsername(sender.username());
		message.setContent(request.content().trim());
		message.setRoomType(RoomType.GLOBAL);
		message.setRoomId(globalRoomId);
		message.setModerationStatus(moderationDecision.status());
		message.setModerationReason(moderationDecision.reason());
		return message;
	}

	private Long requireUserId(ChatPrincipal sender) {
		if (sender.userId() == null) {
			throw new IllegalArgumentException("JWT is missing userId claim required by chat service");
		}
		return sender.userId();
	}
}
