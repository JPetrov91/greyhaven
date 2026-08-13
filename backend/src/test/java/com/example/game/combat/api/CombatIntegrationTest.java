package com.example.game.combat.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

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
import com.example.game.combat.application.CombatApplicationService;
import com.example.game.combat.domain.CombatAction;
import com.example.game.combat.domain.CombatSessionStatus;
import com.example.game.shared.domain.MutableRandomProvider;
import com.example.game.shared.domain.RandomProvider;
import com.jayway.jsonpath.JsonPath;

import jakarta.servlet.http.Cookie;

@Import({ TestcontainersConfiguration.class, CombatIntegrationTest.RandomTestConfig.class })
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class CombatIntegrationTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Autowired
	private RandomProvider randomProvider;

	@Autowired
	private CombatApplicationService combatApplicationService;

	private MutableRandomProvider mutableRandomProvider;
	private Cookie csrfCookie;

	@BeforeEach
	void setUp() throws Exception {
		mutableRandomProvider = (MutableRandomProvider) randomProvider;
		mutableRandomProvider.clear();
		refreshCsrf();
	}

	private void refreshCsrf() throws Exception {
		MvcResult bootstrap = mockMvc.perform(get("/api/v1/bootstrap"))
				.andExpect(status().isOk())
				.andReturn();
		csrfCookie = bootstrap.getResponse().getCookie("XSRF-TOKEN");
		assertThat(csrfCookie).isNotNull();
	}

	@Test
	void flywaySeededMonstersAndEncounterWeights() {
		Integer monsters = jdbcTemplate.queryForObject("select count(*) from monster_definitions", Integer.class);
		Integer weights = jdbcTemplate.queryForObject("select count(*) from location_encounter_weights", Integer.class);
		Integer flywayV9 = jdbcTemplate.queryForObject(
				"select count(*) from flyway_schema_history where version = '9' and success = true",
				Integer.class);
		Integer flywayV10 = jdbcTemplate.queryForObject(
				"select count(*) from flyway_schema_history where version = '10' and success = true",
				Integer.class);

		assertThat(monsters).isEqualTo(5);
		assertThat(weights).isEqualTo(11);
		assertThat(flywayV9).isEqualTo(1);
		assertThat(flywayV10).isEqualTo(1);
	}

	@Test
	void searchRejectedInSafeLocation() throws Exception {
		MockHttpSession session = registerWithCharacter("combat-safe-" + System.nanoTime() + "@greyhaven.test");

		mockMvc.perform(withCsrf(post("/api/v1/encounters/search")).session(session))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.code").value("LOCATION_NOT_DANGEROUS"));
	}

	@Test
	void searchFightWinRewardsAreIdempotentOnRepeatedCompletion() throws Exception {
		String email = "combat-win-" + System.nanoTime() + "@greyhaven.test";
		MockHttpSession session = registerWithCharacter(email);
		UUID characterId = characterIdForEmail(email);
		moveTo(session, "OLD_TOWN");

		// Force Street Thug encounter (first weight bucket).
		mutableRandomProvider.queue(1);
		MvcResult search = mockMvc.perform(withCsrf(post("/api/v1/encounters/search")).session(session))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.found").value(true))
				.andExpect(jsonPath("$.monster.code").value("STREET_THUG"))
				.andReturn();
		UUID encounterId = UUID.fromString(JsonPath.read(search.getResponse().getContentAsString(), "$.encounterId"));

		MvcResult fight = mockMvc.perform(withCsrf(post("/api/v1/encounters/" + encounterId + "/fight")).session(session))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value("ACTIVE"))
				.andReturn();
		UUID combatId = UUID.fromString(JsonPath.read(fight.getResponse().getContentAsString(), "$.id"));

		jdbcTemplate.update("update combat_sessions set enemy_health = 1 where id = ?", combatId);

		// Hit + no crit; enemy should die before acting.
		mutableRandomProvider.queue(5, 90);
		// Loot/gold scripts after win: gold roll + loot chances (may or may not consume all)
		mutableRandomProvider.queue(5, 10, 1, 80);

		int xpBefore = jdbcTemplate.queryForObject(
				"select experience from characters where id = ?",
				Integer.class,
				characterId);
		int goldBefore = jdbcTemplate.queryForObject(
				"select gold from characters where id = ?",
				Integer.class,
				characterId);

		MvcResult win = mockMvc.perform(withCsrf(post("/api/v1/combat/" + combatId + "/actions"))
						.session(session)
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"action\":\"QUICK_ATTACK\"}"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value("PLAYER_WON"))
				.andExpect(jsonPath("$.rewards.xp").value(20))
				.andReturn();

		String winBody = win.getResponse().getContentAsString();
		int xpAwarded = JsonPath.read(winBody, "$.rewards.xp");
		int goldAwarded = JsonPath.read(winBody, "$.rewards.gold");

		int xpAfterWin = jdbcTemplate.queryForObject(
				"select experience from characters where id = ?",
				Integer.class,
				characterId);
		int goldAfterWin = jdbcTemplate.queryForObject(
				"select gold from characters where id = ?",
				Integer.class,
				characterId);
		assertThat(xpAfterWin).isEqualTo(xpBefore + xpAwarded);
		assertThat(goldAfterWin).isEqualTo(goldBefore + goldAwarded);

		Integer rewardRows = jdbcTemplate.queryForObject(
				"select count(*) from combat_reward_items where session_id = ?",
				Integer.class,
				combatId);

		// Repeat completion-style reads/actions must not duplicate XP/gold/loot rows.
		mockMvc.perform(withCsrf(post("/api/v1/combat/" + combatId + "/actions"))
						.session(session)
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"action\":\"QUICK_ATTACK\"}"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value("PLAYER_WON"))
				.andExpect(jsonPath("$.rewards.xp").value(xpAwarded))
				.andExpect(jsonPath("$.rewards.gold").value(goldAwarded));

		mockMvc.perform(get("/api/v1/combat/current").session(session))
				.andExpect(status().isNoContent());

		assertThat(jdbcTemplate.queryForObject(
				"select experience from characters where id = ?",
				Integer.class,
				characterId)).isEqualTo(xpAfterWin);
		assertThat(jdbcTemplate.queryForObject(
				"select gold from characters where id = ?",
				Integer.class,
				characterId)).isEqualTo(goldAfterWin);
		assertThat(jdbcTemplate.queryForObject(
				"select count(*) from combat_reward_items where session_id = ?",
				Integer.class,
				combatId)).isEqualTo(rewardRows);
	}

	@Test
	void concurrentWinningActionsAwardRewardsOnce() throws Exception {
		String email = "combat-race-" + System.nanoTime() + "@greyhaven.test";
		MockHttpSession session = registerWithCharacter(email);
		UUID accountId = accountIdForEmail(email);
		UUID characterId = characterIdForEmail(email);
		moveTo(session, "OLD_TOWN");

		mutableRandomProvider.queue(1);
		MvcResult search = mockMvc.perform(withCsrf(post("/api/v1/encounters/search")).session(session))
				.andExpect(status().isOk())
				.andReturn();
		UUID encounterId = UUID.fromString(JsonPath.read(search.getResponse().getContentAsString(), "$.encounterId"));
		MvcResult fight = mockMvc.perform(withCsrf(post("/api/v1/encounters/" + encounterId + "/fight")).session(session))
				.andExpect(status().isOk())
				.andReturn();
		UUID combatId = UUID.fromString(JsonPath.read(fight.getResponse().getContentAsString(), "$.id"));
		jdbcTemplate.update("update combat_sessions set enemy_health = 1 where id = ?", combatId);

		int xpBefore = jdbcTemplate.queryForObject(
				"select experience from characters where id = ?",
				Integer.class,
				characterId);

		// Enough scripted hits for both threads; the second may observe an already-terminal session.
		mutableRandomProvider.queue(5, 90, 5, 5, 90, 5, 5, 90, 5);

		CountDownLatch start = new CountDownLatch(1);
		ExecutorService pool = Executors.newFixedThreadPool(2);
		AtomicInteger successes = new AtomicInteger();
		try {
			Future<?> first = pool.submit(() -> {
				start.await();
				var view = combatApplicationService.submitAction(accountId, combatId, CombatAction.QUICK_ATTACK);
				if (view.status() == CombatSessionStatus.PLAYER_WON) {
					successes.incrementAndGet();
				}
				return null;
			});
			Future<?> second = pool.submit(() -> {
				start.await();
				var view = combatApplicationService.submitAction(accountId, combatId, CombatAction.QUICK_ATTACK);
				if (view.status() == CombatSessionStatus.PLAYER_WON) {
					successes.incrementAndGet();
				}
				return null;
			});
			start.countDown();
			first.get(20, TimeUnit.SECONDS);
			second.get(20, TimeUnit.SECONDS);
		}
		finally {
			pool.shutdownNow();
		}

		assertThat(successes.get()).isGreaterThanOrEqualTo(1);
		assertThat(jdbcTemplate.queryForObject(
				"select experience from characters where id = ?",
				Integer.class,
				characterId)).isEqualTo(xpBefore + 20);
		assertThat(jdbcTemplate.queryForObject(
				"select rewards_applied from combat_sessions where id = ?",
				Boolean.class,
				combatId)).isTrue();
	}

	@Test
	void moveBlockedDuringActiveCombat() throws Exception {
		String email = "combat-block-" + System.nanoTime() + "@greyhaven.test";
		MockHttpSession session = registerWithCharacter(email);
		moveTo(session, "OLD_TOWN");

		mutableRandomProvider.queue(1);
		MvcResult search = mockMvc.perform(withCsrf(post("/api/v1/encounters/search")).session(session))
				.andExpect(status().isOk())
				.andReturn();
		UUID encounterId = UUID.fromString(JsonPath.read(search.getResponse().getContentAsString(), "$.encounterId"));
		mockMvc.perform(withCsrf(post("/api/v1/encounters/" + encounterId + "/fight")).session(session))
				.andExpect(status().isOk());

		UUID citySquare = locationId("CITY_SQUARE");
		mockMvc.perform(withCsrf(post("/api/v1/world/move"))
						.session(session)
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"destinationLocationId\":\"" + citySquare + "\"}"))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.code").value("COMBAT_IN_PROGRESS"));
	}

	@Test
	void usePotionConsumesInventoryStack() throws Exception {
		String email = "combat-potion-" + System.nanoTime() + "@greyhaven.test";
		MockHttpSession session = registerWithCharacter(email);
		UUID characterId = characterIdForEmail(email);
		moveTo(session, "OLD_TOWN");

		int potionsBefore = jdbcTemplate.queryForObject(
				"""
						select coalesce(sum(i.quantity), 0)
						from item_instances i
						join item_definitions d on d.id = i.item_definition_id
						where i.owner_character_id = ? and d.code = 'HEALING_POTION'
						""",
				Integer.class,
				characterId);
		assertThat(potionsBefore).isGreaterThanOrEqualTo(1);

		mutableRandomProvider.queue(1);
		MvcResult search = mockMvc.perform(withCsrf(post("/api/v1/encounters/search")).session(session))
				.andExpect(status().isOk())
				.andReturn();
		UUID encounterId = UUID.fromString(JsonPath.read(search.getResponse().getContentAsString(), "$.encounterId"));
		MvcResult fight = mockMvc.perform(withCsrf(post("/api/v1/encounters/" + encounterId + "/fight")).session(session))
				.andExpect(status().isOk())
				.andReturn();
		UUID combatId = UUID.fromString(JsonPath.read(fight.getResponse().getContentAsString(), "$.id"));

		jdbcTemplate.update(
				"update combat_sessions set player_health = 40 where id = ?",
				combatId);
		// Enemy miss after potion
		mutableRandomProvider.queue(90);

		mockMvc.perform(withCsrf(post("/api/v1/combat/" + combatId + "/actions"))
						.session(session)
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"action\":\"USE_POTION\"}"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.playerHealth").value(80));

		int potionsAfter = jdbcTemplate.queryForObject(
				"""
						select coalesce(sum(i.quantity), 0)
						from item_instances i
						join item_definitions d on d.id = i.item_definition_id
						where i.owner_character_id = ? and d.code = 'HEALING_POTION'
						""",
				Integer.class,
				characterId);
		assertThat(potionsAfter).isEqualTo(potionsBefore - 1);
	}

	@Test
	void attributeAllocationAfterLevelUp() throws Exception {
		String email = "combat-attrs-" + System.nanoTime() + "@greyhaven.test";
		MockHttpSession session = registerWithCharacter(email);
		UUID characterId = characterIdForEmail(email);

		jdbcTemplate.update(
				"update characters set experience = 90, level = 1, unspent_attribute_points = 0 where id = ?",
				characterId);

		moveTo(session, "OLD_TOWN");
		mutableRandomProvider.queue(1);
		MvcResult search = mockMvc.perform(withCsrf(post("/api/v1/encounters/search")).session(session))
				.andExpect(status().isOk())
				.andReturn();
		UUID encounterId = UUID.fromString(JsonPath.read(search.getResponse().getContentAsString(), "$.encounterId"));
		MvcResult fight = mockMvc.perform(withCsrf(post("/api/v1/encounters/" + encounterId + "/fight")).session(session))
				.andExpect(status().isOk())
				.andReturn();
		UUID combatId = UUID.fromString(JsonPath.read(fight.getResponse().getContentAsString(), "$.id"));
		jdbcTemplate.update("update combat_sessions set enemy_health = 1 where id = ?", combatId);
		mutableRandomProvider.queue(5, 90, 5);

		mockMvc.perform(withCsrf(post("/api/v1/combat/" + combatId + "/actions"))
						.session(session)
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"action\":\"QUICK_ATTACK\"}"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value("PLAYER_WON"));

		mockMvc.perform(get("/api/v1/character").session(session))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.level").value(2))
				.andExpect(jsonPath("$.unspentAttributePoints").value(2));

		mockMvc.perform(withCsrf(post("/api/v1/character/attributes"))
						.session(session)
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"strength\":1,\"agility\":1,\"endurance\":0,\"perception\":0}"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.strength").value(6))
				.andExpect(jsonPath("$.agility").value(6))
				.andExpect(jsonPath("$.unspentAttributePoints").value(0));
	}

	@Test
	void currentCombatSurvivesNewSession() throws Exception {
		String email = "combat-resume-" + System.nanoTime() + "@greyhaven.test";
		String password = "password-123";
		MockHttpSession session = registerWithCharacter(email, password);
		moveTo(session, "OLD_TOWN");
		mutableRandomProvider.queue(1);
		MvcResult search = mockMvc.perform(withCsrf(post("/api/v1/encounters/search")).session(session))
				.andExpect(status().isOk())
				.andReturn();
		UUID encounterId = UUID.fromString(JsonPath.read(search.getResponse().getContentAsString(), "$.encounterId"));
		MvcResult fight = mockMvc.perform(withCsrf(post("/api/v1/encounters/" + encounterId + "/fight")).session(session))
				.andExpect(status().isOk())
				.andReturn();
		UUID combatId = UUID.fromString(JsonPath.read(fight.getResponse().getContentAsString(), "$.id"));

		mockMvc.perform(withCsrf(post("/api/v1/auth/logout")).session(session))
				.andExpect(status().isNoContent());

		refreshCsrf();
		MockHttpSession newSession = new MockHttpSession();
		mockMvc.perform(withCsrf(post("/api/v1/auth/login"))
						.session(newSession)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"email":"%s","password":"%s"}
								""".formatted(email, password)))
				.andExpect(status().isOk());
		refreshCsrf();

		mockMvc.perform(get("/api/v1/combat/current").session(newSession))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(combatId.toString()))
				.andExpect(jsonPath("$.status").value("ACTIVE"));
	}

	@Test
	void defeatForfeitsRewardsAndRestoresTheCharacter() throws Exception {
		String email = "combat-defeat-" + System.nanoTime() + "@greyhaven.test";
		MockHttpSession session = registerWithCharacter(email);
		UUID characterId = characterIdForEmail(email);
		moveTo(session, "OLD_TOWN");

		mutableRandomProvider.queue(1);
		MvcResult search = mockMvc.perform(withCsrf(post("/api/v1/encounters/search")).session(session))
				.andExpect(status().isOk())
				.andReturn();
		UUID encounterId = UUID.fromString(JsonPath.read(search.getResponse().getContentAsString(), "$.encounterId"));
		MvcResult fight = mockMvc.perform(withCsrf(post("/api/v1/encounters/" + encounterId + "/fight")).session(session))
				.andExpect(status().isOk())
				.andReturn();
		UUID combatId = UUID.fromString(JsonPath.read(fight.getResponse().getContentAsString(), "$.id"));

		int xpBefore = intColumn("select experience from characters where id = ?", characterId);
		int goldBefore = intColumn("select gold from characters where id = ?", characterId);
		jdbcTemplate.update("update combat_sessions set player_health = 1 where id = ?", combatId);

		// Enemy always hits for its maximum roll, which outdamages the remaining hit point.
		mutableRandomProvider.queue(0, 8);
		mockMvc.perform(withCsrf(post("/api/v1/combat/" + combatId + "/actions"))
						.session(session)
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"action\":\"DEFEND\"}"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value("PLAYER_LOST"))
				.andExpect(jsonPath("$.rewards").isEmpty());

		assertThat(intColumn("select experience from characters where id = ?", characterId)).isEqualTo(xpBefore);
		assertThat(intColumn("select gold from characters where id = ?", characterId)).isEqualTo(goldBefore);
		assertThat(intColumn("select count(*) from combat_reward_items where session_id = ?", combatId)).isZero();
		assertThat(jdbcTemplate.queryForObject(
				"select rewards_applied from combat_sessions where id = ?",
				Boolean.class,
				combatId)).isFalse();
		assertThat(jdbcTemplate.queryForObject(
				"select status from encounters where id = ?",
				String.class,
				encounterId)).isEqualTo("RESOLVED");

		// Office-first: a defeated character is playable again instead of stranded at 0 health.
		assertThat(intColumn("select current_health from characters where id = ?", characterId))
				.isEqualTo(intColumn("select max_health from characters where id = ?", characterId));
		assertThat(intColumn("select current_stamina from characters where id = ?", characterId))
				.isEqualTo(intColumn("select max_stamina from characters where id = ?", characterId));
	}

	@Test
	void retreatEndsCombatWithoutRewards() throws Exception {
		String email = "combat-retreat-" + System.nanoTime() + "@greyhaven.test";
		MockHttpSession session = registerWithCharacter(email);
		UUID characterId = characterIdForEmail(email);
		moveTo(session, "OLD_TOWN");

		mutableRandomProvider.queue(1);
		MvcResult search = mockMvc.perform(withCsrf(post("/api/v1/encounters/search")).session(session))
				.andExpect(status().isOk())
				.andReturn();
		UUID encounterId = UUID.fromString(JsonPath.read(search.getResponse().getContentAsString(), "$.encounterId"));
		MvcResult fight = mockMvc.perform(withCsrf(post("/api/v1/encounters/" + encounterId + "/fight")).session(session))
				.andExpect(status().isOk())
				.andReturn();
		UUID combatId = UUID.fromString(JsonPath.read(fight.getResponse().getContentAsString(), "$.id"));

		int xpBefore = intColumn("select experience from characters where id = ?", characterId);

		// Escape roll below the agility-derived chance.
		mutableRandomProvider.queue(0);
		mockMvc.perform(withCsrf(post("/api/v1/combat/" + combatId + "/actions"))
						.session(session)
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"action\":\"RETREAT\"}"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value("PLAYER_ESCAPED"))
				.andExpect(jsonPath("$.rewards").isEmpty());

		assertThat(intColumn("select experience from characters where id = ?", characterId)).isEqualTo(xpBefore);
		assertThat(intColumn("select count(*) from combat_reward_items where session_id = ?", combatId)).isZero();
		assertThat(jdbcTemplate.queryForObject(
				"select status from encounters where id = ?",
				String.class,
				encounterId)).isEqualTo("RESOLVED");
	}

	@Test
	void secondSearchRejectedWhileAnEncounterIsUnresolved() throws Exception {
		MockHttpSession session = registerWithCharacter("combat-unresolved-" + System.nanoTime() + "@greyhaven.test");
		moveTo(session, "OLD_TOWN");

		mutableRandomProvider.queue(1);
		mockMvc.perform(withCsrf(post("/api/v1/encounters/search")).session(session))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.found").value(true));

		mutableRandomProvider.queue(1);
		mockMvc.perform(withCsrf(post("/api/v1/encounters/search")).session(session))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.code").value("UNRESOLVED_ENCOUNTER"));
	}

	@Test
	void ignoringAnEncounterResolvesItAndAllowsSearchingAgain() throws Exception {
		MockHttpSession session = registerWithCharacter("combat-ignore-" + System.nanoTime() + "@greyhaven.test");
		moveTo(session, "OLD_TOWN");

		mutableRandomProvider.queue(1);
		MvcResult search = mockMvc.perform(withCsrf(post("/api/v1/encounters/search")).session(session))
				.andExpect(status().isOk())
				.andReturn();
		UUID encounterId = UUID.fromString(JsonPath.read(search.getResponse().getContentAsString(), "$.encounterId"));

		mockMvc.perform(withCsrf(post("/api/v1/encounters/" + encounterId + "/ignore")).session(session))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value("RESOLVED"));

		mockMvc.perform(withCsrf(post("/api/v1/encounters/" + encounterId + "/ignore")).session(session))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.code").value("ENCOUNTER_NOT_AVAILABLE"));

		mutableRandomProvider.queue(1);
		mockMvc.perform(withCsrf(post("/api/v1/encounters/search")).session(session))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.found").value(true));
	}

	@Test
	void fullInventoryAbortsVictoryInsteadOfDiscardingLoot() throws Exception {
		String email = "combat-full-" + System.nanoTime() + "@greyhaven.test";
		MockHttpSession session = registerWithCharacter(email);
		UUID characterId = characterIdForEmail(email);
		moveTo(session, "OLD_TOWN");

		mutableRandomProvider.queue(1);
		MvcResult search = mockMvc.perform(withCsrf(post("/api/v1/encounters/search")).session(session))
				.andExpect(status().isOk())
				.andReturn();
		UUID encounterId = UUID.fromString(JsonPath.read(search.getResponse().getContentAsString(), "$.encounterId"));
		MvcResult fight = mockMvc.perform(withCsrf(post("/api/v1/encounters/" + encounterId + "/fight")).session(session))
				.andExpect(status().isOk())
				.andReturn();
		UUID combatId = UUID.fromString(JsonPath.read(fight.getResponse().getContentAsString(), "$.id"));

		fillInventoryWithNonStackableItems(characterId);
		int xpBefore = intColumn("select experience from characters where id = ?", characterId);
		int goldBefore = intColumn("select gold from characters where id = ?", characterId);
		jdbcTemplate.update("update combat_sessions set enemy_health = 1 where id = ?", combatId);

		// Killing blow, then a gold roll and both Street Thug loot entries dropping.
		mutableRandomProvider.queue(5, 90, 5, 0, 1, 0, 1);
		mockMvc.perform(withCsrf(post("/api/v1/combat/" + combatId + "/actions"))
						.session(session)
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"action\":\"QUICK_ATTACK\"}"))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.code").value("INVENTORY_FULL"));

		// The whole round rolled back: no partial rewards, and the fight can still be finished.
		assertThat(intColumn("select experience from characters where id = ?", characterId)).isEqualTo(xpBefore);
		assertThat(intColumn("select gold from characters where id = ?", characterId)).isEqualTo(goldBefore);
		assertThat(intColumn("select count(*) from combat_reward_items where session_id = ?", combatId)).isZero();
		assertThat(jdbcTemplate.queryForObject(
				"select status from combat_sessions where id = ?",
				String.class,
				combatId)).isEqualTo("ACTIVE");
		assertThat(jdbcTemplate.queryForObject(
				"select rewards_applied from combat_sessions where id = ?",
				Boolean.class,
				combatId)).isFalse();
	}

	private void fillInventoryWithNonStackableItems(UUID characterId) {
		UUID ironSword = jdbcTemplate.queryForObject(
				"select id from item_definitions where code = 'IRON_SWORD'",
				UUID.class);
		for (int i = 0; i < 40; i++) {
			jdbcTemplate.update(
					"""
							insert into item_instances
							(id, item_definition_id, owner_character_id, quantity, stackable, created_at)
							values (?, ?, ?, 1, false, now())
							""",
					UUID.randomUUID(),
					ironSword,
					characterId);
		}
	}

	private int intColumn(String sql, Object argument) {
		return jdbcTemplate.queryForObject(sql, Integer.class, argument);
	}

	private void moveTo(MockHttpSession session, String locationCode) throws Exception {
		UUID destinationId = locationId(locationCode);
		mockMvc.perform(withCsrf(post("/api/v1/world/move"))
						.session(session)
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"destinationLocationId\":\"" + destinationId + "\"}"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.code").value(locationCode));
	}

	private UUID locationId(String code) {
		return jdbcTemplate.queryForObject(
				"select id from locations where code = ?",
				UUID.class,
				code);
	}

	private UUID characterIdForEmail(String email) {
		return jdbcTemplate.queryForObject(
				"""
						select c.id
						from characters c
						join accounts a on a.id = c.account_id
						where a.email = ?
						""",
				UUID.class,
				email);
	}

	private UUID accountIdForEmail(String email) {
		return jdbcTemplate.queryForObject(
				"select id from accounts where email = ?",
				UUID.class,
				email);
	}

	private MockHttpSession registerWithCharacter(String email) throws Exception {
		return registerWithCharacter(email, "password-123");
	}

	private MockHttpSession registerWithCharacter(String email, String password) throws Exception {
		MockHttpSession session = new MockHttpSession();
		mockMvc.perform(withCsrf(post("/api/v1/auth/register"))
						.session(session)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"email":"%s","password":"%s"}
								""".formatted(email, password)))
				.andExpect(status().isCreated());

		String name = "C" + UUID.randomUUID().toString().replace("-", "").substring(0, 10);
		mockMvc.perform(withCsrf(post("/api/v1/characters"))
						.session(session)
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"name\":\"" + name + "\"}"))
				.andExpect(status().isCreated());
		return session;
	}

	private MockHttpServletRequestBuilder withCsrf(MockHttpServletRequestBuilder builder) {
		return builder.header("X-XSRF-TOKEN", csrfCookie.getValue()).cookie(csrfCookie);
	}

	@TestConfiguration
	static class RandomTestConfig {

		@Bean
		@Primary
		RandomProvider mutableTestRandomProvider() {
			return new MutableRandomProvider();
		}
	}
}
