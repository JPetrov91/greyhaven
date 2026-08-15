package com.example.game.character.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.sql.Timestamp;
import java.time.Clock;
import java.time.Instant;
import java.util.List;
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
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import com.example.game.TestcontainersConfiguration;
import com.example.game.character.application.CharacterApplicationService;
import com.example.game.character.application.CharacterProgressionService;
import com.example.game.character.domain.CharacterBalance;
import com.example.game.shared.api.ApiException;
import com.example.game.shared.domain.MutableClock;
import com.example.game.shared.domain.MutableRandomProvider;
import com.example.game.shared.domain.RandomProvider;
import com.jayway.jsonpath.JsonPath;

import jakarta.servlet.http.Cookie;

/**
 * High-risk character integrity nets: concurrent recovery, combat-locked respec,
 * concurrent attribute spend, and the gold floor constraint.
 */
@Import({
		TestcontainersConfiguration.class,
		CharacterIntegritySafetyNetIntegrationTest.RandomAndClockTestConfig.class
})
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class CharacterIntegritySafetyNetIntegrationTest {

	private static final Instant START = Instant.parse("2026-08-15T08:00:00Z");

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Autowired
	private Clock clock;

	@Autowired
	private RandomProvider randomProvider;

	@Autowired
	private CharacterApplicationService characterApplicationService;

	@Autowired
	private CharacterProgressionService characterProgressionService;

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
	void concurrentRecoveryAppliesElapsedTimeOnce() throws Exception {
		String email = "safe-recover-" + System.nanoTime() + "@greyhaven.test";
		registerWithCharacter(email);
		UUID accountId = accountIdForEmail(email);
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

		CountDownLatch start = new CountDownLatch(1);
		ExecutorService pool = Executors.newFixedThreadPool(2);
		try {
			Future<Integer> first = pool.submit(() -> {
				start.await();
				return characterApplicationService.current(accountId).currentHealth();
			});
			Future<Integer> second = pool.submit(() -> {
				start.await();
				return characterApplicationService.current(accountId).currentHealth();
			});
			start.countDown();
			assertThat(List.of(first.get(20, TimeUnit.SECONDS), second.get(20, TimeUnit.SECONDS)))
					.containsOnly(83);
		}
		finally {
			pool.shutdownNow();
		}

		assertThat(intColumn("select current_health from characters where id = ?", characterId)).isEqualTo(83);
		assertThat(intColumn("select current_stamina from characters where id = ?", characterId)).isEqualTo(44);
	}

	@Test
	void respecIsRejectedDuringActiveCombat() throws Exception {
		String email = "safe-respec-" + System.nanoTime() + "@greyhaven.test";
		MockHttpSession session = registerWithCharacter(email);
		moveTo(session, "OLD_TOWN");
		mutableRandomProvider.queue(1);
		MvcResult search = mockMvc.perform(withCsrf(post("/api/v1/encounters/search")).session(session))
				.andExpect(status().isOk())
				.andReturn();
		UUID encounterId = UUID.fromString(JsonPath.read(search.getResponse().getContentAsString(), "$.encounterId"));
		mockMvc.perform(withCsrf(post("/api/v1/encounters/" + encounterId + "/fight")).session(session))
				.andExpect(status().isOk());

		mockMvc.perform(withCsrf(post("/api/v1/character/respec")).session(session))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.code").value("COMBAT_IN_PROGRESS"));
	}

	@Test
	void concurrentAllocationSpendsUnspentPointsOnce() throws Exception {
		String email = "safe-alloc-" + System.nanoTime() + "@greyhaven.test";
		registerWithCharacter(email);
		UUID accountId = accountIdForEmail(email);
		UUID characterId = characterIdForEmail(email);
		jdbcTemplate.update("update characters set unspent_attribute_points = 2 where id = ?", characterId);

		CountDownLatch start = new CountDownLatch(1);
		AtomicInteger successes = new AtomicInteger();
		ExecutorService pool = Executors.newFixedThreadPool(2);
		try {
			List<Future<?>> attempts = List.of(
					pool.submit(allocateAttempt(start, accountId, successes)),
					pool.submit(allocateAttempt(start, accountId, successes)));
			start.countDown();
			for (Future<?> attempt : attempts) {
				attempt.get(20, TimeUnit.SECONDS);
			}
		}
		finally {
			pool.shutdownNow();
		}

		assertThat(successes.get()).isEqualTo(1);
		assertThat(intColumn("select unspent_attribute_points from characters where id = ?", characterId)).isZero();
		assertThat(intColumn("select strength from characters where id = ?", characterId))
				.isEqualTo(CharacterBalance.STARTING_STRENGTH + 2);
	}

	@Test
	void databaseRejectsNegativeGold() {
		String email = "safe-gold-" + System.nanoTime() + "@greyhaven.test";
		try {
			registerWithCharacter(email);
		}
		catch (Exception exception) {
			throw new IllegalStateException(exception);
		}
		UUID characterId = characterIdForEmail(email);

		assertThatThrownBy(() -> jdbcTemplate.update("update characters set gold = -1 where id = ?", characterId))
				.isInstanceOf(DataIntegrityViolationException.class);
		assertThat(intColumn("select gold from characters where id = ?", characterId))
				.isEqualTo(CharacterBalance.STARTING_GOLD);
	}

	private Callable<Void> allocateAttempt(CountDownLatch start, UUID accountId, AtomicInteger successes) {
		return () -> {
			start.await();
			try {
				characterProgressionService.allocateAttributes(accountId, 2, 0, 0, 0);
				successes.incrementAndGet();
			}
			catch (ApiException expected) {
				assertThat(expected.getCode()).isIn("INVALID_ATTRIBUTE_ALLOCATION", "COMBAT_IN_PROGRESS");
			}
			return null;
		};
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

	private int intColumn(String sql, Object... args) {
		Integer value = jdbcTemplate.queryForObject(sql, Integer.class, args);
		assertThat(value).isNotNull();
		return value;
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
		return jdbcTemplate.queryForObject("select id from accounts where email = ?", UUID.class, email);
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
		String name = "S" + UUID.randomUUID().toString().replace("-", "").substring(0, 10);
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
