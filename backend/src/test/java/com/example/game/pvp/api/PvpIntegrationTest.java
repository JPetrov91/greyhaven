package com.example.game.pvp.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
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
import com.example.game.shared.domain.MutableClock;
import com.example.game.shared.domain.MutableRandomProvider;
import com.example.game.shared.domain.RandomProvider;
import com.jayway.jsonpath.JsonPath;

import jakarta.servlet.http.Cookie;

@Import({ TestcontainersConfiguration.class, PvpIntegrationTest.TestConfig.class })
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class PvpIntegrationTest {

	private static final java.time.Instant START = java.time.Instant.parse("2026-08-15T00:00:00Z");

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Autowired
	private RandomProvider randomProvider;

	@Autowired
	private java.time.Clock clock;

	private MutableRandomProvider mutableRandomProvider;
	private MutableClock mutableClock;
	private Cookie csrfCookie;

	@BeforeEach
	void setUp() throws Exception {
		mutableRandomProvider = (MutableRandomProvider) randomProvider;
		mutableRandomProvider.clear();
		mutableClock = (MutableClock) clock;
		mutableClock.setInstant(START);
		MvcResult bootstrap = mockMvc.perform(get("/api/v1/bootstrap")).andExpect(status().isOk()).andReturn();
		csrfCookie = bootstrap.getResponse().getCookie("XSRF-TOKEN");
		assertThat(csrfCookie).isNotNull();
	}

	@Test
	void rankedChallengeRejectedBelowLevelEleven() throws Exception {
		MockHttpSession attackerSession = registerWithCharacter("pvp-low-" + System.nanoTime() + "@greyhaven.test");
		MockHttpSession defenderSession = registerWithCharacter("pvp-low-d-" + System.nanoTime() + "@greyhaven.test");
		UUID defenderId = characterId(defenderSession);
		UUID arenaId = jdbcTemplate.queryForObject("select id from locations where code = 'ARENA'", UUID.class);
		mockMvc.perform(withCsrf(post("/api/v1/world/move"))
						.session(attackerSession)
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"destinationLocationId\":\"" + arenaId + "\"}"))
				.andExpect(status().isOk());
		mockMvc.perform(withCsrf(post("/api/v1/pvp/arena/challenges"))
						.session(attackerSession)
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"defenderId\":\"" + defenderId + "\"}"))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.code").value("ARENA_LEVEL_REQUIRED"));
	}

	@Test
	void inspectRedactsPrivateFieldsSelfChallengeIsRejectedAndOfflineDefenderIsEnough() throws Exception {
		MockHttpSession attackerSession = registerWithCharacter("pvp-a-" + System.nanoTime() + "@greyhaven.test");
		MockHttpSession defenderSession = registerWithCharacter("pvp-d-" + System.nanoTime() + "@greyhaven.test");
		UUID attackerId = characterId(attackerSession);
		UUID defenderId = characterId(defenderSession);
		moveToArena(attackerSession);

		mockMvc.perform(get("/api/v1/pvp/arena").session(attackerSession))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.rating").value(1000))
				.andExpect(jsonPath("$.marks").value(0));

		mockMvc.perform(get("/api/v1/pvp/arena/opponents").session(attackerSession))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.opponents[*].id").value(org.hamcrest.Matchers.hasItem(defenderId.toString())))
				.andExpect(jsonPath("$.page").value(0));

		mockMvc.perform(get("/api/v1/characters/" + defenderId + "/public").session(attackerSession))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.name").exists())
				.andExpect(jsonPath("$.arenaRating").value(1000))
				.andExpect(jsonPath("$.equipment[?(@.slot=='MAIN_HAND')].code").value(org.hamcrest.Matchers.hasItem("RUSTY_SWORD")))
				.andExpect(jsonPath("$.gold").doesNotExist())
				.andExpect(jsonPath("$.accountId").doesNotExist())
				.andExpect(jsonPath("$.currentHealth").doesNotExist())
				.andExpect(jsonPath("$.arenaMarks").doesNotExist());

		mockMvc.perform(withCsrf(post("/api/v1/pvp/arena/challenges"))
						.session(attackerSession)
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"defenderId\":\"" + attackerId + "\"}"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.code").value("SELF_CHALLENGE"));

		MvcResult started = mockMvc.perform(withCsrf(post("/api/v1/pvp/arena/challenges"))
						.session(attackerSession)
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"defenderId\":\"" + defenderId + "\"}"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value("ACTIVE"))
				.andExpect(jsonPath("$.defenderIntent").value(org.hamcrest.Matchers.nullValue()))
				.andReturn();
		UUID matchId = UUID.fromString(JsonPath.read(started.getResponse().getContentAsString(), "$.id"));

		mockMvc.perform(withCsrf(post("/api/v1/pvp/arena/matches/" + matchId + "/actions"))
						.session(defenderSession)
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"action\":\"QUICK_ATTACK\",\"expectedRoundNumber\":0}"))
				.andExpect(status().isNotFound());
	}

	@Test
	void arenaSnapshotIgnoresDefenderEquipmentChangeDuringActiveMatch() throws Exception {
		MockHttpSession attackerSession = registerWithCharacter("pvp-eq-a-" + System.nanoTime() + "@greyhaven.test");
		MockHttpSession defenderSession = registerWithCharacter("pvp-eq-d-" + System.nanoTime() + "@greyhaven.test");
		UUID attackerId = characterId(attackerSession);
		UUID defenderId = characterId(defenderSession);
		moveToArena(attackerSession);

		MvcResult started = mockMvc.perform(withCsrf(post("/api/v1/pvp/arena/challenges"))
						.session(attackerSession)
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"defenderId\":\"" + defenderId + "\"}"))
				.andExpect(status().isOk())
				.andReturn();
		UUID matchId = UUID.fromString(JsonPath.read(started.getResponse().getContentAsString(), "$.id"));
		int defenderMax = JsonPath.read(started.getResponse().getContentAsString(), "$.defenderMaxHealth");
		String snapshotBefore = jdbcTemplate.queryForObject(
				"select payload from pvp_match_snapshots where match_id = ?", String.class, matchId);

		UUID defenderWeapon = jdbcTemplate.queryForObject(
				"""
						select i.id from item_instances i
						join item_definitions d on d.id = i.item_definition_id
						where i.owner_character_id = ? and d.code = 'RUSTY_SWORD'
						""",
				UUID.class,
				defenderId);
		mockMvc.perform(withCsrf(post("/api/v1/inventory/" + defenderWeapon + "/unequip")).session(defenderSession))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.equipment.slots.MAIN_HAND").value(org.hamcrest.Matchers.nullValue()));

		UUID attackerWeapon = jdbcTemplate.queryForObject(
				"""
						select i.id from item_instances i
						join item_definitions d on d.id = i.item_definition_id
						where i.owner_character_id = ? and d.code = 'RUSTY_SWORD'
						""",
				UUID.class,
				attackerId);
		mockMvc.perform(withCsrf(post("/api/v1/inventory/" + attackerWeapon + "/unequip")).session(attackerSession))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.code").value("COMBAT_IN_PROGRESS"));

		String snapshotAfter = jdbcTemplate.queryForObject(
				"select payload from pvp_match_snapshots where match_id = ?", String.class, matchId);
		assertThat(snapshotAfter).isEqualTo(snapshotBefore);
		assertThat(snapshotAfter).contains("RUSTY_SWORD");

		mockMvc.perform(get("/api/v1/characters/" + defenderId + "/public").session(attackerSession))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.equipment[?(@.slot=='MAIN_HAND')]").isEmpty());

		mutableRandomProvider.clear();
		mutableRandomProvider.queue(1, 90, 90, 1, 90, 90);
		mockMvc.perform(withCsrf(post("/api/v1/pvp/arena/matches/" + matchId + "/actions"))
						.session(attackerSession)
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"action\":\"QUICK_ATTACK\",\"expectedRoundNumber\":0}"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.defenderMaxHealth").value(defenderMax));
	}

	@Test
	void arenaRewardsAndRatingApplyOnceIncludingConcurrentCompletion() throws Exception {
		MockHttpSession attackerSession = registerWithCharacter("pvp-w-" + System.nanoTime() + "@greyhaven.test");
		MockHttpSession defenderSession = registerWithCharacter("pvp-l-" + System.nanoTime() + "@greyhaven.test");
		UUID attackerId = characterId(attackerSession);
		UUID defenderId = characterId(defenderSession);
		moveToArena(attackerSession);

		MvcResult started = mockMvc.perform(withCsrf(post("/api/v1/pvp/arena/challenges"))
						.session(attackerSession)
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"defenderId\":\"" + defenderId + "\"}"))
				.andExpect(status().isOk())
				.andReturn();
		UUID matchId = UUID.fromString(JsonPath.read(started.getResponse().getContentAsString(), "$.id"));
		jdbcTemplate.update("update pvp_matches set defender_health = 1 where id = ?", matchId);
		mutableRandomProvider.clear();
		mutableRandomProvider.queue(1, 90, 90, 1, 90);

		ExecutorService executor = Executors.newFixedThreadPool(2);
		CountDownLatch ready = new CountDownLatch(2);
		CountDownLatch go = new CountDownLatch(1);
		AtomicInteger okResponses = new AtomicInteger();
		try {
			Runnable post = () -> {
				ready.countDown();
				try {
					if (!go.await(5, TimeUnit.SECONDS)) {
						return;
					}
					MvcResult result = mockMvc.perform(withCsrf(post("/api/v1/pvp/arena/matches/" + matchId + "/actions"))
									.session(attackerSession)
									.contentType(MediaType.APPLICATION_JSON)
									.content("{\"action\":\"QUICK_ATTACK\",\"expectedRoundNumber\":0}"))
							.andReturn();
					if (result.getResponse().getStatus() == 200) {
						okResponses.incrementAndGet();
					}
				}
				catch (Exception ignored) {
					// One racer may lose the lock race or see a stale round.
				}
			};
			Future<?> first = executor.submit(post);
			Future<?> second = executor.submit(post);
			assertThat(ready.await(5, TimeUnit.SECONDS)).isTrue();
			go.countDown();
			first.get(10, TimeUnit.SECONDS);
			second.get(10, TimeUnit.SECONDS);
		}
		finally {
			executor.shutdownNow();
		}

		Integer applied = jdbcTemplate.queryForObject(
				"select count(*) from pvp_matches where id = ? and settlement_applied = true",
				Integer.class,
				matchId);
		assertThat(applied).isEqualTo(1);
		Integer attackerRating = jdbcTemplate.queryForObject(
				"select arena_rating from characters where id = ?", Integer.class, attackerId);
		Integer defenderRating = jdbcTemplate.queryForObject(
				"select arena_rating from characters where id = ?", Integer.class, defenderId);
		assertThat(attackerRating).isGreaterThan(1000);
		assertThat(defenderRating).isLessThan(1000);
		Integer marks = jdbcTemplate.queryForObject(
				"select arena_marks from characters where id = ?", Integer.class, attackerId);
		assertThat(marks).isEqualTo(8);

		mockMvc.perform(withCsrf(post("/api/v1/pvp/arena/matches/" + matchId + "/actions"))
						.session(attackerSession)
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"action\":\"QUICK_ATTACK\",\"expectedRoundNumber\":1}"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.settlement.applied").value(true));
		Integer marksAfter = jdbcTemplate.queryForObject(
				"select arena_marks from characters where id = ?", Integer.class, attackerId);
		assertThat(marksAfter).isEqualTo(marks);
		Integer attackerRatingAfter = jdbcTemplate.queryForObject(
				"select arena_rating from characters where id = ?", Integer.class, attackerId);
		assertThat(attackerRatingAfter).isEqualTo(attackerRating);
		Integer historyRows = jdbcTemplate.queryForObject(
				"select count(*) from pvp_battle_history where match_id = ?", Integer.class, matchId);
		assertThat(historyRows).isEqualTo(2);
	}

	@Test
	void repeatOpponentZeroesRatingAndOfflineDefenderNeverActs() throws Exception {
		MockHttpSession attackerSession = registerWithCharacter("pvp-r-" + System.nanoTime() + "@greyhaven.test");
		MockHttpSession defenderSession = registerWithCharacter("pvp-o-" + System.nanoTime() + "@greyhaven.test");
		UUID defenderId = characterId(defenderSession);
		moveToArena(attackerSession);
		UUID firstMatch = winArena(attackerSession, defenderId);
		mockMvc.perform(withCsrf(post("/api/v1/pvp/arena/matches/" + firstMatch + "/acknowledge")).session(attackerSession))
				.andExpect(status().isOk());
		Integer ratingAfterFirst = jdbcTemplate.queryForObject(
				"select arena_rating from characters where id = ?", Integer.class, characterId(attackerSession));

		MvcResult second = mockMvc.perform(withCsrf(post("/api/v1/pvp/arena/challenges"))
						.session(attackerSession)
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"defenderId\":\"" + defenderId + "\"}"))
				.andExpect(status().isOk())
				.andReturn();
		UUID matchId = UUID.fromString(JsonPath.read(second.getResponse().getContentAsString(), "$.id"));
		jdbcTemplate.update("update pvp_matches set defender_health = 1 where id = ?", matchId);
		mutableRandomProvider.clear();
		mutableRandomProvider.queue(1, 90, 90);
		mockMvc.perform(withCsrf(post("/api/v1/pvp/arena/matches/" + matchId + "/actions"))
						.session(attackerSession)
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"action\":\"QUICK_ATTACK\",\"expectedRoundNumber\":0}"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value("ATTACKER_WON"))
				.andExpect(jsonPath("$.settlement.attackerRatingDelta").value(0));
		Integer ratingAfterSecond = jdbcTemplate.queryForObject(
				"select arena_rating from characters where id = ?", Integer.class, characterId(attackerSession));
		assertThat(ratingAfterSecond).isEqualTo(ratingAfterFirst);
		Integer marksAfterSecond = jdbcTemplate.queryForObject(
				"select arena_marks from characters where id = ?", Integer.class, characterId(attackerSession));
		assertThat(marksAfterSecond).isEqualTo(8);
	}

	@Test
	void duelResolvesWhenBothActAndExpiresWithoutRanking() throws Exception {
		MockHttpSession challenger = registerWithCharacter("duel-a-" + System.nanoTime() + "@greyhaven.test");
		MockHttpSession opponent = registerWithCharacter("duel-b-" + System.nanoTime() + "@greyhaven.test");
		UUID defenderId = characterId(opponent);
		moveToSparringYard(challenger);
		moveToSparringYard(opponent);
		MvcResult created = mockMvc.perform(withCsrf(post("/api/v1/pvp/duels"))
						.session(challenger)
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"defenderId\":\"" + defenderId + "\"}"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value("PENDING"))
				.andReturn();
		UUID matchId = UUID.fromString(JsonPath.read(created.getResponse().getContentAsString(), "$.id"));
		mockMvc.perform(withCsrf(post("/api/v1/pvp/duels/" + matchId + "/accept")).session(opponent))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value("ACTIVE"));
		mutableRandomProvider.clear();
		mutableRandomProvider.queue(1, 90, 1, 90, 90);
		mockMvc.perform(withCsrf(post("/api/v1/pvp/duels/" + matchId + "/actions"))
						.session(challenger)
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"action\":\"QUICK_ATTACK\",\"expectedRoundNumber\":0}"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.waitingForOpponent").value(true));
		mockMvc.perform(withCsrf(post("/api/v1/pvp/duels/" + matchId + "/actions"))
						.session(opponent)
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"action\":\"DEFEND\",\"expectedRoundNumber\":0}"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.roundNumber").value(1));

		MockHttpSession c2 = registerWithCharacter("duel-c-" + System.nanoTime() + "@greyhaven.test");
		MockHttpSession d2 = registerWithCharacter("duel-e-" + System.nanoTime() + "@greyhaven.test");
		moveToSparringYard(c2);
		moveToSparringYard(d2);
		UUID d2Id = characterId(d2);
		MvcResult pending = mockMvc.perform(withCsrf(post("/api/v1/pvp/duels"))
						.session(c2)
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"defenderId\":\"" + d2Id + "\"}"))
				.andExpect(status().isOk())
				.andReturn();
		UUID pendingId = UUID.fromString(JsonPath.read(pending.getResponse().getContentAsString(), "$.id"));
		mutableClock.advanceSeconds(11 * 60);
		mockMvc.perform(get("/api/v1/pvp/duels/current").session(c2))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(pendingId.toString()))
				.andExpect(jsonPath("$.status").value("EXPIRED"))
				.andExpect(jsonPath("$.settlement.attackerRatingDelta").value(0));

		mutableClock.setInstant(START);
		MockHttpSession c3 = registerWithCharacter("duel-t-" + System.nanoTime() + "@greyhaven.test");
		MockHttpSession d3 = registerWithCharacter("duel-u-" + System.nanoTime() + "@greyhaven.test");
		moveToSparringYard(c3);
		moveToSparringYard(d3);
		UUID d3Id = characterId(d3);
		MvcResult live = mockMvc.perform(withCsrf(post("/api/v1/pvp/duels"))
						.session(c3)
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"defenderId\":\"" + d3Id + "\"}"))
				.andExpect(status().isOk())
				.andReturn();
		UUID liveId = UUID.fromString(JsonPath.read(live.getResponse().getContentAsString(), "$.id"));
		mockMvc.perform(withCsrf(post("/api/v1/pvp/duels/" + liveId + "/accept")).session(d3))
				.andExpect(status().isOk());
		mutableRandomProvider.clear();
		mutableRandomProvider.queue(1, 90, 90, 1, 90, 90);
		mockMvc.perform(withCsrf(post("/api/v1/pvp/duels/" + liveId + "/actions"))
						.session(c3)
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"action\":\"QUICK_ATTACK\",\"expectedRoundNumber\":0}"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.waitingForOpponent").value(true));
		mutableClock.advanceSeconds(11 * 60);
		mockMvc.perform(get("/api/v1/pvp/duels/current").session(c3))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value("ACTIVE"))
				.andExpect(jsonPath("$.roundNumber").value(1));
	}

	@Test
	void defenseConfigurationRejectsUnknownTechnique() throws Exception {
		MockHttpSession session = registerWithCharacter("pvp-def-" + System.nanoTime() + "@greyhaven.test");
		moveToArena(session);
		mockMvc.perform(withCsrf(put("/api/v1/pvp/arena/defense"))
						.session(session)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"preferredAction":"USE_TECHNIQUE","preferredTechniqueCode":"NOPE",
								"healWhenHpPercentBelow":40,"defendWhenStaminaPercentBelow":25,
								"finisherWhenEnemyHpPercentBelow":35}
								"""))
				.andExpect(status().isBadRequest());
	}

	@Test
	void delayedSettlementAppliesRatingDeltaNotAbsoluteOverwrite() throws Exception {
		MockHttpSession attackerSession = registerWithCharacter("pvp-delta-a-" + System.nanoTime() + "@greyhaven.test");
		MockHttpSession firstDefender = registerWithCharacter("pvp-delta-d1-" + System.nanoTime() + "@greyhaven.test");
		MockHttpSession secondDefender = registerWithCharacter("pvp-delta-d2-" + System.nanoTime() + "@greyhaven.test");
		UUID attackerId = characterId(attackerSession);
		moveToArena(attackerSession);
		UUID matchA = winArena(attackerSession, characterId(firstDefender));
		mockMvc.perform(withCsrf(post("/api/v1/pvp/arena/matches/" + matchA + "/acknowledge")).session(attackerSession))
				.andExpect(status().isOk());
		jdbcTemplate.update("delete from pvp_battle_history where match_id = ?", matchA);
		jdbcTemplate.update("update pvp_matches set settlement_applied = false where id = ?", matchA);

		UUID matchB = winArena(attackerSession, characterId(secondDefender));
		mockMvc.perform(withCsrf(post("/api/v1/pvp/arena/matches/" + matchB + "/acknowledge")).session(attackerSession))
				.andExpect(status().isOk());

		mockMvc.perform(withCsrf(post("/api/v1/pvp/arena/matches/" + matchA + "/actions"))
						.session(attackerSession)
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"action\":\"QUICK_ATTACK\",\"expectedRoundNumber\":1}"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.settlement.applied").value(true));

		Integer rating = jdbcTemplate.queryForObject(
				"select arena_rating from characters where id = ?", Integer.class, attackerId);
		assertThat(rating).isEqualTo(1036);
	}

	@Test
	void concurrentDefensesStackRatingDeltas() throws Exception {
		MockHttpSession attackerOne = registerWithCharacter("pvp-c1-" + System.nanoTime() + "@greyhaven.test");
		MockHttpSession attackerTwo = registerWithCharacter("pvp-c2-" + System.nanoTime() + "@greyhaven.test");
		MockHttpSession defenderSession = registerWithCharacter("pvp-cd-" + System.nanoTime() + "@greyhaven.test");
		UUID defenderId = characterId(defenderSession);
		moveToArena(attackerOne);
		moveToArena(attackerTwo);
		winArena(attackerOne, defenderId);
		winArena(attackerTwo, defenderId);
		Integer defenderRating = jdbcTemplate.queryForObject(
				"select arena_rating from characters where id = ?", Integer.class, defenderId);
		assertThat(defenderRating).isEqualTo(976);
	}

	@Test
	void forfeitAwardsNoMarksAndReconnectSettlesBeforeAck() throws Exception {
		MockHttpSession attackerSession = registerWithCharacter("pvp-f-a-" + System.nanoTime() + "@greyhaven.test");
		MockHttpSession defenderSession = registerWithCharacter("pvp-f-d-" + System.nanoTime() + "@greyhaven.test");
		UUID attackerId = characterId(attackerSession);
		UUID defenderId = characterId(defenderSession);
		moveToArena(attackerSession);
		MvcResult started = mockMvc.perform(withCsrf(post("/api/v1/pvp/arena/challenges"))
						.session(attackerSession)
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"defenderId\":\"" + defenderId + "\"}"))
				.andExpect(status().isOk())
				.andReturn();
		UUID matchId = UUID.fromString(JsonPath.read(started.getResponse().getContentAsString(), "$.id"));
		mockMvc.perform(withCsrf(post("/api/v1/pvp/arena/matches/" + matchId + "/actions"))
						.session(attackerSession)
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"action\":\"RETREAT\",\"expectedRoundNumber\":0}"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value("ATTACKER_FORFEIT"))
				.andExpect(jsonPath("$.settlement.attackerMarks").value(0))
				.andExpect(jsonPath("$.settlement.defenderMarks").value(0));
		Integer attackerMarks = jdbcTemplate.queryForObject(
				"select arena_marks from characters where id = ?", Integer.class, attackerId);
		Integer defenderMarks = jdbcTemplate.queryForObject(
				"select arena_marks from characters where id = ?", Integer.class, defenderId);
		assertThat(attackerMarks).isZero();
		assertThat(defenderMarks).isZero();
		Integer attackerRating = jdbcTemplate.queryForObject(
				"select arena_rating from characters where id = ?", Integer.class, attackerId);
		assertThat(attackerRating).isLessThan(1000);

		jdbcTemplate.update("update pvp_matches set settlement_applied = false where id = ?", matchId);
		jdbcTemplate.update("delete from pvp_battle_history where match_id = ?", matchId);
		jdbcTemplate.update("update characters set arena_rating = 1000, arena_marks = 0 where id in (?, ?)", attackerId, defenderId);
		mockMvc.perform(get("/api/v1/pvp/arena/matches/current").session(attackerSession))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.settlement.applied").value(true));
		Integer ratingAfterReconnect = jdbcTemplate.queryForObject(
				"select arena_rating from characters where id = ?", Integer.class, attackerId);
		assertThat(ratingAfterReconnect).isLessThan(1000);
	}

	@Test
	void challengeRejectsOutOfBandOpponentAndDailyLimit() throws Exception {
		MockHttpSession attackerSession = registerWithCharacter("pvp-band-a-" + System.nanoTime() + "@greyhaven.test");
		MockHttpSession defenderSession = registerWithCharacter("pvp-band-d-" + System.nanoTime() + "@greyhaven.test");
		UUID attackerId = characterId(attackerSession);
		UUID defenderId = characterId(defenderSession);
		moveToArena(attackerSession);
		jdbcTemplate.update("update characters set arena_rating = 1500 where id = ?", defenderId);
		mockMvc.perform(withCsrf(post("/api/v1/pvp/arena/challenges"))
						.session(attackerSession)
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"defenderId\":\"" + defenderId + "\"}"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.code").value("OPPONENT_OUT_OF_RANGE"));

		jdbcTemplate.update("update characters set arena_rating = 1000 where id = ?", defenderId);
		for (int i = 0; i < 20; i++) {
			jdbcTemplate.update(
					"""
							insert into pvp_matches (
								id, match_kind, status, attacker_id, defender_id, round_number,
								attacker_health, attacker_stamina, defender_health, defender_stamina,
								attacker_potion_charges, defender_potion_charges, created_at, updated_at)
							values (?, 'ARENA', 'ATTACKER_WON', ?, ?, 1, 1, 1, 1, 1, 0, 0, ?, ?)
							""",
					UUID.randomUUID(),
					attackerId,
					defenderId,
					java.time.OffsetDateTime.parse("2026-08-15T00:00:00Z"),
					java.time.OffsetDateTime.parse("2026-08-15T00:00:00Z"));
		}
		mockMvc.perform(withCsrf(post("/api/v1/pvp/arena/challenges"))
						.session(attackerSession)
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"defenderId\":\"" + defenderId + "\"}"))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.code").value("ARENA_CHALLENGE_LIMIT"));
	}

	@Test
	void arenaConsumesSnapshotPotionsAndBlocksDuelDuringArena() throws Exception {
		MockHttpSession attackerSession = registerWithCharacter("pvp-pot-a-" + System.nanoTime() + "@greyhaven.test");
		MockHttpSession defenderSession = registerWithCharacter("pvp-pot-d-" + System.nanoTime() + "@greyhaven.test");
		UUID attackerId = characterId(attackerSession);
		UUID defenderId = characterId(defenderSession);
		moveToArena(attackerSession);
		Integer potionsBefore = jdbcTemplate.queryForObject(
				"""
						select coalesce(sum(i.quantity), 0) from item_instances i
						join item_definitions d on d.id = i.item_definition_id
						where i.owner_character_id = ? and d.code = 'HEALING_POTION'
						""",
				Integer.class,
				attackerId);
		assertThat(potionsBefore).isGreaterThan(0);
		mockMvc.perform(withCsrf(post("/api/v1/pvp/arena/challenges"))
						.session(attackerSession)
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"defenderId\":\"" + defenderId + "\"}"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.potionAvailable").value(true));
		Integer potionsAfter = jdbcTemplate.queryForObject(
				"""
						select coalesce(sum(i.quantity), 0) from item_instances i
						join item_definitions d on d.id = i.item_definition_id
						where i.owner_character_id = ? and d.code = 'HEALING_POTION'
						""",
				Integer.class,
				attackerId);
		assertThat(potionsAfter).isZero();

		moveToSparringYard(defenderSession);
		mockMvc.perform(withCsrf(post("/api/v1/pvp/duels"))
						.session(attackerSession)
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"defenderId\":\"" + defenderId + "\"}"))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.code").value("NOT_AT_SPARRING_YARD"));
	}

	private UUID winArena(MockHttpSession attackerSession, UUID defenderId) throws Exception {
		MvcResult started = mockMvc.perform(withCsrf(post("/api/v1/pvp/arena/challenges"))
						.session(attackerSession)
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"defenderId\":\"" + defenderId + "\"}"))
				.andExpect(status().isOk())
				.andReturn();
		UUID matchId = UUID.fromString(JsonPath.read(started.getResponse().getContentAsString(), "$.id"));
		jdbcTemplate.update("update pvp_matches set defender_health = 1 where id = ?", matchId);
		mutableRandomProvider.clear();
		mutableRandomProvider.queue(1, 90, 90);
		mockMvc.perform(withCsrf(post("/api/v1/pvp/arena/matches/" + matchId + "/actions"))
						.session(attackerSession)
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"action\":\"QUICK_ATTACK\",\"expectedRoundNumber\":0}"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value("ATTACKER_WON"));
		return matchId;
	}

	private void moveToArena(MockHttpSession session) throws Exception {
		jdbcTemplate.update("update characters set level = 11 where id = ?", characterId(session));
		UUID arenaId = jdbcTemplate.queryForObject("select id from locations where code = 'ARENA'", UUID.class);
		mockMvc.perform(withCsrf(post("/api/v1/world/move"))
						.session(session)
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"destinationLocationId\":\"" + arenaId + "\"}"))
				.andExpect(status().isOk());
	}

	private void moveToSparringYard(MockHttpSession session) throws Exception {
		UUID yardId = jdbcTemplate.queryForObject("select id from locations where code = 'SPARRING_YARD'", UUID.class);
		mockMvc.perform(withCsrf(post("/api/v1/world/move"))
						.session(session)
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"destinationLocationId\":\"" + yardId + "\"}"))
				.andExpect(status().isOk());
	}

	private UUID characterId(MockHttpSession session) throws Exception {
		MvcResult me = mockMvc.perform(get("/api/v1/character").session(session)).andExpect(status().isOk()).andReturn();
		return UUID.fromString(JsonPath.read(me.getResponse().getContentAsString(), "$.id"));
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
		String name = "P" + UUID.randomUUID().toString().replace("-", "").substring(0, 10);
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
	static class TestConfig {
		@Bean
		@Primary
		RandomProvider mutableTestRandomProvider() {
			return new MutableRandomProvider();
		}

		@Bean
		@Primary
		java.time.Clock mutableTestClock() {
			return new MutableClock(START);
		}
	}
}
