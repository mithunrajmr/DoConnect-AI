package com.doconnect.chatservice.config;

import com.doconnect.chatservice.security.JwtService;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;

/**
 * Authenticates STOMP CONNECT frames using the same Bearer token contract as
 * REST requests.
 */
@Component
public class WebSocketAuthChannelInterceptor implements ChannelInterceptor {

	private final JwtService jwtService;

	public WebSocketAuthChannelInterceptor(JwtService jwtService) {
		this.jwtService = jwtService;
	}

	@Override
	public Message<?> preSend(Message<?> message, MessageChannel channel) {
		StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
		if (accessor == null) {
			return message;
		}
		if (StompCommand.CONNECT.equals(accessor.getCommand())) {
			String token = bearerToken(accessor.getFirstNativeHeader("Authorization"));
			if (token == null) {
				token = bearerToken(accessor.getFirstNativeHeader("authorization"));
			}
			if (token == null) {
				throw new AccessDeniedException("Missing WebSocket Authorization header");
			}
			accessor.setUser(jwtService.authenticate(token)
					.orElseThrow(() -> new AccessDeniedException("Invalid WebSocket token")));
		}
		return message;
	}

	private String bearerToken(String authorizationHeader) {
		if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
			return null;
		}
		return authorizationHeader.substring(7);
	}
}
