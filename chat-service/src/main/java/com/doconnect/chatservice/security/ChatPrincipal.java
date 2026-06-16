package com.doconnect.chatservice.security;

import java.security.Principal;
import java.util.Collection;
import java.util.List;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

/**
 * Authenticated user identity extracted from the main service JWT.
 */
public class ChatPrincipal implements Principal {

	private final Long userId;
	private final String username;
	private final String role;

	public ChatPrincipal(Long userId, String username, String role) {
		this.userId = userId;
		this.username = username;
		this.role = role;
	}

	public Long userId() {
		return userId;
	}

	public String username() {
		return username;
	}

	public String role() {
		return role;
	}

	public Collection<? extends GrantedAuthority> authorities() {
		return List.of(new SimpleGrantedAuthority("ROLE_" + role));
	}

	@Override
	public String getName() {
		return username;
	}
}
