package com.example.game.expedition.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Clock;
import java.time.Instant;
import java.sql.Timestamp;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Callable;
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
import com.example.game.expedition.application.ExpeditionApplicationService;
import com.example.game.shared.api.ApiException;
import com.example.game.shared.domain.MutableClock;
import com.example.game.shared.domain.MutableRandomProvider;
import com.example.game.shared.domain.RandomProvider;
import com.jayway.jsonpath.JsonPath;

import jakarta.servlet.http.Cookie;

@Import({
		TestcontainersConfiguration.class,
		ExpeditionIntegrationTest.RandomAndClockTestConfig.class
})
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ExpeditionIntegrationTest {

	private static final Instant START = Instant.parse("2026-08-13T10:00:00Z");

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Autowired
	private RandomProvider randomProvider;

	@Autowired
	private Clock clock;

	@Autowired
	private ExpeditionApplicationService expeditionApplicationService;

	private MutableRandomProvider mutableRandomProvider;
	private MutableClock mutableClock;
	private Cookie csrfCookie;

	@BeforeEach
	void setUp() throws Exception {
		mutableRandomProvider = (MutableRandomProvider) randomProvider;
		mutableRandomProvider.clear();
		mutableClock = (MutableClock) clock;
		mutableClock.setInstant(START);
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
	void flywayMigrationCreatesExpeditionAndActivityTables() {
		Integer expeditions = jdbcTemplate.queryForObject(
				"select count(*) from information_schema.tables where table_name = 'expeditions'",
				Integer.class);
		Integer activity = jdbcTemplate.queryForObject(
				"select count(*) from information_schema.tables where table_name = 'activity_entries'",
				Integer.class);
		Integer flywayV14 = jdbcTemplate.queryForObject(
				"select count(*) from flyway_schema_history where version = '14' and success = true",
				Integer.class);

		assertThat(expeditions).isEqualTo(1);
		assertThat(activity).isEqualTo(1);
		assertThat(flywayV14).isEqualTo(1);
	}

	@Test
	void startRejectedOutsideExpeditionLocation() throws Exception {
		MockHttpSession session = registerWithCharacter("exp-safe-" + System.nanoTime() + "@greyhaven.test");

		mockMvc.perform(withCsrf(post("/api/v1/expeditions"))
						.session(session)
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"strategy\":\"BALANCED\"}"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.code").value("LOCATION_CANNOT_START_EXPEDITION"));
	}

	@Test
	void startInspectCompleteClaimIsIdempotentAndWritesActivity() throws Exception {
		String email = "exp-claim-" + System.nanoTime() + "@greyhaven.test";
		MockHttpSession session = registerWithCharacter(email);
		UUID characterId = characterIdForEmail(email);
		moveToTavern(session);

		// injury miss, empty miss, xp 18, gold 12, only wolf pelt: rolled when the patrol starts
		mutableRandomProvider.queue(90, 50, 18, 12, 10, 1, 40, 20, 10, 10);

		MvcResult started = mockMvc.perform(withCsrf(post("/api/v1/expeditions"))
						.session(session)
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"strategy\":\"BALANCED\"}"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.expeditionType").value("FOREST_PATROL"))
				.andExpect(jsonPath("$.status").value("ACTIVE"))
				.andExpect(jsonPath("$.strategy").value("BALANCED"))
				.andExpect(jsonPath("$.resultReady").value(false))
				.andExpect(jsonPath("$.rewards").isEmpty())
				.andReturn();

		String expeditionId = JsonPath.read(started.getResponse().getContentAsString(), "$.id");
		String completesAt = JsonPath.read(started.getResponse().getContentAsString(), "$.completesAt");
		assertThat(Instant.parse(completesAt)).isEqualTo(START.plusSeconds(20 * 60));

		mockMvc.perform(get("/api/v1/expeditions/current").session(session))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value("ACTIVE"))
				.andExpect(jsonPath("$.resultReady").value(false))
				.andExpect(jsonPath("$.rewards").isEmpty());

		mockMvc.perform(withCsrf(post("/api/v1/expeditions/" + expeditionId + "/claim")).session(session))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.code").value("EXPEDITION_NOT_READY"));

		mutableClock.advanceSeconds(20 * 60);

		MvcResult completed = mockMvc.perform(get("/api/v1/expeditions/current").session(session))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value("COMPLETED"))
				.andExpect(jsonPath("$.resultReady").value(true))
				.andExpect(jsonPath("$.rewards.xp").value(18))
				.andExpect(jsonPath("$.rewards.gold").value(12))
				.andExpect(jsonPath("$.rewards.items[0].itemCode").value("WOLF_PELT"))
				.andReturn();

		String firstRewards = completed.getResponse().getContentAsString();

		MvcResult again = mockMvc.perform(get("/api/v1/expeditions/current").session(session))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.rewards.xp").value(18))
				.andExpect(jsonPath("$.rewards.gold").value(12))
				.andReturn();
		assertThat(again.getResponse().getContentAsString()).isEqualTo(firstRewards);

		int goldBefore = intColumn("select gold from characters where id = ?", characterId);
		int xpBefore = intColumn("select experience from characters where id = ?", characterId);

		mockMvc.perform(withCsrf(post("/api/v1/expeditions/" + expeditionId + "/claim")).session(session))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value("CLAIMED"))
				.andExpect(jsonPath("$.rewards.xp").value(18))
				.andExpect(jsonPath("$.rewards.gold").value(12));

		assertThat(intColumn("select gold from characters where id = ?", characterId)).isEqualTo(goldBefore + 12);
		assertThat(intColumn("select experience from characters where id = ?", characterId)).isEqualTo(xpBefore + 18);

		mockMvc.perform(withCsrf(post("/api/v1/expeditions/" + expeditionId + "/claim")).session(session))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.code").value("EXPEDITION_ALREADY_CLAIMED"));

		MvcResult activity = mockMvc.perform(get("/api/v1/activity").session(session))
				.andExpect(status().isOk())
				.andReturn();
		List<Map<String, Object>> entries = JsonPath.read(activity.getResponse().getContentAsString(), "$");
		assertThat(entries.stream().map(entry -> entry.get("type")).toList())
				.contains("EXPEDITION_COMPLETED", "EXPEDITION_CLAIMED", "ITEM_FOUND");

		mockMvc.perform(get("/api/v1/expeditions/current").session(session))
				.andExpect(status().isNoContent());
	}

	@Test
	void onlyOneActiveExpeditionAllowed() throws Exception {
		MockHttpSession session = registerWithCharacter("exp-one-" + System.nanoTime() + "@greyhaven.test");
		moveToTavern(session);

		mockMvc.perform(withCsrf(post("/api/v1/expeditions"))
						.session(session)
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"strategy\":\"CAUTIOUS\"}"))
				.andExpect(status().isOk());

		mockMvc.perform(withCsrf(post("/api/v1/expeditions"))
						.session(session)
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"strategy\":\"AGGRESSIVE\"}"))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.code").value("EXPEDITION_IN_PROGRESS"));
	}

	@Test
	void expeditionStartIsBlockedByUnresolvedCombatState() throws Exception {
		MockHttpSession unresolved = registerWithCharacter("exp-encounter-" + System.nanoTime() + "@greyhaven.test");
		moveTo(unresolved, "FOREST");
		mutableRandomProvider.queue(1);
		mockMvc.perform(withCsrf(post("/api/v1/encounters/search")).session(unresolved))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.found").value(true));
		assertStartRejected(unresolved, "UNRESOLVED_ENCOUNTER");

		MockHttpSession activeCombat = registerWithCharacter("exp-combat-" + System.nanoTime() + "@greyhaven.test");
		moveTo(activeCombat, "FOREST");
		UUID activeEncounterId = findEncounter(activeCombat);
		mockMvc.perform(withCsrf(post("/api/v1/encounters/" + activeEncounterId + "/fight")).session(activeCombat))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value("ACTIVE"));
		assertStartRejected(activeCombat, "COMBAT_IN_PROGRESS");

		MockHttpSession pendingOutcome = registerWithCharacter("exp-outcome-" + System.nanoTime() + "@greyhaven.test");
		moveTo(pendingOutcome, "FOREST");
		UUID pendingEncounterId = findEncounter(pendingOutcome);
		MvcResult fight = mockMvc.perform(
						withCsrf(post("/api/v1/encounters/" + pendingEncounterId + "/fight")).session(pendingOutcome))
				.andExpect(status().isOk())
				.andReturn();
		UUID combatId = UUID.fromString(JsonPath.read(fight.getResponse().getContentAsString(), "$.id"));
		jdbcTemplate.update("update combat_sessions set enemy_health = 1 where id = ?", combatId);
		mutableRandomProvider.queue(5, 90);
		mockMvc.perform(withCsrf(post("/api/v1/combat/" + combatId + "/actions"))
						.session(pendingOutcome)
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"action\":\"QUICK_ATTACK\",\"expectedRoundNumber\":0}"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value("PLAYER_WON"));
		assertStartRejected(pendingOutcome, "COMBAT_OUTCOME_PENDING");
	}

	@Test
	void theRestOfTheGameStaysPlayableDuringAnActiveExpedition() throws Exception {
		MockHttpSession session = registerWithCharacter("exp-travel-" + System.nanoTime() + "@greyhaven.test");
		moveToTavern(session);

		mockMvc.perform(withCsrf(post("/api/v1/expeditions"))
						.session(session)
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"strategy\":\"BALANCED\"}"))
				.andExpect(status().isOk());

		// Office-first: an away patrol must not freeze travel or the active loop (spec 2.1 / 36).
		moveTo(session, "MARKET");
		moveTo(session, "CITY_SQUARE");
		moveTo(session, "OLD_TOWN");

		mutableRandomProvider.queue(1);
		mockMvc.perform(withCsrf(post("/api/v1/encounters/search")).session(session))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.found").value(true));

		mockMvc.perform(get("/api/v1/expeditions/current").session(session))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value("ACTIVE"));
	}

	@Test
	void aFailedClaimKeepsTheOriginalResultInsteadOfRerollingIt() throws Exception {
		String email = "exp-reroll-" + System.nanoTime() + "@greyhaven.test";
		MockHttpSession session = registerWithCharacter(email);
		UUID characterId = characterIdForEmail(email);
		moveToTavern(session);

		// injury miss, empty miss, xp 18, gold 12, only wolf pelt
		mutableRandomProvider.queue(90, 50, 18, 12, 10, 1, 40, 20, 10, 10);
		MvcResult started = mockMvc.perform(withCsrf(post("/api/v1/expeditions"))
						.session(session)
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"strategy\":\"BALANCED\"}"))
				.andExpect(status().isOk())
				.andReturn();
		String expeditionId = JsonPath.read(started.getResponse().getContentAsString(), "$.id");

		UUID expeditionUuid = UUID.fromString(expeditionId);
		assertThat(jdbcTemplate.queryForObject(
				"select result_generated from expeditions where id = ?",
				Boolean.class,
				expeditionUuid)).isTrue();
		assertThat(intColumn("select planned_xp from expeditions where id = ?", expeditionUuid)).isEqualTo(18);
		assertThat(intColumn("select count(*) from expedition_reward_items where expedition_id = ?", expeditionUuid))
				.isEqualTo(1);

		fillInventoryWithNonStackableItems(characterId);
		mutableClock.advanceSeconds(20 * 60);

		mockMvc.perform(get("/api/v1/expeditions/current").session(session))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value("COMPLETED"))
				.andExpect(jsonPath("$.rewards.xp").value(18))
				.andExpect(jsonPath("$.rewards.gold").value(12));

		// Retrying against a full inventory must not become a reroll of the persisted outcome.
		for (int attempt = 0; attempt < 3; attempt++) {
			mockMvc.perform(withCsrf(post("/api/v1/expeditions/" + expeditionId + "/claim")).session(session))
					.andExpect(status().isConflict())
					.andExpect(jsonPath("$.code").value("INVENTORY_FULL"));

			assertThat(intColumn("select planned_xp from expeditions where id = ?", expeditionUuid)).isEqualTo(18);
			assertThat(intColumn("select planned_gold from expeditions where id = ?", expeditionUuid)).isEqualTo(12);
			assertThat(intColumn(
					"select count(*) from expedition_reward_items where expedition_id = ?",
					expeditionUuid)).isEqualTo(1);
			assertThat(jdbcTemplate.queryForObject(
					"select status from expeditions where id = ?",
					String.class,
					expeditionUuid)).isEqualTo("COMPLETED");
		}

		jdbcTemplate.update(
				"""
						delete from item_instances
						where id in (
							select i.id
							from item_instances i
							join item_definitions d on d.id = i.item_definition_id
							where i.owner_character_id = ? and d.code = 'IRON_SWORD'
						)
						""",
				characterId);

		mockMvc.perform(withCsrf(post("/api/v1/expeditions/" + expeditionId + "/claim")).session(session))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value("CLAIMED"))
				.andExpect(jsonPath("$.rewards.xp").value(18))
				.andExpect(jsonPath("$.rewards.gold").value(12))
				.andExpect(jsonPath("$.rewards.items[0].itemCode").value("WOLF_PELT"));
	}

	@Test
	void claimReportsTheInjuryActuallyAppliedAfterHealthFlooring() throws Exception {
		String email = "exp-injury-" + System.nanoTime() + "@greyhaven.test";
		MockHttpSession session = registerWithCharacter(email);
		UUID characterId = characterIdForEmail(email);
		moveToTavern(session);
		jdbcTemplate.update(
				"update characters set current_health = 5, last_recovery_at = ? where id = ?",
				Timestamp.from(START),
				characterId);

		// Aggressive injury hit for 18 damage, followed by an empty haul.
		mutableRandomProvider.queue(1, 18, 1);
		MvcResult started = mockMvc.perform(withCsrf(post("/api/v1/expeditions"))
						.session(session)
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"strategy\":\"AGGRESSIVE\"}"))
				.andExpect(status().isOk())
				.andReturn();
		UUID expeditionId = UUID.fromString(JsonPath.read(started.getResponse().getContentAsString(), "$.id"));
		mutableClock.advanceSeconds(20 * 60);

		mockMvc.perform(get("/api/v1/expeditions/current").session(session))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.rewards.injuryDamage").value(18));

		mockMvc.perform(withCsrf(post("/api/v1/expeditions/" + expeditionId + "/claim")).session(session))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value("CLAIMED"))
				.andExpect(jsonPath("$.rewards.injuryDamage").value(18));

		assertThat(intColumn("select current_health from characters where id = ?", characterId)).isEqualTo(147);
		assertThat(intColumn("select injury_applied from expeditions where id = ?", expeditionId)).isEqualTo(18);
	}

	@Test
	void concurrentCompletionChecksWriteOneActivityEntry() throws Exception {
		String email = "exp-complete-race-" + System.nanoTime() + "@greyhaven.test";
		MockHttpSession session = registerWithCharacter(email);
		UUID accountId = accountIdForEmail(email);
		UUID characterId = characterIdForEmail(email);
		moveToTavern(session);
		mockMvc.perform(withCsrf(post("/api/v1/expeditions"))
						.session(session)
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"strategy\":\"BALANCED\"}"))
				.andExpect(status().isOk());
		mutableClock.advanceSeconds(20 * 60);

		CountDownLatch start = new CountDownLatch(1);
		ExecutorService pool = Executors.newFixedThreadPool(2);
		try {
			Future<?> first = pool.submit(() -> {
				start.await();
				assertThat(expeditionApplicationService.current(accountId).status().name()).isEqualTo("COMPLETED");
				return null;
			});
			Future<?> second = pool.submit(() -> {
				start.await();
				assertThat(expeditionApplicationService.current(accountId).status().name()).isEqualTo("COMPLETED");
				return null;
			});
			start.countDown();
			first.get(20, TimeUnit.SECONDS);
			second.get(20, TimeUnit.SECONDS);
		}
		finally {
			pool.shutdownNow();
		}

		assertThat(intColumn(
				"select count(*) from activity_entries where character_id = ? and type = 'EXPEDITION_COMPLETED'",
				characterId)).isEqualTo(1);
	}

	@Test
	void concurrentClaimsAwardRewardsOnce() throws Exception {
		String email = "exp-race-" + System.nanoTime() + "@greyhaven.test";
		MockHttpSession session = registerWithCharacter(email);
		UUID accountId = accountIdForEmail(email);
		UUID characterId = characterIdForEmail(email);
		moveToTavern(session);

		MvcResult started = mockMvc.perform(withCsrf(post("/api/v1/expeditions"))
						.session(session)
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"strategy\":\"BALANCED\"}"))
				.andExpect(status().isOk())
				.andReturn();
		UUID expeditionId = UUID.fromString(JsonPath.read(started.getResponse().getContentAsString(), "$.id"));

		int plannedXp = intColumn("select planned_xp from expeditions where id = ?", expeditionId);
		int plannedGold = intColumn("select planned_gold from expeditions where id = ?", expeditionId);
		int xpBefore = intColumn("select experience from characters where id = ?", characterId);
		int goldBefore = intColumn("select gold from characters where id = ?", characterId);
		mutableClock.advanceSeconds(20 * 60);

		CountDownLatch start = new CountDownLatch(1);
		AtomicInteger successes = new AtomicInteger();
		ExecutorService pool = Executors.newFixedThreadPool(2);
		try {
			List<Future<?>> attempts = List.of(
					pool.submit(claimAttempt(start, accountId, expeditionId, successes)),
					pool.submit(claimAttempt(start, accountId, expeditionId, successes)));
			start.countDown();
			for (Future<?> attempt : attempts) {
				attempt.get(20, TimeUnit.SECONDS);
			}
		}
		finally {
			pool.shutdownNow();
		}

		assertThat(successes.get()).isEqualTo(1);
		assertThat(intColumn("select experience from characters where id = ?", characterId))
				.isEqualTo(xpBefore + plannedXp);
		assertThat(intColumn("select gold from characters where id = ?", characterId))
				.isEqualTo(goldBefore + plannedGold);
		assertThat(jdbcTemplate.queryForObject(
				"select status from expeditions where id = ?",
				String.class,
				expeditionId)).isEqualTo("CLAIMED");
	}

	private Callable<Void> claimAttempt(
			CountDownLatch start,
			UUID accountId,
			UUID expeditionId,
			AtomicInteger successes) {
		return () -> {
			start.await();
			try {
				expeditionApplicationService.claim(accountId, expeditionId);
				successes.incrementAndGet();
			}
			catch (ApiException expected) {
				assertThat(expected.getCode()).isEqualTo("EXPEDITION_ALREADY_CLAIMED");
			}
			return null;
		};
	}

	private void fillInventoryWithNonStackableItems(UUID characterId) {
		UUID ironSword = jdbcTemplate.queryForObject(
				"select id from item_definitions where code = 'IRON_SWORD'",
				UUID.class);
		for (int index = 0; index < 40; index++) {
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

	@Test
	void foreignExpeditionClaimIsNotFound() throws Exception {
		MockHttpSession owner = registerWithCharacter("exp-owner-" + System.nanoTime() + "@greyhaven.test");
		moveToTavern(owner);
		MvcResult started = mockMvc.perform(withCsrf(post("/api/v1/expeditions"))
						.session(owner)
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"strategy\":\"BALANCED\"}"))
				.andExpect(status().isOk())
				.andReturn();
		String expeditionId = JsonPath.read(started.getResponse().getContentAsString(), "$.id");

		MockHttpSession other = registerWithCharacter("exp-other-" + System.nanoTime() + "@greyhaven.test");
		mockMvc.perform(withCsrf(post("/api/v1/expeditions/" + expeditionId + "/claim")).session(other))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.code").value("EXPEDITION_NOT_FOUND"));
	}

	private int intColumn(String sql, Object argument) {
		return jdbcTemplate.queryForObject(sql, Integer.class, argument);
	}

	private void moveToTavern(MockHttpSession session) throws Exception {
		moveTo(session, "MARKET");
		moveTo(session, "TAVERN");
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

	private UUID findEncounter(MockHttpSession session) throws Exception {
		mutableRandomProvider.queue(1);
		MvcResult search = mockMvc.perform(withCsrf(post("/api/v1/encounters/search")).session(session))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.found").value(true))
				.andReturn();
		return UUID.fromString(JsonPath.read(search.getResponse().getContentAsString(), "$.encounterId"));
	}

	private void assertStartRejected(MockHttpSession session, String expectedCode) throws Exception {
		mockMvc.perform(withCsrf(post("/api/v1/expeditions"))
						.session(session)
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"strategy\":\"BALANCED\"}"))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.code").value(expectedCode));
	}

	private UUID locationId(String code) {
		return jdbcTemplate.queryForObject(
				"select id from locations where code = ?",
				UUID.class,
				code);
	}

	private UUID accountIdForEmail(String email) {
		return jdbcTemplate.queryForObject(
				"select id from accounts where email = ?",
				UUID.class,
				email);
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

	private MockHttpSession registerWithCharacter(String email) throws Exception {
		MockHttpSession session = new MockHttpSession();
		mockMvc.perform(withCsrf(post("/api/v1/auth/register"))
						.session(session)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"email":"%s","password":"%s"}
								""".formatted(email, "password-123")))
				.andExpect(status().isCreated());

		String name = "E" + UUID.randomUUID().toString().replace("-", "").substring(0, 10);
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
	static class RandomAndClockTestConfig {

		@Bean
		@Primary
		RandomProvider mutableTestRandomProvider() {
			return new MutableRandomProvider();
		}

		@Bean
		@Primary
		Clock mutableTestClock() {
			return new MutableClock(START);
		}
	}
}
