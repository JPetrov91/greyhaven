package com.example.game.shared.api;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.dao.CannotAcquireLockException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.mock.http.MockHttpInputMessage;
import org.springframework.http.HttpMethod;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

class GlobalExceptionHandlerTest {

	private static final Instant FIXED_INSTANT = Instant.parse("2026-08-13T01:00:00Z");

	private GlobalExceptionHandler handler;

	@BeforeEach
	void setUp() {
		handler = new GlobalExceptionHandler(Clock.fixed(FIXED_INSTANT, ZoneOffset.UTC));
	}

	@Test
	void mapsApiExceptionToStructuredError() {
		ApiException exception = new ApiException("TEST_CODE", "Test message", HttpStatus.CONFLICT);

		ResponseEntity<ApiError> response = handler.handleApiException(exception);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
		assertThat(response.getBody()).isNotNull();
		assertThat(response.getBody().code()).isEqualTo("TEST_CODE");
		assertThat(response.getBody().message()).isEqualTo("Test message");
		assertThat(response.getBody().timestamp()).isEqualTo(FIXED_INSTANT);
	}

	@Test
	void mapsUnreadableBodyToBadRequestWithoutLeakingParserDetails() {
		HttpMessageNotReadableException exception = new HttpMessageNotReadableException(
				"JSON parse error: Cannot deserialize value of type UUID from String \"secret-payload\"",
				new MockHttpInputMessage(new byte[0]));

		ResponseEntity<ApiError> response = handler.handleUnreadableRequest(exception);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
		assertThat(response.getBody()).isNotNull();
		assertThat(response.getBody().code()).isEqualTo("MALFORMED_REQUEST");
		assertThat(response.getBody().message()).isEqualTo("The request body could not be read.");
		assertThat(response.getBody().message()).doesNotContain("secret-payload");
	}

	@Test
	void mapsUnconvertiblePathVariableToBadRequestWithoutEchoingTheValue() throws NoSuchMethodException {
		MethodParameter parameter = new MethodParameter(
				Endpoint.class.getDeclaredMethod("equip", UUID.class), 0);
		MethodArgumentTypeMismatchException exception = new MethodArgumentTypeMismatchException(
				"secret-payload", UUID.class, "itemId", parameter, new IllegalArgumentException("bad uuid"));

		ResponseEntity<ApiError> response = handler.handleTypeMismatch(exception);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
		assertThat(response.getBody()).isNotNull();
		assertThat(response.getBody().code()).isEqualTo("MALFORMED_REQUEST");
		assertThat(response.getBody().message()).isEqualTo("'itemId' is not a valid value.");
		assertThat(response.getBody().message()).doesNotContain("secret-payload");
	}

	@Test
	void mapsPessimisticLockFailureToConflictWithoutLeakingDetails() {
		ResponseEntity<ApiError> response = handler.handleLockFailure(
				new CannotAcquireLockException("could not obtain lock on row"));

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
		assertThat(response.getBody()).isNotNull();
		assertThat(response.getBody().code()).isEqualTo("CONCURRENT_UPDATE");
		assertThat(response.getBody().message()).isEqualTo("That action could not be completed. Try again.");
		assertThat(response.getBody().message()).doesNotContain("could not obtain lock");
	}

	@Test
	void mapsMissingResourceToNotFound() {
		ResponseEntity<ApiError> response = handler.handleMissingResource(
				new NoResourceFoundException(HttpMethod.GET, "/api/v1", "/dev/diagnostics"));

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
		assertThat(response.getBody()).isNotNull();
		assertThat(response.getBody().code()).isEqualTo("NOT_FOUND");
		assertThat(response.getBody().message()).isEqualTo("The requested resource was not found.");
	}

	@Test
	void mapsUnexpectedExceptionWithoutLeakingDetails() {
		ResponseEntity<ApiError> response = handler.handleUnexpected(new RuntimeException("secret details"));

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
		assertThat(response.getBody()).isNotNull();
		assertThat(response.getBody().code()).isEqualTo("INTERNAL_ERROR");
		assertThat(response.getBody().message()).isEqualTo("An unexpected error occurred.");
		assertThat(response.getBody().message()).doesNotContain("secret");
	}

	/** Supplies a realistic {@link MethodParameter} for the type-mismatch case. */
	private static final class Endpoint {

		void equip(UUID itemId) {
		}
	}
}
