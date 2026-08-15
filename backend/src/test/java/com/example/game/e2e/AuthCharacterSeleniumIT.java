package com.example.game.e2e;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.UUID;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import com.example.game.TestcontainersConfiguration;

/**
 * Browser automation for Task 2 — account registration, authentication, and character creation.
 *
 * <p>Run with: {@code ./mvnw verify -Pselenium}
 */
@Import(TestcontainersConfiguration.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class AuthCharacterSeleniumIT {

	private static final String PASSWORD = "password123";

	@LocalServerPort
	private int backendPort;

	private FrontendDevServer frontend;
	private WebDriver driver;
	private GreyhavenUi ui;
	private HttpClient httpClient;

	@BeforeAll
	void startBrowserStack() throws Exception {
		if (backendPort <= 0) {
			throw new IllegalStateException("Backend port was not injected before Selenium setup");
		}
		frontend = FrontendDevServer.start(backendPort);
		driver = WebDrivers.createChrome();
		ui = new GreyhavenUi(driver, frontend.baseUrl());
		httpClient = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();
	}

	@AfterAll
	void stopBrowserStack() {
		if (driver != null) {
			driver.quit();
		}
		if (frontend != null) {
			frontend.close();
		}
	}

	@BeforeEach
	void clearBrowserState() {
		ui.clearClientState();
	}

	@Test
	void registrationCreatesAccountAndCharacterWithStartingGold() {
		String email = uniqueEmail("register");
		String characterName = uniqueName("Reg");

		ui.register(email, PASSWORD);
		ui.waitForCreateCharacterPage();
		ui.createCharacter(characterName);
		ui.waitForGameWithCharacter(characterName);

		assertThat(ui.text("character-summary-name")).isEqualTo(characterName);
		assertThat(ui.text("character-summary-level")).isEqualTo("Level 1");
		assertThat(ui.text("topbar-gold")).contains("100");
	}

	@Test
	void duplicateEmailShowsConflictError() {
		String email = uniqueEmail("dup-email");
		String characterName = uniqueName("DupE");

		ui.register(email, PASSWORD);
		ui.waitForCreateCharacterPage();
		ui.createCharacter(characterName);
		ui.waitForGameWithCharacter(characterName);
		ui.logout();

		ui.register(email, PASSWORD);
		assertThat(ui.alertText("register-error"))
				.isEqualTo("An account with this email already exists.");
		assertThat(ui.currentPath()).isEqualTo("/register");
	}

	@Test
	void authenticationSupportsLoginLogoutAndRejectsBadPassword() {
		String email = uniqueEmail("auth");
		String characterName = uniqueName("Auth");

		ui.register(email, PASSWORD);
		ui.waitForCreateCharacterPage();
		ui.createCharacter(characterName);
		ui.waitForGameWithCharacter(characterName);
		ui.logout();

		ui.login(email, "wrong-password");
		assertThat(ui.alertText("login-error")).isEqualTo("Invalid email or password.");
		assertThat(ui.currentPath()).isEqualTo("/login");

		ui.login(email, PASSWORD);
		ui.waitForGameWithCharacter(characterName);
		assertThat(ui.text("character-summary-name")).isEqualTo(characterName);

		ui.logout();
		assertThat(ui.currentPath()).isEqualTo("/login");
	}

	@Test
	void unauthenticatedUsersAreRedirectedAndApiRejectsMe() throws Exception {
		ui.open("/game");
		ui.waitUntilPath("/login");
		assertThat(driver.findElements(By.cssSelector("[data-testid='login-page']"))).isNotEmpty();

		HttpRequest request = HttpRequest.newBuilder()
				.uri(URI.create("http://127.0.0.1:" + backendPort + "/api/v1/me"))
				.timeout(Duration.ofSeconds(5))
				.GET()
				.build();
		HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
		assertThat(response.statusCode()).isEqualTo(401);
		assertThat(response.body()).contains("UNAUTHENTICATED");
	}

	@Test
	void duplicateCharacterNameShowsConflictError() {
		String ownerEmail = uniqueEmail("name-owner");
		String challengerEmail = uniqueEmail("name-challenger");
		String characterName = uniqueName("DupN");

		ui.register(ownerEmail, PASSWORD);
		ui.waitForCreateCharacterPage();
		ui.createCharacter(characterName);
		ui.waitForGameWithCharacter(characterName);
		ui.logout();

		ui.register(challengerEmail, PASSWORD);
		ui.waitForCreateCharacterPage();
		ui.createCharacter(characterName);

		assertThat(ui.alertText("create-character-error"))
				.isEqualTo("A character with this name already exists.");
		assertThat(ui.currentPath()).isEqualTo("/create-character");
	}

	@Test
	void secondCharacterRouteIsRejectedWhenAccountAlreadyHasCharacter() {
		String email = uniqueEmail("second-char");
		String characterName = uniqueName("One");

		ui.register(email, PASSWORD);
		ui.waitForCreateCharacterPage();
		ui.createCharacter(characterName);
		ui.waitForGameWithCharacter(characterName);

		ui.open("/create-character");
		ui.waitForGameWithCharacter(characterName);
		assertThat(ui.currentPath()).isEqualTo("/game");
		assertThat(driver.findElements(By.cssSelector("[data-testid='create-character-page']"))).isEmpty();
	}

	private static String uniqueEmail(String label) {
		return label + "-" + UUID.randomUUID() + "@greyhaven.test";
	}

	private static String uniqueName(String prefix) {
		String suffix = UUID.randomUUID().toString().replace("-", "");
		return prefix + suffix.substring(0, 8);
	}
}
