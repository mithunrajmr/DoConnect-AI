package com.doconnect.backend.answer;

import com.doconnect.backend.common.ResourceNotFoundException;
import com.doconnect.backend.notification.NotificationService;
import com.doconnect.backend.question.Question;
import com.doconnect.backend.question.QuestionService;
import com.doconnect.backend.question.QuestionStatus;
import com.doconnect.backend.user.User;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AnswerService {

	private final AnswerRepository answerRepository;
	private final QuestionService questionService;
	private final NotificationService notificationService;

	public AnswerService(AnswerRepository answerRepository, QuestionService questionService, NotificationService notificationService) {
		this.answerRepository = answerRepository;
		this.questionService = questionService;
		this.notificationService = notificationService;
	}

	@Transactional(readOnly = true)
	public List<AnswerResponse> findByQuestion(Long questionId) {
		questionService.findQuestion(questionId);
		return answerRepository.findByQuestionIdOrderByCreatedAtAsc(questionId)
				.stream()
				.map(AnswerResponse::from)
				.toList();
	}

	@Transactional(readOnly = true)
	public AnswerResponse findById(Long id) {
		return AnswerResponse.from(findAnswer(id));
	}

	@Transactional(readOnly = true)
	public Answer findAnswerById(Long id) {
		return findAnswer(id);
	}

	@Transactional
	public AnswerResponse create(Long questionId, AnswerRequest request, User author) {
		Question question = questionService.findQuestion(questionId);
		Answer answer = new Answer();
		answer.setBody(request.body().trim());
		answer.setQuestion(question);
		answer.setAuthor(author);
		question.setStatus(QuestionStatus.ANSWERED);
		Answer saved = answerRepository.save(answer);
		notificationService.notifyQuestionAnswered(question, saved);
		return AnswerResponse.from(saved);
	}

	@Transactional
	public AnswerResponse update(Long id, AnswerRequest request, User currentUser) {
		Answer answer = findAnswer(id);
		QuestionService.requireOwnerOrAdmin(answer.getAuthor(), currentUser, "Only the answer owner or ADMIN can edit this answer");
		answer.setBody(request.body().trim());
		return AnswerResponse.from(answerRepository.save(answer));
	}

	@Transactional
	public void delete(Long id, User currentUser) {
		Answer answer = findAnswer(id);
		QuestionService.requireOwnerOrAdmin(answer.getAuthor(), currentUser, "Only the answer owner or ADMIN can delete this answer");
		answerRepository.delete(answer);
	}

	private Answer findAnswer(Long id) {
		return answerRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Answer not found"));
	}
}
