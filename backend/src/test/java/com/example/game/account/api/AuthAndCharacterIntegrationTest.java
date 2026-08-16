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
import com.jayway.jsonpath.JsonPath;

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
				.andExpect(jsonPath("$.characterCount").value(0))
				.andExpect(jsonPath("$.activeCharacterId").value((Object) null))
				.andReturn();

		MockHttpSession session = (MockHttpSession) registerResult.getRequest().getSession(false);
		assertThat(session).isNotNull();
		refreshCsrfCookie(registerResult);

		mockMvc.perform(get("/api/v1/me").session(session))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.email").value(email))
				.andExpect(jsonPath("$.characterCount").value(0))
				.andExpect(jsonPath("$.activeCharacterId").value((Object) null));

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
	void characterCreationEnforcesUniquenessAndThreeSlotsPerAccount() throws Exception {
		String emailA = "char-a-" + System.nanoTime() + "@greyhaven.test";
		String emailB = "char-b-" + System.nanoTime() + "@greyhaven.test";
		String sharedName = uniqueName("Ranger");

		MockHttpSession sessionA = registerAndGetSession(emailA);
		MockHttpSession sessionB = registerAndGetSession(emailB);

		mockMvc.perform(get("/api/v1/character").session(sessionA))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.code").value("NO_ACTIVE_CHARACTER"));

		MvcResult createResult = mockMvc.perform(withCsrf(post("/api/v1/characters"))
						.session(sessionA)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"name":"%s"}
								""".formatted(sharedName)))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.name").value(sharedName))
				.andExpect(jsonPath("$.gender").value("MALE"))
				.andExpect(jsonPath("$.avatarCode").value("male_unyielding"))
				.andExpect(jsonPath("$.level").value(1))
				.andExpect(jsonPath("$.strength").value(5))
				.andExpect(jsonPath("$.agility").value(5))
				.andExpect(jsonPath("$.endurance").value(5))
				.andExpect(jsonPath("$.perception").value(5))
				.andExpect(jsonPath("$.gold").value(100))
				.andExpect(jsonPath("$.maxHealth").value(165))
				.andExpect(jsonPath("$.maxStamina").value(85))
				.andExpect(jsonPath("$.currentHealth").value(165))
				.andExpect(jsonPath("$.currentStamina").value(85))
				.andExpect(jsonPath("$.derivedStats.physicalDamage").value(8))
				.andExpect(jsonPath("$.derivedStats.armor").value(3))
				.andReturn();
		refreshCsrfCookie(createResult);

		String firstId = JsonPath.read(createResult.getResponse().getContentAsString(), "$.id");

		mockMvc.perform(withCsrf(post("/api/v1/characters"))
						.session(sessionA)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"name":"%s","slotIndex":0}
								""".formatted(uniqueName("Taken"))))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.code").value("CHARACTER_SLOT_OCCUPIED"));

		mockMvc.perform(get("/api/v1/me").session(sessionA))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.characterCount").value(1))
				.andExpect(jsonPath("$.activeCharacterId").value(firstId));

		mockMvc.perform(get("/api/v1/character").session(sessionA))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.name").value(sharedName));

		String secondName = uniqueName("Another");
		MvcResult second = mockMvc.perform(withCsrf(post("/api/v1/characters"))
						.session(sessionA)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"name":"%s"}
								""".formatted(secondName)))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.name").value(secondName))
				.andReturn();
		refreshCsrfCookie(second);
		String secondId = JsonPath.read(second.getResponse().getContentAsString(), "$.id");

		MvcResult third = mockMvc.perform(withCsrf(post("/api/v1/characters"))
						.session(sessionA)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"name":"%s"}
								""".formatted(uniqueName("Third"))))
				.andExpect(status().isCreated())
				.andReturn();
		refreshCsrfCookie(third);

		mockMvc.perform(withCsrf(post("/api/v1/characters"))
						.session(sessionA)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"name":"%s"}
								""".formatted(uniqueName("Fourth"))))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.code").value("CHARACTER_SLOTS_FULL"));

		mockMvc.perform(get("/api/v1/characters").session(sessionA))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.slots.length()").value(3))
				.andExpect(jsonPath("$.slots[0].empty").value(false))
				.andExpect(jsonPath("$.slots[0].strength").value(5))
				.andExpect(jsonPath("$.slots[0].equipped").isArray())
				.andExpect(jsonPath("$.slots[0].healingPotions").value(org.hamcrest.Matchers.greaterThanOrEqualTo(0)))
				.andExpect(jsonPath("$.slots[1].empty").value(false))
				.andExpect(jsonPath("$.slots[2].empty").value(false));

		mockMvc.perform(withCsrf(post("/api/v1/characters/" + firstId + "/select")).session(sessionA))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(firstId))
				.andExpect(jsonPath("$.name").value(sharedName));

		mockMvc.perform(get("/api/v1/character").session(sessionA))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(firstId));

		mockMvc.perform(get("/api/v1/inventory").session(sessionA))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.usedSlots").value(org.hamcrest.Matchers.greaterThan(0)));

		mockMvc.perform(withCsrf(post("/api/v1/characters/" + secondId + "/select")).session(sessionB))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.code").value("CHARACTER_NOT_FOUND"));

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
	void selectIsRejectedWhileTheActiveCharacterIsInCombat() throws Exception {
		MockHttpSession session = registerAndGetSession("slots-combat-" + System.nanoTime() + "@greyhaven.test");
		MvcResult first = mockMvc.perform(withCsrf(post("/api/v1/characters"))
						.session(session)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"name":"%s"}
								""".formatted(uniqueName("Blade"))))
				.andExpect(status().isCreated())
				.andReturn();
		refreshCsrfCookie(first);
		UUID firstId = UUID.fromString(JsonPath.read(first.getResponse().getContentAsString(), "$.id"));

		MvcResult second = mockMvc.perform(withCsrf(post("/api/v1/characters"))
						.session(session)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"name":"%s"}
								""".formatted(uniqueName("Shade"))))
				.andExpect(status().isCreated())
				.andReturn();
		refreshCsrfCookie(second);
		UUID secondId = UUID.fromString(JsonPath.read(second.getResponse().getContentAsString(), "$.id"));

		UUID encounterId = UUID.randomUUID();
		UUID monsterId = jdbcTemplate.queryForObject("select id from monster_definitions limit 1", UUID.class);
		UUID locationId = jdbcTemplate.queryForObject(
				"select id from locations where code = 'CITY_SQUARE'", UUID.class);
		jdbcTemplate.update(
				"""
						insert into encounters (
							id, character_id, location_id, monster_definition_id, status, created_at, updated_at, dungeon_optional)
						values (?, ?, ?, ?, 'COMBAT_STARTED', now(), now(), false)
						""",
				encounterId,
				secondId,
				locationId,
				monsterId);
		jdbcTemplate.update(
				"""
						insert into combat_sessions (
							id, encounter_id, character_id, monster_definition_id, status, round_number,
							player_health, player_stamina, enemy_health, created_at, updated_at)
						values (?, ?, ?, ?, 'ACTIVE', 1, 100, 80, 40, now(), now())
						""",
				UUID.randomUUID(),
				encounterId,
				secondId,
				monsterId);

		mockMvc.perform(withCsrf(post("/api/v1/characters/" + firstId + "/select")).session(session))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.code").value("COMBAT_IN_PROGRESS"));
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
		Integer accountTable = jdbcTemplate.queryForObject(
				"""
						select count(*) from information_schema.tables
						where table_schema = 'public' and table_name = 'accounts'
						""",
				Integer.class);
		Integer characterTable = jdbcTemplate.queryForObject(
				"""
						select count(*) from information_schema.tables
						where table_schema = 'public' and table_name = 'characters'
						""",
				Integer.class);
		assertThat(accountTable).isEqualTo(1);
		assertThat(characterTable).isEqualTo(1);

		Integer emailIndexCount = jdbcTemplate.queryForObject(
				"select count(*) from pg_indexes where tablename = 'accounts' and indexname = 'uq_accounts_email_lower'",
				Integer.class);
		assertThat(emailIndexCount).isEqualTo(1);

		Integer locationCount = jdbcTemplate.queryForObject(
				"select count(*) from locations where code = 'CITY_SQUARE'",
				Integer.class);
		assertThat(locationCount).isEqualTo(1);
	}

	@Test
	void createCharacterPersistsRequestedAppearanceAndChecksNameAvailability() throws Exception {
		MockHttpSession session = registerAndGetSession("looks-" + System.nanoTime() + "@greyhaven.test");
		String name = uniqueName("Nyx");

		mockMvc.perform(get("/api/v1/characters/name-available").session(session).param("name", name))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.available").value(true));

		mockMvc.perform(withCsrf(post("/api/v1/characters"))
						.session(session)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"name":"%s","gender":"FEMALE","avatarCode":"female_veiled"}
								""".formatted(name)))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.name").value(name))
				.andExpect(jsonPath("$.gender").value("FEMALE"))
				.andExpect(jsonPath("$.avatarCode").value("female_veiled"));

		mockMvc.perform(get("/api/v1/characters/name-available").session(session).param("name", name))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.available").value(false));
	}

	@Test
	void createCharacterRejectsGenderMismatchedAvatar() throws Exception {
		MockHttpSession session = registerAndGetSession("looks-bad-" + System.nanoTime() + "@greyhaven.test");

		mockMvc.perform(withCsrf(post("/api/v1/characters"))
						.session(session)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"name":"%s","gender":"MALE","avatarCode":"female_veiled"}
								""".formatted(uniqueName("Kael"))))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.code").value("INVALID_CHARACTER_APPEARANCE"));
	}

	@Test
	void createCharacterAcceptsSpacedNameAndRejectsSpecialCharacters() throws Exception {
		MockHttpSession session = registerAndGetSession("name-rules-" + System.nanoTime() + "@greyhaven.test");

		mockMvc.perform(withCsrf(post("/api/v1/characters"))
						.session(session)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"name":"Ragnar_Ironfist"}
								"""))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));

		mockMvc.perform(withCsrf(post("/api/v1/characters"))
						.session(session)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"name":"Ragnar Ironfist"}
								"""))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.name").value("Ragnar Ironfist"));
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
