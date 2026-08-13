package com.example.game.shared.api;

import java.time.Clock;
import java.time.Instant;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

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

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ApiError> handleValidation(MethodArgumentNotValidException exception) {
		FieldError fieldError = exception.getBindingResult().getFieldError();
		String message = fieldError != null
				? fieldError.getField() + ": " + fieldError.getDefaultMessage()
				: "Validation failed";
		return ResponseEntity.badRequest().body(error("VALIDATION_ERROR", message));
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
