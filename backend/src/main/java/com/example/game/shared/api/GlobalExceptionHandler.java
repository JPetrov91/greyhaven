package com.example.game.shared.api;

import java.time.Clock;
import java.time.Instant;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.PessimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

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
	 * A body Jackson cannot parse — malformed JSON, or a value of the wrong type such as a
	 * non-UUID identifier — is a client error. Without this handler the catch-all below would
	 * answer 500, because {@code ExceptionHandlerExceptionResolver} runs before Spring MVC's
	 * own default resolver. The cause is logged but never returned.
	 */
	@ExceptionHandler(HttpMessageNotReadableException.class)
	public ResponseEntity<ApiError> handleUnreadableRequest(HttpMessageNotReadableException exception) {
		log.debug("Rejected unreadable request body", exception);
		return ResponseEntity.badRequest()
				.body(error("MALFORMED_REQUEST", "The request body could not be read."));
	}

	/**
	 * A path or query value the binder cannot convert — a non-UUID item id, for example — is a
	 * client error. This needs its own handler for the same reason as the one above: the
	 * catch-all would otherwise claim the exception first and answer 500. Only the parameter
	 * name is echoed, never the submitted value.
	 */
	@ExceptionHandler(MethodArgumentTypeMismatchException.class)
	public ResponseEntity<ApiError> handleTypeMismatch(MethodArgumentTypeMismatchException exception) {
		log.debug("Rejected unconvertible request parameter", exception);
		return ResponseEntity.badRequest()
				.body(error("MALFORMED_REQUEST", "'" + exception.getName() + "' is not a valid value."));
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

	/**
	 * A lock wait that Postgres aborts (deadlock, lock timeout) is a conflict the client can
	 * retry, not an internal error. Market buy/cancel take the listing row first to avoid the
	 * common deadlock; this handler is the remaining safety net.
	 */
	@ExceptionHandler(PessimisticLockingFailureException.class)
	public ResponseEntity<ApiError> handleLockFailure(PessimisticLockingFailureException exception) {
		log.warn("Pessimistic lock failure", exception);
		return ResponseEntity.status(HttpStatus.CONFLICT)
				.body(error("CONCURRENT_UPDATE", "That action could not be completed. Try again."));
	}

	/**
	 * Unmapped API paths (including diagnostics when the property is off) must be 404, not 500.
	 * The catch-all would otherwise claim {@link NoResourceFoundException} first.
	 */
	@ExceptionHandler(NoResourceFoundException.class)
	public ResponseEntity<ApiError> handleMissingResource(NoResourceFoundException exception) {
		log.debug("No handler for {}", exception.getResourcePath());
		return ResponseEntity.status(HttpStatus.NOT_FOUND)
				.body(error("NOT_FOUND", "The requested resource was not found."));
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
