package com.doconnect.backend.config;

import com.doconnect.backend.auth.AppUserDetailsService;
import com.doconnect.backend.auth.JwtService;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Component;

@Component
public class NotificationWebSocketAuthChannelInterceptor implements ChannelInterceptor {

	private final JwtService jwtService;
	private final AppUserDetailsService userDetailsService;

	public NotificationWebSocketAuthChannelInterceptor(JwtService jwtService, AppUserDetailsService userDetailsService) {
		this.jwtService = jwtService;
		this.userDetailsService = userDetailsService;
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

			String email = jwtService.extractSubject(token)
					.orElseThrow(() -> new AccessDeniedException("Invalid WebSocket token"));
			var userDetails = userDetailsService.loadUserByUsername(email);
			if (!jwtService.isTokenValid(token, userDetails.getUsername())) {
				throw new AccessDeniedException("Invalid WebSocket token");
			}
			accessor.setUser(new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities()));
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
