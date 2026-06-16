package com.doconnect.backend.ai;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ImproveDraftRequest(
		@NotBlank @Size(max = 5000) String text
) {
}
