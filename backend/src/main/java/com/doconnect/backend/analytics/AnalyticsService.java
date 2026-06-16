package com.doconnect.backend.analytics;

import com.doconnect.backend.ai.GeminiClient;
import com.doconnect.backend.answer.Answer;
import com.doconnect.backend.answer.AnswerRepository;
import com.doconnect.backend.question.Question;
import com.doconnect.backend.question.QuestionRepository;
import com.doconnect.backend.user.User;
import com.doconnect.backend.user.UserRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AnalyticsService {

	private final UserRepository userRepository;
	private final QuestionRepository questionRepository;
	private final AnswerRepository answerRepository;
	private final GeminiClient geminiClient;
	private final ObjectMapper objectMapper;

	public AnalyticsService(
			UserRepository userRepository,
			QuestionRepository questionRepository,
			AnswerRepository answerRepository,
			GeminiClient geminiClient,
			ObjectMapper objectMapper
	) {
		this.userRepository = userRepository;
		this.questionRepository = questionRepository;
		this.answerRepository = answerRepository;
		this.geminiClient = geminiClient;
		this.objectMapper = objectMapper;
	}

	@Transactional(readOnly = true)
	public OverviewAnalyticsResponse overview() {
		return new OverviewAnalyticsResponse(
				userRepository.count(),
				questionRepository.count(),
				answerRepository.count()
		);
	}

	@Transactional(readOnly = true)
	public List<TagAnalyticsResponse> tags() {
		return questionRepository.findTagUsageCounts().stream()
				.map(row -> new TagAnalyticsResponse((String) row[0], ((Number) row[1]).longValue()))
				.toList();
	}

	@Transactional(readOnly = true)
	public List<UserActivityResponse> activity() {
		Map<Long, Long> questionCounts = countsByUser(questionRepository.findQuestionCountsByAuthor());
		Map<Long, Long> answerCounts = countsByUser(answerRepository.findAnswerCountsByAuthor());
		List<UserActivityResponse> activity = new ArrayList<>();
		for (User user : userRepository.findAll()) {
			long userQuestionCount = questionCounts.getOrDefault(user.getId(), 0L);
			long userAnswerCount = answerCounts.getOrDefault(user.getId(), 0L);
			long totalActivity = userQuestionCount + userAnswerCount;
			if (totalActivity > 0L) {
				activity.add(new UserActivityResponse(
						user.getId(),
						user.getName(),
						user.getEmail(),
						userQuestionCount,
						userAnswerCount,
						totalActivity
				));
			}
		}
		return activity.stream()
				.sorted(Comparator.comparingLong(UserActivityResponse::totalActivity).reversed()
						.thenComparing(UserActivityResponse::name, String.CASE_INSENSITIVE_ORDER))
				.toList();
	}

	@Transactional(readOnly = true)
	public SentimentSummaryResponse sentiment() {
		List<String> items = recentContent();
		if (items.isEmpty()) {
			return new SentimentSummaryResponse(0, "neutral", 0.0d, 1.0d, 0.0d, "No recent questions or answers available.");
		}

		String prompt = """
				You are summarizing sentiment for recent content on a technical discussion platform.
				Review the items and return only valid JSON with:
				{
				  "overallSentiment": "positive" | "neutral" | "negative",
				  "positiveScore": number,
				  "neutralScore": number,
				  "negativeScore": number,
				  "summary": string
				}
				Keep the summary short and practical.

				Content:
				%s
				""".formatted(String.join(System.lineSeparator() + System.lineSeparator(), items));

		return parseSentimentResponse(items.size(), geminiClient.generateText(prompt));
	}

	private Map<Long, Long> countsByUser(List<Object[]> rows) {
		Map<Long, Long> counts = new HashMap<>();
		for (Object[] row : rows) {
			counts.put(((Number) row[0]).longValue(), ((Number) row[1]).longValue());
		}
		return counts;
	}

	private List<String> recentContent() {
		List<String> content = new ArrayList<>();
		for (Question question : questionRepository.findTop10ByOrderByCreatedAtDesc()) {
			content.add("Question: " + truncate(question.getTitle() + " - " + question.getBody()));
		}
		for (Answer answer : answerRepository.findTop10ByOrderByCreatedAtDesc()) {
			content.add("Answer: " + truncate(answer.getBody()));
		}
		return content.stream().limit(20).toList();
	}

	private SentimentSummaryResponse parseSentimentResponse(int analyzedItems, String response) {
		try {
			JsonNode root = objectMapper.readTree(stripCodeFence(response));
			double positive = boundedScore(root.path("positiveScore").asDouble(0.0d));
			double neutral = boundedScore(root.path("neutralScore").asDouble(0.0d));
			double negative = boundedScore(root.path("negativeScore").asDouble(0.0d));
			String overall = normalizeSentiment(root.path("overallSentiment").asText("neutral"));
			String summary = root.path("summary").asText("").trim();
			if (summary.isEmpty()) {
				summary = "Recent platform discussions are mostly " + overall + ".";
			}
			return new SentimentSummaryResponse(analyzedItems, overall, positive, neutral, negative, summary);
		} catch (Exception ignored) {
			String normalized = response == null ? "" : response.toLowerCase(Locale.ROOT);
			String overall = normalized.contains("negative") ? "negative"
					: normalized.contains("positive") ? "positive" : "neutral";
			double positive = "positive".equals(overall) ? 0.7d : 0.2d;
			double negative = "negative".equals(overall) ? 0.7d : 0.1d;
			double neutral = "neutral".equals(overall) ? 0.7d : 0.2d;
			return new SentimentSummaryResponse(
					analyzedItems,
					overall,
					positive,
					neutral,
					negative,
					"Recent platform discussions are mostly " + overall + "."
			);
		}
	}

	private double boundedScore(double value) {
		return Math.max(0.0d, Math.min(1.0d, value));
	}

	private String normalizeSentiment(String value) {
		String normalized = value == null ? "neutral" : value.trim().toLowerCase(Locale.ROOT);
		return List.of("positive", "neutral", "negative").contains(normalized) ? normalized : "neutral";
	}

	private String truncate(String value) {
		String compact = value.lines().collect(Collectors.joining(" ")).trim();
		return compact.length() <= 240 ? compact : compact.substring(0, 237) + "...";
	}

	private String stripCodeFence(String value) {
		return value == null ? "" : value.replace("```json", "").replace("```", "").trim();
	}
}
