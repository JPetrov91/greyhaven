package com.example.game.sparring.api;

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
import com.example.game.shared.domain.MutableRandomProvider;
import com.example.game.shared.domain.RandomProvider;
import com.jayway.jsonpath.JsonPath;

import jakarta.servlet.http.Cookie;

@Import({ TestcontainersConfiguration.class, SparringIntegrationTest.TestConfig.class })
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SparringIntegrationTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Autowired
	private RandomProvider randomProvider;

	private MutableRandomProvider mutableRandomProvider;
	private Cookie csrfCookie;

	@BeforeEach
	void setUp() throws Exception {
		mutableRandomProvider = (MutableRandomProvider) randomProvider;
		mutableRandomProvider.clear();
		MvcResult bootstrap = mockMvc.perform(get("/api/v1/bootstrap")).andExpect(status().isOk()).andReturn();
		csrfCookie = bootstrap.getResponse().getCookie("XSRF-TOKEN");
		assertThat(csrfCookie).isNotNull();
	}

	@Test
	void catalogListsTenNamedBots() throws Exception {
		MockHttpSession session = registerWithCharacter("bots-" + System.nanoTime() + "@greyhaven.test");
		mockMvc.perform(get("/api/v1/sparring/bots").session(session))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.length()").value(10))
				.andExpect(jsonPath("$[0].name").value("Green Recruit"))
				.andExpect(jsonPath("$[9].name").value("Watch Provost"));
	}

	@Test
	void drillRequiresYardAndLeavesEmptyRewards() throws Exception {
		MockHttpSession session = registerWithCharacter("yard-" + System.nanoTime() + "@greyhaven.test");
		UUID characterId = characterId(session);
		int xpBefore = jdbcTemplate.queryForObject(
				"select experience from characters where id = ?", Integer.class, characterId);

		mockMvc.perform(withCsrf(post("/api/v1/sparring/drills"))
						.session(session)
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"botLevel\":2}"))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.code").value("NOT_AT_SPARRING_YARD"));

		moveToYard(session);
		mutableRandomProvider.queue(0, 0);
		MvcResult started = mockMvc.perform(withCsrf(post("/api/v1/sparring/drills"))
						.session(session)
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"botLevel\":2}"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value("ACTIVE"))
				.andExpect(jsonPath("$.monster.name").value("Street Sparrer"))
				.andExpect(jsonPath("$.monster.level").value(2))
				.andExpect(jsonPath("$.enemyMaxHealth").value(org.hamcrest.Matchers.greaterThan(1)))
				.andReturn();
		UUID combatId = UUID.fromString(JsonPath.read(started.getResponse().getContentAsString(), "$.id"));
		int enemyMax = JsonPath.read(started.getResponse().getContentAsString(), "$.enemyMaxHealth");

		mockMvc.perform(get("/api/v1/combat/current").session(session))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(combatId.toString()))
				.andExpect(jsonPath("$.enemyMaxHealth").value(enemyMax))
				.andExpect(jsonPath("$.monster.name").value("Street Sparrer"));

		jdbcTemplate.update("update combat_sessions set enemy_health = 1 where id = ?", combatId);
		mutableRandomProvider.clear();
		mutableRandomProvider.queue(5, 90);
		mockMvc.perform(withCsrf(post("/api/v1/combat/" + combatId + "/actions"))
						.session(session)
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"action\":\"QUICK_ATTACK\",\"expectedRoundNumber\":0}"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value("PLAYER_WON"))
				.andExpect(jsonPath("$.rewards.xp").value(0))
				.andExpect(jsonPath("$.rewards.gold").value(0))
				.andExpect(jsonPath("$.rewards.items.length()").value(0));

		int xpAfter = jdbcTemplate.queryForObject(
				"select experience from characters where id = ?", Integer.class, characterId);
		assertThat(xpAfter).isEqualTo(xpBefore);
	}

	@Test
	void highLevelPlayerCannotDrill() throws Exception {
		MockHttpSession session = registerWithCharacter("high-" + System.nanoTime() + "@greyhaven.test");
		jdbcTemplate.update("update characters set level = 11 where id = ?", characterId(session));
		moveToYard(session);
		mockMvc.perform(withCsrf(post("/api/v1/sparring/drills"))
						.session(session)
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"botLevel\":1}"))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.code").value("SPARRING_LEVEL_REQUIRED"));
	}

	private void moveToYard(MockHttpSession session) throws Exception {
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
		String name = "S" + UUID.randomUUID().toString().replace("-", "").substring(0, 10);
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
	}
}
