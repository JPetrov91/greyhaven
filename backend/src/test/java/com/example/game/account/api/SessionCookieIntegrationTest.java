package com.example.game.account.api;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import com.example.game.TestcontainersConfiguration;

/**
 * Runs against a real servlet container so the actual {@code Set-Cookie} headers can be
 * inspected. MockMvc cannot verify cookie attributes because it never serializes the session
 * cookie.
 */
@Import(TestcontainersConfiguration.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class SessionCookieIntegrationTest {

	@Value("${local.server.port}")
	private int port;

	private final HttpClient httpClient = HttpClient.newHttpClient();

	@Test
	void sessionCookieIsHttpOnlyAndCsrfCookieIsReadableByTheSpa() throws Exception {
		HttpResponse<String> bootstrap = httpClient.send(
				HttpRequest.newBuilder(uri("/api/v1/bootstrap")).GET().build(),
				BodyHandlers.ofString());
		assertThat(bootstrap.statusCode()).isEqualTo(200);

		String csrfCookie = setCookie(bootstrap, "XSRF-TOKEN").orElseThrow();
		assertThat(isHttpOnly(csrfCookie))
				.describedAs("the SPA must be able to read the CSRF cookie from JavaScript")
				.isFalse();
		String csrfToken = cookieValue(csrfCookie);
		assertThat(csrfToken).isNotBlank();

		HttpResponse<String> register = httpClient.send(
				HttpRequest.newBuilder(uri("/api/v1/auth/register"))
						.header("Content-Type", "application/json")
						.header("X-XSRF-TOKEN", csrfToken)
						.header("Cookie", "XSRF-TOKEN=" + csrfToken)
						.POST(HttpRequest.BodyPublishers.ofString("""
								{"email":"cookie-%d@greyhaven.test","password":"password123"}
								""".formatted(System.nanoTime())))
						.build(),
				BodyHandlers.ofString());
		assertThat(register.statusCode()).isEqualTo(201);

		String sessionCookie = setCookie(register, "JSESSIONID").orElseThrow();
		assertThat(isHttpOnly(sessionCookie))
				.describedAs("the session cookie must never be readable from JavaScript")
				.isTrue();
		assertThat(sameSite(sessionCookie))
				.describedAs("SameSite=Lax is required for the SPA session cookie")
				.isEqualTo("lax");
		assertThat(isSecure(sessionCookie))
				.describedAs("Secure must be off under the test profile (plain HTTP)")
				.isFalse();

		HttpResponse<String> me = httpClient.send(
				HttpRequest.newBuilder(uri("/api/v1/me"))
						.header("Cookie", "JSESSIONID=" + cookieValue(sessionCookie))
						.GET().build(),
				BodyHandlers.ofString());
		assertThat(me.statusCode()).isEqualTo(200);
		assertThat(me.body()).contains("\"hasCharacter\":false");
	}

	@Test
	void unauthenticatedApiAccessIsRejectedByTheRunningApplication() throws Exception {
		HttpResponse<String> response = httpClient.send(
				HttpRequest.newBuilder(uri("/api/v1/me")).GET().build(),
				BodyHandlers.ofString());

		assertThat(response.statusCode()).isEqualTo(401);
		assertThat(response.body()).contains("\"code\":\"UNAUTHENTICATED\"");
	}

	private URI uri(String path) {
		return URI.create("http://localhost:" + port + path);
	}

	/**
	 * Returns the last {@code Set-Cookie} header for the given name, which is the value a browser
	 * would keep when a cookie is replaced within one response.
	 */
	private static Optional<String> setCookie(HttpResponse<String> response, String name) {
		List<String> headers = response.headers().allValues("set-cookie");
		return headers.stream()
				.filter(header -> header.startsWith(name + "="))
				.filter(header -> !cookieValue(header).isBlank())
				.reduce((first, second) -> second);
	}

	private static String cookieValue(String setCookieHeader) {
		String withoutName = setCookieHeader.substring(setCookieHeader.indexOf('=') + 1);
		int end = withoutName.indexOf(';');
		return end < 0 ? withoutName : withoutName.substring(0, end);
	}

	private static boolean isHttpOnly(String setCookieHeader) {
		return setCookieHeader.toLowerCase(Locale.ROOT).contains("httponly");
	}

	private static boolean isSecure(String setCookieHeader) {
		return setCookieHeader.toLowerCase(Locale.ROOT).matches(".*(^|;\\s*)secure(;|$).*");
	}

	private static String sameSite(String setCookieHeader) {
		String lower = setCookieHeader.toLowerCase(Locale.ROOT);
		int index = lower.indexOf("samesite=");
		if (index < 0) {
			return "";
		}
		String value = setCookieHeader.substring(index + "samesite=".length());
		int end = value.indexOf(';');
		return (end < 0 ? value : value.substring(0, end)).trim().toLowerCase(Locale.ROOT);
	}
}
