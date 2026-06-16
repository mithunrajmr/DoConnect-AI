package com.doconnect.backend.answer;

import com.doconnect.backend.content.ModerationStatus;
import com.doconnect.backend.question.QuestionResponse.AuthorSummary;
import java.math.BigDecimal;
import java.time.Instant;

public record AnswerResponse(
		Long id,
		Long questionId,
		String body,
		AuthorSummary author,
		boolean accepted,
		boolean aiGenerated,
		BigDecimal aiConfidence,
		ModerationStatus moderationStatus,
		Instant createdAt,
		Instant updatedAt
) {

	public static AnswerResponse from(Answer answer) {
		return new AnswerResponse(
				answer.getId(),
				answer.getQuestion().getId(),
				answer.getBody(),
				AuthorSummary.from(answer.getAuthor()),
				answer.isAccepted(),
				answer.isAiGenerated(),
				answer.getAiConfidence(),
				answer.getModerationStatus(),
				answer.getCreatedAt(),
				answer.getUpdatedAt()
		);
	}
}
