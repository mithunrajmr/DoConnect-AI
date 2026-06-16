package com.doconnect.backend.ai;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import com.doconnect.backend.question.Question;
import com.doconnect.backend.question.QuestionRepository;
import com.doconnect.backend.question.QuestionResponse;
import com.doconnect.backend.question.QuestionService;
import com.doconnect.backend.tag.Tag;
import com.doconnect.backend.user.User;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class RecommendationServiceTest {

	@Mock
	private GeminiClient geminiClient;

	@Mock
	private QuestionRepository questionRepository;

	@Mock
	private QuestionService questionService;

	@Test
	void predictsNormalizedTagsFromGeminiJson() {
		RecommendationService service = service();
		when(geminiClient.generateText(anyString()))
				.thenReturn("[\"Spring Boot\", \"JWT\", \"Spring Security\"]");

		List<String> tags = service.predictTags("JWT issue", "Tokens are rejected");

		assertThat(tags).containsExactly("spring-boot", "jwt", "spring-security");
	}

	@Test
	void returnsTopFiveQuestionsRankedBySharedTags() {
		Question source = question(1L, "Source", "Body", "spring-boot", "jwt");
		Question strongest = question(2L, "Strong", "Body", "spring-boot", "jwt");
		Question partial = question(3L, "Partial", "Body", "jwt");
		Question unrelated = question(4L, "Other", "Body", "react");
		RecommendationService service = service();
		when(questionService.findQuestion(1L)).thenReturn(source);
		when(questionRepository.findAllByOrderByCreatedAtDesc())
				.thenReturn(List.of(unrelated, partial, strongest, source));

		List<QuestionResponse> results = service.findSimilarQuestions(1L);

		assertThat(results).extracting(QuestionResponse::id).containsExactly(2L, 3L);
	}

	@Test
	void searchesUsingGeminiTermsAndRanksTagMatchFirst() {
		Question tagMatch = question(2L, "Token problem", "Authentication fails", "jwt");
		Question bodyMatch = question(3L, "Login issue", "The login token is rejected", "security");
		RecommendationService service = service();
		when(geminiClient.generateText(anyString())).thenReturn("[\"jwt\", \"login-token\"]");
		when(questionRepository.findAllByOrderByCreatedAtDesc()).thenReturn(List.of(bodyMatch, tagMatch));

		List<QuestionResponse> results = service.search("login token issue");

		assertThat(results).extracting(QuestionResponse::id).containsExactly(2L, 3L);
	}

	private RecommendationService service() {
		return new RecommendationService(geminiClient, new ObjectMapper(), questionRepository, questionService);
	}

	private Question question(Long id, String title, String body, String... tagNames) {
		Question question = new Question();
		ReflectionTestUtils.setField(question, "id", id);
		ReflectionTestUtils.setField(question, "createdAt", Instant.now());
		question.setTitle(title);
		question.setBody(body);
		User author = new User();
		ReflectionTestUtils.setField(author, "id", 99L);
		author.setName("Author");
		author.setEmail("author@example.com");
		question.setAuthor(author);
		LinkedHashSet<Tag> tags = new LinkedHashSet<>();
		for (String tagName : tagNames) {
			Tag tag = new Tag();
			tag.setName(tagName);
			tag.setDisplayName(tagName);
			tags.add(tag);
		}
		question.setTags(tags);
		return question;
	}
}
