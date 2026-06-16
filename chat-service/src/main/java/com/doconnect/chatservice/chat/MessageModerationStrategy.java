package com.doconnect.chatservice.chat;

import com.doconnect.chatservice.security.ChatPrincipal;

/**
 * Strategy extension point for future AI/human moderation.
 */
public interface MessageModerationStrategy {

	ModerationDecision evaluate(ChatMessageRequest request, ChatPrincipal sender);
}
