package com.doconnect.backend.ai;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ai")
public class AiController {

	private final AiAnswerAssistantService aiAnswerAssistantService;
	private final AiModerationService aiModerationService;

	public AiController(AiAnswerAssistantService aiAnswerAssistantService, AiModerationService aiModerationService) {
		this.aiAnswerAssistantService = aiAnswerAssistantService;
		this.aiModerationService = aiModerationService;
	}

	@PostMapping("/questions/{questionId}/suggest-answer")
	public AiAnswerResponse suggestAnswer(@PathVariable Long questionId) {
		return aiAnswerAssistantService.suggestAnswer(questionId);
	}

	@PostMapping("/questions/{questionId}/summarize")
	public DiscussionSummaryResponse summarize(@PathVariable Long questionId) {
		return aiAnswerAssistantService.summarizeDiscussion(questionId);
	}

	@PostMapping("/improve-draft")
	public ImproveDraftResponse improveDraft(@Valid @RequestBody ImproveDraftRequest request) {
		return aiAnswerAssistantService.improveDraft(request);
	}

	@PostMapping("/moderation/check")
	public ModerationResponse checkModeration(@Valid @RequestBody ModerationCheckRequest request) {
		return aiModerationService.checkContent(request.content());
	}

	@PostMapping("/moderation/question/{questionId}")
	public ModerationResponse moderateQuestion(@PathVariable Long questionId) {
		return aiModerationService.moderateQuestion(questionId);
	}

	@PostMapping("/moderation/answer/{answerId}")
	public ModerationResponse moderateAnswer(@PathVariable Long answerId) {
		return aiModerationService.moderateAnswer(answerId);
	}

	@GetMapping("/moderation/question/{questionId}/report")
	public ModerationResponse questionReport(@PathVariable Long questionId) {
		return aiModerationService.questionReport(questionId);
	}

	@GetMapping("/moderation/answer/{answerId}/report")
	public ModerationResponse answerReport(@PathVariable Long answerId) {
		return aiModerationService.answerReport(answerId);
	}
}

record ModerationCheckRequest(
		@NotBlank(message = "content is required")
		String content
) {
}

record ModerationResponse(
		boolean toxic,
		boolean spam,
		double score,
		String reason
) {
}
