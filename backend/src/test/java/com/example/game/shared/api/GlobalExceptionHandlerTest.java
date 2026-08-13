package com.example.game.shared.api;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.mock.http.MockHttpInputMessage;

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
	void mapsUnexpectedExceptionWithoutLeakingDetails() {
		ResponseEntity<ApiError> response = handler.handleUnexpected(new RuntimeException("secret details"));

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
		assertThat(response.getBody()).isNotNull();
		assertThat(response.getBody().code()).isEqualTo("INTERNAL_ERROR");
		assertThat(response.getBody().message()).isEqualTo("An unexpected error occurred.");
		assertThat(response.getBody().message()).doesNotContain("secret");
	}
}
