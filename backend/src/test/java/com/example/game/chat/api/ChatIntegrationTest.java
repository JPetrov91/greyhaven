package com.example.game.chat.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.startsWith;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.asyncDispatch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request;
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
import com.example.game.chat.application.ChatApplicationService;
import com.example.game.chat.domain.ChatRules;
import com.example.game.shared.api.ApiException;
import com.jayway.jsonpath.JsonPath;

import jakarta.servlet.http.Cookie;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ChatIntegrationTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Autowired
	private ChatSseHub chatSseHub;

	@Autowired
	private ChatApplicationService chatApplicationService;

	private Cookie csrfCookie;

	@BeforeEach
	void loadCsrfCookie() throws Exception {
		MvcResult bootstrap = mockMvc.perform(get("/api/v1/bootstrap"))
				.andExpect(status().isOk())
				.andReturn();
		csrfCookie = bootstrap.getResponse().getCookie("XSRF-TOKEN");
		assertThat(csrfCookie).isNotNull();
	}

	@Test
	void flywayCreatedChatMessages() {
		Integer flywayV17 = jdbcTemplate.queryForObject(
				"select count(*) from flyway_schema_history where version = '17' and success = true",
				Integer.class);
		Integer table = jdbcTemplate.queryForObject(
				"""
						select count(*) from information_schema.tables
						where table_schema = 'public' and table_name = 'chat_messages'
						""",
				Integer.class);
		assertThat(flywayV17).isEqualTo(1);
		assertThat(table).isEqualTo(1);
	}

	@Test
	void postAndListReturnCharacterNameAndRejectInvalidBodies() throws Exception {
		String email = "chat-" + System.nanoTime() + "@greyhaven.test";
		MockHttpSession session = registerWithCharacter(email, uniqueName("Post"));

		String uniqueBody = "Forest is quiet " + System.nanoTime();
		MvcResult posted = mockMvc.perform(withCsrf(post("/api/v1/chat/messages"))
						.session(session)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"body":"  %s  "}
								""".formatted(uniqueBody)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.characterName").value(startsWith("Post")))
				.andExpect(jsonPath("$.body").value(uniqueBody))
				.andReturn();
		refreshCsrfCookie(posted);
		String speakerName = JsonPath.read(posted.getResponse().getContentAsString(), "$.characterName");

		mockMvc.perform(get("/api/v1/chat/messages").session(session))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[*].body").value(hasItem(uniqueBody)))
				.andExpect(jsonPath("$[*].characterName").value(hasItem(speakerName)));

		mockMvc.perform(withCsrf(post("/api/v1/chat/messages"))
						.session(session)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"body":"<script>alert(1)</script>"}
								"""))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.code").value("CHAT_MESSAGE_INVALID"));

		mockMvc.perform(withCsrf(post("/api/v1/chat/messages"))
						.session(session)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"body":"%s"}
								""".formatted("x".repeat(ChatRules.MAX_BODY_LENGTH + 1))))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
	}

	@Test
	void rateLimitAndHistoryCapAreEnforced() throws Exception {
		String email = "chat-rate-" + System.nanoTime() + "@greyhaven.test";
		MockHttpSession session = registerWithCharacter(email, uniqueName("Rate"));

		refreshCsrfCookie(mockMvc.perform(withCsrf(post("/api/v1/chat/messages"))
						.session(session)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"body":"first"}
								"""))
				.andExpect(status().isOk())
				.andReturn());

		mockMvc.perform(withCsrf(post("/api/v1/chat/messages"))
						.session(session)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"body":"second"}
								"""))
				.andExpect(status().isTooManyRequests())
				.andExpect(jsonPath("$.code").value("CHAT_RATE_LIMITED"));

		UUID characterId = characterIdForEmail(email);
		for (int index = 0; index < ChatRules.HISTORY_LIMIT + 5; index++) {
			jdbcTemplate.update(
					"""
							insert into chat_messages (id, character_id, body, created_at)
							values (?, ?, ?, now() - (? || ' seconds')::interval)
							""",
					UUID.randomUUID(),
					characterId,
					"seed-" + index,
					index);
		}

		mockMvc.perform(get("/api/v1/chat/messages").session(session))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.length()").value(ChatRules.HISTORY_LIMIT));
	}

	@Test
	void streamReplaysMissedMessagesAndReceivesNewPosts() throws Exception {
		String listenerEmail = "chat-sse-l-" + System.nanoTime() + "@greyhaven.test";
		String speakerEmail = "chat-sse-s-" + System.nanoTime() + "@greyhaven.test";
		MockHttpSession listener = registerWithCharacter(listenerEmail, uniqueName("Listen"));
		MockHttpSession speaker = registerWithCharacter(speakerEmail, uniqueName("Speak"));

		MvcResult first = mockMvc.perform(withCsrf(post("/api/v1/chat/messages"))
						.session(speaker)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"body":"before connect"}
								"""))
				.andExpect(status().isOk())
				.andReturn();
		refreshCsrfCookie(first);
		String firstId = JsonPath.read(first.getResponse().getContentAsString(), "$.id");
		String speakerName = JsonPath.read(first.getResponse().getContentAsString(), "$.characterName");
		jdbcTemplate.update(
				"update chat_messages set created_at = created_at - interval '10 seconds' where id = ?",
				UUID.fromString(firstId));

		refreshCsrfCookie(mockMvc.perform(withCsrf(post("/api/v1/chat/messages"))
						.session(speaker)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"body":"missed while disconnected"}
								"""))
				.andExpect(status().isOk())
				.andReturn());

		MvcResult stream = mockMvc.perform(get("/api/v1/chat/stream")
						.session(listener)
						.accept(MediaType.TEXT_EVENT_STREAM)
						.param("after", firstId))
				.andExpect(request().asyncStarted())
				.andReturn();

		jdbcTemplate.update(
				"update chat_messages set created_at = created_at - interval '10 seconds' where character_id = ?",
				characterIdForEmail(speakerEmail));

		refreshCsrfCookie(mockMvc.perform(withCsrf(post("/api/v1/chat/messages"))
						.session(speaker)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"body":"after connect"}
								"""))
				.andExpect(status().isOk())
				.andReturn());

		chatSseHub.shutdown();
		String payload = mockMvc.perform(asyncDispatch(stream))
				.andReturn()
				.getResponse()
				.getContentAsString();
		assertThat(payload).contains("missed while disconnected");
		assertThat(payload).contains("after connect");
		assertThat(payload).contains(speakerName);
		assertThat(payload).contains("\"characterName\":");
	}

	@Test
	void replayAfterAnOldCursorStaysWithinTheHistoryCap() throws Exception {
		String email = "chat-replay-" + System.nanoTime() + "@greyhaven.test";
		MockHttpSession session = registerWithCharacter(email, uniqueName("Replay"));
		UUID accountId = accountIdForEmail(email);
		UUID characterId = characterIdForEmail(email);
		UUID oldestId = UUID.randomUUID();
		for (int index = 0; index < ChatRules.HISTORY_LIMIT + 8; index++) {
			UUID id = index == 0 ? oldestId : UUID.randomUUID();
			jdbcTemplate.update(
					"""
							insert into chat_messages (id, character_id, body, created_at)
							values (?, ?, ?, now() - (? || ' seconds')::interval)
							""",
					id,
					characterId,
					"cap-" + index,
					ChatRules.HISTORY_LIMIT + 8 - index);
		}

		assertThat(chatApplicationService.listAfter(accountId, oldestId)).hasSize(ChatRules.HISTORY_LIMIT);
	}

	@Test
	void concurrentPostsFromTheSameCharacterAreRateLimited() throws Exception {
		String email = "chat-race-" + System.nanoTime() + "@greyhaven.test";
		registerWithCharacter(email, uniqueName("Race"));
		UUID accountId = accountIdForEmail(email);
		UUID characterId = characterIdForEmail(email);

		CountDownLatch start = new CountDownLatch(1);
		ExecutorService pool = Executors.newFixedThreadPool(2);
		AtomicInteger successes = new AtomicInteger();
		AtomicInteger limited = new AtomicInteger();
		try {
			Future<?> first = pool.submit(() -> postRacing(accountId, start, successes, limited));
			Future<?> second = pool.submit(() -> postRacing(accountId, start, successes, limited));
			start.countDown();
			first.get(20, TimeUnit.SECONDS);
			second.get(20, TimeUnit.SECONDS);
		}
		finally {
			pool.shutdownNow();
		}

		assertThat(successes.get()).isEqualTo(1);
		assertThat(limited.get()).isEqualTo(1);
		assertThat(jdbcTemplate.queryForObject(
				"select count(*) from chat_messages where character_id = ?",
				Integer.class,
				characterId)).isEqualTo(1);
	}

	@Test
	void chatRequiresAuthenticationAndACharacter() throws Exception {
		mockMvc.perform(get("/api/v1/chat/messages"))
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.code").value("UNAUTHENTICATED"));

		MvcResult registered = mockMvc.perform(withCsrf(post("/api/v1/auth/register"))
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"email":"chat-nochar-%s@greyhaven.test","password":"password123"}
								""".formatted(System.nanoTime())))
				.andExpect(status().isCreated())
				.andReturn();
		refreshCsrfCookie(registered);
		MockHttpSession session = (MockHttpSession) registered.getRequest().getSession(false);
		assertThat(session).isNotNull();

		mockMvc.perform(get("/api/v1/chat/messages").session(session))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.code").value("CHARACTER_NOT_FOUND"));
	}

	private static String uniqueName(String prefix) {
		return prefix + UUID.randomUUID().toString().replace("-", "").substring(0, 8);
	}

	private Void postRacing(
			UUID accountId,
			CountDownLatch start,
			AtomicInteger successes,
			AtomicInteger limited) throws InterruptedException {
		start.await();
		try {
			chatApplicationService.post(accountId, "race " + Thread.currentThread().threadId());
			successes.incrementAndGet();
		}
		catch (ApiException exception) {
			if (!"CHAT_RATE_LIMITED".equals(exception.getCode())) {
				throw exception;
			}
			limited.incrementAndGet();
		}
		return null;
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
						select c.id from characters c
						join accounts a on a.id = c.account_id
						where a.email = ?
						""",
				UUID.class,
				email);
	}

	private MockHttpSession registerWithCharacter(String email, String name) throws Exception {
		MvcResult registered = mockMvc.perform(withCsrf(post("/api/v1/auth/register"))
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"email":"%s","password":"password123"}
								""".formatted(email)))
				.andExpect(status().isCreated())
				.andReturn();
		refreshCsrfCookie(registered);
		MockHttpSession session = (MockHttpSession) registered.getRequest().getSession(false);
		assertThat(session).isNotNull();

		MvcResult created = mockMvc.perform(withCsrf(post("/api/v1/characters"))
						.session(session)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"name":"%s"}
								""".formatted(name)))
				.andExpect(status().isCreated())
				.andReturn();
		refreshCsrfCookie(created);
		return session;
	}

	private void refreshCsrfCookie(MvcResult result) {
		Cookie updated = latestCsrfCookie(result);
		if (updated != null) {
			csrfCookie = updated;
		}
	}

	private static Cookie latestCsrfCookie(MvcResult result) {
		Cookie latest = null;
		for (Cookie cookie : result.getResponse().getCookies()) {
			if ("XSRF-TOKEN".equals(cookie.getName()) && cookie.getValue() != null && !cookie.getValue().isBlank()) {
				latest = cookie;
			}
		}
		return latest;
	}

	private MockHttpServletRequestBuilder withCsrf(MockHttpServletRequestBuilder builder) {
		return builder.header("X-XSRF-TOKEN", csrfCookie.getValue()).cookie(csrfCookie);
	}
}
