package com.example.game.market.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;

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
import com.jayway.jsonpath.JsonPath;

import jakarta.servlet.http.Cookie;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class MarketIntegrationTest {

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
	void flywayCreatedMarketListings() {
		Integer flywayV15 = jdbcTemplate.queryForObject(
				"select count(*) from flyway_schema_history where version = '15' and success = true",
				Integer.class);
		Integer flywayV16 = jdbcTemplate.queryForObject(
				"select count(*) from flyway_schema_history where version = '16' and success = true",
				Integer.class);
		Integer uniqueIndex = jdbcTemplate.queryForObject(
				"""
						select count(*) from pg_indexes
						where tablename = 'market_listings' and indexname = 'uq_market_listings_active_item'
						""",
				Integer.class);
		assertThat(flywayV15).isEqualTo(1);
		assertThat(flywayV16).isEqualTo(1);
		assertThat(uniqueIndex).isZero();
	}

	@Test
	void createBrowseFilterBuyAndCancelWorkAtTheMarket() throws Exception {
		String sellerEmail = "mkt-seller-" + System.nanoTime() + "@greyhaven.test";
		String buyerEmail = "mkt-buyer-" + System.nanoTime() + "@greyhaven.test";
		MockHttpSession seller = registerWithCharacter(sellerEmail);
		MockHttpSession buyer = registerWithCharacter(buyerEmail);
		UUID sellerId = characterIdForEmail(sellerEmail);
		UUID buyerId = characterIdForEmail(buyerEmail);

		inventoryApplicationService.grantItems(sellerId, ItemCodes.WOLF_PELT, 2);
		inventoryApplicationService.grantItems(sellerId, ItemCodes.OLD_DAGGER, 1);
		UUID peltId = itemInstanceId(sellerId, ItemCodes.WOLF_PELT);
		UUID daggerId = itemInstanceId(sellerId, ItemCodes.OLD_DAGGER);

		moveToMarket(seller);
		moveToMarket(buyer);

		MvcResult peltListed = mockMvc.perform(withCsrf(post("/api/v1/market/listings"))
						.session(seller)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"itemInstanceId":"%s","quantity":2,"price":12}
								""".formatted(peltId)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.itemCode").value(ItemCodes.WOLF_PELT))
				.andExpect(jsonPath("$.rarity").value("COMMON"))
				.andExpect(jsonPath("$.ownListing").value(true))
				.andExpect(jsonPath("$.status").value("ACTIVE"))
				.andReturn();
		refreshCsrfCookie(peltListed);

		MvcResult daggerListed = mockMvc.perform(withCsrf(post("/api/v1/market/listings"))
						.session(seller)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"itemInstanceId":"%s","quantity":1,"price":8}
								""".formatted(daggerId)))
				.andExpect(status().isOk())
				.andReturn();
		refreshCsrfCookie(daggerListed);
		UUID daggerListingId = UUID.fromString(JsonPath.read(daggerListed.getResponse().getContentAsString(), "$.id"));
		UUID peltListingId = UUID.fromString(JsonPath.read(peltListed.getResponse().getContentAsString(), "$.id"));

		mockMvc.perform(get("/api/v1/market/listings").session(buyer))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.listings.length()").value(2))
				.andExpect(jsonPath("$.truncated").value(false));

		mockMvc.perform(get("/api/v1/market/listings").param("itemType", "WEAPON").session(buyer))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.listings.length()").value(1))
				.andExpect(jsonPath("$.listings[0].itemCode").value(ItemCodes.OLD_DAGGER))
				.andExpect(jsonPath("$.listings[0].sellerName").isNotEmpty())
				.andExpect(jsonPath("$.listings[0].price").value(8));

		mockMvc.perform(get("/api/v1/market/listings").param("mine", "true").session(seller))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.listings.length()").value(2));

		MvcResult bought = mockMvc.perform(withCsrf(post("/api/v1/market/listings/" + daggerListingId + "/buy"))
						.session(buyer))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value("SOLD"))
				.andReturn();
		refreshCsrfCookie(bought);

		assertThat(itemQuantity(buyerId, ItemCodes.OLD_DAGGER)).isEqualTo(1);
		assertThat(itemQuantity(sellerId, ItemCodes.OLD_DAGGER)).isEqualTo(0);
		assertThat(goldOf(buyerId)).isEqualTo(92);
		assertThat(goldOf(sellerId)).isEqualTo(108);

		mockMvc.perform(get("/api/v1/activity").session(buyer))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[*].type", hasItem("MARKET_BOUGHT")))
				.andExpect(jsonPath("$[*].message", hasItem("You bought Old Dagger.")));
		mockMvc.perform(get("/api/v1/activity").session(seller))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[*].type", hasItem("MARKET_SOLD")))
				.andExpect(jsonPath("$[*].message", hasItem("Your marketplace listing sold for 8 gold.")));

		MvcResult cancelled = mockMvc.perform(withCsrf(delete("/api/v1/market/listings/" + peltListingId)).session(seller))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value("CANCELLED"))
				.andReturn();
		refreshCsrfCookie(cancelled);

		mockMvc.perform(get("/api/v1/activity").session(seller))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[*].type", hasItem("MARKET_CANCELLED")));
		assertThat(itemQuantity(sellerId, ItemCodes.WOLF_PELT)).isEqualTo(2);
		mockMvc.perform(get("/api/v1/market/listings").session(buyer))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.listings.length()").value(0));
	}

	@Test
	void rejectsSellingEquippedItemsBuyingOwnListingsAndForeignItems() throws Exception {
		String ownerEmail = "mkt-own-" + System.nanoTime() + "@greyhaven.test";
		String otherEmail = "mkt-other-" + System.nanoTime() + "@greyhaven.test";
		MockHttpSession owner = registerWithCharacter(ownerEmail);
		MockHttpSession other = registerWithCharacter(otherEmail);
		UUID ownerId = characterIdForEmail(ownerEmail);
		UUID otherId = characterIdForEmail(otherEmail);
		UUID equippedSword = itemInstanceId(ownerId, ItemCodes.RUSTY_SWORD);
		inventoryApplicationService.grantItems(otherId, ItemCodes.WOLF_PELT, 1);
		UUID foreignPelt = itemInstanceId(otherId, ItemCodes.WOLF_PELT);

		moveToMarket(owner);

		mockMvc.perform(withCsrf(post("/api/v1/market/listings"))
						.session(owner)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"itemInstanceId":"%s","quantity":1,"price":5}
								""".formatted(equippedSword)))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.code").value("CANNOT_SELL_EQUIPPED_ITEM"));

		mockMvc.perform(withCsrf(post("/api/v1/market/listings"))
						.session(owner)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"itemInstanceId":"%s","quantity":1,"price":5}
								""".formatted(foreignPelt)))
				.andExpect(status().isForbidden())
				.andExpect(jsonPath("$.code").value("ITEM_NOT_OWNED"));

		UUID potionId = itemInstanceId(ownerId, ItemCodes.HEALING_POTION);
		MvcResult listed = mockMvc.perform(withCsrf(post("/api/v1/market/listings"))
						.session(owner)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"itemInstanceId":"%s","quantity":2,"price":5}
								""".formatted(potionId)))
				.andExpect(status().isOk())
				.andReturn();
		refreshCsrfCookie(listed);
		UUID listingId = UUID.fromString(JsonPath.read(listed.getResponse().getContentAsString(), "$.id"));

		mockMvc.perform(withCsrf(post("/api/v1/market/listings/" + listingId + "/buy")).session(owner))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.code").value("CANNOT_BUY_OWN_LISTING"));

		mockMvc.perform(withCsrf(post("/api/v1/inventory/" + potionId + "/use")).session(owner))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.code").value("ITEM_LISTED"));
	}

	@Test
	void buyingAStackMergesIntoTheBuyerExistingStackWithoutDuplicatingRows() throws Exception {
		String sellerEmail = "mkt-merge-s-" + System.nanoTime() + "@greyhaven.test";
		String buyerEmail = "mkt-merge-b-" + System.nanoTime() + "@greyhaven.test";
		MockHttpSession seller = registerWithCharacter(sellerEmail);
		MockHttpSession buyer = registerWithCharacter(buyerEmail);
		UUID sellerId = characterIdForEmail(sellerEmail);
		UUID buyerId = characterIdForEmail(buyerEmail);

		inventoryApplicationService.grantItems(sellerId, ItemCodes.WOLF_PELT, 3);
		inventoryApplicationService.grantItems(buyerId, ItemCodes.WOLF_PELT, 1);
		UUID sellerPeltId = itemInstanceId(sellerId, ItemCodes.WOLF_PELT);
		UUID buyerPeltId = itemInstanceId(buyerId, ItemCodes.WOLF_PELT);

		moveToMarket(seller);
		moveToMarket(buyer);

		MvcResult listed = mockMvc.perform(withCsrf(post("/api/v1/market/listings"))
						.session(seller)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"itemInstanceId":"%s","quantity":2,"price":7}
								""".formatted(sellerPeltId)))
				.andExpect(status().isOk())
				.andReturn();
		refreshCsrfCookie(listed);
		UUID listingId = UUID.fromString(JsonPath.read(listed.getResponse().getContentAsString(), "$.id"));

		mockMvc.perform(withCsrf(post("/api/v1/market/listings/" + listingId + "/buy")).session(buyer))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value("SOLD"));

		assertThat(itemQuantity(buyerId, ItemCodes.WOLF_PELT)).isEqualTo(3);
		assertThat(itemQuantity(sellerId, ItemCodes.WOLF_PELT)).isEqualTo(1);
		assertThat(itemInstanceCount(buyerId, ItemCodes.WOLF_PELT)).isEqualTo(1);
		assertThat(itemInstanceCount(sellerId, ItemCodes.WOLF_PELT)).isEqualTo(1);
		assertThat(itemInstanceId(buyerId, ItemCodes.WOLF_PELT)).isEqualTo(buyerPeltId);
		assertThat(goldOf(buyerId)).isEqualTo(93);
		assertThat(goldOf(sellerId)).isEqualTo(107);
	}

	@Test
	void buyingTheWholeSellerStackDeletesTheEmptyRowAndMergesIntoTheBuyer() throws Exception {
		String sellerEmail = "mkt-stack-s-" + System.nanoTime() + "@greyhaven.test";
		String buyerEmail = "mkt-stack-b-" + System.nanoTime() + "@greyhaven.test";
		MockHttpSession seller = registerWithCharacter(sellerEmail);
		MockHttpSession buyer = registerWithCharacter(buyerEmail);
		UUID sellerId = characterIdForEmail(sellerEmail);
		UUID buyerId = characterIdForEmail(buyerEmail);
		UUID sellerPotionId = itemInstanceId(sellerId, ItemCodes.HEALING_POTION);

		moveToMarket(seller);
		moveToMarket(buyer);

		MvcResult listed = mockMvc.perform(withCsrf(post("/api/v1/market/listings"))
						.session(seller)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"itemInstanceId":"%s","quantity":2,"price":6}
								""".formatted(sellerPotionId)))
				.andExpect(status().isOk())
				.andReturn();
		refreshCsrfCookie(listed);
		UUID listingId = UUID.fromString(JsonPath.read(listed.getResponse().getContentAsString(), "$.id"));

		mockMvc.perform(withCsrf(post("/api/v1/market/listings/" + listingId + "/buy")).session(buyer))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value("SOLD"));

		assertThat(itemQuantity(buyerId, ItemCodes.HEALING_POTION)).isEqualTo(4);
		assertThat(itemQuantity(sellerId, ItemCodes.HEALING_POTION)).isEqualTo(0);
		assertThat(itemInstanceCount(buyerId, ItemCodes.HEALING_POTION)).isEqualTo(1);
		assertThat(itemInstanceCount(sellerId, ItemCodes.HEALING_POTION)).isEqualTo(0);
		assertThat(goldOf(buyerId)).isEqualTo(94);
		assertThat(goldOf(sellerId)).isEqualTo(106);
	}

	@Test
	void insufficientGoldAndWrongLocationAreRejected() throws Exception {
		String sellerEmail = "mkt-gold-" + System.nanoTime() + "@greyhaven.test";
		String buyerEmail = "mkt-poor-" + System.nanoTime() + "@greyhaven.test";
		MockHttpSession seller = registerWithCharacter(sellerEmail);
		MockHttpSession buyer = registerWithCharacter(buyerEmail);
		UUID sellerId = characterIdForEmail(sellerEmail);
		UUID buyerId = characterIdForEmail(buyerEmail);
		inventoryApplicationService.grantItems(sellerId, ItemCodes.WOLF_PELT, 1);
		UUID peltId = itemInstanceId(sellerId, ItemCodes.WOLF_PELT);

		mockMvc.perform(withCsrf(post("/api/v1/market/listings"))
						.session(seller)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"itemInstanceId":"%s","quantity":1,"price":5}
								""".formatted(peltId)))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.code").value("LOCATION_CANNOT_USE_MARKET"));

		moveToMarket(seller);
		MvcResult listed = mockMvc.perform(withCsrf(post("/api/v1/market/listings"))
						.session(seller)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"itemInstanceId":"%s","quantity":1,"price":50}
								""".formatted(peltId)))
				.andExpect(status().isOk())
				.andReturn();
		refreshCsrfCookie(listed);
		UUID listingId = UUID.fromString(JsonPath.read(listed.getResponse().getContentAsString(), "$.id"));

		moveToMarket(buyer);
		jdbcTemplate.update("update characters set gold = 10 where id = ?", buyerId);

		mockMvc.perform(withCsrf(post("/api/v1/market/listings/" + listingId + "/buy")).session(buyer))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.code").value("INSUFFICIENT_GOLD"));
		assertThat(goldOf(buyerId)).isEqualTo(10);
		assertThat(goldOf(sellerId)).isEqualTo(100);
		assertThat(itemQuantity(sellerId, ItemCodes.WOLF_PELT)).isEqualTo(1);
	}

	@Test
	void browseWorksAwayFromTheMarketButWritesDoNot() throws Exception {
		String sellerEmail = "mkt-browse-" + System.nanoTime() + "@greyhaven.test";
		String buyerEmail = "mkt-browse-b-" + System.nanoTime() + "@greyhaven.test";
		MockHttpSession seller = registerWithCharacter(sellerEmail);
		MockHttpSession buyer = registerWithCharacter(buyerEmail);
		UUID sellerId = characterIdForEmail(sellerEmail);
		inventoryApplicationService.grantItems(sellerId, ItemCodes.WOLF_PELT, 1);
		UUID peltId = itemInstanceId(sellerId, ItemCodes.WOLF_PELT);

		moveToMarket(seller);
		MvcResult listed = mockMvc.perform(withCsrf(post("/api/v1/market/listings"))
						.session(seller)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"itemInstanceId":"%s","quantity":1,"price":9}
								""".formatted(peltId)))
				.andExpect(status().isOk())
				.andReturn();
		refreshCsrfCookie(listed);

		mockMvc.perform(get("/api/v1/market/listings").session(buyer))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.listings.length()").value(1))
				.andExpect(jsonPath("$.listings[0].itemCode").value(ItemCodes.WOLF_PELT));
		mockMvc.perform(get("/api/v1/market/listings").param("mine", "true").session(seller))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.listings.length()").value(1));

		UUID listingId = UUID.fromString(JsonPath.read(listed.getResponse().getContentAsString(), "$.id"));
		mockMvc.perform(withCsrf(post("/api/v1/market/listings/" + listingId + "/buy")).session(buyer))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.code").value("LOCATION_CANNOT_USE_MARKET"));

		MvcResult cancelled = mockMvc.perform(withCsrf(delete("/api/v1/market/listings/" + listingId)).session(seller))
				.andExpect(status().isOk())
				.andReturn();
		refreshCsrfCookie(cancelled);
	}

	@Test
	void remainderOfAPartiallyListedStackCanBeListedSeparately() throws Exception {
		String sellerEmail = "mkt-remain-" + System.nanoTime() + "@greyhaven.test";
		MockHttpSession seller = registerWithCharacter(sellerEmail);
		UUID sellerId = characterIdForEmail(sellerEmail);
		inventoryApplicationService.grantItems(sellerId, ItemCodes.WOLF_PELT, 3);
		UUID peltId = itemInstanceId(sellerId, ItemCodes.WOLF_PELT);
		moveToMarket(seller);

		MvcResult first = mockMvc.perform(withCsrf(post("/api/v1/market/listings"))
						.session(seller)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"itemInstanceId":"%s","quantity":1,"price":4}
								""".formatted(peltId)))
				.andExpect(status().isOk())
				.andReturn();
		refreshCsrfCookie(first);

		MvcResult second = mockMvc.perform(withCsrf(post("/api/v1/market/listings"))
						.session(seller)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"itemInstanceId":"%s","quantity":2,"price":7}
								""".formatted(peltId)))
				.andExpect(status().isOk())
				.andReturn();
		refreshCsrfCookie(second);

		mockMvc.perform(withCsrf(post("/api/v1/market/listings"))
						.session(seller)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"itemInstanceId":"%s","quantity":1,"price":3}
								""".formatted(peltId)))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.code").value("INVALID_LISTING_QUANTITY"));

		mockMvc.perform(get("/api/v1/market/listings").param("mine", "true").session(seller))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.listings.length()").value(2));

		UUID firstId = UUID.fromString(JsonPath.read(first.getResponse().getContentAsString(), "$.id"));
		UUID secondId = UUID.fromString(JsonPath.read(second.getResponse().getContentAsString(), "$.id"));
		refreshCsrfCookie(mockMvc.perform(withCsrf(delete("/api/v1/market/listings/" + firstId)).session(seller))
				.andExpect(status().isOk())
				.andReturn());
		refreshCsrfCookie(mockMvc.perform(withCsrf(delete("/api/v1/market/listings/" + secondId)).session(seller))
				.andExpect(status().isOk())
				.andReturn());
	}

	@Test
	void cancelRequiresOwnershipAndRejectsInvalidCreatePayloads() throws Exception {
		String ownerEmail = "mkt-cancel-" + System.nanoTime() + "@greyhaven.test";
		String otherEmail = "mkt-cancel-o-" + System.nanoTime() + "@greyhaven.test";
		MockHttpSession owner = registerWithCharacter(ownerEmail);
		MockHttpSession other = registerWithCharacter(otherEmail);
		UUID ownerId = characterIdForEmail(ownerEmail);
		inventoryApplicationService.grantItems(ownerId, ItemCodes.WOLF_PELT, 1);
		UUID peltId = itemInstanceId(ownerId, ItemCodes.WOLF_PELT);

		moveToMarket(owner);
		moveToMarket(other);

		mockMvc.perform(withCsrf(post("/api/v1/market/listings"))
						.session(owner)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"itemInstanceId":"%s","quantity":1,"price":0}
								""".formatted(peltId)))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));

		MvcResult listed = mockMvc.perform(withCsrf(post("/api/v1/market/listings"))
						.session(owner)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"itemInstanceId":"%s","quantity":1,"price":5}
								""".formatted(peltId)))
				.andExpect(status().isOk())
				.andReturn();
		refreshCsrfCookie(listed);
		UUID listingId = UUID.fromString(JsonPath.read(listed.getResponse().getContentAsString(), "$.id"));

		mockMvc.perform(withCsrf(delete("/api/v1/market/listings/" + listingId)).session(other))
				.andExpect(status().isForbidden())
				.andExpect(jsonPath("$.code").value("LISTING_NOT_OWNED"));

		refreshCsrfCookie(mockMvc.perform(withCsrf(delete("/api/v1/market/listings/" + listingId)).session(owner))
				.andExpect(status().isOk())
				.andReturn());
	}

	@Test
	void buyIsRejectedWhenTheBuyerHasNoInventorySpace() throws Exception {
		String sellerEmail = "mkt-full-s-" + System.nanoTime() + "@greyhaven.test";
		String buyerEmail = "mkt-full-b-" + System.nanoTime() + "@greyhaven.test";
		MockHttpSession seller = registerWithCharacter(sellerEmail);
		MockHttpSession buyer = registerWithCharacter(buyerEmail);
		UUID sellerId = characterIdForEmail(sellerEmail);
		UUID buyerId = characterIdForEmail(buyerEmail);

		inventoryApplicationService.grantItems(sellerId, ItemCodes.OLD_DAGGER, 1);
		UUID daggerId = itemInstanceId(sellerId, ItemCodes.OLD_DAGGER);
		inventoryApplicationService.grantItems(buyerId, ItemCodes.WOLF_PELT, 1);
		int remaining = 40 - 4;
		inventoryApplicationService.grantItems(buyerId, ItemCodes.IRON_SWORD, remaining);

		moveToMarket(seller);
		moveToMarket(buyer);

		MvcResult listed = mockMvc.perform(withCsrf(post("/api/v1/market/listings"))
						.session(seller)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"itemInstanceId":"%s","quantity":1,"price":5}
								""".formatted(daggerId)))
				.andExpect(status().isOk())
				.andReturn();
		refreshCsrfCookie(listed);
		UUID listingId = UUID.fromString(JsonPath.read(listed.getResponse().getContentAsString(), "$.id"));

		mockMvc.perform(withCsrf(post("/api/v1/market/listings/" + listingId + "/buy")).session(buyer))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.code").value("INVENTORY_FULL"));
		assertThat(itemQuantity(sellerId, ItemCodes.OLD_DAGGER)).isEqualTo(1);
		assertThat(goldOf(buyerId)).isEqualTo(100);
		assertThat(goldOf(sellerId)).isEqualTo(100);

		refreshCsrfCookie(mockMvc.perform(withCsrf(delete("/api/v1/market/listings/" + listingId)).session(seller))
				.andExpect(status().isOk())
				.andReturn());
	}

	@Test
	void marketRequiresAuthentication() throws Exception {
		mockMvc.perform(get("/api/v1/market/listings"))
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.code").value("UNAUTHENTICATED"));
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

		String name = "Mkt" + UUID.randomUUID().toString().replace("-", "").substring(0, 10);
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
