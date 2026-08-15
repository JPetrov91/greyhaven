package com.example.game.e2e;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;
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
import com.example.game.world.domain.LocationCodes;

/**
 * Browser automation for Task 3 — Greyhaven locations, movement, persistence, and nearby characters.
 *
 * <p>Run with: {@code ./mvnw verify -Pselenium}
 */
@Import(TestcontainersConfiguration.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class WorldLocationSeleniumIT {

	private static final String PASSWORD = "password123";
	private static final UUID TAVERN_ID = UUID.fromString("a0000000-0000-4000-8000-000000000002");
	private static final UUID OLD_TOWN_ID = UUID.fromString("a0000000-0000-4000-8000-000000000004");

	@LocalServerPort
	private int backendPort;

	private FrontendDevServer frontend;
	private WebDriver driver;
	private GreyhavenUi ui;

	@BeforeAll
	void startBrowserStack() throws Exception {
		if (backendPort <= 0) {
			throw new IllegalStateException("Backend port was not injected before Selenium setup");
		}
		frontend = FrontendDevServer.start(backendPort);
		driver = WebDrivers.createChrome();
		ui = new GreyhavenUi(driver, frontend.baseUrl());
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
	void newCharacterStartsInCitySquareWithConnectedDestinations() {
		String email = uniqueEmail("world-start");
		String characterName = uniqueName("Start");

		registerAndEnterGame(email, characterName);

		ui.waitForWorld();
		ui.waitForLocation("City Square");
		assertThat(ui.text("location-code")).isEqualTo(LocationCodes.CITY_SQUARE);
		assertThat(ui.text("location-safety")).isEqualTo("Safe Zone");
		assertThat(ui.text("location-description")).contains("Greyhaven");
		assertThat(driver.findElements(By.cssSelector("[data-testid='destination-list']"))).isNotEmpty();

		ui.waitForDestination(LocationCodes.FOREST);
		ui.waitForDestination(LocationCodes.OLD_TOWN);
		ui.waitForDestination(LocationCodes.MARKET);
		ui.waitForDestination(LocationCodes.NORTH_ROAD);
		assertThat(ui.hasDestination(LocationCodes.TAVERN)).isFalse();
	}

	@Test
	void validMovementIsInstantaneousAndUpdatesDestinations() {
		String email = uniqueEmail("world-move");
		String characterName = uniqueName("Move");

		registerAndEnterGame(email, characterName);
		ui.waitForLocation("City Square");

		ui.travelTo(LocationCodes.FOREST, "Forest");
		assertThat(ui.text("location-code")).isEqualTo(LocationCodes.FOREST);
		assertThat(ui.text("location-safety")).isEqualTo("Dangerous");
		ui.waitForDestination(LocationCodes.CITY_SQUARE);
		assertThat(ui.hasDestination(LocationCodes.OLD_TOWN)).isFalse();
		assertThat(ui.hasDestination(LocationCodes.MARKET)).isFalse();
	}

	@Test
	void invalidMovementIsRejectedAndLocationUnchanged() {
		String email = uniqueEmail("world-invalid");
		String characterName = uniqueName("Block");

		registerAndEnterGame(email, characterName);
		ui.waitForLocation("City Square");
		assertThat(ui.hasDestination(LocationCodes.TAVERN)).isFalse();

		Map<String, Object> response = ui.postMoveViaBrowserSession(TAVERN_ID.toString());

		assertThat(response.get("status")).isEqualTo(400L);
		assertThat(response.get("code")).isEqualTo("INVALID_MOVEMENT");
		assertThat(String.valueOf(response.get("message")))
				.contains("cannot travel directly");

		ui.refreshGame();
		ui.waitForWorld();
		ui.waitForLocation("City Square");
		assertThat(ui.text("location-code")).isEqualTo(LocationCodes.CITY_SQUARE);
	}

	@Test
	void browserRefreshPreservesLocation() {
		String email = uniqueEmail("world-persist");
		String characterName = uniqueName("Stay");

		registerAndEnterGame(email, characterName);
		ui.waitForLocation("City Square");
		ui.travelTo(LocationCodes.MARKET, "Market");
		assertThat(ui.text("location-code")).isEqualTo(LocationCodes.MARKET);

		ui.refreshGame();

		ui.waitForLocation("Market");
		assertThat(ui.text("location-code")).isEqualTo(LocationCodes.MARKET);
		ui.waitForDestination(LocationCodes.CITY_SQUARE);
		ui.waitForDestination(LocationCodes.TAVERN);
	}

	@Test
	void nearbyCharactersListsOthersAtSameLocationOnly() throws Exception {
		String companionName = uniqueName("NearC");
		BackendApiClient companion = new BackendApiClient(backendPort);
		companion.registerWithCharacter(uniqueEmail("nearby-c"), PASSWORD, companionName);
		companion.moveTo(OLD_TOWN_ID);

		String email = uniqueEmail("nearby-a");
		String characterName = uniqueName("NearA");
		registerAndEnterGame(email, characterName);
		ui.waitForLocation("City Square");

		ui.travelTo(LocationCodes.OLD_TOWN, "Old Town");
		ui.waitForNearbyCharacter(companionName);
		assertThat(ui.text("nearby-" + companionName)).contains(companionName).contains("Level 1");

		ui.travelTo(LocationCodes.CITY_SQUARE, "City Square");
		ui.travelTo(LocationCodes.FOREST, "Forest");
		ui.waitUntilNearbyDoesNotContain(companionName);
		assertThat(driver.findElements(By.cssSelector("[data-testid='nearby-" + companionName + "']"))).isEmpty();
	}

	private void registerAndEnterGame(String email, String characterName) {
		ui.register(email, PASSWORD);
		ui.waitForCreateCharacterPage();
		ui.createCharacter(characterName);
		ui.waitForGameWithCharacter(characterName);
	}

	private static String uniqueEmail(String label) {
		return label + "-" + UUID.randomUUID() + "@greyhaven.test";
	}

	private static String uniqueName(String prefix) {
		String suffix = UUID.randomUUID().toString().replace("-", "");
		return prefix + suffix.substring(0, 8);
	}
}
