package com.example.game.account.api;

import static org.assertj.core.api.Assertions.assertThat;
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

import jakarta.servlet.http.Cookie;

/**
 * The uniqueness rules are pre-checked in the application and enforced by the database. These
 * tests drive both writers past the pre-check simultaneously so that the database constraint is
 * the thing that rejects the loser, and assert it surfaces as a conflict rather than a 500.
 */
@Import(TestcontainersConfiguration.class)
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class RegistrationConcurrencyIntegrationTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Test
	void concurrentRegistrationOfTheSameEmailCreatesExactlyOneAccount() throws Exception {
		String email = "race-" + System.nanoTime() + "@greyhaven.test";
		String body = """
				{"email":"%s","password":"password123"}
				""".formatted(email);

		List<MvcResult> results = inParallel(
				() -> mockMvc.perform(withCsrf(post("/api/v1/auth/register"), freshCsrfCookie())
						.contentType(MediaType.APPLICATION_JSON)
						.content(body)).andReturn(),
				() -> mockMvc.perform(withCsrf(post("/api/v1/auth/register"), freshCsrfCookie())
						.contentType(MediaType.APPLICATION_JSON)
						.content(body)).andReturn());

		assertConflictLoser(results, "EMAIL_ALREADY_EXISTS");
		Integer accountCount = jdbcTemplate.queryForObject(
				"select count(*) from accounts where lower(email) = ?", Integer.class, email);
		assertThat(accountCount).isEqualTo(1);
	}

	@Test
	void concurrentCreationOfTheSameCharacterNameCreatesExactlyOneCharacter() throws Exception {
		String name = uniqueName("Contender");
		MockHttpSession sessionA = registerAccount("race-name-a-" + System.nanoTime() + "@greyhaven.test");
		MockHttpSession sessionB = registerAccount("race-name-b-" + System.nanoTime() + "@greyhaven.test");
		String body = """
				{"name":"%s"}
				""".formatted(name);

		List<MvcResult> results = inParallel(
				() -> mockMvc.perform(withCsrf(post("/api/v1/characters"), freshCsrfCookie())
						.session(sessionA)
						.contentType(MediaType.APPLICATION_JSON)
						.content(body)).andReturn(),
				() -> mockMvc.perform(withCsrf(post("/api/v1/characters"), freshCsrfCookie())
						.session(sessionB)
						.contentType(MediaType.APPLICATION_JSON)
						.content(body)).andReturn());

		assertConflictLoser(results, "CHARACTER_NAME_TAKEN");
		Integer characterCount = jdbcTemplate.queryForObject(
				"select count(*) from characters where lower(name) = ?", Integer.class, name.toLowerCase());
		assertThat(characterCount).isEqualTo(1);
	}

	@Test
	void concurrentCharacterCreationForOneAccountCreatesExactlyOneCharacter() throws Exception {
		String email = "race-one-per-account-" + System.nanoTime() + "@greyhaven.test";
		MockHttpSession session = registerAccount(email);
		String firstName = uniqueName("First");
		String secondName = uniqueName("Second");

		List<MvcResult> results = inParallel(
				() -> mockMvc.perform(withCsrf(post("/api/v1/characters"), freshCsrfCookie())
						.session(session)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"name":"%s"}
								""".formatted(firstName))).andReturn(),
				() -> mockMvc.perform(withCsrf(post("/api/v1/characters"), freshCsrfCookie())
						.session(session)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"name":"%s"}
								""".formatted(secondName))).andReturn());

		assertConflictLoser(results, "CHARACTER_ALREADY_EXISTS");
		Integer characterCount = jdbcTemplate.queryForObject(
				"select count(*) from characters c join accounts a on a.id = c.account_id where a.email = ?",
				Integer.class,
				email);
		assertThat(characterCount).isEqualTo(1);
	}

	private void assertConflictLoser(List<MvcResult> results, String expectedCode) throws Exception {
		List<Integer> statuses = results.stream().map(result -> result.getResponse().getStatus()).toList();
		assertThat(statuses).containsExactlyInAnyOrder(201, 409);

		MvcResult conflict = results.stream()
				.filter(result -> result.getResponse().getStatus() == 409)
				.findFirst()
				.orElseThrow();
		assertThat(conflict.getResponse().getContentAsString()).contains("\"code\":\"" + expectedCode + "\"");
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

	private MockHttpSession registerAccount(String email) throws Exception {
		MvcResult result = mockMvc.perform(withCsrf(post("/api/v1/auth/register"), freshCsrfCookie())
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"email":"%s","password":"password123"}
								""".formatted(email)))
				.andExpect(status().isCreated())
				.andReturn();
		MockHttpSession session = (MockHttpSession) result.getRequest().getSession(false);
		assertThat(session).isNotNull();
		return session;
	}

	/** Character names are limited to 24 characters, so the unique suffix has to stay short. */
	private static String uniqueName(String prefix) {
		return prefix + UUID.randomUUID().toString().replace("-", "").substring(0, 12);
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
}
