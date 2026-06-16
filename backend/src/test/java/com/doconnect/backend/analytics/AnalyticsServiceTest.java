package com.doconnect.backend.analytics;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import com.doconnect.backend.ai.GeminiClient;
import com.doconnect.backend.answer.Answer;
import com.doconnect.backend.answer.AnswerRepository;
import com.doconnect.backend.question.Question;
import com.doconnect.backend.question.QuestionRepository;
import com.doconnect.backend.user.User;
import com.doconnect.backend.user.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class AnalyticsServiceTest {

	@Mock
	private UserRepository userRepository;

	@Mock
	private QuestionRepository questionRepository;

	@Mock
	private AnswerRepository answerRepository;

	@Mock
	private GeminiClient geminiClient;

	@Test
	void overviewReturnsRepositoryCounts() {
		AnalyticsService service = service();
		when(userRepository.count()).thenReturn(4L);
		when(questionRepository.count()).thenReturn(6L);
		when(answerRepository.count()).thenReturn(9L);

		OverviewAnalyticsResponse response = service.overview();

		assertThat(response.totalUsers()).isEqualTo(4L);
		assertThat(response.totalQuestions()).isEqualTo(6L);
		assertThat(response.totalAnswers()).isEqualTo(9L);
	}

	@Test
	void activityCombinesQuestionAndAnswerCountsPerUser() {
		AnalyticsService service = service();
		User user = user(1L, "Alice", "alice@example.com");
		when(userRepository.findAll()).thenReturn(List.of(user));
		when(questionRepository.findQuestionCountsByAuthor()).thenReturn(List.<Object[]>of(new Object[] {1L, 2L}));
		when(answerRepository.findAnswerCountsByAuthor()).thenReturn(List.<Object[]>of(new Object[] {1L, 3L}));

		List<UserActivityResponse> response = service.activity();

		assertThat(response).hasSize(1);
		assertThat(response.get(0).totalActivity()).isEqualTo(5L);
		assertThat(response.get(0).questionCount()).isEqualTo(2L);
		assertThat(response.get(0).answerCount()).isEqualTo(3L);
	}

	@Test
	void sentimentParsesGeminiJson() {
		AnalyticsService service = service();
		Question question = new Question();
		question.setTitle("JWT issue");
		question.setBody("Token is expired");
		Answer answer = new Answer();
		answer.setBody("Refresh the token.");
		when(questionRepository.findTop10ByOrderByCreatedAtDesc()).thenReturn(List.of(question));
		when(answerRepository.findTop10ByOrderByCreatedAtDesc()).thenReturn(List.of(answer));
		when(geminiClient.generateText(anyString()))
				.thenReturn("""
						{
						  "overallSentiment": "neutral",
						  "positiveScore": 0.2,
						  "neutralScore": 0.7,
						  "negativeScore": 0.1,
						  "summary": "Recent discussions are mostly neutral and solution-focused."
						}
						""");

		SentimentSummaryResponse response = service.sentiment();

		assertThat(response.analyzedItems()).isEqualTo(2);
		assertThat(response.overallSentiment()).isEqualTo("neutral");
		assertThat(response.summary()).contains("neutral");
	}

	private AnalyticsService service() {
		return new AnalyticsService(userRepository, questionRepository, answerRepository, geminiClient, new ObjectMapper());
	}

	private User user(Long id, String name, String email) {
		User user = new User();
		ReflectionTestUtils.setField(user, "id", id);
		user.setName(name);
		user.setEmail(email);
		user.setPasswordHash("hash");
		return user;
	}
}
