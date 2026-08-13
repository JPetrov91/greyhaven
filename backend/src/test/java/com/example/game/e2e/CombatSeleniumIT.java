package com.example.game.e2e;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import java.util.UUID;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import com.example.game.TestcontainersConfiguration;

/**
 * Browser automation for Task 5 — encounter search and combat UI.
 *
 * <p>Run with: {@code ./mvnw verify -Pselenium}
 */
@Import(TestcontainersConfiguration.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class CombatSeleniumIT {

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
		driver.manage().deleteAllCookies();
		driver.get(frontend.baseUrl() + "/login");
	}

	@Test
	void searchFightAndResolveCombatInBrowser() {
		String email = "combat-e2e-" + UUID.randomUUID() + "@greyhaven.test";
		String name = "Cmbt" + UUID.randomUUID().toString().replace("-", "").substring(0, 8);
		ui.register(email, PASSWORD);
		ui.createCharacter(name);
		ui.waitForGameWithCharacter(name);

		ui.travelTo("OLD_TOWN", "Old Town");

		WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(8));
		boolean startedCombat = false;
		for (int attempt = 0; attempt < 12 && !startedCombat; attempt++) {
			ui.clickAction("search-encounter-button");
			wait.until(ExpectedConditions.or(
					ExpectedConditions.visibilityOfElementLocated(By.cssSelector("[data-testid='encounter-prompt']")),
					ExpectedConditions.visibilityOfElementLocated(By.cssSelector("[data-testid='encounter-nothing']"))));

			if (ui.hasTestId("encounter-nothing")) {
				ui.clickAction("encounter-dismiss");
				continue;
			}

			ui.clickAction("encounter-fight");
			wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("[data-testid='combat-panel']")));
			wait.until(ExpectedConditions.or(
					ExpectedConditions.elementToBeClickable(By.cssSelector("[data-testid='combat-action-QUICK_ATTACK']")),
					ExpectedConditions.visibilityOfElementLocated(By.cssSelector("[data-testid='combat-rewards']")),
					ExpectedConditions.visibilityOfElementLocated(By.cssSelector("[data-testid='combat-ended']"))));
			startedCombat = true;
		}

		assertThat(startedCombat).as("expected an encounter within search attempts").isTrue();

		boolean finished = ui.hasTestId("combat-rewards") || ui.hasTestId("combat-ended");
		for (int round = 0; round < 50 && !finished; round++) {
			try {
				new WebDriverWait(driver, Duration.ofSeconds(3))
						.until(ExpectedConditions.elementToBeClickable(
								By.cssSelector("[data-testid='combat-action-QUICK_ATTACK']")))
						.click();
			}
			catch (TimeoutException ignored) {
				// Combat may already be terminal between rounds.
			}
			finished = ui.hasTestId("combat-rewards") || ui.hasTestId("combat-ended");
		}

		assertThat(finished).as("combat should end within action budget").isTrue();
		ui.clickAction("combat-dismiss");
		ui.waitForInventory();
		assertThat(ui.hasTestId("location-panel")).isTrue();
	}
}
