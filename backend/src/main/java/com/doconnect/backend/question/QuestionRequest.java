package com.doconnect.backend.question;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.List;

public record QuestionRequest(
		@NotBlank @Size(max = 180) String title,
		@NotBlank @Size(min = 10) String body,
		@Size(max = 8) List<@NotBlank @Size(max = 80) String> tags
) {
}
