package com.doconnect.backend.answer;

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
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AnswerController {

	private final AnswerService answerService;

	public AnswerController(AnswerService answerService) {
		this.answerService = answerService;
	}

	@GetMapping("/api/questions/{questionId}/answers")
	public List<AnswerResponse> findByQuestion(@PathVariable Long questionId) {
		return answerService.findByQuestion(questionId);
	}

	@PostMapping("/api/questions/{questionId}/answers")
	@ResponseStatus(HttpStatus.CREATED)
	public AnswerResponse create(
			@PathVariable Long questionId,
			@AuthenticationPrincipal AppUserDetails principal,
			@Valid @RequestBody AnswerRequest request
	) {
		return answerService.create(questionId, request, principal.user());
	}

	@GetMapping("/api/answers/{id}")
	public AnswerResponse findById(@PathVariable Long id) {
		return answerService.findById(id);
	}

	@PutMapping("/api/answers/{id}")
	public AnswerResponse update(
			@PathVariable Long id,
			@AuthenticationPrincipal AppUserDetails principal,
			@Valid @RequestBody AnswerRequest request
	) {
		return answerService.update(id, request, principal.user());
	}

	@DeleteMapping("/api/answers/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void delete(@PathVariable Long id, @AuthenticationPrincipal AppUserDetails principal) {
		answerService.delete(id, principal.user());
	}
}
