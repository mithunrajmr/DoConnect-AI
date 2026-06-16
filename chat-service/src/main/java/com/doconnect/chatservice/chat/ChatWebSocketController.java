package com.doconnect.chatservice.chat;

import com.doconnect.chatservice.security.ChatPrincipal;
import jakarta.validation.Valid;
import java.security.Principal;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;

@Controller
public class ChatWebSocketController {

	private final ChatService chatService;

	public ChatWebSocketController(ChatService chatService) {
		this.chatService = chatService;
	}

	@MessageMapping("/chat.send")
	public void sendGlobalMessage(@Valid @Payload ChatMessageRequest request, Principal principal) {
		ChatPrincipal chatPrincipal = resolvePrincipal(principal);
		chatService.publishGlobalMessage(request, chatPrincipal);
	}

	private ChatPrincipal resolvePrincipal(Principal principal) {
		if (principal instanceof ChatPrincipal chatPrincipal) {
			return chatPrincipal;
		}
		if (principal instanceof Authentication authentication
				&& authentication.getPrincipal() instanceof ChatPrincipal chatPrincipal) {
			return chatPrincipal;
		}
		throw new IllegalArgumentException("Authenticated chat principal is required");
	}
}
