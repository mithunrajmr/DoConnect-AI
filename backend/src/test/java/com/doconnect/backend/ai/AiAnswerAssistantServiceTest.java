package com.doconnect.backend.ai;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.doconnect.backend.answer.Answer;
import com.doconnect.backend.answer.AnswerRepository;
import com.doconnect.backend.question.Question;
import com.doconnect.backend.question.QuestionService;
import com.doconnect.backend.tag.Tag;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class AiAnswerAssistantServiceTest {

	@Mock
	private AnswerRepository answerRepository;

	@Mock
	private GeminiClient geminiClient;

	@Mock
	private QuestionService questionService;

	@Test
	void suggestAnswerBuildsGroundedPromptFromQuestionAndAnswers() {
		Question question = question("How to use JWT?", "I need JWT authentication in Spring Boot.");
		Answer answer = answer("Use a filter and validate the bearer token.");
		AiAnswerAssistantService service = new AiAnswerAssistantService(answerRepository, geminiClient, questionService);

		when(questionService.findQuestion(10L)).thenReturn(question);
		when(answerRepository.findByQuestionIdOrderByCreatedAtAsc(10L)).thenReturn(List.of(answer));
		when(geminiClient.generateText(anyString())).thenReturn("Generated answer");

		AiAnswerResponse response = service.suggestAnswer(10L);

		ArgumentCaptor<String> promptCaptor = ArgumentCaptor.forClass(String.class);
		verify(geminiClient).generateText(promptCaptor.capture());
		assertThat(promptCaptor.getValue())
				.contains("How to use JWT?")
				.contains("I need JWT authentication in Spring Boot.")
				.contains("Use a filter and validate the bearer token.");
		assertThat(response.questionId()).isEqualTo(10L);
		assertThat(response.suggestedAnswer()).isEqualTo("Generated answer");
	}

	@Test
	void summarizeDiscussionUsesQuestionAndAllAnswers() {
		Question question = question("What is JPA?", "I need a concise explanation.");
		AiAnswerAssistantService service = new AiAnswerAssistantService(answerRepository, geminiClient, questionService);

		when(questionService.findQuestion(10L)).thenReturn(question);
		when(answerRepository.findByQuestionIdOrderByCreatedAtAsc(10L))
				.thenReturn(List.of(answer("JPA maps Java objects to database tables.")));
		when(geminiClient.generateText(anyString())).thenReturn("Summary");

		DiscussionSummaryResponse response = service.summarizeDiscussion(10L);

		ArgumentCaptor<String> promptCaptor = ArgumentCaptor.forClass(String.class);
		verify(geminiClient).generateText(promptCaptor.capture());
		assertThat(promptCaptor.getValue())
				.contains("What is JPA?")
				.contains("JPA maps Java objects to database tables.");
		assertThat(response.summary()).isEqualTo("Summary");
	}

	@Test
	void improveDraftKeepsMeaningInstructionAndReturnsImprovedText() {
		AiAnswerAssistantService service = new AiAnswerAssistantService(answerRepository, geminiClient, questionService);
		when(geminiClient.generateText(anyString())).thenReturn("This is clearer.");

		ImproveDraftResponse response = service.improveDraft(new ImproveDraftRequest("this are unclear"));

		ArgumentCaptor<String> promptCaptor = ArgumentCaptor.forClass(String.class);
		verify(geminiClient).generateText(promptCaptor.capture());
		assertThat(promptCaptor.getValue())
				.contains("Improve grammar and clarity")
				.contains("preserving the original meaning")
				.contains("this are unclear");
		assertThat(response.improvedText()).isEqualTo("This is clearer.");
	}

	private Question question(String title, String body) {
		Question question = new Question();
		ReflectionTestUtils.setField(question, "id", 10L);
		question.setTitle(title);
		question.setBody(body);
		Tag tag = new Tag();
		tag.setName("jwt");
		tag.setDisplayName("JWT");
		question.setTags(Set.of(tag));
		return question;
	}

	private Answer answer(String body) {
		Answer answer = new Answer();
		answer.setBody(body);
		return answer;
	}
}
