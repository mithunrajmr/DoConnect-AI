package com.doconnect.backend.question;

import com.doconnect.backend.content.ModerationStatus;
import com.doconnect.backend.tag.Tag;
import com.doconnect.backend.user.User;
import java.time.Instant;
import java.util.List;

public record QuestionResponse(
		Long id,
		String title,
		String body,
		AuthorSummary author,
		List<String> tags,
		QuestionStatus status,
		ModerationStatus moderationStatus,
		Long acceptedAnswerId,
		long answerCount,
		long viewCount,
		String aiSummary,
		Instant createdAt,
		Instant updatedAt
) {

	public static QuestionResponse from(Question question) {
		return new QuestionResponse(
				question.getId(),
				question.getTitle(),
				question.getBody(),
				AuthorSummary.from(question.getAuthor()),
				question.getTags().stream().map(Tag::getDisplayName).toList(),
				question.getStatus(),
				question.getModerationStatus(),
				question.getAcceptedAnswerId(),
				question.getAnswers().size(),
				question.getViewCount(),
				question.getAiSummary(),
				question.getCreatedAt(),
				question.getUpdatedAt()
		);
	}

	public record AuthorSummary(Long id, String name, String email) {

		public static AuthorSummary from(User user) {
			return new AuthorSummary(user.getId(), user.getName(), user.getEmail());
		}
	}
}
