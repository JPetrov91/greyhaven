package com.example.game.crafting.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
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
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import com.example.game.TestcontainersConfiguration;
import com.example.game.inventory.application.InventoryApplicationService;
import com.example.game.inventory.domain.InventoryBalance;
import com.example.game.item.domain.ItemCodes;
import com.example.game.shared.domain.MutableClock;
import com.jayway.jsonpath.JsonPath;

import jakarta.servlet.http.Cookie;

@Import({
		TestcontainersConfiguration.class,
		CraftingIntegrationTest.RandomAndClockTestConfig.class
})
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class CraftingIntegrationTest {

	private static final Instant START = Instant.parse("2026-08-15T10:00:00Z");

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Autowired
	private InventoryApplicationService inventoryApplicationService;

	@Autowired
	private Clock clock;

	private MutableClock mutableClock;
	private Cookie csrfCookie;

	@BeforeEach
	void setUp() throws Exception {
		mutableClock = (MutableClock) clock;
		mutableClock.setInstant(START);
		MvcResult bootstrap = mockMvc.perform(get("/api/v1/bootstrap")).andExpect(status().isOk()).andReturn();
		csrfCookie = bootstrap.getResponse().getCookie("XSRF-TOKEN");
		assertThat(csrfCookie).isNotNull();
	}

	@Test
	void professionProgressionCraftingCompletionAndDuplicateClaim() throws Exception {
		String email = "craft-" + System.nanoTime() + "@greyhaven.test";
		MockHttpSession session = registerWithCharacter(email);
		UUID characterId = characterIdForEmail(email);
		inventoryApplicationService.grantItems(characterId, ItemCodes.IRON_ORE, 3);
		jdbcTemplate.update(
				"update character_professions set xp = 35 where character_id = ? and profession = 'BLACKSMITH'",
				characterId);
		moveToWard(session);

		mockMvc.perform(get("/api/v1/crafting/professions").session(session))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[?(@.profession=='BLACKSMITH')].rank").value(hasItem(1)));

		MvcResult started = mockMvc.perform(withCsrf(post("/api/v1/crafting/jobs"))
						.session(session)
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"recipeCode\":\"SMELT_IRON_INGOT\"}"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value("ACTIVE"))
				.andExpect(jsonPath("$.outputItemCode").value(ItemCodes.IRON_INGOT))
				.andReturn();
		UUID jobId = UUID.fromString(JsonPath.read(started.getResponse().getContentAsString(), "$.id"));

		mockMvc.perform(withCsrf(post("/api/v1/crafting/jobs/" + jobId + "/claim")).session(session))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.code").value("CRAFTING_JOB_NOT_READY"));

		mutableClock.advanceSeconds(60);
		mockMvc.perform(get("/api/v1/crafting/jobs/current").session(session))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value("COMPLETED"));

		mockMvc.perform(withCsrf(post("/api/v1/crafting/jobs/" + jobId + "/claim")).session(session))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value("CLAIMED"));

		assertThat(itemQuantity(characterId, ItemCodes.IRON_INGOT)).isEqualTo(1);
		assertThat(itemQuantity(characterId, ItemCodes.IRON_ORE)).isEqualTo(0);
		Integer rank = jdbcTemplate.queryForObject(
				"select rank from character_professions where character_id = ? and profession = 'BLACKSMITH'",
				Integer.class,
				characterId);
		assertThat(rank).isEqualTo(2);

		mockMvc.perform(withCsrf(post("/api/v1/crafting/jobs/" + jobId + "/claim")).session(session))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.code").value("CRAFTING_JOB_ALREADY_CLAIMED"));

		mockMvc.perform(get("/api/v1/activity").session(session))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[*].type", hasItem("CRAFTING_STARTED")))
				.andExpect(jsonPath("$[*].type", hasItem("CRAFTING_CLAIMED")))
				.andExpect(jsonPath("$[*].type", hasItem("PROFESSION_RANK_UP")));
	}

	@Test
	void craftedEquipmentRarityIsPersistedAndDoesNotRerollOnRefresh() throws Exception {
		String email = "craft-rarity-" + System.nanoTime() + "@greyhaven.test";
		MockHttpSession session = registerWithCharacter(email);
		UUID characterId = characterIdForEmail(email);
		inventoryApplicationService.grantItems(characterId, ItemCodes.IRON_INGOT, 4);
		jdbcTemplate.update(
				"update character_professions set rank = 2, xp = 40 where character_id = ? and profession = 'BLACKSMITH'",
				characterId);
		moveToWard(session);

		MvcResult started = mockMvc.perform(withCsrf(post("/api/v1/crafting/jobs"))
						.session(session)
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"recipeCode\":\"FORGE_IRON_SWORD\"}"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value("ACTIVE"))
				.andExpect(jsonPath("$.outputItemCode").value(ItemCodes.IRON_SWORD))
				.andExpect(jsonPath("$.rarity").isNotEmpty())
				.andReturn();
		String rarity = JsonPath.read(started.getResponse().getContentAsString(), "$.rarity");
		UUID jobId = UUID.fromString(JsonPath.read(started.getResponse().getContentAsString(), "$.id"));

		mockMvc.perform(get("/api/v1/crafting/jobs/current").session(session))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(jobId.toString()))
				.andExpect(jsonPath("$.rarity").value(rarity));

		mutableClock.advanceSeconds(180);
		mockMvc.perform(withCsrf(post("/api/v1/crafting/jobs/" + jobId + "/claim")).session(session))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value("CLAIMED"))
				.andExpect(jsonPath("$.rarity").value(rarity));

		String persisted = jdbcTemplate.queryForObject(
				"""
						select i.rarity from item_instances i
						join item_definitions d on d.id = i.item_definition_id
						where i.owner_character_id = ? and d.code = ?
						""",
				String.class,
				characterId,
				ItemCodes.IRON_SWORD);
		assertThat(persisted).isEqualTo(rarity);
	}

	@Test
	void missingMaterialsWrongLocationAndRankAreRejected() throws Exception {
		String email = "craft-rej-" + System.nanoTime() + "@greyhaven.test";
		MockHttpSession session = registerWithCharacter(email);

		mockMvc.perform(withCsrf(post("/api/v1/crafting/jobs"))
						.session(session)
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"recipeCode\":\"SMELT_IRON_INGOT\"}"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.code").value("LOCATION_CANNOT_CRAFT"));

		moveToWard(session);
		mockMvc.perform(withCsrf(post("/api/v1/crafting/jobs"))
						.session(session)
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"recipeCode\":\"SMELT_IRON_INGOT\"}"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.code").value("MISSING_MATERIALS"));

		mockMvc.perform(withCsrf(post("/api/v1/crafting/jobs"))
						.session(session)
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"recipeCode\":\"FORGE_IRON_SWORD\"}"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.code").value("PROFESSION_RANK_TOO_LOW"));
	}

	@Test
	void salvageRejectsEquippedAndListedItemsThenDestroysUnequippedGear() throws Exception {
		String email = "salvage-" + System.nanoTime() + "@greyhaven.test";
		MockHttpSession session = registerWithCharacter(email);
		UUID characterId = characterIdForEmail(email);
		UUID equipped = itemInstanceId(characterId, ItemCodes.RUSTY_SWORD);
		inventoryApplicationService.grantItems(characterId, ItemCodes.OLD_DAGGER, 1);
		UUID dagger = itemInstanceId(characterId, ItemCodes.OLD_DAGGER);

		moveToWard(session);
		mockMvc.perform(withCsrf(post("/api/v1/items/" + equipped + "/salvage")).session(session))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.code").value("CANNOT_SALVAGE_EQUIPPED_ITEM"));

		moveTo(session, "CITY_SQUARE");
		moveTo(session, "MARKET");
		MvcResult listed = mockMvc.perform(withCsrf(post("/api/v1/market/listings"))
						.session(session)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"itemInstanceId":"%s","quantity":1,"price":8}
								""".formatted(dagger)))
				.andExpect(status().isOk())
				.andReturn();
		refreshCsrf(listed);
		moveTo(session, "CITY_SQUARE");
		moveToWard(session);
		mockMvc.perform(withCsrf(post("/api/v1/items/" + dagger + "/salvage")).session(session))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.code").value("CANNOT_SALVAGE_LISTED_ITEM"));

		inventoryApplicationService.grantItems(characterId, ItemCodes.LEATHER_CAP, 1);
		UUID cap = itemInstanceId(characterId, ItemCodes.LEATHER_CAP);
		mockMvc.perform(withCsrf(post("/api/v1/items/" + cap + "/salvage")).session(session))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.sourceItemCode").value(ItemCodes.LEATHER_CAP));
		assertThat(itemQuantity(characterId, ItemCodes.ARMOR_SCRAPS)
				+ itemQuantity(characterId, ItemCodes.LEATHER_STRIPS)
				+ itemQuantity(characterId, ItemCodes.CURED_LEATHER)).isGreaterThan(0);
	}

	@Test
	void concurrentClaimGrantsTheCraftOnce() throws Exception {
		String email = "craft-race-" + System.nanoTime() + "@greyhaven.test";
		MockHttpSession session = registerWithCharacter(email);
		UUID characterId = characterIdForEmail(email);
		inventoryApplicationService.grantItems(characterId, ItemCodes.IRON_ORE, 3);
		moveToWard(session);

		MvcResult started = mockMvc.perform(withCsrf(post("/api/v1/crafting/jobs"))
						.session(session)
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"recipeCode\":\"SMELT_IRON_INGOT\"}"))
				.andExpect(status().isOk())
				.andReturn();
		UUID jobId = UUID.fromString(JsonPath.read(started.getResponse().getContentAsString(), "$.id"));
		mutableClock.advanceSeconds(60);

		List<MvcResult> results = inParallel(
				() -> mockMvc.perform(withCsrf(post("/api/v1/crafting/jobs/" + jobId + "/claim")).session(session))
						.andReturn(),
				() -> mockMvc.perform(withCsrf(post("/api/v1/crafting/jobs/" + jobId + "/claim")).session(session))
						.andReturn());
		List<Integer> statuses = results.stream().map(result -> result.getResponse().getStatus()).toList();
		assertThat(statuses.stream().filter(status -> status == 200).count()).isEqualTo(1);
		assertThat(statuses).contains(409);
		assertThat(itemQuantity(characterId, ItemCodes.IRON_INGOT)).isEqualTo(1);
	}

	@Test
	void fullInventoryClaimSucceedsAfterSalvageFreesASlot() throws Exception {
		String email = "craft-full-" + System.nanoTime() + "@greyhaven.test";
		MockHttpSession session = registerWithCharacter(email);
		UUID characterId = characterIdForEmail(email);
		inventoryApplicationService.grantItems(characterId, ItemCodes.IRON_ORE, 3);
		inventoryApplicationService.grantItems(characterId, ItemCodes.WEAPON_COMPONENTS, 1);
		moveToWard(session);

		MvcResult started = mockMvc.perform(withCsrf(post("/api/v1/crafting/jobs"))
						.session(session)
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"recipeCode\":\"SMELT_IRON_INGOT\"}"))
				.andExpect(status().isOk())
				.andReturn();
		UUID jobId = UUID.fromString(JsonPath.read(started.getResponse().getContentAsString(), "$.id"));
		inventoryApplicationService.grantItems(characterId, ItemCodes.IRON_ORE, 1);

		int used = inventoryApplicationService.usedCapacity(characterId);
		int remaining = InventoryBalance.DEFAULT_CAPACITY - used;
		if (remaining > 0) {
			inventoryApplicationService.grantItems(characterId, ItemCodes.IRON_SWORD, remaining);
		}
		assertThat(inventoryApplicationService.usedCapacity(characterId)).isEqualTo(InventoryBalance.DEFAULT_CAPACITY);

		mutableClock.advanceSeconds(60);
		mockMvc.perform(withCsrf(post("/api/v1/crafting/jobs/" + jobId + "/claim")).session(session))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.code").value("INVENTORY_FULL"));

		UUID sword = itemInstanceId(characterId, ItemCodes.IRON_SWORD);
		mockMvc.perform(withCsrf(post("/api/v1/items/" + sword + "/salvage")).session(session))
				.andExpect(status().isOk());

		mockMvc.perform(withCsrf(post("/api/v1/crafting/jobs/" + jobId + "/claim")).session(session))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value("CLAIMED"));
		assertThat(itemQuantity(characterId, ItemCodes.IRON_INGOT)).isEqualTo(1);
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

	private void moveToWard(MockHttpSession session) throws Exception {
		moveTo(session, "CRAFTSMEN_WARD");
	}

	private void moveTo(MockHttpSession session, String locationCode) throws Exception {
		UUID destinationId = jdbcTemplate.queryForObject(
				"select id from locations where code = ?",
				UUID.class,
				locationCode);
		MvcResult moved = mockMvc.perform(withCsrf(post("/api/v1/world/move"))
						.session(session)
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"destinationLocationId\":\"" + destinationId + "\"}"))
				.andExpect(status().isOk())
				.andReturn();
		refreshCsrf(moved);
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
		MockHttpSession session = new MockHttpSession();
		mockMvc.perform(withCsrf(post("/api/v1/auth/register"))
						.session(session)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"email":"%s","password":"password-123"}
								""".formatted(email)))
				.andExpect(status().isCreated());
		String name = "C" + UUID.randomUUID().toString().replace("-", "").substring(0, 10);
		mockMvc.perform(withCsrf(post("/api/v1/characters"))
						.session(session)
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"name\":\"" + name + "\"}"))
				.andExpect(status().isCreated());
		return session;
	}

	private void refreshCsrf(MvcResult result) {
		Cookie cookie = result.getResponse().getCookie("XSRF-TOKEN");
		if (cookie != null) {
			csrfCookie = cookie;
		}
	}

	private MockHttpServletRequestBuilder withCsrf(MockHttpServletRequestBuilder builder) {
		return builder.header("X-XSRF-TOKEN", csrfCookie.getValue()).cookie(csrfCookie);
	}

	@TestConfiguration
	static class RandomAndClockTestConfig {

		@Bean
		@Primary
		Clock mutableTestClock() {
			return new MutableClock(START, ZoneOffset.UTC);
		}
	}
}
