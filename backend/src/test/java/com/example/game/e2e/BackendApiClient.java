package com.example.game.e2e;

import java.io.IOException;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.HttpCookie;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.UUID;

/**
 * Cookie-aware HTTP helper for seeding companion accounts during Selenium tests.
 */
final class BackendApiClient {

	private final String baseUrl;
	private final CookieManager cookieManager;
	private final HttpClient httpClient;

	BackendApiClient(int backendPort) {
		this.baseUrl = "http://127.0.0.1:" + backendPort;
		this.cookieManager = new CookieManager();
		this.cookieManager.setCookiePolicy(CookiePolicy.ACCEPT_ALL);
		this.httpClient = HttpClient.newBuilder()
				.cookieHandler(cookieManager)
				.connectTimeout(Duration.ofSeconds(5))
				.build();
	}

	void registerWithCharacter(String email, String password, String characterName)
			throws IOException, InterruptedException {
		bootstrap();
		postJson("/api/v1/auth/register", """
				{"email":"%s","password":"%s"}
				""".formatted(email, password), 201);
		postJson("/api/v1/characters", """
				{"name":"%s"}
				""".formatted(characterName), 201);
	}

	void moveTo(UUID destinationLocationId) throws IOException, InterruptedException {
		postJson("/api/v1/world/move", """
				{"destinationLocationId":"%s"}
				""".formatted(destinationLocationId), 200);
	}

	private void bootstrap() throws IOException, InterruptedException {
		HttpRequest request = HttpRequest.newBuilder()
				.uri(URI.create(baseUrl + "/api/v1/bootstrap"))
				.timeout(Duration.ofSeconds(5))
				.GET()
				.build();
		HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
		if (response.statusCode() != 200) {
			throw new IllegalStateException(
					"Bootstrap failed with status " + response.statusCode() + ": " + response.body());
		}
	}

	private void postJson(String path, String body, int expectedStatus)
			throws IOException, InterruptedException {
		bootstrap();
		String csrf = requireCsrfToken();
		HttpRequest request = HttpRequest.newBuilder()
				.uri(URI.create(baseUrl + path))
				.timeout(Duration.ofSeconds(5))
				.header("Content-Type", "application/json")
				.header("X-XSRF-TOKEN", csrf)
				.POST(HttpRequest.BodyPublishers.ofString(body))
				.build();
		HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
		if (response.statusCode() != expectedStatus) {
			throw new IllegalStateException(
					"POST " + path + " expected " + expectedStatus
							+ " but was " + response.statusCode() + ": " + response.body());
		}
	}

	private String requireCsrfToken() {
		return cookieManager.getCookieStore().getCookies().stream()
				.filter(cookie -> "XSRF-TOKEN".equals(cookie.getName()))
				.map(HttpCookie::getValue)
				.filter(value -> value != null && !value.isBlank())
				.reduce((left, right) -> right)
				.orElseThrow(() -> new IllegalStateException("XSRF-TOKEN cookie was not set"));
	}
}
