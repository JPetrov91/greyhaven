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
import com.example.game.market.domain.MarketFeeCalculator;
import com.jayway.jsonpath.JsonPath;

import jakarta.servlet.http.Cookie;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class MarketBuyOrderConcurrencyIntegrationTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Autowired
	private InventoryApplicationService inventoryApplicationService;

	@Test
	void concurrentFulfillmentDoesNotOverfillTheOrder() throws Exception {
		AuthedPlayer buyer = registerPlayer("bo-b-");
		AuthedPlayer sellerA = registerPlayer("bo-a-");
		AuthedPlayer sellerB = registerPlayer("bo-c-");
		UUID peltDefinitionId = jdbcTemplate.queryForObject(
				"select id from item_definitions where code = ?",
				UUID.class,
				ItemCodes.WOLF_PELT);
		inventoryApplicationService.grantItems(sellerA.characterId, ItemCodes.WOLF_PELT, 1);
		inventoryApplicationService.grantItems(sellerB.characterId, ItemCodes.WOLF_PELT, 1);
		UUID peltA = itemInstanceId(sellerA.characterId, ItemCodes.WOLF_PELT);
		UUID peltB = itemInstanceId(sellerB.characterId, ItemCodes.WOLF_PELT);

		AuthedPlayer buyerAtMarket = moveToMarket(buyer);
		AuthedPlayer sellerAAtMarket = moveToMarket(sellerA);
		AuthedPlayer sellerBAtMarket = moveToMarket(sellerB);

		MvcResult created = mockMvc.perform(withCsrf(post("/api/v1/market/buy-orders"), buyerAtMarket.csrf)
						.session(buyerAtMarket.session)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"itemDefinitionId":"%s","quantity":1,"maxUnitPrice":10}
								""".formatted(peltDefinitionId)))
				.andExpect(status().isOk())
				.andReturn();
		UUID orderId = UUID.fromString(JsonPath.read(created.getResponse().getContentAsString(), "$.id"));

		List<MvcResult> results = inParallel(
				() -> mockMvc.perform(withCsrf(post("/api/v1/market/buy-orders/" + orderId + "/fulfill"), sellerAAtMarket.csrf)
						.session(sellerAAtMarket.session)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"itemInstanceId":"%s","quantity":1}
								""".formatted(peltA))).andReturn(),
				() -> mockMvc.perform(withCsrf(post("/api/v1/market/buy-orders/" + orderId + "/fulfill"), sellerBAtMarket.csrf)
						.session(sellerBAtMarket.session)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"itemInstanceId":"%s","quantity":1}
								""".formatted(peltB))).andReturn());

		List<Integer> statuses = results.stream().map(result -> result.getResponse().getStatus()).toList();
		assertThat(statuses).contains(200);
		assertThat(statuses.stream().filter(status -> status == 200).count()).isEqualTo(1);
		assertThat(itemQuantity(buyer.characterId, ItemCodes.WOLF_PELT)).isEqualTo(1);
		int remainingSellerPelts = itemQuantity(sellerA.characterId, ItemCodes.WOLF_PELT)
				+ itemQuantity(sellerB.characterId, ItemCodes.WOLF_PELT);
		assertThat(remainingSellerPelts).isEqualTo(1);
		int sellerGold = goldOf(sellerA.characterId) + goldOf(sellerB.characterId);
		assertThat(sellerGold).isEqualTo(200 - 10 + MarketFeeCalculator.sellerProceeds(10) + 10);
		assertThat(goldOf(buyer.characterId)).isEqualTo(90 - MarketFeeCalculator.buyOrderPostingFee(10));
		Integer tradeEvents = jdbcTemplate.queryForObject(
				"""
						select count(*) from game_telemetry_events
						where event_type = 'MARKET_TRADE'
						and character_id in (?, ?, ?)
						""",
				Integer.class,
				buyer.characterId,
				sellerA.characterId,
				sellerB.characterId);
		assertThat(tradeEvents).isEqualTo(1);
	}

	@Test
	void concurrentFulfillAndCancelLeaveExactlyOneGoldAndItemOutcome() throws Exception {
		AuthedPlayer buyer = registerPlayer("bo-cx-b-");
		AuthedPlayer seller = registerPlayer("bo-cx-s-");
		UUID peltDefinitionId = jdbcTemplate.queryForObject(
				"select id from item_definitions where code = ?",
				UUID.class,
				ItemCodes.WOLF_PELT);
		inventoryApplicationService.grantItems(seller.characterId, ItemCodes.WOLF_PELT, 1);
		UUID peltId = itemInstanceId(seller.characterId, ItemCodes.WOLF_PELT);

		AuthedPlayer buyerAtMarket = moveToMarket(buyer);
		AuthedPlayer sellerAtMarket = moveToMarket(seller);

		MvcResult created = mockMvc.perform(withCsrf(post("/api/v1/market/buy-orders"), buyerAtMarket.csrf)
						.session(buyerAtMarket.session)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"itemDefinitionId":"%s","quantity":1,"maxUnitPrice":10}
								""".formatted(peltDefinitionId)))
				.andExpect(status().isOk())
				.andReturn();
		UUID orderId = UUID.fromString(JsonPath.read(created.getResponse().getContentAsString(), "$.id"));
		AuthedPlayer buyerReady = new AuthedPlayer(
				buyerAtMarket.session,
				csrfOr(created, buyerAtMarket.csrf),
				buyer.characterId);

		List<MvcResult> results = inParallel(
				() -> mockMvc.perform(withCsrf(
								post("/api/v1/market/buy-orders/" + orderId + "/fulfill"),
								sellerAtMarket.csrf)
						.session(sellerAtMarket.session)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"itemInstanceId":"%s","quantity":1}
								""".formatted(peltId))).andReturn(),
				() -> mockMvc.perform(withCsrf(
								delete("/api/v1/market/buy-orders/" + orderId),
								buyerReady.csrf)
						.session(buyerReady.session)).andReturn());

		List<Integer> statuses = results.stream().map(result -> result.getResponse().getStatus()).toList();
		assertThat(statuses).containsExactlyInAnyOrder(200, 409);

		String orderStatus = jdbcTemplate.queryForObject(
				"select status from market_buy_orders where id = ?",
				String.class,
				orderId);
		assertThat(orderStatus).isIn("FILLED", "CANCELLED");
		int postingFee = MarketFeeCalculator.buyOrderPostingFee(10);
		if ("FILLED".equals(orderStatus)) {
			assertThat(itemQuantity(buyer.characterId, ItemCodes.WOLF_PELT)).isEqualTo(1);
			assertThat(itemQuantity(seller.characterId, ItemCodes.WOLF_PELT)).isEqualTo(0);
			assertThat(goldOf(buyer.characterId)).isEqualTo(90 - postingFee);
			assertThat(goldOf(seller.characterId)).isEqualTo(100 + MarketFeeCalculator.sellerProceeds(10));
		}
		else {
			assertThat(itemQuantity(buyer.characterId, ItemCodes.WOLF_PELT)).isEqualTo(0);
			assertThat(itemQuantity(seller.characterId, ItemCodes.WOLF_PELT)).isEqualTo(1);
			assertThat(goldOf(buyer.characterId)).isEqualTo(100 - postingFee);
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
		String name = "Bo" + UUID.randomUUID().toString().replace("-", "").substring(0, 10);
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
		UUID marketId = jdbcTemplate.queryForObject("select id from locations where code = 'MARKET'", UUID.class);
		MvcResult moved = mockMvc.perform(withCsrf(post("/api/v1/world/move"), player.csrf)
						.session(player.session)
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"destinationLocationId\":\"" + marketId + "\"}"))
				.andExpect(status().isOk())
				.andReturn();
		return new AuthedPlayer(player.session, csrfOr(moved, player.csrf), player.characterId);
	}

	private int goldOf(UUID characterId) {
		Integer gold = jdbcTemplate.queryForObject("select gold from characters where id = ?", Integer.class, characterId);
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
		return mockMvc.perform(get("/api/v1/bootstrap")).andReturn().getResponse().getCookie("XSRF-TOKEN");
	}

	private static Cookie csrfOr(MvcResult result, Cookie fallback) {
		Cookie latest = fallback;
		for (Cookie cookie : result.getResponse().getCookies()) {
			if ("XSRF-TOKEN".equals(cookie.getName()) && cookie.getValue() != null && !cookie.getValue().isBlank()) {
				latest = cookie;
			}
		}
		return latest;
	}

	private static MockHttpServletRequestBuilder withCsrf(MockHttpServletRequestBuilder builder, Cookie csrf) {
		return builder.header("X-XSRF-TOKEN", csrf.getValue()).cookie(csrf);
	}

	private record AuthedPlayer(MockHttpSession session, Cookie csrf, UUID characterId) {
	}
}
