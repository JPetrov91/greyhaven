package com.example.game.world.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Duration;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

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
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import com.example.game.TestcontainersConfiguration;
import com.example.game.character.application.CharacterLocationService;
import com.example.game.shared.api.ApiException;
import com.example.game.world.application.WorldApplicationService;

import jakarta.servlet.http.Cookie;

/**
 * Movement is a read-modify-write of the character row, so it takes a pessimistic lock. Without
 * that lock a second request can validate against a location the character has already left, and
 * its write silently discards the first move.
 */
@Import(TestcontainersConfiguration.class)
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class WorldMovementConcurrencyIntegrationTest {

	private static final UUID CITY_SQUARE_ID = UUID.fromString("a0000000-0000-4000-8000-000000000001");
	private static final UUID MARKET_ID = UUID.fromString("a0000000-0000-4000-8000-000000000003");
	private static final UUID FOREST_ID = UUID.fromString("a0000000-0000-4000-8000-000000000005");

	private static final Duration BLOCKED_OBSERVATION_WINDOW = Duration.ofMillis(300);
	private static final Duration TASK_TIMEOUT = Duration.ofSeconds(30);

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Autowired
	private WorldApplicationService worldApplicationService;

	@Autowired
	private CharacterLocationService characterLocationService;

	@Autowired
	private PlatformTransactionManager transactionManager;

	@Test
	void aMoveInFlightCannotValidateAgainstAnAlreadyAbandonedLocation() throws Exception {
		UUID accountId = registerAccountWithCharacter("move-race-" + System.nanoTime() + "@greyhaven.test");
		CountDownLatch holderOwnsRow = new CountDownLatch(1);
		CountDownLatch releaseHolder = new CountDownLatch(1);

		try (ExecutorService executor = Executors.newFixedThreadPool(2)) {
			// Moves City Square -> Market, then keeps the transaction open so it still owns the row.
			Future<?> holder = executor.submit(() -> new TransactionTemplate(transactionManager)
					.executeWithoutResult(transaction -> {
						characterLocationService.lockLocationOf(accountId);
						characterLocationService.relocate(accountId, MARKET_ID);
						holderOwnsRow.countDown();
						awaitQuietly(releaseHolder);
					}));

			assertThat(holderOwnsRow.await(TASK_TIMEOUT.toSeconds(), TimeUnit.SECONDS)).isTrue();

			// City Square -> Forest is legal, Market -> Forest is not. Reaching the correct verdict
			// requires observing Market, which is only possible by waiting for the holder's lock.
			Future<?> challenger = executor.submit(() -> assertThatThrownBy(
					() -> worldApplicationService.move(accountId, FOREST_ID))
					.isInstanceOf(ApiException.class)
					.hasFieldOrPropertyWithValue("code", "INVALID_MOVEMENT"));

			// Cannot finish while the holder owns the row; an unlocked read would already be done.
			Thread.sleep(BLOCKED_OBSERVATION_WINDOW.toMillis());
			assertThat(challenger.isDone()).isFalse();

			releaseHolder.countDown();
			holder.get(TASK_TIMEOUT.toSeconds(), TimeUnit.SECONDS);
			challenger.get(TASK_TIMEOUT.toSeconds(), TimeUnit.SECONDS);
		}

		assertThat(persistedLocationOf(accountId)).isEqualTo(MARKET_ID);
	}

	/**
	 * Market and Forest are both reachable from City Square but not from each other, so the mover
	 * that commits second must be judged against the destination the winner moved to.
	 */
	@Test
	void concurrentMovesToMutuallyUnconnectedDestinationsProduceExactlyOneWinner() throws Exception {
		UUID accountId = registerAccountWithCharacter("move-race-two-" + System.nanoTime() + "@greyhaven.test");
		CyclicBarrier startGate = new CyclicBarrier(2);

		List<UUID> reached;
		try (ExecutorService executor = Executors.newFixedThreadPool(2)) {
			Future<UUID> toMarket = executor.submit(attemptMove(startGate, accountId, MARKET_ID));
			Future<UUID> toForest = executor.submit(attemptMove(startGate, accountId, FOREST_ID));
			reached = Stream.of(
							toMarket.get(TASK_TIMEOUT.toSeconds(), TimeUnit.SECONDS),
							toForest.get(TASK_TIMEOUT.toSeconds(), TimeUnit.SECONDS))
					.filter(Objects::nonNull)
					.toList();
		}

		assertThat(reached).hasSize(1);
		assertThat(persistedLocationOf(accountId)).isEqualTo(reached.getFirst());
	}

	/** Returns the destination on success, or {@code null} when the move was legitimately rejected. */
	private Callable<UUID> attemptMove(CyclicBarrier startGate, UUID accountId, UUID destinationId) {
		return () -> {
			startGate.await(TASK_TIMEOUT.toSeconds(), TimeUnit.SECONDS);
			try {
				worldApplicationService.move(accountId, destinationId);
				return destinationId;
			}
			catch (ApiException exception) {
				assertThat(exception.getCode()).isEqualTo("INVALID_MOVEMENT");
				return null;
			}
		};
	}

	private UUID persistedLocationOf(UUID accountId) {
		return jdbcTemplate.queryForObject(
				"select current_location_id from characters where account_id = ?",
				UUID.class,
				accountId);
	}

	private UUID registerAccountWithCharacter(String email) throws Exception {
		MvcResult registered = mockMvc.perform(withCsrf(post("/api/v1/auth/register"), freshCsrfCookie())
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"email":"%s","password":"password123"}
								""".formatted(email)))
				.andExpect(status().isCreated())
				.andReturn();
		MockHttpSession session = (MockHttpSession) registered.getRequest().getSession(false);
		assertThat(session).isNotNull();

		mockMvc.perform(withCsrf(post("/api/v1/characters"), freshCsrfCookie())
						.session(session)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"name":"Racer%s"}
								""".formatted(UUID.randomUUID().toString().replace("-", "").substring(0, 12))))
				.andExpect(status().isCreated());

		UUID accountId = jdbcTemplate.queryForObject(
				"select id from accounts where email = ?", UUID.class, email);
		assertThat(persistedLocationOf(accountId)).isEqualTo(CITY_SQUARE_ID);
		return accountId;
	}

	private Cookie freshCsrfCookie() throws Exception {
		MvcResult bootstrap = mockMvc.perform(get("/api/v1/bootstrap"))
				.andExpect(status().isOk())
				.andReturn();
		Cookie cookie = bootstrap.getResponse().getCookie("XSRF-TOKEN");
		assertThat(cookie).isNotNull();
		return cookie;
	}

	private static MockHttpServletRequestBuilder withCsrf(MockHttpServletRequestBuilder builder, Cookie csrfCookie) {
		return builder.header("X-XSRF-TOKEN", csrfCookie.getValue()).cookie(csrfCookie);
	}

	private static void awaitQuietly(CountDownLatch latch) {
		try {
			if (!latch.await(TASK_TIMEOUT.toSeconds(), TimeUnit.SECONDS)) {
				throw new IllegalStateException("Timed out coordinating the movement race");
			}
		}
		catch (InterruptedException exception) {
			Thread.currentThread().interrupt();
			throw new IllegalStateException("Interrupted while coordinating the movement race", exception);
		}
	}
}
