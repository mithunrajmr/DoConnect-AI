package com.doconnect.chatservice.chat;

public record ModerationDecision(
		ModerationStatus status,
		String reason
) {
}
