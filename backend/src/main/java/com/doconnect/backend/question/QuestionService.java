package com.doconnect.backend.question;

import com.doconnect.backend.answer.Answer;
import com.doconnect.backend.answer.AnswerRepository;
import com.doconnect.backend.common.ResourceNotFoundException;
import com.doconnect.backend.user.User;
import com.doconnect.backend.user.UserRole;
import java.util.List;
import org.springframework.security.access.AccessDeniedException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
public class QuestionService {

	private final QuestionRepository questionRepository;
	private final AnswerRepository answerRepository;
	private final TagService tagService;

	public QuestionService(QuestionRepository questionRepository, AnswerRepository answerRepository, TagService tagService) {
		this.questionRepository = questionRepository;
		this.answerRepository = answerRepository;
		this.tagService = tagService;
	}

	@Transactional(readOnly = true)
	public List<QuestionResponse> findAll() {
		return questionRepository.findAllByOrderByCreatedAtDesc()
				.stream()
				.map(QuestionResponse::from)
				.toList();
	}

	@Transactional(readOnly = true)
	public QuestionResponse findById(Long id) {
		return QuestionResponse.from(findQuestion(id));
	}

	@Transactional
	public QuestionResponse create(QuestionRequest request, User author) {
		Question question = new Question();
		question.setTitle(request.title().trim());
		question.setBody(request.body().trim());
		question.setAuthor(author);
		question.setTags(tagService.resolveTags(request.tags()));
		Question savedQuestion = questionRepository.save(question);
		log.info("Question created. questionId={}, userId={}", savedQuestion.getId(), author.getId());
		return QuestionResponse.from(savedQuestion);
	}

	@Transactional
	public QuestionResponse update(Long id, QuestionRequest request, User currentUser) {
		Question question = findQuestion(id);
		try {
			requireOwnerOrAdmin(question.getAuthor(), currentUser, "Only the question owner or ADMIN can edit this question");
		} catch (AccessDeniedException e) {
			log.warn("Unauthorized question modification attempt. questionId={}, userId={}", id, currentUser.getId());
			throw e;
		}
		question.setTitle(request.title().trim());
		question.setBody(request.body().trim());
		question.setTags(tagService.resolveTags(request.tags()));
		Question savedQuestion = questionRepository.save(question);
		log.info("Question updated. questionId={}, userId={}", savedQuestion.getId(), currentUser.getId());
		return QuestionResponse.from(savedQuestion);
	}

	@Transactional
	public void delete(Long id, User currentUser) {
		Question question = findQuestion(id);
		try {
			requireOwnerOrAdmin(question.getAuthor(), currentUser, "Only the question owner or ADMIN can delete this question");
		} catch (AccessDeniedException e) {
			log.warn("Unauthorized question modification attempt. questionId={}, userId={}", id, currentUser.getId());
			throw e;
		}
		questionRepository.delete(question);
		log.info("Question deleted. questionId={}, userId={}", id, currentUser.getId());
	}

	@Transactional
	public void recordView(Long id) {
		// Verify existence first
		findQuestion(id);
		questionRepository.incrementViewCount(id);
	}

	@Transactional
	public QuestionResponse acceptAnswer(Long questionId, Long answerId, User currentUser) {
		Question question = findQuestion(questionId);
		Answer answer = answerRepository.findById(answerId)
				.orElseThrow(() -> new ResourceNotFoundException("Answer not found"));
		if (!answer.getQuestion().getId().equals(question.getId())) {
			throw new ResourceNotFoundException("Answer not found for question");
		}

		try {
			requireOwnerOrAdmin(question.getAuthor(), currentUser, "Only the question owner or ADMIN can accept an answer");
		} catch (AccessDeniedException e) {
			log.warn("Unauthorized question modification attempt. questionId={}, userId={}", questionId, currentUser.getId());
			throw e;
		}

		List<Answer> questionAnswers = answerRepository.findByQuestionId(questionId);
		for (Answer candidate : questionAnswers) {
			candidate.setAccepted(candidate.getId().equals(answerId));
		}

		question.setAcceptedAnswerId(answerId);
		question.setStatus(QuestionStatus.SOLVED);

		answerRepository.saveAll(questionAnswers);
		Question savedQuestion = questionRepository.save(question);
		
		log.info("Answer accepted. questionId={}, answerId={}, userId={}", savedQuestion.getId(), answerId, currentUser.getId());
		log.info("Question status changed. questionId={}, status={}", savedQuestion.getId(), QuestionStatus.SOLVED);
		
		return QuestionResponse.from(savedQuestion);
	}

	public Question findQuestion(Long id) {
		return questionRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Question not found"));
	}

	public static void requireOwnerOrAdmin(User owner, User currentUser, String message) {
		boolean isOwner = owner.getId().equals(currentUser.getId());
		boolean isAdmin = currentUser.getRole() == UserRole.ADMIN;
		if (!isOwner && !isAdmin) {
			throw new AccessDeniedException(message);
		}
	}
}
