package com.doconnect.backend.ai;

import com.doconnect.backend.question.Question;
import com.doconnect.backend.question.QuestionRepository;
import com.doconnect.backend.question.QuestionResponse;
import com.doconnect.backend.question.QuestionService;
import com.doconnect.backend.tag.Tag;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RecommendationService {

	private static final Pattern NON_TAG_CHARACTERS = Pattern.compile("[^a-z0-9+#.-]+");
	private static final Set<String> STOP_WORDS = Set.of(
			"about", "after", "again", "being", "could", "from", "have", "issue", "problem",
			"should", "that", "their", "there", "these", "this", "using", "valid", "what", "when", "where", "which", "with", "would"
	);

	private final GeminiClient geminiClient;
	private final ObjectMapper objectMapper;
	private final QuestionRepository questionRepository;
	private final QuestionService questionService;

	public RecommendationService(
			GeminiClient geminiClient,
			ObjectMapper objectMapper,
			QuestionRepository questionRepository,
			QuestionService questionService
	) {
		this.geminiClient = geminiClient;
		this.objectMapper = objectMapper;
		this.questionRepository = questionRepository;
		this.questionService = questionService;
	}

	public List<String> predictTags(String title, String description) {
		String prompt = """
				Predict 3 to 5 concise technical tags for this question.
				Use lowercase kebab-case matching tags such as spring-boot, jwt, spring-security.
				Return only a JSON array of strings and no explanation.

				Title: %s
				Description: %s
				""".formatted(title.trim(), description.trim());
		List<String> tags = parseTerms(geminiClient.generateText(prompt), 5);
		if (tags.size() < 3) {
			tags = mergeTerms(tags, localTerms(title + " " + description, 5), 5);
		}
		return tags;
	}

	@Transactional(readOnly = true)
	public List<QuestionResponse> findSimilarQuestions(Long questionId) {
		Question source = questionService.findQuestion(questionId);
		Set<String> sourceTags = normalizedTags(source);
		return questionRepository.findAllByOrderByCreatedAtDesc().stream()
				.filter(candidate -> !candidate.getId().equals(source.getId()))
				.map(candidate -> new RankedQuestion(candidate, sharedTagCount(sourceTags, candidate)))
				.filter(ranked -> ranked.score() > 0)
				.sorted(Comparator.comparingInt(RankedQuestion::score).reversed()
						.thenComparing(ranked -> ranked.question().getCreatedAt(), Comparator.nullsLast(Comparator.reverseOrder())))
				.limit(5)
				.map(ranked -> QuestionResponse.from(ranked.question()))
				.toList();
	}

	@Transactional(readOnly = true)
	public List<QuestionResponse> search(String query) {
		String prompt = """
				Extract 3 to 8 concise technical search keywords or tags from this query.
				Use lowercase terms and kebab-case for multi-word technologies.
				Return only a JSON array of strings and no explanation.

				Query: %s
				""".formatted(query.trim());
		List<String> terms = mergeTerms(
				parseTerms(geminiClient.generateText(prompt), 8),
				localTerms(query, 8),
				10
		);

		return questionRepository.findAllByOrderByCreatedAtDesc().stream()
				.map(question -> new RankedQuestion(question, searchScore(question, terms)))
				.filter(ranked -> ranked.score() > 0)
				.sorted(Comparator.comparingInt(RankedQuestion::score).reversed()
						.thenComparing(ranked -> ranked.question().getCreatedAt(), Comparator.nullsLast(Comparator.reverseOrder())))
				.limit(20)
				.map(ranked -> QuestionResponse.from(ranked.question()))
				.toList();
	}

	private int searchScore(Question question, List<String> terms) {
		String title = question.getTitle().toLowerCase(Locale.ROOT);
		String body = question.getBody().toLowerCase(Locale.ROOT);
		Set<String> tags = normalizedTags(question);
		int score = 0;
		for (String term : terms) {
			String phrase = term.replace('-', ' ');
			if (tags.contains(term)) {
				score += 8;
			}
			if (title.contains(term) || title.contains(phrase)) {
				score += 4;
			}
			if (body.contains(term) || body.contains(phrase)) {
				score += 1;
			}
		}
		return score;
	}

	private int sharedTagCount(Set<String> sourceTags, Question candidate) {
		int shared = 0;
		for (String tag : normalizedTags(candidate)) {
			if (sourceTags.contains(tag)) {
				shared++;
			}
		}
		return shared;
	}

	private Set<String> normalizedTags(Question question) {
		Set<String> tags = new LinkedHashSet<>();
		for (Tag tag : question.getTags()) {
			tags.add(normalize(tag.getName()));
		}
		return tags;
	}

	private List<String> parseTerms(String value, int limit) {
		List<String> rawTerms = new ArrayList<>();
		try {
			JsonNode root = objectMapper.readTree(stripCodeFence(value));
			if (root.isArray()) {
				root.forEach(node -> rawTerms.add(node.asText()));
			}
		} catch (Exception ignored) {
			// Gemini may occasionally return plain comma-separated text instead of JSON.
		}
		if (rawTerms.isEmpty()) {
			for (String term : stripCodeFence(value).split("[,\\n]")) {
				rawTerms.add(term);
			}
		}
		return normalizeTerms(rawTerms, limit);
	}

	private List<String> localTerms(String value, int limit) {
		List<String> terms = List.of(value.toLowerCase(Locale.ROOT).split("\\s+"));
		return normalizeTerms(terms, limit);
	}

	private List<String> normalizeTerms(List<String> values, int limit) {
		LinkedHashSet<String> terms = new LinkedHashSet<>();
		for (String value : values) {
			String normalized = normalize(value);
			if (normalized.length() >= 2 && !STOP_WORDS.contains(normalized)) {
				terms.add(normalized);
			}
			if (terms.size() == limit) {
				break;
			}
		}
		return List.copyOf(terms);
	}

	private List<String> mergeTerms(List<String> first, List<String> second, int limit) {
		LinkedHashSet<String> merged = new LinkedHashSet<>(first);
		merged.addAll(second);
		return merged.stream().limit(limit).toList();
	}

	private String normalize(String value) {
		String cleaned = value.trim().toLowerCase(Locale.ROOT).replace('_', '-');
		cleaned = NON_TAG_CHARACTERS.matcher(cleaned).replaceAll("-");
		return cleaned.replaceAll("^-+|-+$", "").replaceAll("-{2,}", "-");
	}

	private String stripCodeFence(String value) {
		return value == null ? "" : value.replace("```json", "").replace("```", "").trim();
	}

	private record RankedQuestion(Question question, int score) {
	}
}
