package com.example.game.quest.api;

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
import com.example.game.quest.domain.QuestCodes;
import com.example.game.world.domain.LocationCodes;

import jakarta.servlet.http.Cookie;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class QuestBoardIntegrationTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private JdbcTemplate jdbcTemplate;

	private Cookie csrfCookie;

	@BeforeEach
	void setUp() throws Exception {
		MvcResult bootstrap = mockMvc.perform(get("/api/v1/bootstrap")).andExpect(status().isOk()).andReturn();
		csrfCookie = bootstrap.getResponse().getCookie("XSRF-TOKEN");
		assertThat(csrfCookie).isNotNull();
	}

	@Test
	void citySquareBoardListsFixtureQuestsAndAcceptsAtBoard() throws Exception {
		MockHttpSession session = registerWithCharacter("board-" + System.nanoTime() + "@greyhaven.test");

		mockMvc.perform(get("/api/v1/world/location").session(session))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.actions[?(@=='NOTICE_BOARD')]").exists());

		mockMvc.perform(get("/api/v1/world/locations/CITY_SQUARE/quest-board").session(session))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.locationCode").value(LocationCodes.CITY_SQUARE))
				.andExpect(jsonPath("$.quests[?(@.code=='QST_RAT_PROBLEM')].listState").value("AVAILABLE"))
				.andExpect(jsonPath("$.quests[?(@.code=='QST_MISSING_CARAVAN')].listState").value("UNAVAILABLE"))
				.andExpect(jsonPath("$.quests[?(@.code=='QST_MILITIA_NOTICE')]").isEmpty());

		mockMvc.perform(get("/api/v1/quests/" + QuestCodes.RAT_PROBLEM).session(session))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.questType").value("EXTERMINATION"))
				.andExpect(jsonPath("$.difficulty").value("EASY"))
				.andExpect(jsonPath("$.boardLocationCode").value(LocationCodes.CITY_SQUARE))
				.andExpect(jsonPath("$.actionHint").value("TRAVEL"))
				.andExpect(jsonPath("$.actionLocationCode").value(LocationCodes.SEWERS));

		moveTo(session, LocationCodes.OLD_TOWN);
		mockMvc.perform(get("/api/v1/world/locations/CITY_SQUARE/quest-board").session(session))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.code").value("QUEST_WRONG_LOCATION"));
		mockMvc.perform(withCsrf(post("/api/v1/quests/" + QuestCodes.RAT_PROBLEM + "/accept")).session(session))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.code").value("QUEST_WRONG_LOCATION"));

		moveTo(session, LocationCodes.CITY_SQUARE);
		mockMvc.perform(withCsrf(post("/api/v1/quests/" + QuestCodes.RAT_PROBLEM + "/accept")).session(session))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value("ACTIVE"))
				.andExpect(jsonPath("$.tracked").value(true));

		mockMvc.perform(get("/api/v1/world/locations/CITY_SQUARE/quest-board").session(session))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.quests[?(@.code=='QST_RAT_PROBLEM')].listState").value("ACTIVE"));

		mockMvc.perform(withCsrf(post("/api/v1/quests/" + QuestCodes.RAT_PROBLEM + "/accept")).session(session))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.code").value("QUEST_ALREADY_ACCEPTED"));

		mockMvc.perform(withCsrf(post("/api/v1/quests/" + QuestCodes.MISSING_CARAVAN + "/accept")).session(session))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.code").value("QUEST_NOT_AVAILABLE"));
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

	private MockHttpSession registerWithCharacter(String email) throws Exception {
		MockHttpSession session = new MockHttpSession();
		mockMvc.perform(withCsrf(post("/api/v1/auth/register"))
						.session(session)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"email":"%s","password":"password-123"}
								""".formatted(email)))
				.andExpect(status().isCreated());
		String name = "B" + UUID.randomUUID().toString().replace("-", "").substring(0, 10);
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
}
