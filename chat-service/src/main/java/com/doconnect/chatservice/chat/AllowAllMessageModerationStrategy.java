package com.doconnect.chatservice.chat;

import com.doconnect.chatservice.security.ChatPrincipal;
import org.springframework.stereotype.Component;

/**
 * Default moderation strategy for the current phase. It approves every message
 * while preserving the strategy seam for a later AI moderation implementation.
 */
@Component
public class AllowAllMessageModerationStrategy implements MessageModerationStrategy {

	@Override
	public ModerationDecision evaluate(ChatMessageRequest request, ChatPrincipal sender) {
		return new ModerationDecision(ModerationStatus.APPROVED, null);
	}
}
