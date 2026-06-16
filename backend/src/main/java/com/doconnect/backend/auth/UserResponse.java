package com.doconnect.backend.auth;

import com.doconnect.backend.user.User;
import com.doconnect.backend.user.UserRole;
import java.time.Instant;

public record UserResponse(
		Long id,
		String name,
		String email,
		UserRole role,
		Instant createdAt,
		Instant updatedAt
) {

	public static UserResponse from(User user) {
		return new UserResponse(
				user.getId(),
				user.getName(),
				user.getEmail(),
				user.getRole(),
				user.getCreatedAt(),
				user.getUpdatedAt()
		);
	}
}
