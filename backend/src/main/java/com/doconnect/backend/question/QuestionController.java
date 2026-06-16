package com.doconnect.backend.question;

import com.doconnect.backend.auth.AppUserDetails;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/questions")
public class QuestionController {

	private final QuestionService questionService;

	public QuestionController(QuestionService questionService) {
		this.questionService = questionService;
	}

	@GetMapping
	public List<QuestionResponse> findAll() {
		return questionService.findAll();
	}

	@GetMapping("/{id}")
	public QuestionResponse findById(@PathVariable Long id, @RequestParam(defaultValue = "false") boolean trackView) {
		if (trackView) {
			questionService.recordView(id);
		}
		return questionService.findById(id);
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public QuestionResponse create(
			@AuthenticationPrincipal AppUserDetails principal,
			@Valid @RequestBody QuestionRequest request) {
		return questionService.create(request, principal.user());
	}

	@PutMapping("/{id}")
	public QuestionResponse update(
			@PathVariable Long id,
			@AuthenticationPrincipal AppUserDetails principal,
			@Valid @RequestBody QuestionRequest request) {
		return questionService.update(id, request, principal.user());
	}

	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void delete(@PathVariable Long id, @AuthenticationPrincipal AppUserDetails principal) {
		questionService.delete(id, principal.user());
	}

	@PostMapping("/{id}/view")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void recordView(@PathVariable Long id) {
		questionService.recordView(id);
	}

	@PostMapping("/{questionId}/accept/{answerId}")
	public QuestionResponse acceptAnswer(
			@PathVariable Long questionId,
			@PathVariable Long answerId,
			@AuthenticationPrincipal AppUserDetails principal
	) {
		return questionService.acceptAnswer(questionId, answerId, principal.user());
	}
}
