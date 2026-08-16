package com.example.game.e2e;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.openqa.selenium.WebDriver;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import com.example.game.TestcontainersConfiguration;
import com.example.game.item.domain.ItemCodes;

/**
 * Browser automation for Task 4 — inventory and equipment screens.
 *
 * <p>Run with: {@code ./mvnw verify -Pselenium}
 */
@Import(TestcontainersConfiguration.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class InventorySeleniumIT {

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
	void starterLoadoutIsShownWithRarityStatsAndQuantity() {
		registerAndEnterGame(uniqueEmail("inv-e2e-start"), uniqueName("InvStart"));
		ui.waitForInventory();

		assertThat(ui.text("inventory-capacity")).isEqualTo("2 / 40 slots");
		assertThat(ui.hasTestId("inventory-item-" + ItemCodes.RUSTY_SWORD)).isFalse();
		assertThat(ui.text("inventory-item-" + ItemCodes.WORN_LEATHER_ARMOR)).contains("Armor 3");
		assertThat(ui.text("inventory-item-" + ItemCodes.HEALING_POTION))
				.contains("Qty 2")
				.contains("Heal 40");

		ui.waitForEquipment();
		assertThat(ui.text("equipped-weapon")).isEqualTo("Empty");
		assertThat(ui.text("equipped-armor")).isEqualTo("Worn Leather Armor");
	}

	@Test
	void unequippingAndReequippingUpdatesDerivedStats() {
		registerAndEnterGame(uniqueEmail("inv-e2e-equip"), uniqueName("InvEquip"));
		ui.waitForInventory();
		ui.clickAction("inventory-item-" + ItemCodes.WORN_LEATHER_ARMOR);
		ui.clickAction("unequip-" + ItemCodes.WORN_LEATHER_ARMOR);

		ui.waitForEquipment();
		ui.waitForText("equipped-armor", "Empty");
		ui.waitForText("derived-armor", "0");

		ui.waitForInventory();
		ui.clickAction("inventory-item-" + ItemCodes.WORN_LEATHER_ARMOR);
		ui.clickAction("equip-" + ItemCodes.WORN_LEATHER_ARMOR);

		ui.waitForEquipment();
		ui.waitForText("equipped-armor", "Worn Leather Armor");
		ui.waitForText("derived-armor", "3");
	}

	@Test
	void equipmentStateSurvivesBrowserRefresh() {
		registerAndEnterGame(uniqueEmail("inv-e2e-refresh"), uniqueName("InvKeep"));
		ui.waitForInventory();
		ui.clickAction("inventory-item-" + ItemCodes.WORN_LEATHER_ARMOR);
		ui.clickAction("unequip-" + ItemCodes.WORN_LEATHER_ARMOR);

		ui.waitForEquipment();
		ui.waitForText("equipped-armor", "Empty");
		ui.waitForText("derived-armor", "0");

		ui.refreshGame();
		ui.waitForInventory();
		ui.clickAction("inventory-item-" + ItemCodes.WORN_LEATHER_ARMOR);
		assertThat(ui.hasTestId("equip-" + ItemCodes.WORN_LEATHER_ARMOR)).isTrue();

		ui.waitForEquipment();
		assertThat(ui.text("equipped-armor")).isEqualTo("Empty");
		assertThat(ui.text("derived-armor")).isEqualTo("0");
	}

	@Test
	void usingHealingPotionsConsumesTheStackOneAtATime() {
		registerAndEnterGame(uniqueEmail("inv-e2e-potion"), uniqueName("InvPotion"));
		ui.waitForInventory();
		assertThat(ui.text("inventory-item-" + ItemCodes.HEALING_POTION)).contains("Qty 2");

		ui.clickAction("inventory-item-" + ItemCodes.HEALING_POTION);
		ui.clickAction("use-" + ItemCodes.HEALING_POTION);
		ui.waitForTextContaining("inventory-item-" + ItemCodes.HEALING_POTION, "Qty 1");
		assertThat(ui.text("inventory-capacity")).isEqualTo("2 / 40 slots");

		ui.clickAction("use-" + ItemCodes.HEALING_POTION);
		ui.waitUntilGone("inventory-item-" + ItemCodes.HEALING_POTION);
		ui.waitForText("inventory-capacity", "1 / 40 slots");
	}

	@Test
	void actionButtonsFollowTheFlagsTheServerSends() {
		registerAndEnterGame(uniqueEmail("inv-e2e-actions"), uniqueName("InvActs"));
		ui.waitForInventory();

		ui.clickAction("inventory-item-" + ItemCodes.WORN_LEATHER_ARMOR);
		assertThat(ui.hasTestId("use-" + ItemCodes.WORN_LEATHER_ARMOR)).isFalse();
		ui.clickAction("inventory-item-" + ItemCodes.HEALING_POTION);
		assertThat(ui.hasTestId("equip-" + ItemCodes.HEALING_POTION)).isFalse();
		assertThat(ui.hasTestId("unequip-" + ItemCodes.HEALING_POTION)).isFalse();
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
