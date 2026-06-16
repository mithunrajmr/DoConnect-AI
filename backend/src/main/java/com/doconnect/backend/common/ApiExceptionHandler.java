package com.doconnect.backend.common;

import com.doconnect.backend.auth.DuplicateEmailException;
import com.doconnect.backend.ai.GeminiException;
import java.time.Instant;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ApiExceptionHandler {

	@ExceptionHandler(DuplicateEmailException.class)
	public ResponseEntity<ApiError> handleDuplicateEmail(DuplicateEmailException ex) {
		return error(HttpStatus.CONFLICT, ex.getMessage(), List.of());
	}

	@ExceptionHandler(BadCredentialsException.class)
	public ResponseEntity<ApiError> handleBadCredentials(BadCredentialsException ex) {
		return error(HttpStatus.UNAUTHORIZED, ex.getMessage(), List.of());
	}

	@ExceptionHandler(ResourceNotFoundException.class)
	public ResponseEntity<ApiError> handleNotFound(ResourceNotFoundException ex) {
		return error(HttpStatus.NOT_FOUND, ex.getMessage(), List.of());
	}

	@ExceptionHandler(AccessDeniedException.class)
	public ResponseEntity<ApiError> handleAccessDenied(AccessDeniedException ex) {
		return error(HttpStatus.FORBIDDEN, ex.getMessage(), List.of());
	}

	@ExceptionHandler(GeminiException.class)
	public ResponseEntity<ApiError> handleGeminiException(GeminiException ex) {
		HttpStatus status = ex.getStatusCode() != null && ex.getStatusCode() == 401
				? HttpStatus.UNAUTHORIZED
				: HttpStatus.BAD_GATEWAY;
		return error(status, ex.getMessage(), List.of());
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ApiError> handleValidation(MethodArgumentNotValidException ex) {
		List<String> details = ex.getBindingResult()
				.getFieldErrors()
				.stream()
				.map(this::formatFieldError)
				.toList();
		return error(HttpStatus.BAD_REQUEST, "Validation failed", details);
	}

	private ResponseEntity<ApiError> error(HttpStatus status, String message, List<String> details) {
		return ResponseEntity.status(status)
				.body(new ApiError(Instant.now(), status.value(), status.getReasonPhrase(), message, details));
	}

	private String formatFieldError(FieldError error) {
		return error.getField() + ": " + error.getDefaultMessage();
	}
}
