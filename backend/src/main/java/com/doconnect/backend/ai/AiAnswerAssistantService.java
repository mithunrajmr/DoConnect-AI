package com.doconnect.backend.ai;

import com.doconnect.backend.answer.Answer;
import com.doconnect.backend.answer.AnswerRepository;
import com.doconnect.backend.question.Question;
import com.doconnect.backend.question.QuestionService;
import com.doconnect.backend.tag.Tag;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * User-facing AI assistant for answer generation, discussion summaries, and
 * draft improvement. It builds grounded prompts from existing project data and
 * delegates model calls to the reusable Gemini client.
 */
@Service
public class AiAnswerAssistantService {

	private final AnswerRepository answerRepository;
	private final GeminiClient geminiClient;
	private final QuestionService questionService;

	public AiAnswerAssistantService(
			AnswerRepository answerRepository,
			GeminiClient geminiClient,
			QuestionService questionService
	) {
		this.answerRepository = answerRepository;
		this.geminiClient = geminiClient;
		this.questionService = questionService;
	}

	@Transactional(readOnly = true)
	public AiAnswerResponse suggestAnswer(Long questionId) {
		Question question = questionService.findQuestion(questionId);
		List<Answer> answers = answerRepository.findByQuestionIdOrderByCreatedAtAsc(questionId);
		String prompt = buildAnswerPrompt(question, answers);
		return new AiAnswerResponse(question.getId(), geminiClient.generateText(prompt));
	}

	@Transactional(readOnly = true)
	public DiscussionSummaryResponse summarizeDiscussion(Long questionId) {
		Question question = questionService.findQuestion(questionId);
		List<Answer> answers = answerRepository.findByQuestionIdOrderByCreatedAtAsc(questionId);
		String prompt = buildSummaryPrompt(question, answers);
		return new DiscussionSummaryResponse(question.getId(), geminiClient.generateText(prompt));
	}

	public ImproveDraftResponse improveDraft(ImproveDraftRequest request) {
		String prompt = """
				You are improving a user's draft answer for a technical discussion platform.
				Improve grammar and clarity while preserving the original meaning.
				Do not add unsupported claims. Return only the improved draft.

				Draft:
				%s
				""".formatted(request.text().trim());
		return new ImproveDraftResponse(geminiClient.generateText(prompt));
	}

	private String buildAnswerPrompt(Question question, List<Answer> answers) {
		return """
				You are an expert technical mentor on DoConnect AI.
				Generate a high-quality answer suggestion for the question below.
				Use the existing answers as context, but do not simply repeat them.
				If existing answers are incomplete, improve on them.
				Keep the answer practical, clear, and concise.
				Return only the suggested answer.

				Question title:
				%s

				Question body:
				%s

				Tags:
				%s

				Existing answers:
				%s
				""".formatted(question.getTitle(), question.getBody(), tags(question), answers(answers));
	}

	private String buildSummaryPrompt(Question question, List<Answer> answers) {
		return """
				You are summarizing a technical discussion for a reader.
				Create a concise summary of the question and all answers.
				Highlight the main issue, useful solutions, and any unresolved points.
				Return only the summary.

				Question title:
				%s

				Question body:
				%s

				Answers:
				%s
				""".formatted(question.getTitle(), question.getBody(), answers(answers));
	}

	private String tags(Question question) {
		if (question.getTags().isEmpty()) {
			return "No tags provided.";
		}
		return question.getTags().stream()
				.map(Tag::getDisplayName)
				.collect(Collectors.joining(", "));
	}

	private String answers(List<Answer> answers) {
		if (answers.isEmpty()) {
			return "No answers yet.";
		}
		StringBuilder builder = new StringBuilder();
		for (int i = 0; i < answers.size(); i++) {
			builder.append(i + 1)
					.append(". ")
					.append(answers.get(i).getBody())
					.append(System.lineSeparator());
		}
		return builder.toString();
	}
}
