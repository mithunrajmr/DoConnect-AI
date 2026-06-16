package com.doconnect.chatservice.chat;

public record ChatNotificationEvent(
		Long senderId,
		String senderName,
		Long messageId,
		String content,
		String roomId
) {
	public static ChatNotificationEvent from(ChatMessageResponse response) {
		return new ChatNotificationEvent(
				response.getSenderId(),
				response.getUsername(),
				response.getId(),
				response.getContent(),
				response.getRoomId()
		);
	}
}
