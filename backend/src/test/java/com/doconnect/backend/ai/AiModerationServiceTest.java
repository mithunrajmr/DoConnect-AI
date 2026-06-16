package com.doconnect.backend.ai;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import com.doconnect.backend.answer.Answer;
import com.doconnect.backend.answer.AnswerService;
import com.doconnect.backend.question.Question;
import com.doconnect.backend.question.QuestionService;
import com.doconnect.backend.tag.Tag;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class AiModerationServiceTest {

	@Mock
	private GeminiClient geminiClient;

	@Mock
	private QuestionService questionService;

	@Mock
	private AnswerService answerService;

	@Test
	void checkContentParsesGeminiJson() {
		AiModerationService service = service();
		when(geminiClient.generateText(anyString()))
				.thenReturn("""
						{
						  "toxic": false,
						  "spam": false,
						  "score": 0.12,
						  "reason": "Content appears safe."
						}
						""");

		ModerationResponse response = service.checkContent("How do I configure JWT?");

		assertThat(response.toxic()).isFalse();
		assertThat(response.spam()).isFalse();
		assertThat(response.score()).isEqualTo(0.12d);
		assertThat(response.reason()).isEqualTo("Content appears safe.");
	}

	@Test
	void questionReportUsesStoredQuestionContent() {
		AiModerationService service = service();
		Question question = new Question();
		ReflectionTestUtils.setField(question, "id", 10L);
		question.setTitle("Need help");
		question.setBody("Why is my token rejected?");
		Tag tag = new Tag();
		tag.setName("jwt");
		tag.setDisplayName("JWT");
		question.setTags(Set.of(tag));
		when(questionService.findQuestion(10L)).thenReturn(question);
		when(geminiClient.generateText(anyString()))
				.thenReturn("{\"toxic\":false,\"spam\":false,\"score\":0.18,\"reason\":\"Technical and safe.\"}");

		ModerationResponse response = service.questionReport(10L);

		assertThat(response.reason()).isEqualTo("Technical and safe.");
	}

	@Test
	void answerModerationFallsBackWhenGeminiReturnsPlainText() {
		AiModerationService service = service();
		Answer answer = new Answer();
		answer.setBody("Buy followers now");
		Question question = new Question();
		question.setTitle("Spam?");
		answer.setQuestion(question);
		when(answerService.findAnswerById(7L)).thenReturn(answer);
		when(geminiClient.generateText(anyString())).thenReturn("This looks like spam and promotion.");

		ModerationResponse response = service.moderateAnswer(7L);

		assertThat(response.spam()).isTrue();
		assertThat(response.score()).isGreaterThanOrEqualTo(0.7d);
	}

	private AiModerationService service() {
		return new AiModerationService(geminiClient, new ObjectMapper(), questionService, answerService);
	}
}
