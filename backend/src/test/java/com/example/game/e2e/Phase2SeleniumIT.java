package com.example.game.e2e;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import com.example.game.TestcontainersConfiguration;

/**
 * Browser smoke for Phase 2 Task 10 — screens, Office Mode, and persistence after reload.
 *
 * <p>Run with: {@code ./mvnw verify -Pselenium}
 */
@Import(TestcontainersConfiguration.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class Phase2SeleniumIT {

	private static final String PASSWORD = "password123";

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
	void phase2ScreensOfficeModeAndReloadPersist() {
		String email = uniqueEmail("p2-e2e");
		String characterName = uniqueName("P2");
		ui.register(email, PASSWORD);
		ui.waitForCreateCharacterPage();
		ui.createCharacter(characterName);
		ui.waitForGameWithCharacter(characterName);

		ui.waitForInventory();
		assertThat(ui.hasTestId("inventory-panel")).isTrue();
		ui.waitForEquipment();
		assertThat(ui.hasTestId("equipment-panel")).isTrue();
		ui.waitForCrafting();
		assertThat(ui.hasTestId("crafting-panel")).isTrue();
		ui.waitForMarket();
		assertThat(ui.hasTestId("market-panel")).isTrue();
		ui.waitForArena();
		assertThat(ui.hasTestId("arena-defense-form")).isTrue();

		ui.enableOfficeMode();
		String mode = String.valueOf(((JavascriptExecutor) driver)
				.executeScript("return document.documentElement.dataset.uiMode"));
		assertThat(mode).isEqualTo("compact");

		ui.waitForHome();
		ui.refreshGame();
		ui.waitForGameWithCharacter(characterName);
		ui.waitForHome();
		String modeAfterReload = String.valueOf(((JavascriptExecutor) driver)
				.executeScript("return document.documentElement.dataset.uiMode"));
		assertThat(modeAfterReload).isEqualTo("compact");
	}

	private static String uniqueEmail(String prefix) {
		return prefix + "-" + UUID.randomUUID() + "@greyhaven.test";
	}

	private static String uniqueName(String prefix) {
		return prefix + UUID.randomUUID().toString().replace("-", "").substring(0, 12);
	}
}
