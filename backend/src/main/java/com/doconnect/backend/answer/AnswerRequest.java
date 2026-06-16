package com.doconnect.backend.answer;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AnswerRequest(
		@NotBlank @Size(min = 5) String body
) {
}
