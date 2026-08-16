package com.example.game.combat.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.Map;
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

	@Autowired
	private com.example.game.inventory.application.InventoryApplicationService inventoryApplicationService;

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
		assertThat(monsters).isEqualTo(28);
		assertThat(weights).isEqualTo(28);
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
						.content("{\"action\":\"QUICK_ATTACK\",\"expectedRoundNumber\":0}"))
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
						.content("{\"action\":\"QUICK_ATTACK\",\"expectedRoundNumber\":0}"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value("PLAYER_WON"))
				.andExpect(jsonPath("$.rewards.xp").value(xpAwarded))
				.andExpect(jsonPath("$.rewards.gold").value(goldAwarded));

		// Reward screen remains resumable until acknowledged.
		mockMvc.perform(get("/api/v1/combat/current").session(session))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(combatId.toString()))
				.andExpect(jsonPath("$.status").value("PLAYER_WON"))
				.andExpect(jsonPath("$.rewards.xp").value(xpAwarded));

		mockMvc.perform(withCsrf(post("/api/v1/auth/logout")).session(session))
				.andExpect(status().isNoContent());
		refreshCsrf();
		MockHttpSession resumedSession = new MockHttpSession();
		mockMvc.perform(withCsrf(post("/api/v1/auth/login"))
						.session(resumedSession)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"email":"%s","password":"password-123"}
								""".formatted(email)))
				.andExpect(status().isOk());
		refreshCsrf();
		mockMvc.perform(get("/api/v1/combat/current").session(resumedSession))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(combatId.toString()))
				.andExpect(jsonPath("$.status").value("PLAYER_WON"))
				.andExpect(jsonPath("$.rewards.xp").value(xpAwarded));

		mockMvc.perform(withCsrf(post("/api/v1/encounters/search")).session(resumedSession))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.code").value("COMBAT_OUTCOME_PENDING"));

		mockMvc.perform(withCsrf(post("/api/v1/combat/" + combatId + "/acknowledge")).session(resumedSession))
				.andExpect(status().isNoContent());

		mockMvc.perform(get("/api/v1/combat/current").session(resumedSession))
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

		// Encounter, planned gold, and both Street Thug drops are rolled when combat starts.
		mutableRandomProvider.queue(1, 5, 0, 1, 0, 1);
		MvcResult search = mockMvc.perform(withCsrf(post("/api/v1/encounters/search")).session(session))
				.andExpect(status().isOk())
				.andReturn();
		UUID encounterId = UUID.fromString(JsonPath.read(search.getResponse().getContentAsString(), "$.encounterId"));
		MvcResult fight = mockMvc.perform(withCsrf(post("/api/v1/encounters/" + encounterId + "/fight")).session(session))
				.andExpect(status().isOk())
				.andReturn();
		UUID combatId = UUID.fromString(JsonPath.read(fight.getResponse().getContentAsString(), "$.id"));
		int plannedRewardRows = intColumn(
				"select count(*) from combat_reward_items where session_id = ?",
				combatId);
		assertThat(plannedRewardRows).isEqualTo(2);
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
				var view = combatApplicationService.submitAction(accountId, combatId, CombatAction.QUICK_ATTACK, 0);
				if (view.status() == CombatSessionStatus.PLAYER_WON) {
					successes.incrementAndGet();
				}
				return null;
			});
			Future<?> second = pool.submit(() -> {
				start.await();
				var view = combatApplicationService.submitAction(accountId, combatId, CombatAction.QUICK_ATTACK, 0);
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
		MvcResult activity = mockMvc.perform(get("/api/v1/activity").session(session))
				.andExpect(status().isOk())
				.andReturn();
		List<Map<String, Object>> entries = JsonPath.read(activity.getResponse().getContentAsString(), "$");
		assertThat(entries.stream().map(entry -> entry.get("type")).toList())
				.contains("ITEM_FOUND");
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
						.content("{\"action\":\"USE_POTION\",\"expectedRoundNumber\":0}"))
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
	void activeCombatBlocksOutOfTurnMutationsAndRejectsAStaleRound() throws Exception {
		String email = "combat-guard-" + System.nanoTime() + "@greyhaven.test";
		MockHttpSession session = registerWithCharacter(email);
		UUID characterId = characterIdForEmail(email);
		moveTo(session, "OLD_TOWN");

		UUID potionId = jdbcTemplate.queryForObject(
				"""
						select i.id
						from item_instances i
						join item_definitions d on d.id = i.item_definition_id
						where i.owner_character_id = ? and d.code = 'HEALING_POTION'
						""",
				UUID.class,
				characterId);
		jdbcTemplate.update(
				"update characters set unspent_attribute_points = 1 where id = ?",
				characterId);

		mutableRandomProvider.queue(1);
		MvcResult search = mockMvc.perform(withCsrf(post("/api/v1/encounters/search")).session(session))
				.andExpect(status().isOk())
				.andReturn();
		UUID encounterId = UUID.fromString(JsonPath.read(search.getResponse().getContentAsString(), "$.encounterId"));
		MvcResult fight = mockMvc.perform(withCsrf(post("/api/v1/encounters/" + encounterId + "/fight")).session(session))
				.andExpect(status().isOk())
				.andReturn();
		UUID combatId = UUID.fromString(JsonPath.read(fight.getResponse().getContentAsString(), "$.id"));

		mockMvc.perform(withCsrf(post("/api/v1/inventory/" + potionId + "/use")).session(session))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.code").value("COMBAT_IN_PROGRESS"));
		mockMvc.perform(withCsrf(post("/api/v1/character/attributes"))
						.session(session)
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"strength\":1,\"agility\":0,\"endurance\":0,\"perception\":0}"))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.code").value("COMBAT_IN_PROGRESS"));

		mutableRandomProvider.queue(5, 90, 90);
		mockMvc.perform(withCsrf(post("/api/v1/combat/" + combatId + "/actions"))
						.session(session)
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"action\":\"QUICK_ATTACK\",\"expectedRoundNumber\":0}"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.roundNumber").value(1))
				.andExpect(jsonPath("$.status").value("ACTIVE"));

		mockMvc.perform(withCsrf(post("/api/v1/combat/" + combatId + "/actions"))
						.session(session)
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"action\":\"QUICK_ATTACK\",\"expectedRoundNumber\":0}"))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.code").value("STALE_COMBAT_STATE"));

		assertThat(intColumn("select round_number from combat_sessions where id = ?", combatId)).isEqualTo(1);
	}

	@Test
	void combatAndEncounterIdsCannotBeUsedByAnotherAccount() throws Exception {
		MockHttpSession owner = registerWithCharacter("combat-owner-" + System.nanoTime() + "@greyhaven.test");
		moveTo(owner, "OLD_TOWN");
		mutableRandomProvider.queue(1);
		MvcResult search = mockMvc.perform(withCsrf(post("/api/v1/encounters/search")).session(owner))
				.andExpect(status().isOk())
				.andReturn();
		UUID encounterId = UUID.fromString(JsonPath.read(search.getResponse().getContentAsString(), "$.encounterId"));

		MockHttpSession other = registerWithCharacter("combat-other-" + System.nanoTime() + "@greyhaven.test");
		mockMvc.perform(withCsrf(post("/api/v1/encounters/" + encounterId + "/fight")).session(other))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.code").value("ENCOUNTER_NOT_FOUND"));

		MvcResult fight = mockMvc.perform(withCsrf(post("/api/v1/encounters/" + encounterId + "/fight")).session(owner))
				.andExpect(status().isOk())
				.andReturn();
		UUID combatId = UUID.fromString(JsonPath.read(fight.getResponse().getContentAsString(), "$.id"));

		mockMvc.perform(withCsrf(post("/api/v1/combat/" + combatId + "/actions"))
						.session(other)
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"action\":\"QUICK_ATTACK\",\"expectedRoundNumber\":0}"))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.code").value("COMBAT_NOT_FOUND"));
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
						.content("{\"action\":\"QUICK_ATTACK\",\"expectedRoundNumber\":0}"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value("PLAYER_WON"))
				.andExpect(jsonPath("$.rewards.previousLevel").value(1))
				.andExpect(jsonPath("$.rewards.newLevel").value(2))
				.andExpect(jsonPath("$.rewards.attributePointsGained").value(2));

		mockMvc.perform(get("/api/v1/character").session(session))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.level").value(2))
				.andExpect(jsonPath("$.unspentAttributePoints").value(2));

		MvcResult activity = mockMvc.perform(get("/api/v1/activity").session(session))
				.andExpect(status().isOk())
				.andReturn();
		List<Map<String, Object>> entries = JsonPath.read(activity.getResponse().getContentAsString(), "$");
		assertThat(entries.stream().map(entry -> entry.get("type")).toList())
				.contains("COMBAT_VICTORY", "LEVEL_UP");
		assertThat(entries.stream()
				.filter(entry -> "LEVEL_UP".equals(entry.get("type")))
				.map(entry -> entry.get("message")))
				.containsExactly("LEVEL UP — Level 1 → 2 (+2 Attribute Points)");

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
						.content("{\"action\":\"DEFEND\",\"expectedRoundNumber\":0}"))
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

		// Office-first: partial recovery keeps the loop playable without a full free refill.
		int maxHealth = intColumn("select max_health from characters where id = ?", characterId);
		int maxStamina = intColumn("select max_stamina from characters where id = ?", characterId);
		assertThat(intColumn("select current_health from characters where id = ?", characterId))
				.isEqualTo(maxHealth / 2);
		assertThat(intColumn("select current_stamina from characters where id = ?", characterId))
				.isEqualTo(maxStamina / 2);

		mockMvc.perform(get("/api/v1/combat/current").session(session))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value("PLAYER_LOST"));
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
						.content("{\"action\":\"RETREAT\",\"expectedRoundNumber\":0}"))
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
		MvcResult search = mockMvc.perform(withCsrf(post("/api/v1/encounters/search")).session(session))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.found").value(true))
				.andReturn();
		UUID encounterId = UUID.fromString(JsonPath.read(search.getResponse().getContentAsString(), "$.encounterId"));

		mockMvc.perform(get("/api/v1/encounters/current").session(session))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.found").value(true))
				.andExpect(jsonPath("$.encounterId").value(encounterId.toString()));

		UUID citySquare = locationId("CITY_SQUARE");
		mockMvc.perform(withCsrf(post("/api/v1/world/move"))
						.session(session)
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"destinationLocationId\":\"" + citySquare + "\"}"))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.code").value("UNRESOLVED_ENCOUNTER"));

		mutableRandomProvider.queue(1);
		mockMvc.perform(withCsrf(post("/api/v1/encounters/search")).session(session))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.code").value("UNRESOLVED_ENCOUNTER"));
	}

	@Test
	void availableEncounterSurvivesLogoutAndLogin() throws Exception {
		String email = "combat-encounter-resume-" + System.nanoTime() + "@greyhaven.test";
		String password = "password-123";
		MockHttpSession session = registerWithCharacter(email, password);
		moveTo(session, "OLD_TOWN");

		mutableRandomProvider.queue(1);
		MvcResult search = mockMvc.perform(withCsrf(post("/api/v1/encounters/search")).session(session))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.found").value(true))
				.andReturn();
		UUID encounterId = UUID.fromString(JsonPath.read(search.getResponse().getContentAsString(), "$.encounterId"));
		String monsterCode = JsonPath.read(search.getResponse().getContentAsString(), "$.monster.code");

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
				.andExpect(status().isNoContent());
		mockMvc.perform(get("/api/v1/encounters/current").session(newSession))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.found").value(true))
				.andExpect(jsonPath("$.encounterId").value(encounterId.toString()))
				.andExpect(jsonPath("$.monster.code").value(monsterCode));

		mockMvc.perform(withCsrf(post("/api/v1/encounters/" + encounterId + "/fight")).session(newSession))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value("ACTIVE"));
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

		// Encounter, planned gold, and both Street Thug drops are rolled when combat starts.
		mutableRandomProvider.queue(1, 5, 0, 1, 0, 1);
		MvcResult search = mockMvc.perform(withCsrf(post("/api/v1/encounters/search")).session(session))
				.andExpect(status().isOk())
				.andReturn();
		UUID encounterId = UUID.fromString(JsonPath.read(search.getResponse().getContentAsString(), "$.encounterId"));
		MvcResult fight = mockMvc.perform(withCsrf(post("/api/v1/encounters/" + encounterId + "/fight")).session(session))
				.andExpect(status().isOk())
				.andReturn();
		UUID combatId = UUID.fromString(JsonPath.read(fight.getResponse().getContentAsString(), "$.id"));
		int plannedRewardRows = intColumn(
				"select count(*) from combat_reward_items where session_id = ?",
				combatId);
		assertThat(plannedRewardRows).isEqualTo(2);

		fillInventoryWithNonStackableItems(characterId);
		int xpBefore = intColumn("select experience from characters where id = ?", characterId);
		int goldBefore = intColumn("select gold from characters where id = ?", characterId);
		jdbcTemplate.update("update combat_sessions set enemy_health = 1 where id = ?", combatId);

		// Killing blow uses the reward plan persisted when combat started.
		mutableRandomProvider.queue(5, 90);
		mockMvc.perform(withCsrf(post("/api/v1/combat/" + combatId + "/actions"))
						.session(session)
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"action\":\"QUICK_ATTACK\",\"expectedRoundNumber\":0}"))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.code").value("INVENTORY_FULL"));

		assertThat(intColumn("select experience from characters where id = ?", characterId)).isEqualTo(xpBefore);
		assertThat(intColumn("select gold from characters where id = ?", characterId)).isEqualTo(goldBefore);
		assertThat(intColumn("select count(*) from combat_reward_items where session_id = ?", combatId))
				.isEqualTo(plannedRewardRows);
		assertThat(jdbcTemplate.queryForObject(
				"select status from combat_sessions where id = ?",
				String.class,
				combatId)).isEqualTo("PLAYER_WON");
		assertThat(jdbcTemplate.queryForObject(
				"select enemy_health from combat_sessions where id = ?",
				Integer.class,
				combatId)).isZero();
		assertThat(jdbcTemplate.queryForObject(
				"select rewards_applied from combat_sessions where id = ?",
				Boolean.class,
				combatId)).isFalse();

		mockMvc.perform(withCsrf(post("/api/v1/combat/" + combatId + "/acknowledge")).session(session))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.code").value("INVENTORY_FULL"));
	}

	@Test
	void failedVictoryRetryDoesNotRerollPlannedItemRolls() throws Exception {
		String email = "combat-reroll-" + System.nanoTime() + "@greyhaven.test";
		MockHttpSession session = registerWithCharacter(email);
		UUID characterId = characterIdForEmail(email);
		moveTo(session, "OLD_TOWN");

		mutableRandomProvider.queue(1, 5, 0, 1, 0, 1, 61, 100, 0, 0, 4);
		MvcResult search = mockMvc.perform(withCsrf(post("/api/v1/encounters/search")).session(session))
				.andExpect(status().isOk())
				.andReturn();
		UUID encounterId = UUID.fromString(JsonPath.read(search.getResponse().getContentAsString(), "$.encounterId"));
		MvcResult fight = mockMvc.perform(withCsrf(post("/api/v1/encounters/" + encounterId + "/fight")).session(session))
				.andExpect(status().isOk())
				.andReturn();
		UUID combatId = UUID.fromString(JsonPath.read(fight.getResponse().getContentAsString(), "$.id"));

		Map<String, Object> planned = jdbcTemplate.queryForMap(
				"""
						select r.rarity, r.rolled_affixes, r.rolled_weapon_damage
						from combat_reward_items r
						join item_definitions d on d.id = r.item_definition_id
						where r.session_id = ? and d.code = 'OLD_DAGGER'
						""",
				combatId);
		assertThat(planned.get("rarity")).isEqualTo("UNCOMMON");
		assertThat(planned.get("rolled_affixes")).isEqualTo("PREFIX:BALANCED:0:4");

		fillInventoryWithNonStackableItems(characterId);
		jdbcTemplate.update("update combat_sessions set enemy_health = 1 where id = ?", combatId);
		mutableRandomProvider.queue(5, 90);
		mockMvc.perform(withCsrf(post("/api/v1/combat/" + combatId + "/actions"))
						.session(session)
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"action\":\"QUICK_ATTACK\",\"expectedRoundNumber\":0}"))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.code").value("INVENTORY_FULL"));

		jdbcTemplate.update(
				"""
						delete from item_instances
						where owner_character_id = ?
							and item_definition_id = (select id from item_definitions where code = 'IRON_SWORD')
						""",
				characterId);

		mockMvc.perform(withCsrf(post("/api/v1/combat/" + combatId + "/actions"))
						.session(session)
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"action\":\"QUICK_ATTACK\",\"expectedRoundNumber\":0}"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value("PLAYER_WON"));

		assertThat(jdbcTemplate.queryForObject(
				"""
						select i.rarity from item_instances i
						join item_definitions d on d.id = i.item_definition_id
						where i.owner_character_id = ? and d.code = 'OLD_DAGGER'
						""",
				String.class,
				characterId)).isEqualTo("UNCOMMON");
		assertThat(jdbcTemplate.queryForObject(
				"""
						select a.affix_code from item_instance_affixes a
						join item_instances i on i.id = a.item_instance_id
						join item_definitions d on d.id = i.item_definition_id
						where i.owner_character_id = ? and d.code = 'OLD_DAGGER'
						""",
				String.class,
				characterId)).isEqualTo("BALANCED");
		assertThat(mutableRandomProvider.remainingScripted()).isZero();
	}

	@Test
	void combat2PersistsStatusesAcrossCurrentReload() throws Exception {
		MockHttpSession session = registerWithCharacter("combat2-status-" + System.nanoTime() + "@greyhaven.test");
		moveTo(session, "OLD_TOWN");
		mutableRandomProvider.queue(1);
		MvcResult search = mockMvc.perform(withCsrf(post("/api/v1/encounters/search")).session(session))
				.andExpect(status().isOk())
				.andReturn();
		UUID encounterId = UUID.fromString(JsonPath.read(search.getResponse().getContentAsString(), "$.encounterId"));
		MvcResult fight = mockMvc.perform(withCsrf(post("/api/v1/encounters/" + encounterId + "/fight")).session(session))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.rulesVersion").value(2))
				.andExpect(jsonPath("$.enemyIntent.kind").value("HEAVY_ATTACK"))
				.andExpect(jsonPath("$.actionPreviews[0].action").value("QUICK_ATTACK"))
				.andExpect(jsonPath("$.actionPreviews[0].hitChancePercent").isNumber())
				.andExpect(jsonPath("$.possibleLoot").isArray())
				.andReturn();
		UUID combatId = UUID.fromString(JsonPath.read(fight.getResponse().getContentAsString(), "$.id"));

		jdbcTemplate.update(
				"""
						insert into combat_status_effects
						(id, session_id, target, status_type, stacks, remaining_rounds)
						values (?, ?, 'ENEMY', 'BLEED', 1, 3)
						""",
				UUID.randomUUID(),
				combatId);

		mockMvc.perform(get("/api/v1/combat/current").session(session))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(combatId.toString()))
				.andExpect(jsonPath("$.enemyStatuses[0].type").value("BLEED"))
				.andExpect(jsonPath("$.enemyStatuses[0].stacks").value(1));
	}

	@Test
	void combat2TechniqueAppliesBleedAndSurvivesReload() throws Exception {
		String email = "combat2-tech-" + System.nanoTime() + "@greyhaven.test";
		MockHttpSession session = registerWithCharacter(email);
		UUID characterId = characterIdForEmail(email);
		inventoryApplicationService.grantCatalogExact(characterId, com.example.game.item.domain.ItemCodes.RUSTY_SWORD, 1);
		UUID rustyId = jdbcTemplate.queryForObject(
				"""
						select i.id from item_instances i
						join item_definitions d on d.id = i.item_definition_id
						where i.owner_character_id = ? and d.code = 'RUSTY_SWORD'
						""",
				UUID.class,
				characterId);
		inventoryApplicationService.equipOwnedItem(characterId, rustyId);
		mockMvc.perform(get("/api/v1/character/techniques").session(session))
				.andExpect(status().isOk());
		jdbcTemplate.update(
				"""
						update technique_loadout_slots
						set technique_code = 'SWORD_DEEP_CUT'
						where character_id = ? and slot_index = 0
						""",
				characterId);
		moveTo(session, "OLD_TOWN");
		mutableRandomProvider.queue(1);
		MvcResult search = mockMvc.perform(withCsrf(post("/api/v1/encounters/search")).session(session))
				.andExpect(status().isOk())
				.andReturn();
		UUID encounterId = UUID.fromString(JsonPath.read(search.getResponse().getContentAsString(), "$.encounterId"));
		MvcResult fight = mockMvc.perform(withCsrf(post("/api/v1/encounters/" + encounterId + "/fight")).session(session))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.rulesVersion").value(2))
				.andExpect(jsonPath("$.techniques[0].code").value("SWORD_DEEP_CUT"))
				.andReturn();
		UUID combatId = UUID.fromString(JsonPath.read(fight.getResponse().getContentAsString(), "$.id"));

		mutableRandomProvider.queue(1, 6, 99, 99, 5);
		mockMvc.perform(withCsrf(post("/api/v1/combat/" + combatId + "/actions"))
						.session(session)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"action":"USE_TECHNIQUE","expectedRoundNumber":0,"techniqueCode":"SWORD_DEEP_CUT"}
								"""))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value("ACTIVE"))
				.andExpect(jsonPath("$.enemyStatuses[?(@.type=='BLEED')]").isNotEmpty());

		mockMvc.perform(get("/api/v1/combat/current").session(session))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(combatId.toString()))
				.andExpect(jsonPath("$.enemyStatuses[0].type").value("BLEED"));
	}

	@Test
	void combat2RejectsLowStaminaAttacks() throws Exception {
		MockHttpSession session = registerWithCharacter("combat2-stam-" + System.nanoTime() + "@greyhaven.test");
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
		jdbcTemplate.update("update combat_sessions set player_stamina = 0 where id = ?", combatId);

		mockMvc.perform(withCsrf(post("/api/v1/combat/" + combatId + "/actions"))
						.session(session)
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"action\":\"HEAVY_ATTACK\",\"expectedRoundNumber\":0}"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.code").value("INSUFFICIENT_STAMINA"));
	}

	@Test
	void combat2PlayerStunSkipsActionAndPersistsImmunity() throws Exception {
		MockHttpSession session = registerWithCharacter("combat2-stun-" + System.nanoTime() + "@greyhaven.test");
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
		jdbcTemplate.update(
				"""
						insert into combat_status_effects
						(id, session_id, target, status_type, stacks, remaining_rounds)
						values (?, ?, 'PLAYER', 'STUN', 1, 1)
						""",
				UUID.randomUUID(),
				combatId);

		mutableRandomProvider.queue(99, 5);
		mockMvc.perform(withCsrf(post("/api/v1/combat/" + combatId + "/actions"))
						.session(session)
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"action\":\"QUICK_ATTACK\",\"expectedRoundNumber\":0}"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value("ACTIVE"))
				.andExpect(jsonPath("$.playerStunned").value(false))
				.andExpect(jsonPath("$.playerStatuses[0].type").value("STUN_IMMUNITY"));
	}

	@Test
	void legacyCombatRejectsTechniques() throws Exception {
		MockHttpSession session = registerWithCharacter("combat-legacy-" + System.nanoTime() + "@greyhaven.test");
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
		jdbcTemplate.update("update combat_sessions set rules_version = 1 where id = ?", combatId);

		mockMvc.perform(withCsrf(post("/api/v1/combat/" + combatId + "/actions"))
						.session(session)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"action":"USE_TECHNIQUE","expectedRoundNumber":0,"techniqueCode":"SWORD_DEEP_CUT"}
								"""))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.code").value("INVALID_TECHNIQUE"));
	}

	private void fillInventoryWithNonStackableItems(UUID characterId) {
		UUID ironSword = jdbcTemplate.queryForObject(
				"select id from item_definitions where code = 'IRON_SWORD'",
				UUID.class);
		for (int i = 0; i < 40; i++) {
			jdbcTemplate.update(
					"""
							insert into item_instances
							(id, item_definition_id, owner_character_id, quantity, stackable, rarity, created_at)
							values (?, ?, ?, 1, false, 'UNCOMMON', now())
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
