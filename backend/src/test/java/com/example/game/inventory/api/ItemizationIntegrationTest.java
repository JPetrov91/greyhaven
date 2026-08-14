package com.example.game.inventory.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.nullValue;
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
import com.example.game.inventory.application.InventoryApplicationService;
import com.example.game.item.domain.ItemCodes;
import com.example.game.shared.domain.MutableRandomProvider;
import com.example.game.shared.domain.RandomProvider;

import jakarta.servlet.http.Cookie;

@Import({
		TestcontainersConfiguration.class,
		ItemizationIntegrationTest.RandomTestConfig.class
})
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ItemizationIntegrationTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Autowired
	private InventoryApplicationService inventoryApplicationService;

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
	void generatedItemPersistsRolledStateAcrossRefresh() throws Exception {
		String email = "item-gen-" + System.nanoTime() + "@greyhaven.test";
		MockHttpSession session = registerWithCharacter(email);
		UUID characterId = characterIdForEmail(email);
		mutableRandomProvider.queue(1, 100, 0, 0, 4);

		inventoryApplicationService.grantItems(characterId, ItemCodes.IRON_AXE, 1);
		UUID axeId = itemInstanceId(characterId, ItemCodes.IRON_AXE);

		MvcResult first = mockMvc.perform(get("/api/v1/inventory").session(session))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.items[?(@.code=='IRON_AXE')].legacy", hasItem(false)))
				.andExpect(jsonPath("$.items[?(@.code=='IRON_AXE')].rarity", hasItem("UNCOMMON")))
				.andExpect(jsonPath("$.items[?(@.code=='IRON_AXE')].weaponDamage", hasItem(13)))
				.andExpect(jsonPath("$.items[?(@.code=='IRON_AXE')].affixes.length()", hasItem(1)))
				.andExpect(jsonPath("$.items[?(@.id=='" + axeId + "')].affixes[0].code", hasItem("BALANCED")))
				.andReturn();
		String body = first.getResponse().getContentAsString();

		mockMvc.perform(get("/api/v1/inventory").session(session))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.items[?(@.id=='" + axeId + "')].rarity", hasItem("UNCOMMON")))
				.andExpect(jsonPath("$.items[?(@.id=='" + axeId + "')].affixes[0].magnitude", hasItem(4)));

		assertThat(mutableRandomProvider.remainingScripted()).isZero();
		mutableRandomProvider.queue(99, 99, 99);

		MvcResult second = mockMvc.perform(get("/api/v1/inventory").session(session)).andReturn();
		assertThat(second.getResponse().getContentAsString()).contains("BALANCED");
		assertThat(second.getResponse().getContentAsString()).isEqualTo(body);
		assertThat(mutableRandomProvider.remainingScripted()).isEqualTo(3);

		Integer instanceCount = jdbcTemplate.queryForObject(
				"select count(*) from item_instances where owner_character_id = ?",
				Integer.class,
				characterId);
		assertThat(instanceCount).isEqualTo(4);
	}

	@Test
	void twoHandedWeaponUnequipsOffHandAndThenBlocksOffHand() throws Exception {
		String email = "item-2h-" + System.nanoTime() + "@greyhaven.test";
		MockHttpSession session = registerWithCharacter(email);
		UUID characterId = characterIdForEmail(email);
		mutableRandomProvider.queue(1, 100, 1, 100);

		inventoryApplicationService.grantItems(characterId, ItemCodes.WOODEN_BUCKLER, 1);
		inventoryApplicationService.grantItems(characterId, ItemCodes.HUNTING_BOW, 1);
		UUID shieldId = itemInstanceId(characterId, ItemCodes.WOODEN_BUCKLER);
		UUID bowId = itemInstanceId(characterId, ItemCodes.HUNTING_BOW);

		mockMvc.perform(withCsrf(post("/api/v1/inventory/" + shieldId + "/equip")).session(session))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.equipment.slots.OFF_HAND").value(shieldId.toString()));

		mockMvc.perform(withCsrf(post("/api/v1/inventory/" + bowId + "/equip")).session(session))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.equipment.slots.MAIN_HAND").value(bowId.toString()))
				.andExpect(jsonPath("$.equipment.slots.OFF_HAND").value(nullValue()))
				.andExpect(jsonPath("$.items[?(@.code=='WOODEN_BUCKLER')].equipped", hasItem(false)));

		mockMvc.perform(withCsrf(post("/api/v1/inventory/" + shieldId + "/equip")).session(session))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.code").value("EQUIP_TWO_HANDED_BLOCKS_OFF_HAND"));
	}

	@Test
	void leatherCapUsesHeadSlot() throws Exception {
		String email = "item-head-" + System.nanoTime() + "@greyhaven.test";
		MockHttpSession session = registerWithCharacter(email);
		UUID characterId = characterIdForEmail(email);
		mutableRandomProvider.queue(1, 100);

		inventoryApplicationService.grantItems(characterId, ItemCodes.LEATHER_CAP, 1);
		UUID capId = itemInstanceId(characterId, ItemCodes.LEATHER_CAP);

		mockMvc.perform(withCsrf(post("/api/v1/inventory/" + capId + "/equip")).session(session))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.equipment.slots.HEAD").value(capId.toString()))
				.andExpect(jsonPath("$.equipment.slots.CHEST").isNotEmpty());
	}

	@Test
	void respecUnequipsGearThatNoLongerMeetsRequirements() throws Exception {
		String email = "item-respec-" + System.nanoTime() + "@greyhaven.test";
		MockHttpSession session = registerWithCharacter(email);
		UUID characterId = characterIdForEmail(email);
		mutableRandomProvider.queue(1, 100);
		jdbcTemplate.update(
				"update characters set unspent_attribute_points = 4 where id = ?",
				characterId);

		mockMvc.perform(withCsrf(post("/api/v1/character/attributes"))
						.session(session)
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"strength\":3,\"agility\":0,\"endurance\":1,\"perception\":0}"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.strength").value(8))
				.andExpect(jsonPath("$.endurance").value(6));

		inventoryApplicationService.grantItems(characterId, ItemCodes.IRON_HELM, 1);
		UUID helmId = itemInstanceId(characterId, ItemCodes.IRON_HELM);
		mockMvc.perform(withCsrf(post("/api/v1/inventory/" + helmId + "/equip")).session(session))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.equipment.slots.HEAD").value(helmId.toString()));

		mockMvc.perform(withCsrf(post("/api/v1/character/respec")).session(session))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.strength").value(5));

		mockMvc.perform(get("/api/v1/inventory").session(session))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.equipment.slots.HEAD").value(nullValue()))
				.andExpect(jsonPath("$.items[?(@.code=='IRON_HELM')].equipped", hasItem(false)));
	}

	@Test
	void inventoryIncludesComparisonAgainstEquippedSlot() throws Exception {
		String email = "item-compare-" + System.nanoTime() + "@greyhaven.test";
		MockHttpSession session = registerWithCharacter(email);
		UUID characterId = characterIdForEmail(email);
		mutableRandomProvider.queue(1, 100, 0, 0, 3);

		inventoryApplicationService.grantItems(characterId, ItemCodes.IRON_AXE, 1);

		mockMvc.perform(get("/api/v1/inventory").session(session))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.items[?(@.code=='IRON_AXE')].comparison.slot", hasItem("MAIN_HAND")))
				.andExpect(jsonPath("$.items[?(@.code=='IRON_AXE')].comparison.verdict", hasItem("MIXED")))
				.andExpect(jsonPath("$.items[?(@.code=='IRON_AXE')].comparison.deltas[?(@.stat=='Damage')].delta",
						hasItem(7)))
				.andExpect(jsonPath("$.items[?(@.code=='IRON_AXE')].comparison.deltas[?(@.stat=='Accuracy')].delta",
						hasItem(-1)))
				.andExpect(jsonPath("$.items[?(@.code=='IRON_AXE')].comparison.deltas[?(@.stat=='Critical')].delta",
						hasItem(2)));
	}

	@Test
	void newLegacyTemplateDropsAreRolledAndNotMarkedLegacy() throws Exception {
		String email = "item-legacy-template-" + System.nanoTime() + "@greyhaven.test";
		MockHttpSession session = registerWithCharacter(email);
		UUID characterId = characterIdForEmail(email);
		mutableRandomProvider.queue(1, 100, 0, 0, 4);

		inventoryApplicationService.grantItems(characterId, ItemCodes.IRON_SWORD, 1);

		mockMvc.perform(get("/api/v1/inventory").session(session))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.items[?(@.code=='IRON_SWORD')].legacy", hasItem(false)))
				.andExpect(jsonPath("$.items[?(@.code=='IRON_SWORD')].rarity", hasItem("UNCOMMON")))
				.andExpect(jsonPath("$.items[?(@.code=='IRON_SWORD')].affixes.length()", hasItem(1)));
	}

	@Test
	void additionalLightArmorDoesNotStackCategoryDodge() throws Exception {
		String email = "item-dodge-" + System.nanoTime() + "@greyhaven.test";
		MockHttpSession session = registerWithCharacter(email);
		UUID characterId = characterIdForEmail(email);
		mutableRandomProvider.queue(1, 100);

		mockMvc.perform(get("/api/v1/inventory").session(session))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.derivedStats.dodge").value(8));

		inventoryApplicationService.grantItems(characterId, ItemCodes.LEATHER_CAP, 1);
		UUID capId = itemInstanceId(characterId, ItemCodes.LEATHER_CAP);
		mockMvc.perform(withCsrf(post("/api/v1/inventory/" + capId + "/equip")).session(session))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.derivedStats.dodge").value(8));
	}

	@Test
	void respecUnequipsOffHandLeftOnATwoHandedWeapon() throws Exception {
		String email = "item-respec-2h-" + System.nanoTime() + "@greyhaven.test";
		MockHttpSession session = registerWithCharacter(email);
		UUID characterId = characterIdForEmail(email);
		mutableRandomProvider.queue(1, 100, 1, 100);

		inventoryApplicationService.grantItems(characterId, ItemCodes.HUNTING_BOW, 1);
		inventoryApplicationService.grantItems(characterId, ItemCodes.WOODEN_BUCKLER, 1);
		UUID bowId = itemInstanceId(characterId, ItemCodes.HUNTING_BOW);
		UUID shieldId = itemInstanceId(characterId, ItemCodes.WOODEN_BUCKLER);
		mockMvc.perform(withCsrf(post("/api/v1/inventory/" + bowId + "/equip")).session(session))
				.andExpect(status().isOk());

		jdbcTemplate.update(
				"""
						insert into equipment (id, character_id, slot, item_instance_id)
						values (?, ?, 'OFF_HAND', ?)
						""",
				UUID.randomUUID(),
				characterId,
				shieldId);

		mockMvc.perform(withCsrf(post("/api/v1/character/respec")).session(session))
				.andExpect(status().isOk());

		mockMvc.perform(get("/api/v1/inventory").session(session))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.equipment.slots.MAIN_HAND").value(bowId.toString()))
				.andExpect(jsonPath("$.equipment.slots.OFF_HAND").value(nullValue()));
	}

	@Test
	void calculatedItemStatsIncludeAffixes() throws Exception {
		String email = "item-calc-" + System.nanoTime() + "@greyhaven.test";
		MockHttpSession session = registerWithCharacter(email);
		UUID characterId = characterIdForEmail(email);
		mutableRandomProvider.queue(1, 100, 0, 2, 8);

		inventoryApplicationService.grantItems(characterId, ItemCodes.IRON_AXE, 1);

		mockMvc.perform(get("/api/v1/inventory").session(session))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.items[?(@.code=='IRON_AXE')].rolledWeaponDamage", hasItem(13)))
				.andExpect(jsonPath("$.items[?(@.code=='IRON_AXE')].weaponDamage", hasItem(14)))
				.andExpect(jsonPath("$.items[?(@.code=='IRON_AXE')].affixes[0].code", hasItem("SHARP")));
	}

	@Test
	void copperAmuletEquipsInAmuletSlot() throws Exception {
		String email = "item-amulet-" + System.nanoTime() + "@greyhaven.test";
		MockHttpSession session = registerWithCharacter(email);
		UUID characterId = characterIdForEmail(email);
		mutableRandomProvider.queue(1);

		inventoryApplicationService.grantItems(characterId, ItemCodes.COPPER_AMULET, 1);
		UUID amuletId = itemInstanceId(characterId, ItemCodes.COPPER_AMULET);

		mockMvc.perform(withCsrf(post("/api/v1/inventory/" + amuletId + "/equip")).session(session))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.equipment.slots.AMULET").value(amuletId.toString()))
				.andExpect(jsonPath("$.items[?(@.code=='COPPER_AMULET')].type", hasItem("ACCESSORY")));
	}

	private UUID itemInstanceId(UUID characterId, String code) {
		return jdbcTemplate.queryForObject(
				"""
						select i.id from item_instances i
						join item_definitions d on d.id = i.item_definition_id
						where i.owner_character_id = ? and d.code = ?
						order by i.created_at
						limit 1
						""",
				UUID.class,
				characterId,
				code);
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

		String name = "Itm" + UUID.randomUUID().toString().replace("-", "").substring(0, 10);
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

	@TestConfiguration
	static class RandomTestConfig {

		@Bean
		@Primary
		RandomProvider mutableTestRandomProvider() {
			return new MutableRandomProvider();
		}
	}
}
