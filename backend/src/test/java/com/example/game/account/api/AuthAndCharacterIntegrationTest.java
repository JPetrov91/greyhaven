package com.example.game.account.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Locale;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import com.example.game.TestcontainersConfiguration;
import com.example.game.account.infrastructure.AccountPrincipal;

import jakarta.servlet.http.Cookie;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthAndCharacterIntegrationTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private JdbcTemplate jdbcTemplate;

	private Cookie csrfCookie;

	@BeforeEach
	void loadCsrfCookie() throws Exception {
		MvcResult bootstrap = mockMvc.perform(get("/api/v1/bootstrap"))
				.andExpect(status().isOk())
				.andReturn();
		csrfCookie = bootstrap.getResponse().getCookie("XSRF-TOKEN");
		assertThat(csrfCookie).isNotNull();
		assertThat(csrfCookie.getValue()).isNotBlank();
	}

	@Test
	void registerLoginLogoutAndRejectsUnauthenticatedAccess() throws Exception {
		String email = "hero-" + System.nanoTime() + "@greyhaven.test";

		mockMvc.perform(get("/api/v1/me"))
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.code").value("UNAUTHENTICATED"));

		MvcResult registerResult = mockMvc.perform(withCsrf(post("/api/v1/auth/register"))
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"email":"%s","password":"password123"}
								""".formatted(email)))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.email").value(email))
				.andExpect(jsonPath("$.hasCharacter").value(false))
				.andReturn();

		MockHttpSession session = (MockHttpSession) registerResult.getRequest().getSession(false);
		assertThat(session).isNotNull();
		refreshCsrfCookie(registerResult);

		mockMvc.perform(get("/api/v1/me").session(session))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.email").value(email))
				.andExpect(jsonPath("$.hasCharacter").value(false));

		MvcResult logoutResult = mockMvc.perform(withCsrf(post("/api/v1/auth/logout")).session(session))
				.andExpect(status().isNoContent())
				.andReturn();
		refreshCsrfCookie(logoutResult);

		mockMvc.perform(get("/api/v1/me").session(session))
				.andExpect(status().isUnauthorized());

		MvcResult loginResult = mockMvc.perform(withCsrf(post("/api/v1/auth/login"))
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"email":"%s","password":"password123"}
								""".formatted(email)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.email").value(email))
				.andReturn();

		MockHttpSession loginSession = (MockHttpSession) loginResult.getRequest().getSession(false);
		assertThat(loginSession).isNotNull();

		mockMvc.perform(get("/api/v1/me").session(loginSession))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.email").value(email));
	}

	@Test
	void duplicateEmailIsRejected() throws Exception {
		String email = "duplicate-" + System.nanoTime() + "@greyhaven.test";

		MvcResult first = mockMvc.perform(withCsrf(post("/api/v1/auth/register"))
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"email":"%s","password":"password123"}
								""".formatted(email)))
				.andExpect(status().isCreated())
				.andReturn();
		refreshCsrfCookie(first);

		mockMvc.perform(withCsrf(post("/api/v1/auth/register"))
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"email":"%s","password":"password123"}
								""".formatted(email)))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.code").value("EMAIL_ALREADY_EXISTS"));
	}

	@Test
	void invalidLoginIsRejected() throws Exception {
		String email = "invalid-login-" + System.nanoTime() + "@greyhaven.test";

		MvcResult registerResult = mockMvc.perform(withCsrf(post("/api/v1/auth/register"))
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"email":"%s","password":"password123"}
								""".formatted(email)))
				.andExpect(status().isCreated())
				.andReturn();
		refreshCsrfCookie(registerResult);

		mockMvc.perform(withCsrf(post("/api/v1/auth/login"))
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"email":"%s","password":"wrong-password"}
								""".formatted(email)))
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.code").value("INVALID_CREDENTIALS"));
	}

	@Test
	void characterCreationEnforcesUniquenessAndOnePerAccount() throws Exception {
		String emailA = "char-a-" + System.nanoTime() + "@greyhaven.test";
		String emailB = "char-b-" + System.nanoTime() + "@greyhaven.test";
		String sharedName = uniqueName("Ranger");

		MockHttpSession sessionA = registerAndGetSession(emailA);
		MockHttpSession sessionB = registerAndGetSession(emailB);

		mockMvc.perform(get("/api/v1/character").session(sessionA))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.code").value("CHARACTER_NOT_FOUND"));

		MvcResult createResult = mockMvc.perform(withCsrf(post("/api/v1/characters"))
						.session(sessionA)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"name":"%s"}
								""".formatted(sharedName)))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.name").value(sharedName))
				.andExpect(jsonPath("$.level").value(1))
				.andExpect(jsonPath("$.strength").value(5))
				.andExpect(jsonPath("$.agility").value(5))
				.andExpect(jsonPath("$.endurance").value(5))
				.andExpect(jsonPath("$.perception").value(5))
				.andExpect(jsonPath("$.gold").value(100))
				.andExpect(jsonPath("$.maxHealth").value(160))
				.andExpect(jsonPath("$.maxStamina").value(80))
				.andExpect(jsonPath("$.currentHealth").value(160))
				.andExpect(jsonPath("$.currentStamina").value(80))
				.andExpect(jsonPath("$.derivedStats.physicalDamage").value(14))
				.andExpect(jsonPath("$.derivedStats.armor").value(3))
				.andReturn();
		refreshCsrfCookie(createResult);

		mockMvc.perform(get("/api/v1/me").session(sessionA))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.hasCharacter").value(true));

		mockMvc.perform(get("/api/v1/character").session(sessionA))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.name").value(sharedName));

		mockMvc.perform(withCsrf(post("/api/v1/characters"))
						.session(sessionA)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"name":"%s"}
								""".formatted(uniqueName("Another"))))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.code").value("CHARACTER_ALREADY_EXISTS"));

		mockMvc.perform(withCsrf(post("/api/v1/characters"))
						.session(sessionB)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"name":"%s"}
								""".formatted(sharedName)))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.code").value("CHARACTER_NAME_TAKEN"));

		Integer accountCount = jdbcTemplate.queryForObject(
				"select count(*) from accounts where email in (?, ?)",
				Integer.class,
				emailA,
				emailB);
		Integer characterCount = jdbcTemplate.queryForObject(
				"select count(*) from characters where name = ?",
				Integer.class,
				sharedName);
		assertThat(accountCount).isEqualTo(2);
		assertThat(characterCount).isEqualTo(1);
	}

	@Test
	void duplicateEmailIsRejectedIgnoringCase() throws Exception {
		String email = "MixedCase-" + System.nanoTime() + "@Greyhaven.TEST";

		MvcResult first = mockMvc.perform(withCsrf(post("/api/v1/auth/register"))
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"email":"%s","password":"password123"}
								""".formatted(email)))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.email").value(email.toLowerCase(Locale.ROOT)))
				.andReturn();
		refreshCsrfCookie(first);

		mockMvc.perform(withCsrf(post("/api/v1/auth/register"))
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"email":"%s","password":"password123"}
								""".formatted(email.toUpperCase(Locale.ROOT))))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.code").value("EMAIL_ALREADY_EXISTS"));

		Integer accountCount = jdbcTemplate.queryForObject(
				"select count(*) from accounts where lower(email) = ?",
				Integer.class,
				email.toLowerCase(Locale.ROOT));
		assertThat(accountCount).isEqualTo(1);
	}

	@Test
	void loginAcceptsAnyEmailCasing() throws Exception {
		String email = "casing-" + System.nanoTime() + "@greyhaven.test";
		MockHttpSession ignored = registerAndGetSession(email);
		assertThat(ignored).isNotNull();

		mockMvc.perform(withCsrf(post("/api/v1/auth/login"))
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"email":"%s","password":"password123"}
								""".formatted(email.toUpperCase(Locale.ROOT))))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.email").value(email));
	}

	@Test
	void duplicateCharacterNameIsRejectedIgnoringCase() throws Exception {
		String name = uniqueName("Ranger");
		MockHttpSession sessionA = registerAndGetSession("case-a-" + System.nanoTime() + "@greyhaven.test");
		MockHttpSession sessionB = registerAndGetSession("case-b-" + System.nanoTime() + "@greyhaven.test");

		MvcResult created = mockMvc.perform(withCsrf(post("/api/v1/characters"))
						.session(sessionA)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"name":"%s"}
								""".formatted(name)))
				.andExpect(status().isCreated())
				.andReturn();
		refreshCsrfCookie(created);

		mockMvc.perform(withCsrf(post("/api/v1/characters"))
						.session(sessionB)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"name":"%s"}
								""".formatted(name.toUpperCase(Locale.ROOT))))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.code").value("CHARACTER_NAME_TAKEN"));

		Integer characterCount = jdbcTemplate.queryForObject(
				"select count(*) from characters where lower(name) = ?",
				Integer.class,
				name.toLowerCase(Locale.ROOT));
		assertThat(characterCount).isEqualTo(1);
	}

	@Test
	void stateChangingRequestsWithoutCsrfTokenAreRejected() throws Exception {
		mockMvc.perform(post("/api/v1/auth/register")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"email":"no-csrf-%d@greyhaven.test","password":"password123"}
								""".formatted(System.nanoTime())))
				.andExpect(status().isForbidden())
				.andExpect(jsonPath("$.code").value("ACCESS_DENIED"));

		MockHttpSession session = registerAndGetSession("csrf-" + System.nanoTime() + "@greyhaven.test");

		mockMvc.perform(post("/api/v1/characters")
						.session(session)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"name":"%s"}
								""".formatted(uniqueName("NoCsrf"))))
				.andExpect(status().isForbidden())
				.andExpect(jsonPath("$.code").value("ACCESS_DENIED"));

		mockMvc.perform(post("/api/v1/auth/logout").session(session))
				.andExpect(status().isForbidden());

		mockMvc.perform(get("/api/v1/me").session(session))
				.andExpect(status().isOk());
	}

	@Test
	void csrfTokenAndSessionAreRotatedOnAuthentication() throws Exception {
		String email = "rotation-" + System.nanoTime() + "@greyhaven.test";
		MockHttpSession session = registerAndGetSession(email);

		MvcResult logout = mockMvc.perform(withCsrf(post("/api/v1/auth/logout")).session(session))
				.andExpect(status().isNoContent())
				.andReturn();
		refreshCsrfCookie(logout);

		String tokenBeforeLogin = csrfCookie.getValue();
		MockHttpSession preLoginSession = new MockHttpSession();
		String sessionIdBeforeLogin = preLoginSession.getId();

		MvcResult login = mockMvc.perform(withCsrf(post("/api/v1/auth/login"))
						.session(preLoginSession)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"email":"%s","password":"password123"}
								""".formatted(email)))
				.andExpect(status().isOk())
				.andReturn();

		Cookie rotated = latestCsrfCookie(login);
		assertThat(rotated).isNotNull();
		assertThat(rotated.getValue()).isNotBlank().isNotEqualTo(tokenBeforeLogin);

		MockHttpSession authenticatedSession = (MockHttpSession) login.getRequest().getSession(false);
		assertThat(authenticatedSession).isNotNull();
		assertThat(authenticatedSession.getId()).isNotEqualTo(sessionIdBeforeLogin);
	}

	@Test
	void authenticatedSessionDoesNotRetainThePasswordHash() throws Exception {
		MockHttpSession session = registerAndGetSession("erasure-" + System.nanoTime() + "@greyhaven.test");

		SecurityContext context = (SecurityContext) session.getAttribute(
				HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY);
		assertThat(context).isNotNull();

		AccountPrincipal principal = (AccountPrincipal) context.getAuthentication().getPrincipal();
		assertThat(principal.getAccountId()).isNotNull();
		assertThat(principal.getPassword()).isNull();
		assertThat(context.getAuthentication().getCredentials()).isNull();
	}

	@Test
	void flywayMigrationsWereApplied() {
		Integer flywayCount = jdbcTemplate.queryForObject(
				"select count(*) from flyway_schema_history where version in ('2', '3', '4', '5', '6') and success = true",
				Integer.class);
		assertThat(flywayCount).isEqualTo(5);

		Integer emailIndexCount = jdbcTemplate.queryForObject(
				"select count(*) from pg_indexes where tablename = 'accounts' and indexname = 'uq_accounts_email_lower'",
				Integer.class);
		assertThat(emailIndexCount).isEqualTo(1);

		Integer locationCount = jdbcTemplate.queryForObject(
				"select count(*) from locations where code = 'CITY_SQUARE'",
				Integer.class);
		assertThat(locationCount).isEqualTo(1);
	}

	private MockHttpSession registerAndGetSession(String email) throws Exception {
		MvcResult result = mockMvc.perform(withCsrf(post("/api/v1/auth/register"))
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"email":"%s","password":"password123"}
								""".formatted(email)))
				.andExpect(status().isCreated())
				.andReturn();
		refreshCsrfCookie(result);
		MockHttpSession session = (MockHttpSession) result.getRequest().getSession(false);
		assertThat(session).isNotNull();
		return session;
	}

	/** Character names are limited to 24 characters, so the unique suffix has to stay short. */
	private static String uniqueName(String prefix) {
		return prefix + UUID.randomUUID().toString().replace("-", "").substring(0, 12);
	}

	private MockHttpServletRequestBuilder withCsrf(MockHttpServletRequestBuilder builder) {
		return builder.header("X-XSRF-TOKEN", csrfCookie.getValue()).cookie(csrfCookie);
	}

	private void refreshCsrfCookie(MvcResult result) {
		Cookie updated = latestCsrfCookie(result);
		if (updated != null) {
			csrfCookie = updated;
		}
	}

	/**
	 * Rotating the token writes a deletion cookie followed by the replacement, so the last
	 * non-blank {@code XSRF-TOKEN} cookie is the one a browser would keep.
	 */
	private static Cookie latestCsrfCookie(MvcResult result) {
		Cookie latest = null;
		for (Cookie cookie : result.getResponse().getCookies()) {
			if ("XSRF-TOKEN".equals(cookie.getName()) && cookie.getValue() != null && !cookie.getValue().isBlank()) {
				latest = cookie;
			}
		}
		return latest;
	}
}
