package com.example.game.market.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import com.example.game.TestcontainersConfiguration;
import com.example.game.inventory.application.InventoryApplicationService;
import com.example.game.item.domain.ItemCodes;
import com.example.game.market.domain.MerchantCodes;
import com.example.game.market.domain.MerchantPriceCalculator;
import com.jayway.jsonpath.JsonPath;

import jakarta.servlet.http.Cookie;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class MerchantIntegrationTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Autowired
	private InventoryApplicationService inventoryApplicationService;

	private Cookie csrfCookie;

	@BeforeEach
	void loadCsrfCookie() throws Exception {
		MvcResult bootstrap = mockMvc.perform(get("/api/v1/bootstrap"))
				.andExpect(status().isOk())
				.andReturn();
		csrfCookie = bootstrap.getResponse().getCookie("XSRF-TOKEN");
		assertThat(csrfCookie).isNotNull();
	}

	@Test
	void flywaySeededMerchantCatalog() {
		Integer flywayV24 = jdbcTemplate.queryForObject(
				"select count(*) from flyway_schema_history where version = '24' and success = true",
				Integer.class);
		Integer flywayV25 = jdbcTemplate.queryForObject(
				"select count(*) from flyway_schema_history where version = '25' and success = true",
				Integer.class);
		Integer flywayV26 = jdbcTemplate.queryForObject(
				"select count(*) from flyway_schema_history where version = '26' and success = true",
				Integer.class);
		Integer flywayV27 = jdbcTemplate.queryForObject(
				"select count(*) from flyway_schema_history where version = '27' and success = true",
				Integer.class);
		Integer merchants = jdbcTemplate.queryForObject("select count(*) from merchant_definitions", Integer.class);
		Integer stock = jdbcTemplate.queryForObject("select count(*) from merchant_stock", Integer.class);
		assertThat(flywayV24).isEqualTo(1);
		assertThat(flywayV25).isEqualTo(1);
		assertThat(flywayV26).isEqualTo(1);
		assertThat(flywayV27).isEqualTo(1);
		assertThat(merchants).isEqualTo(4);
		assertThat(stock).isEqualTo(19);
	}

	@Test
	void listsMerchantsWithServerPricedCoreStock() throws Exception {
		MockHttpSession session = registerWithCharacter("mrc-list-" + System.nanoTime() + "@greyhaven.test");

		mockMvc.perform(get("/api/v1/market/merchants").session(session))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.merchants.length()").value(4))
				.andExpect(jsonPath("$.merchants[0].code").value(MerchantCodes.WEAPONSMITH))
				.andExpect(jsonPath("$.merchants[0].name").value("Edric Varn"))
				.andExpect(jsonPath("$.merchants[0].stock[0].itemCode").value(ItemCodes.RUSTY_SWORD))
				.andExpect(jsonPath("$.merchants[0].stock[0].sellPrice").value(7))
				.andExpect(jsonPath("$.merchants[0].stock[0].rarity").value("COMMON"))
				.andExpect(jsonPath("$.merchants[0].stock[?(@.itemCode=='WOODSMAN_AXE')].rarity", hasItem("COMMON")))
				.andExpect(jsonPath("$.merchants[0].stock[?(@.itemCode=='KNOBBED_CLUB')].rarity", hasItem("COMMON")))
				.andExpect(jsonPath("$.merchants[0].stock[?(@.itemCode=='MILITIA_SHORTSWORD')].weaponDamage", hasItem(7)))
				.andExpect(jsonPath("$.merchants[0].stock[?(@.itemCode=='MILITIA_SHORTSWORD')].accuracy", hasItem(4)))
				.andExpect(jsonPath("$.merchants[0].stock[?(@.itemCode=='ARMING_SWORD')].weaponDamage", hasItem(8)))
				.andExpect(jsonPath("$.merchants[0].stock[?(@.itemCode=='ARMING_SWORD')].accuracy", hasItem(4)))
				.andExpect(jsonPath("$.merchants[0].stock[?(@.itemCode=='ARMING_SWORD')].criticalChance", hasItem(1)))
				.andExpect(jsonPath("$.merchants[1].code").value(MerchantCodes.ARMORER))
				.andExpect(jsonPath("$.merchants[1].stock[?(@.itemCode=='PADDED_JACK')].rarity", hasItem("COMMON")))
				.andExpect(jsonPath("$.merchants[1].stock[?(@.itemCode=='SPLINT_VEST')].rarity", hasItem("COMMON")))
				.andExpect(jsonPath("$.merchants[1].stock[?(@.itemCode=='IRON_PLATE')]").isEmpty())
				.andExpect(jsonPath("$.merchants[0].stock[?(@.itemCode=='IRON_AXE')]").isEmpty())
				.andExpect(jsonPath("$.merchants[2].code").value(MerchantCodes.APOTHECARY))
				.andExpect(jsonPath("$.merchants[2].stock[0].itemCode").value(ItemCodes.HEALING_POTION))
				.andExpect(jsonPath("$.merchants[3].code").value(MerchantCodes.GENERAL));
	}

	@Test
	void purchaseCreatesCatalogExactItemAndDeductsGold() throws Exception {
		String email = "mrc-buy-" + System.nanoTime() + "@greyhaven.test";
		MockHttpSession session = registerWithCharacter(email);
		UUID characterId = characterIdForEmail(email);
		moveToMarket(session);

		UUID merchantId = merchantId(MerchantCodes.WEAPONSMITH);
		UUID swordDefinitionId = itemDefinitionId(ItemCodes.RUSTY_SWORD);
		int swordsBefore = itemQuantity(characterId, ItemCodes.RUSTY_SWORD);

		MvcResult bought = mockMvc.perform(withCsrf(post("/api/v1/market/merchants/" + merchantId + "/purchases"))
						.session(session)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"itemDefinitionId":"%s","quantity":1}
								""".formatted(swordDefinitionId)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.itemCode").value(ItemCodes.RUSTY_SWORD))
				.andExpect(jsonPath("$.quantity").value(1))
				.andExpect(jsonPath("$.pricePaid").value(7))
				.andExpect(jsonPath("$.goldRemaining").value(93))
				.andReturn();
		refreshCsrfCookie(bought);

		assertThat(goldOf(characterId)).isEqualTo(93);
		assertThat(itemQuantity(characterId, ItemCodes.RUSTY_SWORD)).isEqualTo(swordsBefore + 1);
		assertThat(itemInstanceCount(characterId, ItemCodes.RUSTY_SWORD)).isEqualTo(swordsBefore + 1);

		mockMvc.perform(get("/api/v1/activity").session(session))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].type").value("MARKET_BOUGHT"))
				.andExpect(jsonPath("$[0].message").value("You bought Rusty Sword for 7 Gold."));
	}

	@Test
	void purchaseRejectsInsufficientGoldInvalidMerchantWrongStockAndQuantity() throws Exception {
		String email = "mrc-buy-err-" + System.nanoTime() + "@greyhaven.test";
		MockHttpSession session = registerWithCharacter(email);
		UUID characterId = characterIdForEmail(email);
		moveToMarket(session);

		UUID weaponsmith = merchantId(MerchantCodes.WEAPONSMITH);
		UUID swordDefinitionId = itemDefinitionId(ItemCodes.RUSTY_SWORD);
		UUID potionDefinitionId = itemDefinitionId(ItemCodes.HEALING_POTION);

		jdbcTemplate.update("update characters set gold = 0 where id = ?", characterId);
		mockMvc.perform(withCsrf(post("/api/v1/market/merchants/" + weaponsmith + "/purchases"))
						.session(session)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"itemDefinitionId":"%s","quantity":1}
								""".formatted(swordDefinitionId)))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.code").value("INSUFFICIENT_GOLD"));
		assertThat(itemQuantity(characterId, ItemCodes.RUSTY_SWORD)).isEqualTo(1);

		jdbcTemplate.update("update characters set gold = 100 where id = ?", characterId);
		mockMvc.perform(withCsrf(post("/api/v1/market/merchants/" + UUID.randomUUID() + "/purchases"))
						.session(session)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"itemDefinitionId":"%s","quantity":1}
								""".formatted(swordDefinitionId)))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.code").value("MERCHANT_NOT_FOUND"));

		mockMvc.perform(withCsrf(post("/api/v1/market/merchants/" + weaponsmith + "/purchases"))
						.session(session)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"itemDefinitionId":"%s","quantity":1}
								""".formatted(potionDefinitionId)))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.code").value("ITEM_NOT_SOLD_BY_MERCHANT"));

		mockMvc.perform(withCsrf(post("/api/v1/market/merchants/" + weaponsmith + "/purchases"))
						.session(session)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"itemDefinitionId":"%s","quantity":2}
								""".formatted(swordDefinitionId)))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.code").value("INVALID_MERCHANT_QUANTITY"));
		assertThat(goldOf(characterId)).isEqualTo(100);
	}

	@Test
	void purchaseAndSaleRequireMarketLocation() throws Exception {
		String email = "mrc-loc-" + System.nanoTime() + "@greyhaven.test";
		MockHttpSession session = registerWithCharacter(email);
		UUID characterId = characterIdForEmail(email);
		inventoryApplicationService.grantItems(characterId, ItemCodes.WOLF_PELT, 1);
		UUID peltId = itemInstanceId(characterId, ItemCodes.WOLF_PELT);
		UUID merchantId = merchantId(MerchantCodes.WEAPONSMITH);
		UUID swordDefinitionId = itemDefinitionId(ItemCodes.RUSTY_SWORD);

		mockMvc.perform(withCsrf(post("/api/v1/market/merchants/" + merchantId + "/purchases"))
						.session(session)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"itemDefinitionId":"%s","quantity":1}
								""".formatted(swordDefinitionId)))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.code").value("LOCATION_CANNOT_USE_MARKET"));

		mockMvc.perform(withCsrf(post("/api/v1/market/merchant-sales"))
						.session(session)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"itemInstanceId":"%s","quantity":1}
								""".formatted(peltId)))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.code").value("LOCATION_CANNOT_USE_MARKET"));

		assertThat(goldOf(characterId)).isEqualTo(100);
		assertThat(itemQuantity(characterId, ItemCodes.RUSTY_SWORD)).isEqualTo(1);
		assertThat(itemQuantity(characterId, ItemCodes.WOLF_PELT)).isEqualTo(1);
	}

	@Test
	void potionPurchaseRejectsQuantityAboveConfiguredCap() throws Exception {
		String email = "mrc-cap-" + System.nanoTime() + "@greyhaven.test";
		MockHttpSession session = registerWithCharacter(email);
		UUID characterId = characterIdForEmail(email);
		moveToMarket(session);
		jdbcTemplate.update("update characters set gold = 20000 where id = ?", characterId);

		UUID merchantId = merchantId(MerchantCodes.APOTHECARY);
		UUID potionDefinitionId = itemDefinitionId(ItemCodes.HEALING_POTION);

		mockMvc.perform(withCsrf(post("/api/v1/market/merchants/" + merchantId + "/purchases"))
						.session(session)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"itemDefinitionId":"%s","quantity":100}
								""".formatted(potionDefinitionId)))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.code").value("INVALID_MERCHANT_QUANTITY"));
		assertThat(goldOf(characterId)).isEqualTo(20000);
		assertThat(itemQuantity(characterId, ItemCodes.HEALING_POTION)).isEqualTo(2);

		int unitPrice = MerchantPriceCalculator.merchantSellPrice(10, com.example.game.item.domain.ItemRarity.COMMON);
		mockMvc.perform(withCsrf(post("/api/v1/market/merchants/" + merchantId + "/purchases"))
						.session(session)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"itemDefinitionId":"%s","quantity":99}
								""".formatted(potionDefinitionId)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.quantity").value(99))
				.andExpect(jsonPath("$.pricePaid").value(unitPrice * 99));
		assertThat(itemQuantity(characterId, ItemCodes.HEALING_POTION)).isEqualTo(101);
		assertThat(goldOf(characterId)).isEqualTo(20000 - unitPrice * 99);
	}

	@Test
	void concurrentPurchasesWithGoldForOneItemChargeExactlyOnce() throws Exception {
		String email = "mrc-race-" + System.nanoTime() + "@greyhaven.test";
		MockHttpSession session = registerWithCharacter(email);
		UUID characterId = characterIdForEmail(email);
		moveToMarket(session);

		UUID merchantId = merchantId(MerchantCodes.APOTHECARY);
		UUID potionDefinitionId = itemDefinitionId(ItemCodes.HEALING_POTION);
		int unitPrice = MerchantPriceCalculator.merchantSellPrice(10, com.example.game.item.domain.ItemRarity.COMMON);
		jdbcTemplate.update("update characters set gold = ? where id = ?", unitPrice, characterId);

		CyclicBarrier startGate = new CyclicBarrier(2);
		try (ExecutorService executor = Executors.newFixedThreadPool(2)) {
			Callable<MvcResult> buy = () -> {
				startGate.await();
				return mockMvc.perform(withCsrf(post("/api/v1/market/merchants/" + merchantId + "/purchases"))
						.session(session)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"itemDefinitionId":"%s","quantity":1}
								""".formatted(potionDefinitionId)))
						.andReturn();
			};
			Future<MvcResult> first = executor.submit(buy);
			Future<MvcResult> second = executor.submit(buy);
			List<Integer> statuses = List.of(
					first.get().getResponse().getStatus(),
					second.get().getResponse().getStatus());
			assertThat(statuses).containsExactlyInAnyOrder(200, 400);
		}

		assertThat(goldOf(characterId)).isGreaterThanOrEqualTo(0).isEqualTo(0);
		assertThat(itemQuantity(characterId, ItemCodes.HEALING_POTION)).isEqualTo(3);
	}

	@Test
	void failedPurchaseDoesNotCreateAnItemWhenInventoryIsFull() throws Exception {
		String email = "mrc-full-" + System.nanoTime() + "@greyhaven.test";
		MockHttpSession session = registerWithCharacter(email);
		UUID characterId = characterIdForEmail(email);
		int remaining = 40 - 3;
		inventoryApplicationService.grantItems(characterId, ItemCodes.OLD_DAGGER, remaining);
		moveToMarket(session);

		UUID merchantId = merchantId(MerchantCodes.WEAPONSMITH);
		UUID bowDefinitionId = itemDefinitionId(ItemCodes.HUNTING_BOW);

		mockMvc.perform(withCsrf(post("/api/v1/market/merchants/" + merchantId + "/purchases"))
						.session(session)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"itemDefinitionId":"%s","quantity":1}
								""".formatted(bowDefinitionId)))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.code").value("INVENTORY_FULL"));
		assertThat(goldOf(characterId)).isEqualTo(100);
		assertThat(itemQuantity(characterId, ItemCodes.HUNTING_BOW)).isZero();
	}

	@Test
	void potionPurchaseSupportsQuantityAndDoesNotSpamActivity() throws Exception {
		String email = "mrc-pot-" + System.nanoTime() + "@greyhaven.test";
		MockHttpSession session = registerWithCharacter(email);
		UUID characterId = characterIdForEmail(email);
		moveToMarket(session);

		UUID merchantId = merchantId(MerchantCodes.APOTHECARY);
		UUID potionDefinitionId = itemDefinitionId(ItemCodes.HEALING_POTION);
		int expectedPrice = MerchantPriceCalculator.merchantSellPrice(10, com.example.game.item.domain.ItemRarity.COMMON)
				* 5;

		MvcResult bought = mockMvc.perform(withCsrf(post("/api/v1/market/merchants/" + merchantId + "/purchases"))
						.session(session)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"itemDefinitionId":"%s","quantity":5}
								""".formatted(potionDefinitionId)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.itemCode").value(ItemCodes.HEALING_POTION))
				.andExpect(jsonPath("$.quantity").value(5))
				.andExpect(jsonPath("$.pricePaid").value(expectedPrice))
				.andReturn();
		refreshCsrfCookie(bought);

		assertThat(itemQuantity(characterId, ItemCodes.HEALING_POTION)).isEqualTo(7);
		assertThat(itemInstanceCount(characterId, ItemCodes.HEALING_POTION)).isEqualTo(1);
		assertThat(goldOf(characterId)).isEqualTo(100 - expectedPrice);

		MvcResult activity = mockMvc.perform(get("/api/v1/activity").session(session))
				.andExpect(status().isOk())
				.andReturn();
		assertThat(activity.getResponse().getContentAsString()).doesNotContain("MARKET_BOUGHT");
	}

	@Test
	void sellAwardsGoldAndRemovesTheItem() throws Exception {
		String email = "mrc-sell-" + System.nanoTime() + "@greyhaven.test";
		MockHttpSession session = registerWithCharacter(email);
		UUID characterId = characterIdForEmail(email);
		UUID armorId = itemInstanceId(characterId, ItemCodes.WORN_LEATHER_ARMOR);
		moveToMarket(session);

		MvcResult unequipped = mockMvc.perform(withCsrf(post("/api/v1/inventory/" + armorId + "/unequip")).session(session))
				.andExpect(status().isOk())
				.andReturn();
		refreshCsrfCookie(unequipped);

		MvcResult sold = mockMvc.perform(withCsrf(post("/api/v1/market/merchant-sales"))
						.session(session)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"itemInstanceId":"%s","quantity":1}
								""".formatted(armorId)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.itemCode").value(ItemCodes.WORN_LEATHER_ARMOR))
				.andExpect(jsonPath("$.goldAwarded").value(3))
				.andExpect(jsonPath("$.goldRemaining").value(103))
				.andReturn();
		refreshCsrfCookie(sold);

		assertThat(itemQuantity(characterId, ItemCodes.WORN_LEATHER_ARMOR)).isZero();
		assertThat(goldOf(characterId)).isEqualTo(103);
		mockMvc.perform(get("/api/v1/inventory").session(session))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.items[?(@.code=='RUSTY_SWORD')].merchantBuyPrice", hasItem(3)));

		mockMvc.perform(withCsrf(post("/api/v1/market/merchant-sales"))
						.session(session)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"itemInstanceId":"%s","quantity":1}
								""".formatted(armorId)))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.code").value("ITEM_NOT_FOUND"));
	}

	@Test
	void saleRejectsEquippedListedWrongOwnerAndInvalidQuantity() throws Exception {
		String ownerEmail = "mrc-sell-o-" + System.nanoTime() + "@greyhaven.test";
		String otherEmail = "mrc-sell-x-" + System.nanoTime() + "@greyhaven.test";
		MockHttpSession owner = registerWithCharacter(ownerEmail);
		MockHttpSession other = registerWithCharacter(otherEmail);
		UUID ownerId = characterIdForEmail(ownerEmail);
		UUID swordId = itemInstanceId(ownerId, ItemCodes.RUSTY_SWORD);
		inventoryApplicationService.grantItems(ownerId, ItemCodes.WOLF_PELT, 3);
		UUID peltId = itemInstanceId(ownerId, ItemCodes.WOLF_PELT);
		moveToMarket(owner);
		moveToMarket(other);

		mockMvc.perform(withCsrf(post("/api/v1/market/merchant-sales"))
						.session(owner)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"itemInstanceId":"%s","quantity":1}
								""".formatted(swordId)))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.code").value("CANNOT_SELL_EQUIPPED_ITEM"));
		assertThat(itemQuantity(ownerId, ItemCodes.RUSTY_SWORD)).isEqualTo(1);

		MvcResult listed = mockMvc.perform(withCsrf(post("/api/v1/market/listings"))
						.session(owner)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"itemInstanceId":"%s","quantity":2,"price":8}
								""".formatted(peltId)))
				.andExpect(status().isOk())
				.andReturn();
		refreshCsrfCookie(listed);

		mockMvc.perform(withCsrf(post("/api/v1/market/merchant-sales"))
						.session(owner)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"itemInstanceId":"%s","quantity":3}
								""".formatted(peltId)))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.code").value("CANNOT_SELL_LISTED_ITEM"));

		mockMvc.perform(withCsrf(post("/api/v1/market/merchant-sales"))
						.session(other)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"itemInstanceId":"%s","quantity":1}
								""".formatted(peltId)))
				.andExpect(status().isForbidden())
				.andExpect(jsonPath("$.code").value("ITEM_NOT_OWNED"));

		mockMvc.perform(withCsrf(post("/api/v1/market/merchant-sales"))
						.session(owner)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"itemInstanceId":"%s","quantity":0}
								""".formatted(peltId)))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));

		assertThat(goldOf(ownerId)).isEqualTo(99);
		assertThat(itemQuantity(ownerId, ItemCodes.WOLF_PELT)).isEqualTo(3);
	}

	@Test
	void stackSaleRemovesOnlyRequestedQuantity() throws Exception {
		String email = "mrc-stack-" + System.nanoTime() + "@greyhaven.test";
		MockHttpSession session = registerWithCharacter(email);
		UUID characterId = characterIdForEmail(email);
		inventoryApplicationService.grantItems(characterId, ItemCodes.WOLF_PELT, 4);
		UUID peltId = itemInstanceId(characterId, ItemCodes.WOLF_PELT);
		moveToMarket(session);

		int unitPrice = MerchantPriceCalculator.merchantBuyPrice(6, com.example.game.item.domain.ItemRarity.COMMON, 0);
		MvcResult sold = mockMvc.perform(withCsrf(post("/api/v1/market/merchant-sales"))
						.session(session)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"itemInstanceId":"%s","quantity":3}
								""".formatted(peltId)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.goldAwarded").value(unitPrice * 3))
				.andReturn();
		refreshCsrfCookie(sold);

		assertThat(itemQuantity(characterId, ItemCodes.WOLF_PELT)).isEqualTo(1);
		assertThat(goldOf(characterId)).isEqualTo(100 + unitPrice * 3);
	}

	@Test
	void merchantPurchaseDoesNotAlterPlayerListings() throws Exception {
		String sellerEmail = "mrc-reg-s-" + System.nanoTime() + "@greyhaven.test";
		String buyerEmail = "mrc-reg-b-" + System.nanoTime() + "@greyhaven.test";
		MockHttpSession seller = registerWithCharacter(sellerEmail);
		MockHttpSession buyer = registerWithCharacter(buyerEmail);
		UUID sellerId = characterIdForEmail(sellerEmail);
		inventoryApplicationService.grantItems(sellerId, ItemCodes.OLD_DAGGER, 1);
		UUID daggerId = itemInstanceId(sellerId, ItemCodes.OLD_DAGGER);
		moveToMarket(seller);
		moveToMarket(buyer);

		MvcResult listed = mockMvc.perform(withCsrf(post("/api/v1/market/listings"))
						.session(seller)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"itemInstanceId":"%s","quantity":1,"price":8}
								""".formatted(daggerId)))
				.andExpect(status().isOk())
				.andReturn();
		refreshCsrfCookie(listed);
		UUID listingId = UUID.fromString(JsonPath.read(listed.getResponse().getContentAsString(), "$.id"));

		UUID merchantId = merchantId(MerchantCodes.ARMORER);
		UUID bucklerId = itemDefinitionId(ItemCodes.WOODEN_BUCKLER);
		refreshCsrfCookie(mockMvc.perform(withCsrf(post("/api/v1/market/merchants/" + merchantId + "/purchases"))
						.session(buyer)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"itemDefinitionId":"%s","quantity":1}
								""".formatted(bucklerId)))
				.andExpect(status().isOk())
				.andReturn());

		mockMvc.perform(get("/api/v1/market/listings").session(buyer))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.listings[?(@.id=='" + listingId + "')].status", hasItem("ACTIVE")));
	}

	@Test
	void merchantsRequireAuthentication() throws Exception {
		mockMvc.perform(get("/api/v1/market/merchants"))
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.code").value("UNAUTHENTICATED"));
	}

	private UUID merchantId(String code) {
		return jdbcTemplate.queryForObject("select id from merchant_definitions where code = ?", UUID.class, code);
	}

	private UUID itemDefinitionId(String code) {
		return jdbcTemplate.queryForObject("select id from item_definitions where code = ?", UUID.class, code);
	}

	private void moveToMarket(MockHttpSession session) throws Exception {
		UUID marketId = jdbcTemplate.queryForObject(
				"select id from locations where code = 'MARKET'",
				UUID.class);
		MvcResult moved = mockMvc.perform(withCsrf(post("/api/v1/world/move"))
						.session(session)
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"destinationLocationId\":\"" + marketId + "\"}"))
				.andExpect(status().isOk())
				.andReturn();
		refreshCsrfCookie(moved);
	}

	private int goldOf(UUID characterId) {
		Integer gold = jdbcTemplate.queryForObject(
				"select gold from characters where id = ?",
				Integer.class,
				characterId);
		return gold == null ? 0 : gold;
	}

	private int itemInstanceCount(UUID characterId, String code) {
		Integer count = jdbcTemplate.queryForObject(
				"""
						select count(*) from item_instances i
						join item_definitions d on d.id = i.item_definition_id
						where i.owner_character_id = ? and d.code = ?
						""",
				Integer.class,
				characterId,
				code);
		return count == null ? 0 : count;
	}

	private int itemQuantity(UUID characterId, String code) {
		Integer quantity = jdbcTemplate.queryForObject(
				"""
						select coalesce(sum(i.quantity), 0) from item_instances i
						join item_definitions d on d.id = i.item_definition_id
						where i.owner_character_id = ? and d.code = ?
						""",
				Integer.class,
				characterId,
				code);
		return quantity == null ? 0 : quantity;
	}

	private UUID itemInstanceId(UUID characterId, String code) {
		return jdbcTemplate.queryForObject(
				"""
						select i.id from item_instances i
						join item_definitions d on d.id = i.item_definition_id
						where i.owner_character_id = ? and d.code = ?
						order by i.created_at
						limit 1
						""",
				UUID.class,
				characterId,
				code);
	}

	private UUID characterIdForEmail(String email) {
		return jdbcTemplate.queryForObject(
				"""
						select c.id from characters c
						join accounts a on a.id = c.account_id
						where a.email = ?
						""",
				UUID.class,
				email);
	}

	private MockHttpSession registerWithCharacter(String email) throws Exception {
		MvcResult registered = mockMvc.perform(withCsrf(post("/api/v1/auth/register"))
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"email":"%s","password":"password123"}
								""".formatted(email)))
				.andExpect(status().isCreated())
				.andReturn();
		refreshCsrfCookie(registered);
		MockHttpSession session = (MockHttpSession) registered.getRequest().getSession(false);
		assertThat(session).isNotNull();

		String name = "Mrc" + UUID.randomUUID().toString().replace("-", "").substring(0, 10);
		MvcResult created = mockMvc.perform(withCsrf(post("/api/v1/characters"))
						.session(session)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"name":"%s"}
								""".formatted(name)))
				.andExpect(status().isCreated())
				.andReturn();
		refreshCsrfCookie(created);
		return session;
	}

	private void refreshCsrfCookie(MvcResult result) {
		Cookie updated = latestCsrfCookie(result);
		if (updated != null) {
			csrfCookie = updated;
		}
	}

	private static Cookie latestCsrfCookie(MvcResult result) {
		Cookie latest = null;
		for (Cookie cookie : result.getResponse().getCookies()) {
			if ("XSRF-TOKEN".equals(cookie.getName()) && cookie.getValue() != null && !cookie.getValue().isBlank()) {
				latest = cookie;
			}
		}
		return latest;
	}

	private MockHttpServletRequestBuilder withCsrf(MockHttpServletRequestBuilder builder) {
		return builder.header("X-XSRF-TOKEN", csrfCookie.getValue()).cookie(csrfCookie);
	}
}
