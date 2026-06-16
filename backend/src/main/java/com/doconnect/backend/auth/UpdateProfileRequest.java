package com.doconnect.backend.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateProfileRequest(
		@NotBlank @Size(max = 120) String name,
		@Size(min = 8, max = 72) String password
) {
}
