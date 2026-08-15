package com.example.game.combat.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.Map;
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
import com.example.game.character.domain.ProgressionBalance;
import com.example.game.shared.domain.MutableRandomProvider;
import com.example.game.shared.domain.RandomProvider;
import com.jayway.jsonpath.JsonPath;

import jakarta.servlet.http.Cookie;

/**
 * High-risk combat settlement nets: multi-level XP in one grant, and once-per-character loot.
 */
@Import({ TestcontainersConfiguration.class, CombatSettlementSafetyNetIntegrationTest.RandomTestConfig.class })
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class CombatSettlementSafetyNetIntegrationTest {

	private static final UUID STREET_THUG_DAGGER_LOOT = UUID.fromString("e0000000-0000-4000-8000-000000000002");
	private static final UUID STREET_THUG_POTION_LOOT = UUID.fromString("e0000000-0000-4000-8000-000000000001");

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
		MvcResult bootstrap = mockMvc.perform(get("/api/v1/bootstrap"))
				.andExpect(status().isOk())
				.andReturn();
		csrfCookie = bootstrap.getResponse().getCookie("XSRF-TOKEN");
		assertThat(csrfCookie).isNotNull();
	}

	@Test
	void multiLevelCombatGrantPersistsPointsAndDoesNotDoubleOnRefresh() throws Exception {
		String email = "safe-multixp-" + System.nanoTime() + "@greyhaven.test";
		MockHttpSession session = registerWithCharacter(email);
		UUID characterId = characterIdForEmail(email);
		moveTo(session, "OLD_TOWN");
		UUID combatId = startStreetThugFight(session);
		jdbcTemplate.update("update combat_sessions set enemy_health = 1, planned_xp = 800 where id = ?", combatId);
		mutableRandomProvider.queue(5, 90);

		mockMvc.perform(withCsrf(post("/api/v1/combat/" + combatId + "/actions"))
						.session(session)
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"action\":\"QUICK_ATTACK\",\"expectedRoundNumber\":0}"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value("PLAYER_WON"))
				.andExpect(jsonPath("$.rewards.xp").value(800))
				.andExpect(jsonPath("$.rewards.previousLevel").value(1))
				.andExpect(jsonPath("$.rewards.newLevel").value(4))
				.andExpect(jsonPath("$.rewards.attributePointsGained").value(6));

		mockMvc.perform(get("/api/v1/character").session(session))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.level").value(4))
				.andExpect(jsonPath("$.experience").value(800))
				.andExpect(jsonPath("$.unspentAttributePoints").value(6))
				.andExpect(jsonPath("$.progression.experienceIntoCurrentLevel").value(
						800 - ProgressionBalance.cumulativeXpForLevel(4)));

		mockMvc.perform(get("/api/v1/character").session(session))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.level").value(4))
				.andExpect(jsonPath("$.unspentAttributePoints").value(6));

		MvcResult activity = mockMvc.perform(get("/api/v1/activity").session(session))
				.andExpect(status().isOk())
				.andReturn();
		List<Map<String, Object>> entries = JsonPath.read(activity.getResponse().getContentAsString(), "$");
		assertThat(entries.stream().filter(entry -> "LEVEL_UP".equals(entry.get("type"))).count()).isEqualTo(3);
		assertThat(intColumn("select experience from characters where id = ?", characterId)).isEqualTo(800);
		assertThat(intColumn("select unspent_attribute_points from characters where id = ?", characterId)).isEqualTo(6);
	}

	@Test
	void uniqueLootIsRecordedOnceAndExcludedFromLaterPlans() throws Exception {
		jdbcTemplate.update(
				"""
						update monster_loot_entries
						set drop_chance_percent = 100, once_per_character = true
						where id = ?
						""",
				STREET_THUG_DAGGER_LOOT);
		jdbcTemplate.update(
				"update monster_loot_entries set drop_chance_percent = 0 where id = ?",
				STREET_THUG_POTION_LOOT);
		try {
			String email = "safe-unique-" + System.nanoTime() + "@greyhaven.test";
			MockHttpSession session = registerWithCharacter(email);
			UUID characterId = characterIdForEmail(email);
			int daggersBefore = countItem(characterId, "OLD_DAGGER");

			moveTo(session, "OLD_TOWN");
			winStreetThug(session);
			assertThat(countItem(characterId, "OLD_DAGGER")).isEqualTo(daggersBefore + 1);
			assertThat(uniqueDropCount(characterId, "OLD_DAGGER")).isEqualTo(1);

			winStreetThug(session);
			assertThat(countItem(characterId, "OLD_DAGGER")).isEqualTo(daggersBefore + 1);
			assertThat(uniqueDropCount(characterId, "OLD_DAGGER")).isEqualTo(1);
		}
		finally {
			jdbcTemplate.update(
					"""
							update monster_loot_entries
							set drop_chance_percent = 10, once_per_character = false
							where id = ?
							""",
					STREET_THUG_DAGGER_LOOT);
			jdbcTemplate.update(
					"update monster_loot_entries set drop_chance_percent = 25 where id = ?",
					STREET_THUG_POTION_LOOT);
		}
	}

	private void winStreetThug(MockHttpSession session) throws Exception {
		UUID combatId = startStreetThugFight(session);
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
	}

	private UUID startStreetThugFight(MockHttpSession session) throws Exception {
		mutableRandomProvider.queue(1);
		MvcResult search = mockMvc.perform(withCsrf(post("/api/v1/encounters/search")).session(session))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.monster.code").value("STREET_THUG"))
				.andReturn();
		UUID encounterId = UUID.fromString(JsonPath.read(search.getResponse().getContentAsString(), "$.encounterId"));
		MvcResult fight = mockMvc.perform(withCsrf(post("/api/v1/encounters/" + encounterId + "/fight")).session(session))
				.andExpect(status().isOk())
				.andReturn();
		return UUID.fromString(JsonPath.read(fight.getResponse().getContentAsString(), "$.id"));
	}

	private int uniqueDropCount(UUID characterId, String itemCode) {
		return intColumn(
				"""
						select count(*) from character_unique_drops
						where character_id = ? and item_code = ?
						""",
				characterId,
				itemCode);
	}

	private int countItem(UUID characterId, String code) {
		return intColumn(
				"""
						select coalesce(sum(i.quantity), 0)
						from item_instances i
						join item_definitions d on d.id = i.item_definition_id
						where i.owner_character_id = ? and d.code = ?
						""",
				characterId,
				code);
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

	private MockHttpSession registerWithCharacter(String email) throws Exception {
		MockHttpSession session = new MockHttpSession();
		mockMvc.perform(withCsrf(post("/api/v1/auth/register"))
						.session(session)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"email":"%s","password":"password-123"}
								""".formatted(email)))
				.andExpect(status().isCreated());
		String name = "N" + UUID.randomUUID().toString().replace("-", "").substring(0, 10);
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
