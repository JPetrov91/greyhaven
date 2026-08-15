package com.example.game.character.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Clock;
import java.time.Instant;
import java.sql.Timestamp;
import java.util.UUID;

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
import com.example.game.character.domain.CharacterBalance;
import com.example.game.character.domain.ProgressionBalance;
import com.example.game.shared.domain.MutableClock;
import com.example.game.shared.domain.MutableRandomProvider;
import com.example.game.shared.domain.RandomProvider;
import com.jayway.jsonpath.JsonPath;

import jakarta.servlet.http.Cookie;

@Import({
		TestcontainersConfiguration.class,
		CharacterProgressionIntegrationTest.RandomAndClockTestConfig.class
})
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class CharacterProgressionIntegrationTest {

	private static final Instant START = Instant.parse("2026-08-14T08:00:00Z");

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Autowired
	private Clock clock;

	@Autowired
	private RandomProvider randomProvider;

	private MutableClock mutableClock;
	private MutableRandomProvider mutableRandomProvider;
	private Cookie csrfCookie;

	@BeforeEach
	void setUp() throws Exception {
		mutableClock = (MutableClock) clock;
		mutableClock.setInstant(START);
		mutableRandomProvider = (MutableRandomProvider) randomProvider;
		mutableRandomProvider.clear();
		MvcResult bootstrap = mockMvc.perform(get("/api/v1/bootstrap"))
				.andExpect(status().isOk())
				.andReturn();
		csrfCookie = bootstrap.getResponse().getCookie("XSRF-TOKEN");
		assertThat(csrfCookie).isNotNull();
	}

	@Test
	void flywayAddsRecoveryBaseline() {
		Integer recoveryColumn = jdbcTemplate.queryForObject(
				"""
						select count(*) from information_schema.columns
						where table_schema = 'public' and table_name = 'characters' and column_name = 'last_recovery_at'
						""",
				Integer.class);
		Integer levelMax = jdbcTemplate.queryForObject(
				"""
						select count(*) from pg_constraint
						where conname = 'chk_characters_level'
						""",
				Integer.class);
		assertThat(recoveryColumn).isEqualTo(1);
		assertThat(levelMax).isEqualTo(1);
	}

	@Test
	void newCharacterProgressionUsesServerXpTable() throws Exception {
		MockHttpSession session = registerWithCharacter("prog-new-" + System.nanoTime() + "@greyhaven.test");

		mockMvc.perform(get("/api/v1/character").session(session))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.level").value(1))
				.andExpect(jsonPath("$.experience").value(0))
				.andExpect(jsonPath("$.maxHealth").value(165))
				.andExpect(jsonPath("$.maxStamina").value(85))
				.andExpect(jsonPath("$.progression.totalExperience").value(0))
				.andExpect(jsonPath("$.progression.experienceIntoCurrentLevel").value(0))
				.andExpect(jsonPath("$.progression.experienceRequiredForNextLevel").value(100))
				.andExpect(jsonPath("$.progression.experienceRemaining").value(100))
				.andExpect(jsonPath("$.progression.progressPercent").value(0.0))
				.andExpect(jsonPath("$.progression.maxLevel").value(false));
	}

	@Test
	void legacyExperienceCatchUpAwardsPendingLevels() throws Exception {
		String email = "prog-legacy-" + System.nanoTime() + "@greyhaven.test";
		MockHttpSession session = registerWithCharacter(email);
		UUID characterId = characterIdForEmail(email);
		jdbcTemplate.update(
				"""
						update characters
						set level = 5, experience = 1600, unspent_attribute_points = 0
						where id = ?
						""",
				characterId);

		mockMvc.perform(get("/api/v1/character").session(session))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.level").value(6))
				.andExpect(jsonPath("$.experience").value(1600))
				.andExpect(jsonPath("$.unspentAttributePoints").value(2))
				.andExpect(jsonPath("$.progression.experienceIntoCurrentLevel").value(90));
	}

	@Test
	void invalidAllocationIsRejected() throws Exception {
		MockHttpSession session = registerWithCharacter("prog-alloc-" + System.nanoTime() + "@greyhaven.test");

		mockMvc.perform(withCsrf(post("/api/v1/character/attributes"))
						.session(session)
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"strength\":1,\"agility\":0,\"endurance\":0,\"perception\":0}"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.code").value("INVALID_ATTRIBUTE_ALLOCATION"));
	}

	@Test
	void respecRefundsAllocatedPointsForFreeAtLowLevel() throws Exception {
		String email = "prog-respec-" + System.nanoTime() + "@greyhaven.test";
		MockHttpSession session = registerWithCharacter(email);
		UUID characterId = characterIdForEmail(email);
		jdbcTemplate.update(
				"update characters set unspent_attribute_points = 2, gold = 0 where id = ?",
				characterId);

		mockMvc.perform(withCsrf(post("/api/v1/character/attributes"))
						.session(session)
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"strength\":2,\"agility\":0,\"endurance\":0,\"perception\":0}"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.strength").value(7));

		mockMvc.perform(withCsrf(post("/api/v1/character/respec")).session(session))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.strength").value(5))
				.andExpect(jsonPath("$.agility").value(5))
				.andExpect(jsonPath("$.endurance").value(5))
				.andExpect(jsonPath("$.perception").value(5))
				.andExpect(jsonPath("$.unspentAttributePoints").value(2))
				.andExpect(jsonPath("$.gold").value(0));
	}

	@Test
	void respecRequiresGoldAfterLevelTen() throws Exception {
		String email = "prog-respec-gold-" + System.nanoTime() + "@greyhaven.test";
		MockHttpSession session = registerWithCharacter(email);
		UUID characterId = characterIdForEmail(email);
		jdbcTemplate.update(
				"update characters set level = 11, gold = 0 where id = ?",
				characterId);

		mockMvc.perform(withCsrf(post("/api/v1/character/respec")).session(session))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.code").value("INSUFFICIENT_GOLD"));

		int cost = ProgressionBalance.respecGoldCost(11);
		jdbcTemplate.update("update characters set gold = ? where id = ?", cost, characterId);

		mockMvc.perform(withCsrf(post("/api/v1/character/respec")).session(session))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.gold").value(0));

		Integer destroyed = jdbcTemplate.queryForObject(
				"""
						select count(*) from game_telemetry_events
						where character_id = ? and event_type = 'GOLD_DESTROYED'
						and payload ->> 'reason' = 'RESPEC'
						""",
				Integer.class,
				characterId);
		assertThat(destroyed).isEqualTo(1);
	}

	@Test
	void injuredCharacterRecoversOverElapsedTimeAndDoesNotDoubleOnRepeatGet() throws Exception {
		String email = "prog-recover-" + System.nanoTime() + "@greyhaven.test";
		MockHttpSession session = registerWithCharacter(email);
		UUID characterId = characterIdForEmail(email);
		jdbcTemplate.update(
				"""
						update characters
						set current_health = 50, current_stamina = 10, last_recovery_at = ?
						where id = ?
						""",
				Timestamp.from(START),
				characterId);

		mutableClock.advanceSeconds(60);
		mockMvc.perform(get("/api/v1/character").session(session))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.currentHealth").value(83))
				.andExpect(jsonPath("$.currentStamina").value(44));

		Timestamp recoveryAt = jdbcTemplate.queryForObject(
				"select last_recovery_at from characters where id = ?",
				Timestamp.class,
				characterId);
		Timestamp updatedAt = jdbcTemplate.queryForObject(
				"select updated_at from characters where id = ?",
				Timestamp.class,
				characterId);

		mockMvc.perform(get("/api/v1/character").session(session))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.currentHealth").value(83))
				.andExpect(jsonPath("$.currentStamina").value(44));

		assertThat(jdbcTemplate.queryForObject(
				"select last_recovery_at from characters where id = ?",
				Timestamp.class,
				characterId)).isEqualTo(recoveryAt);
		assertThat(jdbcTemplate.queryForObject(
				"select updated_at from characters where id = ?",
				Timestamp.class,
				characterId)).isEqualTo(updatedAt);
	}

	@Test
	void characterGetDoesNotWriteWhenVitalsAreAlreadyCurrent() throws Exception {
		String email = "prog-get-nowrite-" + System.nanoTime() + "@greyhaven.test";
		MockHttpSession session = registerWithCharacter(email);
		UUID characterId = characterIdForEmail(email);
		Timestamp recoveryAt = jdbcTemplate.queryForObject(
				"select last_recovery_at from characters where id = ?",
				Timestamp.class,
				characterId);
		Timestamp updatedAt = jdbcTemplate.queryForObject(
				"select updated_at from characters where id = ?",
				Timestamp.class,
				characterId);

		mockMvc.perform(get("/api/v1/character").session(session))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.currentHealth").value(165))
				.andExpect(jsonPath("$.maxHealth").value(165));

		assertThat(jdbcTemplate.queryForObject(
				"select last_recovery_at from characters where id = ?",
				Timestamp.class,
				characterId)).isEqualTo(recoveryAt);
		assertThat(jdbcTemplate.queryForObject(
				"select updated_at from characters where id = ?",
				Timestamp.class,
				characterId)).isEqualTo(updatedAt);
	}

	@Test
	void activeCombatPausesPassiveRecoveryUntilCombatEnds() throws Exception {
		String email = "prog-combat-rec-" + System.nanoTime() + "@greyhaven.test";
		MockHttpSession session = registerWithCharacter(email);
		UUID characterId = characterIdForEmail(email);
		moveTo(session, "OLD_TOWN");
		jdbcTemplate.update(
				"""
						update characters
						set current_health = 50, current_stamina = 10, last_recovery_at = ?
						where id = ?
						""",
				Timestamp.from(START),
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

		mutableClock.advanceSeconds(10 * 60);
		mockMvc.perform(get("/api/v1/character").session(session))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.currentHealth").value(50));

		jdbcTemplate.update("update combat_sessions set enemy_health = 1 where id = ?", combatId);
		mutableRandomProvider.queue(5, 90, 5);
		mockMvc.perform(withCsrf(post("/api/v1/combat/" + combatId + "/actions"))
						.session(session)
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"action\":\"QUICK_ATTACK\",\"expectedRoundNumber\":0}"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value("PLAYER_WON"));

		Integer healthAtEnd = jdbcTemplate.queryForObject(
				"select current_health from characters where id = ?",
				Integer.class,
				characterId);
		mockMvc.perform(get("/api/v1/character").session(session))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.currentHealth").value(healthAtEnd));

		mutableClock.advanceSeconds(60);
		int recovered = Math.min(
				CharacterBalance.maxHealth(5, 1),
				healthAtEnd + (int) Math.floor(CharacterBalance.maxHealth(5, 1) * 0.20));
		mockMvc.perform(get("/api/v1/character").session(session))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.currentHealth").value(recovered));
	}

	@Test
	void maxLevelProgressionOmitsNextLevelFields() throws Exception {
		String email = "prog-cap-" + System.nanoTime() + "@greyhaven.test";
		MockHttpSession session = registerWithCharacter(email);
		UUID characterId = characterIdForEmail(email);
		jdbcTemplate.update(
				"""
						update characters
						set level = 30, experience = ?
						where id = ?
						""",
				ProgressionBalance.cumulativeXpForLevel(30),
				characterId);

		mockMvc.perform(get("/api/v1/character").session(session))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.level").value(30))
				.andExpect(jsonPath("$.progression.maxLevel").value(true))
				.andExpect(jsonPath("$.progression.experienceRequiredForNextLevel").value(nullValue()))
				.andExpect(jsonPath("$.progression.experienceRemaining").value(nullValue()))
				.andExpect(jsonPath("$.progression.progressPercent").value(100.0));
	}

	private void moveTo(MockHttpSession session, String locationCode) throws Exception {
		UUID destinationId = jdbcTemplate.queryForObject(
				"select id from locations where code = ?",
				UUID.class,
				locationCode);
		mockMvc.perform(withCsrf(post("/api/v1/world/move"))
						.session(session)
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"destinationLocationId\":\"" + destinationId + "\"}"))
				.andExpect(status().isOk());
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
								{"email":"%s","password":"password123"}
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

	private MockHttpServletRequestBuilder withCsrf(MockHttpServletRequestBuilder request) {
		return request.header("X-XSRF-TOKEN", csrfCookie.getValue()).cookie(csrfCookie);
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
