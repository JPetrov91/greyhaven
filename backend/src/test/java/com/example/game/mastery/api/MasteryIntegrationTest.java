package com.example.game.mastery.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
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
import com.example.game.inventory.application.InventoryApplicationService;
import com.example.game.item.domain.ItemCodes;
import com.example.game.mastery.application.TechniqueLoadoutQuery;
import com.example.game.shared.domain.MutableRandomProvider;
import com.example.game.shared.domain.RandomProvider;
import com.jayway.jsonpath.JsonPath;

import jakarta.servlet.http.Cookie;

@Import({ TestcontainersConfiguration.class, MasteryIntegrationTest.RandomTestConfig.class })
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class MasteryIntegrationTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Autowired
	private RandomProvider randomProvider;

	@Autowired
	private InventoryApplicationService inventoryApplicationService;

	@Autowired
	private TechniqueLoadoutQuery techniqueLoadoutQuery;

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
	void flywaySeedsTwentyFiveTechniques() {
		Integer techniques = jdbcTemplate.queryForObject(
				"select count(*) from combat_technique_definitions",
				Integer.class);
		assertThat(techniques).isEqualTo(25);
	}

	@Test
	void newCharacterHasEmptyMasteriesAndCatalog() throws Exception {
		MockHttpSession session = registerWithCharacter("mastery-new-" + System.nanoTime() + "@greyhaven.test");

		mockMvc.perform(get("/api/v1/character/masteries").session(session))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.equippedWeaponFamily").value("SWORD"))
				.andExpect(jsonPath("$.masteries.length()").value(5))
				.andExpect(jsonPath("$.masteries[?(@.weaponFamily=='SWORD')].level").value(org.hamcrest.Matchers.hasItem(0)))
				.andExpect(jsonPath("$.masteries[?(@.weaponFamily=='AXE')].totalExperience")
						.value(org.hamcrest.Matchers.hasItem(0)));

		mockMvc.perform(get("/api/v1/character/techniques").session(session))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.techniques.length()").value(25))
				.andExpect(jsonPath("$.techniques[?(@.unlocked==true)]").isEmpty())
				.andExpect(jsonPath("$.loadout.slots.length()").value(4))
				.andExpect(jsonPath("$.loadout.compatibleWithEquippedWeapon").value(true));
	}

	@Test
	void swordVictoryGrantsOnlySwordMasteryAndIsIdempotent() throws Exception {
		String email = "mastery-win-" + System.nanoTime() + "@greyhaven.test";
		MockHttpSession session = registerWithCharacter(email);
		UUID characterId = characterIdForEmail(email);

		UUID combatId = startCombat(session);
		jdbcTemplate.update("update combat_sessions set enemy_health = 1 where id = ?", combatId);
		mutableRandomProvider.queue(5, 90);
		mockMvc.perform(withCsrf(post("/api/v1/combat/" + combatId + "/actions"))
						.session(session)
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"action\":\"QUICK_ATTACK\",\"expectedRoundNumber\":0}"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value("PLAYER_WON"));

		assertThat(masteryXp(characterId, "SWORD")).isEqualTo(12);
		assertThat(masteryXp(characterId, "AXE")).isZero();

		mockMvc.perform(withCsrf(post("/api/v1/combat/" + combatId + "/actions"))
						.session(session)
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"action\":\"QUICK_ATTACK\",\"expectedRoundNumber\":0}"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value("PLAYER_WON"));

		assertThat(masteryXp(characterId, "SWORD")).isEqualTo(12);
	}

	@Test
	void defeatAndRetreatGrantNoMasteryXp() throws Exception {
		String email = "mastery-loss-" + System.nanoTime() + "@greyhaven.test";
		MockHttpSession session = registerWithCharacter(email);
		UUID characterId = characterIdForEmail(email);

		UUID combatId = startCombat(session);
		jdbcTemplate.update("update combat_sessions set player_health = 1 where id = ?", combatId);
		mutableRandomProvider.queue(0, 8);
		mockMvc.perform(withCsrf(post("/api/v1/combat/" + combatId + "/actions"))
						.session(session)
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"action\":\"DEFEND\",\"expectedRoundNumber\":0}"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value("PLAYER_LOST"));
		mockMvc.perform(withCsrf(post("/api/v1/combat/" + combatId + "/acknowledge")).session(session))
				.andExpect(status().isNoContent());
		assertThat(masteryXp(characterId, "SWORD")).isZero();

		UUID retreatId = startCombat(session);
		mutableRandomProvider.queue(0);
		mockMvc.perform(withCsrf(post("/api/v1/combat/" + retreatId + "/actions"))
						.session(session)
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"action\":\"RETREAT\",\"expectedRoundNumber\":0}"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value("PLAYER_ESCAPED"));
		assertThat(masteryXp(characterId, "SWORD")).isZero();
	}

	@Test
	void axeVictoryGrantsAxeMasteryNotSword() throws Exception {
		String email = "mastery-axe-" + System.nanoTime() + "@greyhaven.test";
		MockHttpSession session = registerWithCharacter(email);
		UUID characterId = characterIdForEmail(email);

		inventoryApplicationService.grantItems(characterId, ItemCodes.IRON_AXE, 1);
		jdbcTemplate.update("update characters set level = 2 where id = ?", characterId);
		UUID axeId = itemInstanceId(characterId, ItemCodes.IRON_AXE);
		mockMvc.perform(withCsrf(post("/api/v1/inventory/" + axeId + "/equip")).session(session))
				.andExpect(status().isOk());

		winCombat(session);

		assertThat(masteryXp(characterId, "AXE")).isEqualTo(12);
		assertThat(masteryXp(characterId, "SWORD")).isZero();
		mockMvc.perform(get("/api/v1/character/masteries").session(session))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.equippedWeaponFamily").value("AXE"));
	}

	@Test
	void loadoutFamilyMismatchIsReportedButStillSavable() throws Exception {
		String email = "mastery-mismatch-" + System.nanoTime() + "@greyhaven.test";
		MockHttpSession session = registerWithCharacter(email);
		UUID characterId = characterIdForEmail(email);

		jdbcTemplate.update(
				"""
						update weapon_masteries
						set level = 1, total_experience = 188
						where character_id = ? and weapon_family = 'SWORD'
						""",
				characterId);
		winCombat(session);

		mockMvc.perform(withCsrf(put("/api/v1/character/technique-loadout"))
						.session(session)
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"slots\":[\"SWORD_RIPOSTE\",null,null,null]}"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.loadout.compatibleWithEquippedWeapon").value(true));

		inventoryApplicationService.grantItems(characterId, ItemCodes.HUNTING_BOW, 1);
		UUID bowId = itemInstanceId(characterId, ItemCodes.HUNTING_BOW);
		mockMvc.perform(withCsrf(post("/api/v1/inventory/" + bowId + "/equip")).session(session))
				.andExpect(status().isOk());

		mockMvc.perform(get("/api/v1/character/techniques").session(session))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.equippedWeaponFamily").value("BOW"))
				.andExpect(jsonPath("$.loadout.loadoutFamily").value("SWORD"))
				.andExpect(jsonPath("$.loadout.compatibleWithEquippedWeapon").value(false));
	}

	@Test
	void combatTechniquesIgnoreLoadoutWhenEquippedFamilyDiffers() throws Exception {
		String email = "mastery-combat-codes-" + System.nanoTime() + "@greyhaven.test";
		MockHttpSession session = registerWithCharacter(email);
		UUID characterId = characterIdForEmail(email);

		jdbcTemplate.update(
				"""
						update weapon_masteries
						set level = 1, total_experience = 188
						where character_id = ? and weapon_family = 'SWORD'
						""",
				characterId);
		winCombat(session);

		mockMvc.perform(withCsrf(put("/api/v1/character/technique-loadout"))
						.session(session)
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"slots\":[\"SWORD_RIPOSTE\",null,null,null]}"))
				.andExpect(status().isOk());

		assertThat(techniqueLoadoutQuery.activeTechniqueCodes(characterId)).containsExactly("SWORD_RIPOSTE");

		inventoryApplicationService.grantItems(characterId, ItemCodes.HUNTING_BOW, 1);
		UUID bowId = itemInstanceId(characterId, ItemCodes.HUNTING_BOW);
		mockMvc.perform(withCsrf(post("/api/v1/inventory/" + bowId + "/equip")).session(session))
				.andExpect(status().isOk());

		assertThat(techniqueLoadoutQuery.activeTechniqueCodes(characterId)).isEmpty();

		mockMvc.perform(withCsrf(post("/api/v1/inventory/" + bowId + "/unequip")).session(session))
				.andExpect(status().isOk());
		assertThat(techniqueLoadoutQuery.activeTechniqueCodes(characterId)).isEmpty();
	}

	@Test
	void unarmedVictoryGrantsNoMasteryXp() throws Exception {
		String email = "mastery-unarmed-" + System.nanoTime() + "@greyhaven.test";
		MockHttpSession session = registerWithCharacter(email);
		UUID characterId = characterIdForEmail(email);
		UUID swordId = itemInstanceId(characterId, ItemCodes.RUSTY_SWORD);

		mockMvc.perform(withCsrf(post("/api/v1/inventory/" + swordId + "/unequip")).session(session))
				.andExpect(status().isOk());

		winCombat(session);

		assertThat(masteryXp(characterId, "SWORD")).isZero();
	}

	@Test
	void crossingMasteryTwoUnlocksRiposteOnceAndLoadoutValidates() throws Exception {
		String email = "mastery-unlock-" + System.nanoTime() + "@greyhaven.test";
		MockHttpSession session = registerWithCharacter(email);
		UUID characterId = characterIdForEmail(email);

		jdbcTemplate.update(
				"""
						update weapon_masteries
						set level = 1, total_experience = 188
						where character_id = ? and weapon_family = 'SWORD'
						""",
				characterId);

		winCombat(session);

		assertThat(masteryLevel(characterId, "SWORD")).isEqualTo(2);
		Integer unlocks = jdbcTemplate.queryForObject(
				"""
						select count(*) from character_techniques
						where character_id = ? and technique_code = 'SWORD_RIPOSTE'
						""",
				Integer.class,
				characterId);
		assertThat(unlocks).isEqualTo(1);

		winCombat(session);
		Integer stillOne = jdbcTemplate.queryForObject(
				"""
						select count(*) from character_techniques
						where character_id = ? and technique_code = 'SWORD_RIPOSTE'
						""",
				Integer.class,
				characterId);
		assertThat(stillOne).isEqualTo(1);

		mockMvc.perform(withCsrf(put("/api/v1/character/technique-loadout"))
						.session(session)
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"slots\":[\"SWORD_RIPOSTE\",null,null,null]}"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.loadout.slots[0]").value("SWORD_RIPOSTE"))
				.andExpect(jsonPath("$.loadout.loadoutFamily").value("SWORD"))
				.andExpect(jsonPath("$.loadout.compatibleWithEquippedWeapon").value(true));

		mockMvc.perform(withCsrf(put("/api/v1/character/technique-loadout"))
						.session(session)
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"slots\":[\"SWORD_RIPOSTE\",\"BOW_AIMED_SHOT\",null,null]}"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.code").value("INVALID_TECHNIQUE_LOADOUT"));

		mockMvc.perform(withCsrf(put("/api/v1/character/technique-loadout"))
						.session(session)
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"slots\":[\"SWORD_RIPOSTE\",null,null]}"))
				.andExpect(status().isBadRequest());

		mockMvc.perform(withCsrf(put("/api/v1/character/technique-loadout"))
						.session(session)
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"slots\":[\"SWORD_MASTERY\",null,null,null]}"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.code").value("INVALID_TECHNIQUE_LOADOUT"));
	}

	@Test
	void loadoutCannotChangeDuringCombat() throws Exception {
		String email = "mastery-combat-" + System.nanoTime() + "@greyhaven.test";
		MockHttpSession session = registerWithCharacter(email);
		startCombat(session);

		mockMvc.perform(withCsrf(put("/api/v1/character/technique-loadout"))
						.session(session)
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"slots\":[null,null,null,null]}"))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.code").value("COMBAT_IN_PROGRESS"));
	}

	private void winCombat(MockHttpSession session) throws Exception {
		UUID combatId = startCombat(session);
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

	private UUID startCombat(MockHttpSession session) throws Exception {
		ensureAt(session, "OLD_TOWN");
		mutableRandomProvider.queue(1);
		MvcResult search = mockMvc.perform(withCsrf(post("/api/v1/encounters/search")).session(session))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.found").value(true))
				.andReturn();
		UUID encounterId = UUID.fromString(JsonPath.read(search.getResponse().getContentAsString(), "$.encounterId"));
		MvcResult fight = mockMvc.perform(withCsrf(post("/api/v1/encounters/" + encounterId + "/fight")).session(session))
				.andExpect(status().isOk())
				.andReturn();
		return UUID.fromString(JsonPath.read(fight.getResponse().getContentAsString(), "$.id"));
	}

	private int masteryXp(UUID characterId, String family) {
		return jdbcTemplate.queryForObject(
				"select total_experience from weapon_masteries where character_id = ? and weapon_family = ?",
				Integer.class,
				characterId,
				family);
	}

	private int masteryLevel(UUID characterId, String family) {
		return jdbcTemplate.queryForObject(
				"select level from weapon_masteries where character_id = ? and weapon_family = ?",
				Integer.class,
				characterId,
				family);
	}

	private void ensureAt(MockHttpSession session, String locationCode) throws Exception {
		MvcResult current = mockMvc.perform(get("/api/v1/world/location").session(session))
				.andExpect(status().isOk())
				.andReturn();
		String code = JsonPath.read(current.getResponse().getContentAsString(), "$.code");
		if (!locationCode.equals(code)) {
			moveTo(session, locationCode);
		}
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

	private UUID itemInstanceId(UUID characterId, String code) {
		return jdbcTemplate.queryForObject(
				"""
						select i.id
						from item_instances i
						join item_definitions d on d.id = i.item_definition_id
						where i.owner_character_id = ? and d.code = ?
						""",
				UUID.class,
				characterId,
				code);
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
		String name = "M" + UUID.randomUUID().toString().replace("-", "").substring(0, 10);
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
