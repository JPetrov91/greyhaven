package com.example.game.shared.api;

import java.time.Clock;
import java.time.Instant;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.example.game.shared.infrastructure.ConstraintViolations;

@RestControllerAdvice
public class GlobalExceptionHandler {

	private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

	private final Clock clock;

	public GlobalExceptionHandler(Clock clock) {
		this.clock = clock;
	}

	@ExceptionHandler(ApiException.class)
	public ResponseEntity<ApiError> handleApiException(ApiException exception) {
		return ResponseEntity.status(exception.getStatus()).body(error(exception.getCode(), exception.getMessage()));
	}

	@ExceptionHandler({BadCredentialsException.class, AuthenticationException.class})
	public ResponseEntity<ApiError> handleAuthenticationFailure(AuthenticationException exception) {
		return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
				.body(error("INVALID_CREDENTIALS", "Invalid email or password."));
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ApiError> handleValidation(MethodArgumentNotValidException exception) {
		FieldError fieldError = exception.getBindingResult().getFieldError();
		String message = fieldError != null
				? fieldError.getField() + ": " + fieldError.getDefaultMessage()
				: "Validation failed";
		return ResponseEntity.badRequest().body(error("VALIDATION_ERROR", message));
	}

	/**
	 * Safety net for constraint violations a service did not translate into a domain error.
	 * A rejected write is a conflict, never an internal error.
	 */
	@ExceptionHandler(DataIntegrityViolationException.class)
	public ResponseEntity<ApiError> handleDataIntegrityViolation(DataIntegrityViolationException exception) {
		log.warn("Untranslated database constraint violation: {}",
				ConstraintViolations.violatedConstraintName(exception), exception);
		return ResponseEntity.status(HttpStatus.CONFLICT)
				.body(error("CONSTRAINT_VIOLATION", "The request conflicts with existing data."));
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ApiError> handleUnexpected(Exception exception) {
		log.error("Unhandled exception", exception);
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
				.body(error("INTERNAL_ERROR", "An unexpected error occurred."));
	}

	private ApiError error(String code, String message) {
		return new ApiError(code, message, Instant.now(clock));
	}
}
