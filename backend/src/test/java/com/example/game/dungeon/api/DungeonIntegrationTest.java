package com.example.game.dungeon.api;

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

@Import({ TestcontainersConfiguration.class, DungeonIntegrationTest.RandomTestConfig.class })
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class DungeonIntegrationTest {

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
	void ruinedKeepPersistsBranchOptionalSkipBossRewardAndReload() throws Exception {
		String email = "dungeon-" + System.nanoTime() + "@greyhaven.test";
		MockHttpSession session = registerWithCharacter(email);
		UUID characterId = characterIdForEmail(email);

		moveTo(session, "NORTH_ROAD");
		moveTo(session, "BANDIT_CAMP");
		mockMvc.perform(get("/api/v1/world/location").session(session))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.safety").value("DANGEROUS"))
				.andExpect(jsonPath("$.recommendedLevelMin").value(12));
		moveTo(session, "ANCIENT_RUINS");

		mockMvc.perform(withCsrf(post("/api/v1/dungeons/enter")).session(session))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.dungeonCode").value("RUINED_KEEP"))
				.andExpect(jsonPath("$.currentRoomCode").value("ENTRANCE"))
				.andExpect(jsonPath("$.status").value("ACTIVE"));

		mockMvc.perform(get("/api/v1/dungeons/current").session(session))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.currentRoomCode").value("ENTRANCE"))
				.andExpect(jsonPath("$.choices[0].edgeCode").value("CONTINUE"));

		advance(session, "CONTINUE");
		mockMvc.perform(get("/api/v1/dungeons/current").session(session))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.currentRoomCode").value("GUARD_ROOM"))
				.andExpect(jsonPath("$.encounter.monster.code").value("CAVE_BRUTE"));

		winCurrentDungeonFight(session);
		advance(session, "CONTINUE");
		mockMvc.perform(get("/api/v1/dungeons/current").session(session))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.currentRoomCode").value("COURTYARD"));

		advance(session, "ARMORY");
		mockMvc.perform(get("/api/v1/dungeons/current").session(session))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.chosenBranch").value("ARMORY"))
				.andExpect(jsonPath("$.rooms[?(@.code=='PRISON')].state").value("SKIPPED"))
				.andExpect(jsonPath("$.currentRoomCode").value("ARMORY"));

		winCurrentDungeonFight(session);
		advance(session, "CONTINUE");
		winCurrentDungeonFight(session);

		mockMvc.perform(get("/api/v1/dungeons/current").session(session))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.currentRoomCode").value("COMMAND_HALL"))
				.andExpect(jsonPath("$.choices[?(@.edgeCode=='OPTIONAL')]").exists())
				.andExpect(jsonPath("$.choices[?(@.edgeCode=='CONTINUE')]").exists());

		advance(session, "CONTINUE");
		mockMvc.perform(get("/api/v1/dungeons/current").session(session))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.rooms[?(@.code=='CRYPT')].state").value("SKIPPED"))
				.andExpect(jsonPath("$.currentRoomCode").value("THRONE"))
				.andExpect(jsonPath("$.encounter.monster.code").value("WARDEN_OF_THE_KEEP"))
				.andExpect(jsonPath("$.encounter.monster.tier").value("BOSS"));

		int signetsBefore = countItem(characterId, "WARDENS_SIGNET");
		String combatId = winCurrentDungeonFight(session);
		assertThat(countItem(characterId, "WARDENS_SIGNET")).isEqualTo(signetsBefore + 1);

		mockMvc.perform(withCsrf(post("/api/v1/combat/" + combatId + "/actions"))
						.session(session)
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"action\":\"QUICK_ATTACK\",\"expectedRoundNumber\":0}"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value("PLAYER_WON"));
		assertThat(countItem(characterId, "WARDENS_SIGNET")).isEqualTo(signetsBefore + 1);

		Boolean unique = jdbcTemplate.queryForObject(
				"""
						select unique_reward_granted from dungeon_runs
						where character_id = ? order by created_at desc limit 1
						""",
				Boolean.class,
				characterId);
		assertThat(unique).isTrue();
		Integer uniqueDropRows = jdbcTemplate.queryForObject(
				"""
						select count(*) from character_unique_drops
						where character_id = ? and item_code = 'WARDENS_SIGNET'
						""",
				Integer.class,
				characterId);
		assertThat(uniqueDropRows).isEqualTo(1);

		Integer affixes = jdbcTemplate.queryForObject(
				"""
						select count(*) from item_instance_affixes a
						join item_instances i on i.id = a.item_instance_id
						join item_definitions d on d.id = i.item_definition_id
						where i.owner_character_id = ? and d.code = 'WARDENS_SIGNET'
						""",
				Integer.class,
				characterId);
		assertThat(affixes).isZero();

		mockMvc.perform(get("/api/v1/dungeons/current").session(session)).andExpect(status().isNoContent());

		mockMvc.perform(withCsrf(post("/api/v1/dungeons/enter")).session(session))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.uniqueRewardGranted").value(true));
		clearKeepFromEntrance(session);
		assertThat(countItem(characterId, "WARDENS_SIGNET")).isEqualTo(signetsBefore + 1);
	}

	@Test
	void leaveKeepDefersTheRoomFightAndAllowsTravel() throws Exception {
		String email = "dungeon-leave-" + System.nanoTime() + "@greyhaven.test";
		MockHttpSession session = registerWithCharacter(email);

		moveTo(session, "NORTH_ROAD");
		moveTo(session, "BANDIT_CAMP");
		moveTo(session, "ANCIENT_RUINS");
		mockMvc.perform(withCsrf(post("/api/v1/dungeons/enter")).session(session)).andExpect(status().isOk());
		advance(session, "CONTINUE");
		mockMvc.perform(get("/api/v1/encounters/current").session(session))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.monster.code").value("CAVE_BRUTE"));

		mockMvc.perform(withCsrf(post("/api/v1/dungeons/leave")).session(session))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.paused").value(true));
		mockMvc.perform(get("/api/v1/encounters/current").session(session)).andExpect(status().isNoContent());

		moveTo(session, "BANDIT_CAMP");
		mockMvc.perform(get("/api/v1/world/location").session(session))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.code").value("BANDIT_CAMP"));
	}

	@Test
	void eliteBanditVeteranGuaranteesTokenUnlikeNormalBandit() throws Exception {
		String email = "elite-" + System.nanoTime() + "@greyhaven.test";
		MockHttpSession session = registerWithCharacter(email);
		UUID characterId = characterIdForEmail(email);
		moveTo(session, "NORTH_ROAD");
		mutableRandomProvider.queue(71);
		MvcResult search = mockMvc.perform(withCsrf(post("/api/v1/encounters/search")).session(session))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.monster.code").value("BANDIT_VETERAN"))
				.andExpect(jsonPath("$.monster.tier").value("ELITE"))
				.andReturn();
		UUID encounterId = UUID.fromString(JsonPath.read(search.getResponse().getContentAsString(), "$.encounterId"));
		MvcResult fight = mockMvc.perform(withCsrf(post("/api/v1/encounters/" + encounterId + "/fight")).session(session))
				.andExpect(status().isOk())
				.andReturn();
		UUID combatId = UUID.fromString(JsonPath.read(fight.getResponse().getContentAsString(), "$.id"));
		jdbcTemplate.update("update combat_sessions set enemy_health = 1 where id = ?", combatId);
		mutableRandomProvider.queue(5, 90);
		mockMvc.perform(withCsrf(post("/api/v1/combat/" + combatId + "/actions"))
						.session(session)
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"action\":\"QUICK_ATTACK\",\"expectedRoundNumber\":0}"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value("PLAYER_WON"));
		assertThat(countItem(characterId, "BANDIT_TOKEN")).isEqualTo(1);
	}

	private void clearKeepFromEntrance(MockHttpSession session) throws Exception {
		advance(session, "CONTINUE");
		winCurrentDungeonFight(session);
		advance(session, "CONTINUE");
		advance(session, "ARMORY");
		winCurrentDungeonFight(session);
		advance(session, "CONTINUE");
		winCurrentDungeonFight(session);
		advance(session, "CONTINUE");
		winCurrentDungeonFight(session);
	}

	private String winCurrentDungeonFight(MockHttpSession session) throws Exception {
		MvcResult current = mockMvc.perform(get("/api/v1/encounters/current").session(session))
				.andExpect(status().isOk())
				.andReturn();
		UUID encounterId = UUID.fromString(JsonPath.read(current.getResponse().getContentAsString(), "$.encounterId"));
		MvcResult fight = mockMvc.perform(withCsrf(post("/api/v1/encounters/" + encounterId + "/fight")).session(session))
				.andExpect(status().isOk())
				.andReturn();
		UUID combatId = UUID.fromString(JsonPath.read(fight.getResponse().getContentAsString(), "$.id"));
		jdbcTemplate.update("update combat_sessions set enemy_health = 1 where id = ?", combatId);
		mutableRandomProvider.queue(5, 90);
		mockMvc.perform(withCsrf(post("/api/v1/combat/" + combatId + "/actions"))
						.session(session)
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"action\":\"QUICK_ATTACK\",\"expectedRoundNumber\":0}"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value("PLAYER_WON"));
		mockMvc.perform(withCsrf(post("/api/v1/combat/" + combatId + "/acknowledge")).session(session))
				.andExpect(status().isNoContent());
		return combatId.toString();
	}

	private void advance(MockHttpSession session, String edgeCode) throws Exception {
		mockMvc.perform(withCsrf(post("/api/v1/dungeons/advance")).session(session)
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"edgeCode\":\"%s\"}".formatted(edgeCode)))
				.andExpect(status().isOk());
	}

	private int countItem(UUID characterId, String code) {
		Integer count = jdbcTemplate.queryForObject(
				"""
						select coalesce(sum(i.quantity), 0)
						from item_instances i
						join item_definitions d on d.id = i.item_definition_id
						where i.owner_character_id = ? and d.code = ?
						""",
				Integer.class,
				characterId,
				code);
		return count == null ? 0 : count;
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
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.code").value(locationCode));
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

	private MockHttpSession registerWithCharacter(String email) throws Exception {
		MockHttpSession session = new MockHttpSession();
		mockMvc.perform(withCsrf(post("/api/v1/auth/register"))
						.session(session)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"email":"%s","password":"password-123"}
								""".formatted(email)))
				.andExpect(status().isCreated());
		String name = "D" + UUID.randomUUID().toString().replace("-", "").substring(0, 10);
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
	static class RandomTestConfig {
		@Bean
		@Primary
		RandomProvider mutableTestRandomProvider() {
			return new MutableRandomProvider();
		}
	}
}
