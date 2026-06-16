package com.doconnect.backend.ai;

import com.doconnect.backend.answer.Answer;
import com.doconnect.backend.answer.AnswerService;
import com.doconnect.backend.question.Question;
import com.doconnect.backend.question.QuestionService;
import com.doconnect.backend.tag.Tag;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Locale;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
public class AiModerationService {

	private final GeminiClient geminiClient;
	private final ObjectMapper objectMapper;
	private final QuestionService questionService;
	private final AnswerService answerService;

	public AiModerationService(
			GeminiClient geminiClient,
			ObjectMapper objectMapper,
			QuestionService questionService,
			AnswerService answerService
	) {
		this.geminiClient = geminiClient;
		this.objectMapper = objectMapper;
		this.questionService = questionService;
		this.answerService = answerService;
	}

	public ModerationResponse checkContent(String content) {
		return moderate(content, false);
	}

	@Transactional(readOnly = true)
	public ModerationResponse moderateQuestion(Long questionId) {
		Question question = questionService.findQuestion(questionId);
		return moderate(questionContent(question), false);
	}

	@Transactional(readOnly = true)
	public ModerationResponse moderateAnswer(Long answerId) {
		Answer answer = answerService.findAnswerById(answerId);
		return moderate(answerContent(answer), false);
	}

	@Transactional(readOnly = true)
	public ModerationResponse questionReport(Long questionId) {
		Question question = questionService.findQuestion(questionId);
		return moderate(questionContent(question), true);
	}

	@Transactional(readOnly = true)
	public ModerationResponse answerReport(Long answerId) {
		Answer answer = answerService.findAnswerById(answerId);
		return moderate(answerContent(answer), true);
	}

	private ModerationResponse moderate(String content, boolean detailed) {
		String prompt = """
				You are moderating content for a professional technical discussion platform.
				Evaluate the content for toxicity, harassment, hate, abuse, and obvious spam or promotion.
				Return only valid JSON with these fields:
				{
				  "toxic": boolean,
				  "spam": boolean,
				  "score": number,
				  "reason": string
				}
				Use a score from 0.0 to 1.0 where higher means more unsafe.
				Keep the reason %s.

				Content:
				%s
				""".formatted(detailed ? "specific and explanatory" : "brief and concise", content.trim());
		ModerationResponse result = parseModerationResponse(geminiClient.generateText(prompt));
		if (result.toxic() || result.spam()) {
			log.warn("Toxic or spam content detected. toxic={}, spam={}, score={}", result.toxic(), result.spam(), result.score());
			log.info("Content flagged. reason={}", result.reason());
		} else {
			log.info("Content approved. score={}", result.score());
		}
		return result;
	}

	private ModerationResponse parseModerationResponse(String response) {
		try {
			JsonNode root = objectMapper.readTree(stripCodeFence(response));
			boolean toxic = root.path("toxic").asBoolean(false);
			boolean spam = root.path("spam").asBoolean(false);
			double score = Math.max(0.0d, Math.min(1.0d, root.path("score").asDouble(0.0d)));
			String reason = root.path("reason").asText("").trim();
			if (reason.isEmpty()) {
				reason = fallbackReason(toxic, spam, score);
			}
			return new ModerationResponse(toxic, spam, score, reason);
		} catch (Exception ignored) {
			String normalized = response == null ? "" : response.toLowerCase(Locale.ROOT);
			boolean toxic = normalized.contains("toxic") || normalized.contains("abusive") || normalized.contains("harassment");
			boolean spam = normalized.contains("spam") || normalized.contains("promotion") || normalized.contains("advertis");
			double score = toxic || spam ? 0.7d : 0.1d;
			return new ModerationResponse(toxic, spam, score, fallbackReason(toxic, spam, score));
		}
	}

	private String questionContent(Question question) {
		return """
				Question title: %s
				Question body: %s
				Tags: %s
				""".formatted(
				question.getTitle(),
				question.getBody(),
				question.getTags().isEmpty()
						? "None"
						: question.getTags().stream().map(Tag::getDisplayName).collect(Collectors.joining(", "))
		);
	}

	private String answerContent(Answer answer) {
		return """
				Answer body: %s
				Related question title: %s
				""".formatted(answer.getBody(), answer.getQuestion().getTitle());
	}

	private String fallbackReason(boolean toxic, boolean spam, double score) {
		if (toxic && spam) {
			return "Content appears unsafe and potentially spam-like.";
		}
		if (toxic) {
			return "Content appears potentially toxic or abusive.";
		}
		if (spam) {
			return "Content appears promotional or spam-like.";
		}
		return score <= 0.2d ? "Content appears safe." : "Content appears mostly safe with mild risk.";
	}

	private String stripCodeFence(String value) {
		return value == null ? "" : value.replace("```json", "").replace("```", "").trim();
	}
}
