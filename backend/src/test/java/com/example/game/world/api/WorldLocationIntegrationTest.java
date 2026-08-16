package com.example.game.world.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import com.example.game.TestcontainersConfiguration;
import com.example.game.character.application.CharacterLocationService;
import com.example.game.world.domain.LocationCodes;

import jakarta.servlet.http.Cookie;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class WorldLocationIntegrationTest {

	private static final UUID CITY_SQUARE_ID = UUID.fromString("a0000000-0000-4000-8000-000000000001");
	private static final UUID FOREST_ID = UUID.fromString("a0000000-0000-4000-8000-000000000005");
	private static final UUID TAVERN_ID = UUID.fromString("a0000000-0000-4000-8000-000000000002");
	private static final UUID OLD_TOWN_ID = UUID.fromString("a0000000-0000-4000-8000-000000000004");

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Autowired
	private CharacterLocationService characterLocationService;

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
	void flywaySeededLocationsAndConnections() {
		Integer locationCount = jdbcTemplate.queryForObject(
				"select count(*) from locations",
				Integer.class);
		Integer connectionCount = jdbcTemplate.queryForObject(
				"select count(*) from location_connections",
				Integer.class);
		assertThat(locationCount).isEqualTo(14);
		assertThat(connectionCount).isEqualTo(28);
		assertThat(jdbcTemplate.queryForObject(
				"""
						select count(*) from location_connections c
						join locations a on a.id = c.from_location_id
						join locations b on b.id = c.to_location_id
						where a.code = 'HARBOUR' and b.code = 'ANCIENT_RUINS'
						""",
				Integer.class)).isZero();
		assertThat(jdbcTemplate.queryForObject(
				"""
						select count(*) from location_connections c
						join locations a on a.id = c.from_location_id
						join locations b on b.id = c.to_location_id
						where a.code = 'BANDIT_CAMP' and b.code = 'ANCIENT_RUINS'
						""",
				Integer.class)).isEqualTo(1);
		assertThat(jdbcTemplate.queryForObject(
				"""
						select count(*) from location_encounter_weights w
						join locations l on l.id = w.location_id
						join monster_definitions m on m.id = w.monster_definition_id
						where l.code = 'SEWERS' and m.code = 'GIANT_RAT'
						""",
				Integer.class)).isZero();
		assertThat(jdbcTemplate.queryForObject(
				"""
						select count(*) from location_encounter_weights w
						join locations l on l.id = w.location_id
						join monster_definitions m on m.id = w.monster_definition_id
						where l.code = 'BANDIT_CAMP' and m.code = 'BANDIT'
						""",
				Integer.class)).isZero();
		assertThat(jdbcTemplate.queryForObject(
				"""
						select count(*) from location_encounter_weights w
						join locations l on l.id = w.location_id
						join monster_definitions m on m.id = w.monster_definition_id
						where l.code = 'ANCIENT_RUINS' and m.code = 'BANDIT_VETERAN'
						""",
				Integer.class)).isZero();
		assertThat(jdbcTemplate.queryForObject(
				"""
						select skip_room_code from dungeon_room_edges e
						join dungeon_rooms r on r.id = e.from_room_id
						where r.code = 'COMMAND_HALL' and e.edge_code = 'CONTINUE'
						""",
				String.class)).isEqualTo("CRYPT");
	}

	@Test
	void nearbyCharacterLookupIsIndexed() {
		Integer indexCount = jdbcTemplate.queryForObject(
				"""
						select count(*) from pg_indexes
						where tablename = 'characters' and indexname = 'idx_characters_current_location'
						""",
				Integer.class);

		assertThat(indexCount).isEqualTo(1);
	}

	@Test
	void newCharacterStartsInCitySquareAndCanInspectWorld() throws Exception {
		MockHttpSession session = registerWithCharacter("world-start-" + System.nanoTime() + "@greyhaven.test");

		mockMvc.perform(get("/api/v1/character").session(session))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.currentLocationId").value(CITY_SQUARE_ID.toString()));

		mockMvc.perform(get("/api/v1/world/location").session(session))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.code").value(LocationCodes.CITY_SQUARE))
				.andExpect(jsonPath("$.name").value("City Square"))
				.andExpect(jsonPath("$.safety").value("SAFE"))
				.andExpect(jsonPath("$.region").value("Greyhaven"))
				.andExpect(jsonPath("$.actions[0]").value("INSPECT"))
				.andExpect(jsonPath("$.actions[1]").value("MOVE"))
				.andExpect(jsonPath("$.actions[2]").value("VIEW_NEARBY"));

		mockMvc.perform(get("/api/v1/world/destinations").session(session))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.destinations.length()").value(8))
				.andExpect(jsonPath("$.destinations[?(@.code=='FOREST')]").exists())
				.andExpect(jsonPath("$.destinations[?(@.code=='OLD_TOWN')]").exists())
				.andExpect(jsonPath("$.destinations[?(@.code=='MARKET')]").exists())
				.andExpect(jsonPath("$.destinations[?(@.code=='NORTH_ROAD')]").exists())
				.andExpect(jsonPath("$.destinations[?(@.code=='ARENA')]").exists())
				.andExpect(jsonPath("$.destinations[?(@.code=='SPARRING_YARD')]").exists())
				.andExpect(jsonPath("$.destinations[?(@.code=='CRAFTSMEN_WARD')]").exists())
				.andExpect(jsonPath("$.destinations[?(@.code=='HARBOUR')]").exists())
				.andExpect(jsonPath("$.destinations[?(@.code=='TAVERN')]").isEmpty());
	}

	@Test
	void validMovementIsInstantaneousAndPersisted() throws Exception {
		String email = "world-move-" + System.nanoTime() + "@greyhaven.test";
		MockHttpSession session = registerWithCharacter(email);

		MvcResult move = mockMvc.perform(withCsrf(post("/api/v1/world/move"))
						.session(session)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"destinationLocationId":"%s"}
								""".formatted(FOREST_ID)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.code").value(LocationCodes.FOREST))
				.andExpect(jsonPath("$.safety").value("DANGEROUS"))
				.andReturn();
		refreshCsrfCookie(move);

		mockMvc.perform(get("/api/v1/world/location").session(session))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.code").value(LocationCodes.FOREST));

		mockMvc.perform(get("/api/v1/character").session(session))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.currentLocationId").value(FOREST_ID.toString()));

		String persistedLocation = jdbcTemplate.queryForObject(
				"""
						select c.current_location_id::text
						from characters c
						join accounts a on a.id = c.account_id
						where a.email = ?
						""",
				String.class,
				email);
		assertThat(persistedLocation).isEqualTo(FOREST_ID.toString());

		mockMvc.perform(get("/api/v1/world/destinations").session(session))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.destinations.length()").value(2))
				.andExpect(jsonPath("$.destinations[?(@.code=='CITY_SQUARE')]").exists())
				.andExpect(jsonPath("$.destinations[?(@.code=='OLD_MINE')]").exists());
	}

	@Test
	void invalidMovementIsRejectedAndLocationUnchanged() throws Exception {
		MockHttpSession session = registerWithCharacter("world-invalid-" + System.nanoTime() + "@greyhaven.test");

		mockMvc.perform(withCsrf(post("/api/v1/world/move"))
						.session(session)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"destinationLocationId":"%s"}
								""".formatted(TAVERN_ID)))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.code").value("INVALID_MOVEMENT"));

		mockMvc.perform(get("/api/v1/world/location").session(session))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.code").value(LocationCodes.CITY_SQUARE));

		Integer stillAtSquare = jdbcTemplate.queryForObject(
				"""
						select count(*) from characters c
						join accounts a on a.id = c.account_id
						where a.email like 'world-invalid-%' and c.current_location_id = ?
						""",
				Integer.class,
				CITY_SQUARE_ID);
		assertThat(stillAtSquare).isEqualTo(1);
	}

	@Test
	void malformedDestinationIdIsRejectedAsBadRequest() throws Exception {
		MockHttpSession session = registerWithCharacter("world-malformed-" + System.nanoTime() + "@greyhaven.test");

		mockMvc.perform(withCsrf(post("/api/v1/world/move"))
						.session(session)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"destinationLocationId":"not-a-uuid"}
								"""))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.code").value("MALFORMED_REQUEST"));

		mockMvc.perform(withCsrf(post("/api/v1/world/move"))
						.session(session)
						.contentType(MediaType.APPLICATION_JSON)
						.content("{"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.code").value("MALFORMED_REQUEST"));

		mockMvc.perform(withCsrf(post("/api/v1/world/move"))
						.session(session)
						.contentType(MediaType.APPLICATION_JSON)
						.content("{}"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
	}

	@Test
	void unknownDestinationIsNotFound() throws Exception {
		MockHttpSession session = registerWithCharacter("world-missing-" + System.nanoTime() + "@greyhaven.test");

		mockMvc.perform(withCsrf(post("/api/v1/world/move"))
						.session(session)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"destinationLocationId":"%s"}
								""".formatted(UUID.randomUUID())))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.code").value("LOCATION_NOT_FOUND"));
	}

	@Test
	void nearbyCharactersListsOthersAtSameLocationOnly() throws Exception {
		String nameA = uniqueName("NearA");
		String nameB = uniqueName("NearB");
		String nameC = uniqueName("NearC");
		MockHttpSession sessionA = registerWithCharacter("nearby-a-" + System.nanoTime() + "@greyhaven.test", nameA);
		MockHttpSession sessionB = registerWithCharacter("nearby-b-" + System.nanoTime() + "@greyhaven.test", nameB);
		MockHttpSession sessionC = registerWithCharacter("nearby-c-" + System.nanoTime() + "@greyhaven.test", nameC);

		// Move the pair to Old Town so leftover City Square characters from other tests do not interfere.
		MvcResult moveA = mockMvc.perform(withCsrf(post("/api/v1/world/move"))
						.session(sessionA)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"destinationLocationId":"%s"}
								""".formatted(OLD_TOWN_ID)))
				.andExpect(status().isOk())
				.andReturn();
		refreshCsrfCookie(moveA);

		MvcResult moveC = mockMvc.perform(withCsrf(post("/api/v1/world/move"))
						.session(sessionC)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"destinationLocationId":"%s"}
								""".formatted(OLD_TOWN_ID)))
				.andExpect(status().isOk())
				.andReturn();
		refreshCsrfCookie(moveC);

		MvcResult moveB = mockMvc.perform(withCsrf(post("/api/v1/world/move"))
						.session(sessionB)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"destinationLocationId":"%s"}
								""".formatted(FOREST_ID)))
				.andExpect(status().isOk())
				.andReturn();
		refreshCsrfCookie(moveB);

		mockMvc.perform(get("/api/v1/world/nearby").session(sessionA))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.characters.length()").value(1))
				.andExpect(jsonPath("$.characters[0].name").value(nameC))
				.andExpect(jsonPath("$.characters[0].level").value(1))
				.andExpect(jsonPath("$.truncated").value(false));

		mockMvc.perform(get("/api/v1/world/nearby").session(sessionC))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.characters.length()").value(1))
				.andExpect(jsonPath("$.characters[0].name").value(nameA));

		mockMvc.perform(get("/api/v1/world/nearby").session(sessionB))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.characters[?(@.name=='%s')]".formatted(nameA)).isEmpty())
				.andExpect(jsonPath("$.characters[?(@.name=='%s')]".formatted(nameC)).isEmpty());
	}

	/**
	 * The cap has to be a database limit, not a trim after loading every character at the location.
	 */
	@Test
	void nearbyLookupAppliesTheLimitInTheQuery() throws Exception {
		registerWithCharacter("nearby-cap-a-" + System.nanoTime() + "@greyhaven.test");
		registerWithCharacter("nearby-cap-b-" + System.nanoTime() + "@greyhaven.test");
		UUID noSuchCharacter = UUID.randomUUID();

		assertThat(characterLocationService.othersAt(CITY_SQUARE_ID, noSuchCharacter, 1)).hasSize(1);
		assertThat(characterLocationService.othersAt(CITY_SQUARE_ID, noSuchCharacter, 100)).hasSizeGreaterThan(1);
	}

	@Test
	void worldEndpointsRequireAuthenticationAndCharacter() throws Exception {
		mockMvc.perform(get("/api/v1/world/location"))
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.code").value("UNAUTHENTICATED"));

		MockHttpSession session = registerAndGetSession("world-nochar-" + System.nanoTime() + "@greyhaven.test");

		mockMvc.perform(get("/api/v1/world/location").session(session))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.code").value("NO_ACTIVE_CHARACTER"));
	}

	private MockHttpSession registerWithCharacter(String email) throws Exception {
		return registerWithCharacter(email, uniqueName("Traveler"));
	}

	private MockHttpSession registerWithCharacter(String email, String characterName) throws Exception {
		MockHttpSession session = registerAndGetSession(email);
		MvcResult created = mockMvc.perform(withCsrf(post("/api/v1/characters"))
						.session(session)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"name":"%s"}
								""".formatted(characterName)))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.currentLocationId").value(CITY_SQUARE_ID.toString()))
				.andReturn();
		refreshCsrfCookie(created);
		return session;
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
