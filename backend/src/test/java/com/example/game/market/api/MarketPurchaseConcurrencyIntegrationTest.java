package com.example.game.market.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

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
class MarketPurchaseConcurrencyIntegrationTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Autowired
	private InventoryApplicationService inventoryApplicationService;

	@Test
	void concurrentPurchasesOfTheSameListingSucceedExactlyOnce() throws Exception {
		AuthedPlayer seller = registerPlayer("mkt-race-s-");
		AuthedPlayer buyerA = registerPlayer("mkt-race-a-");
		AuthedPlayer buyerB = registerPlayer("mkt-race-b-");

		inventoryApplicationService.grantItems(seller.characterId, ItemCodes.OLD_DAGGER, 1);
		UUID daggerId = itemInstanceId(seller.characterId, ItemCodes.OLD_DAGGER);

		AuthedPlayer sellerAtMarket = moveToMarket(seller);
		AuthedPlayer buyerAAtMarket = moveToMarket(buyerA);
		AuthedPlayer buyerBAtMarket = moveToMarket(buyerB);

		MvcResult listed = mockMvc.perform(withCsrf(post("/api/v1/market/listings"), sellerAtMarket.csrf)
						.session(sellerAtMarket.session)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"itemInstanceId":"%s","quantity":1,"price":10}
								""".formatted(daggerId)))
				.andExpect(status().isOk())
				.andReturn();
		UUID listingId = UUID.fromString(JsonPath.read(listed.getResponse().getContentAsString(), "$.id"));

		List<MvcResult> results = inParallel(
				() -> mockMvc.perform(withCsrf(post("/api/v1/market/listings/" + listingId + "/buy"), buyerAAtMarket.csrf)
						.session(buyerAAtMarket.session)).andReturn(),
				() -> mockMvc.perform(withCsrf(post("/api/v1/market/listings/" + listingId + "/buy"), buyerBAtMarket.csrf)
						.session(buyerBAtMarket.session)).andReturn());

		List<Integer> statuses = results.stream().map(result -> result.getResponse().getStatus()).toList();
		assertThat(statuses).containsExactlyInAnyOrder(200, 409);

		MvcResult conflict = results.stream()
				.filter(result -> result.getResponse().getStatus() == 409)
				.findFirst()
				.orElseThrow();
		assertThat(conflict.getResponse().getContentAsString()).contains("\"code\":\"LISTING_NOT_ACTIVE\"");

		Integer soldCount = jdbcTemplate.queryForObject(
				"select count(*) from market_listings where id = ? and status = 'SOLD'",
				Integer.class,
				listingId);
		assertThat(soldCount).isEqualTo(1);

		int ownerCount = itemQuantity(buyerA.characterId, ItemCodes.OLD_DAGGER)
				+ itemQuantity(buyerB.characterId, ItemCodes.OLD_DAGGER);
		assertThat(ownerCount).isEqualTo(1);
		assertThat(itemQuantity(seller.characterId, ItemCodes.OLD_DAGGER)).isEqualTo(0);

		int buyerGold = goldOf(buyerA.characterId) + goldOf(buyerB.characterId);
		assertThat(buyerGold).isEqualTo(190);
		assertThat(goldOf(seller.characterId)).isEqualTo(110);
	}

	@Test
	void concurrentBuyAndCancelOfTheSameListingLeaveExactlyOneOutcome() throws Exception {
		AuthedPlayer seller = registerPlayer("mkt-lock-s-");
		AuthedPlayer buyer = registerPlayer("mkt-lock-b-");

		inventoryApplicationService.grantItems(seller.characterId, ItemCodes.OLD_DAGGER, 1);
		UUID daggerId = itemInstanceId(seller.characterId, ItemCodes.OLD_DAGGER);

		AuthedPlayer sellerAtMarket = moveToMarket(seller);
		AuthedPlayer buyerAtMarket = moveToMarket(buyer);

		MvcResult listed = mockMvc.perform(withCsrf(post("/api/v1/market/listings"), sellerAtMarket.csrf)
						.session(sellerAtMarket.session)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"itemInstanceId":"%s","quantity":1,"price":10}
								""".formatted(daggerId)))
				.andExpect(status().isOk())
				.andReturn();
		UUID listingId = UUID.fromString(JsonPath.read(listed.getResponse().getContentAsString(), "$.id"));
		AuthedPlayer sellerReady = new AuthedPlayer(
				sellerAtMarket.session,
				csrfOr(listed, sellerAtMarket.csrf),
				seller.characterId);

		List<MvcResult> results = inParallel(
				() -> mockMvc.perform(withCsrf(
						post("/api/v1/market/listings/" + listingId + "/buy"),
						buyerAtMarket.csrf).session(buyerAtMarket.session)).andReturn(),
				() -> mockMvc.perform(withCsrf(
						delete("/api/v1/market/listings/" + listingId),
						sellerReady.csrf).session(sellerReady.session)).andReturn());

		List<Integer> statuses = results.stream().map(result -> result.getResponse().getStatus()).toList();
		assertThat(statuses).containsExactlyInAnyOrder(200, 409);

		String listingStatus = jdbcTemplate.queryForObject(
				"select status from market_listings where id = ?",
				String.class,
				listingId);
		assertThat(listingStatus).isIn("SOLD", "CANCELLED");

		if ("SOLD".equals(listingStatus)) {
			assertThat(itemQuantity(buyer.characterId, ItemCodes.OLD_DAGGER)).isEqualTo(1);
			assertThat(itemQuantity(seller.characterId, ItemCodes.OLD_DAGGER)).isEqualTo(0);
			assertThat(goldOf(buyer.characterId)).isEqualTo(90);
			assertThat(goldOf(seller.characterId)).isEqualTo(110);
		}
		else {
			assertThat(itemQuantity(buyer.characterId, ItemCodes.OLD_DAGGER)).isEqualTo(0);
			assertThat(itemQuantity(seller.characterId, ItemCodes.OLD_DAGGER)).isEqualTo(1);
			assertThat(goldOf(buyer.characterId)).isEqualTo(100);
			assertThat(goldOf(seller.characterId)).isEqualTo(100);
		}
	}

	private List<MvcResult> inParallel(Callable<MvcResult> first, Callable<MvcResult> second) throws Exception {
		CyclicBarrier startGate = new CyclicBarrier(2);
		try (ExecutorService executor = Executors.newFixedThreadPool(2)) {
			Future<MvcResult> firstResult = executor.submit(atBarrier(startGate, first));
			Future<MvcResult> secondResult = executor.submit(atBarrier(startGate, second));
			return List.of(firstResult.get(), secondResult.get());
		}
	}

	private static Callable<MvcResult> atBarrier(CyclicBarrier startGate, Callable<MvcResult> call) {
		return () -> {
			startGate.await();
			return call.call();
		};
	}

	private AuthedPlayer registerPlayer(String emailPrefix) throws Exception {
		Cookie csrf = freshCsrfCookie();
		String email = emailPrefix + System.nanoTime() + "@greyhaven.test";
		MvcResult registered = mockMvc.perform(withCsrf(post("/api/v1/auth/register"), csrf)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"email":"%s","password":"password123"}
								""".formatted(email)))
				.andExpect(status().isCreated())
				.andReturn();
		csrf = csrfOr(registered, csrf);
		MockHttpSession session = (MockHttpSession) registered.getRequest().getSession(false);
		assertThat(session).isNotNull();

		String name = "R" + UUID.randomUUID().toString().replace("-", "").substring(0, 10);
		MvcResult created = mockMvc.perform(withCsrf(post("/api/v1/characters"), csrf)
						.session(session)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"name":"%s"}
								""".formatted(name)))
				.andExpect(status().isCreated())
				.andReturn();
		csrf = csrfOr(created, csrf);
		UUID characterId = jdbcTemplate.queryForObject(
				"""
						select c.id from characters c
						join accounts a on a.id = c.account_id
						where a.email = ?
						""",
				UUID.class,
				email);
		return new AuthedPlayer(session, csrf, characterId);
	}

	private AuthedPlayer moveToMarket(AuthedPlayer player) throws Exception {
		UUID marketId = jdbcTemplate.queryForObject(
				"select id from locations where code = 'MARKET'",
				UUID.class);
		MvcResult moved = mockMvc.perform(withCsrf(post("/api/v1/world/move"), player.csrf)
						.session(player.session)
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"destinationLocationId\":\"" + marketId + "\"}"))
				.andExpect(status().isOk())
				.andReturn();
		return new AuthedPlayer(player.session, csrfOr(moved, player.csrf), player.characterId);
	}

	private int goldOf(UUID characterId) {
		Integer gold = jdbcTemplate.queryForObject(
				"select gold from characters where id = ?",
				Integer.class,
				characterId);
		return gold == null ? 0 : gold;
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

	private Cookie freshCsrfCookie() throws Exception {
		MvcResult bootstrap = mockMvc.perform(get("/api/v1/bootstrap"))
				.andExpect(status().isOk())
				.andReturn();
		Cookie cookie = bootstrap.getResponse().getCookie("XSRF-TOKEN");
		assertThat(cookie).isNotNull();
		return cookie;
	}

	private static Cookie csrfOr(MvcResult result, Cookie fallback) {
		Cookie latest = null;
		for (Cookie cookie : result.getResponse().getCookies()) {
			if ("XSRF-TOKEN".equals(cookie.getName()) && cookie.getValue() != null && !cookie.getValue().isBlank()) {
				latest = cookie;
			}
		}
		return latest != null ? latest : fallback;
	}

	private static MockHttpServletRequestBuilder withCsrf(MockHttpServletRequestBuilder builder, Cookie csrfCookie) {
		return builder.header("X-XSRF-TOKEN", csrfCookie.getValue()).cookie(csrfCookie);
	}

	private record AuthedPlayer(MockHttpSession session, Cookie csrf, UUID characterId) {
	}
}
