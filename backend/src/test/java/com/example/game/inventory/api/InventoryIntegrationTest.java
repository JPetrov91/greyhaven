package com.example.game.inventory.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hamcrest.Matchers.containsInAnyOrder;
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
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
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
import com.example.game.shared.api.ApiException;

import jakarta.servlet.http.Cookie;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class InventoryIntegrationTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Autowired
	private InventoryApplicationService inventoryApplicationService;

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
	void flywaySeededItemDefinitions() {
		Integer definitionCount = jdbcTemplate.queryForObject(
				"select count(*) from item_definitions",
				Integer.class);
		Integer flywayV6 = jdbcTemplate.queryForObject(
				"select count(*) from flyway_schema_history where version = '6' and success = true",
				Integer.class);
		Integer flywayV8 = jdbcTemplate.queryForObject(
				"select count(*) from flyway_schema_history where version = '8' and success = true",
				Integer.class);
		Integer flywayV18 = jdbcTemplate.queryForObject(
				"select count(*) from flyway_schema_history where version = '18' and success = true",
				Integer.class);

		Integer flywayV20 = jdbcTemplate.queryForObject(
				"select count(*) from flyway_schema_history where version = '20' and success = true",
				Integer.class);
		Integer flywayV27 = jdbcTemplate.queryForObject(
				"select count(*) from flyway_schema_history where version = '27' and success = true",
				Integer.class);
		Integer modifierCount = jdbcTemplate.queryForObject(
				"select count(*) from item_definition_modifiers",
				Integer.class);
		Integer rustyAccuracy = jdbcTemplate.queryForObject(
				"""
						select m.magnitude from item_definition_modifiers m
						join item_definitions d on d.id = m.item_definition_id
						where d.code = 'RUSTY_SWORD' and m.stat = 'ACCURACY'
						""",
				Integer.class);
		Integer chestModifiers = jdbcTemplate.queryForObject(
				"""
						select count(*) from item_definition_modifiers m
						join item_definitions d on d.id = m.item_definition_id
						where d.code = 'WORN_LEATHER_ARMOR'
						""",
				Integer.class);

		assertThat(definitionCount).isEqualTo(31);
		assertThat(flywayV6).isEqualTo(1);
		assertThat(flywayV8).isEqualTo(1);
		assertThat(flywayV18).isEqualTo(1);
		assertThat(flywayV20).isEqualTo(1);
		assertThat(flywayV27).isEqualTo(1);
		assertThat(modifierCount).isEqualTo(29);
		assertThat(rustyAccuracy).isEqualTo(4);
		assertThat(chestModifiers).isZero();
	}

	@Test
	void newCharacterReceivesEquippedStarterLoadoutAndDerivedStats() throws Exception {
		MockHttpSession session = registerWithCharacter("inv-start-" + System.nanoTime() + "@greyhaven.test");

		mockMvc.perform(get("/api/v1/inventory").session(session))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.capacity").value(40))
				.andExpect(jsonPath("$.usedSlots").value(3))
				.andExpect(jsonPath("$.items.length()").value(3))
				.andExpect(jsonPath("$.equipment.slots.MAIN_HAND").isNotEmpty())
				.andExpect(jsonPath("$.equipment.slots.CHEST").isNotEmpty())
				.andExpect(jsonPath("$.equipment.slots.HEAD").value(nullValue()))
				.andExpect(jsonPath("$.items[?(@.code=='RUSTY_SWORD')].legacy", hasItem(false)))
				.andExpect(jsonPath("$.items[?(@.code=='RUSTY_SWORD')].affixes.length()", hasItem(0)))
				.andExpect(jsonPath("$.items[?(@.code=='RUSTY_SWORD')].accuracy", hasItem(4)))
				.andExpect(jsonPath("$.items[?(@.code=='RUSTY_SWORD')].criticalChance", hasItem(1)))
				.andExpect(jsonPath("$.derivedStats.physicalDamage").value(14))
				.andExpect(jsonPath("$.derivedStats.armor").value(3))
				.andExpect(jsonPath("$.derivedStats.accuracy").value(87))
				.andExpect(jsonPath("$.derivedStats.dodge").value(8))
				.andExpect(jsonPath("$.derivedStats.criticalChance").value(8));

		mockMvc.perform(get("/api/v1/character").session(session))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.derivedStats.physicalDamage").value(14))
				.andExpect(jsonPath("$.derivedStats.armor").value(3));
	}

	@Test
	void unequipAndEquipUpdateEquipmentAndDerivedStats() throws Exception {
		String email = "inv-equip-" + System.nanoTime() + "@greyhaven.test";
		MockHttpSession session = registerWithCharacter(email);
		UUID weaponId = itemInstanceId(characterIdForEmail(email), ItemCodes.RUSTY_SWORD);

		mockMvc.perform(withCsrf(post("/api/v1/inventory/" + weaponId + "/unequip")).session(session))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.equipment.slots.MAIN_HAND").value(nullValue()))
				.andExpect(jsonPath("$.derivedStats.physicalDamage").value(8))
				.andExpect(jsonPath("$.items[?(@.code=='RUSTY_SWORD')].equipped", hasItem(false)));

		mockMvc.perform(withCsrf(post("/api/v1/inventory/" + weaponId + "/equip")).session(session))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.equipment.slots.MAIN_HAND").value(weaponId.toString()))
				.andExpect(jsonPath("$.derivedStats.physicalDamage").value(14));
	}

	@Test
	void equipmentAttributeRequirementsAreEnforced() throws Exception {
		String email = "inv-attr-req-" + System.nanoTime() + "@greyhaven.test";
		MockHttpSession session = registerWithCharacter(email);
		UUID characterId = characterIdForEmail(email);

		inventoryApplicationService.grantItems(characterId, ItemCodes.IRON_HELM, 1);
		UUID helmId = itemInstanceId(characterId, ItemCodes.IRON_HELM);

		mockMvc.perform(withCsrf(post("/api/v1/inventory/" + helmId + "/equip")).session(session))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.code").value("EQUIP_REQUIREMENTS_NOT_MET"));
	}

	@Test
	void equipmentRequirementsAreEnforced() throws Exception {
		String email = "inv-req-" + System.nanoTime() + "@greyhaven.test";
		MockHttpSession session = registerWithCharacter(email);
		UUID characterId = characterIdForEmail(email);

		inventoryApplicationService.grantItems(characterId, ItemCodes.IRON_SWORD, 1);
		UUID ironSwordId = itemInstanceId(characterId, ItemCodes.IRON_SWORD);

		mockMvc.perform(withCsrf(post("/api/v1/inventory/" + ironSwordId + "/equip")).session(session))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.code").value("EQUIP_REQUIREMENTS_NOT_MET"));
	}

	@Test
	void nonEquippableItemsCannotBeEquipped() throws Exception {
		String email = "inv-material-" + System.nanoTime() + "@greyhaven.test";
		MockHttpSession session = registerWithCharacter(email);
		UUID characterId = characterIdForEmail(email);

		inventoryApplicationService.grantItems(characterId, ItemCodes.WOLF_PELT, 1);
		UUID peltId = itemInstanceId(characterId, ItemCodes.WOLF_PELT);

		mockMvc.perform(withCsrf(post("/api/v1/inventory/" + peltId + "/equip")).session(session))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.code").value("ITEM_NOT_EQUIPPABLE"));
	}

	@Test
	void unequippingAnItemThatIsNotEquippedIsRejected() throws Exception {
		String email = "inv-notequipped-" + System.nanoTime() + "@greyhaven.test";
		MockHttpSession session = registerWithCharacter(email);
		UUID potionId = itemInstanceId(characterIdForEmail(email), ItemCodes.HEALING_POTION);

		mockMvc.perform(withCsrf(post("/api/v1/inventory/" + potionId + "/unequip")).session(session))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.code").value("ITEM_NOT_EQUIPPED"));
	}

	@Test
	void nonConsumableItemsCannotBeUsed() throws Exception {
		String email = "inv-notusable-" + System.nanoTime() + "@greyhaven.test";
		MockHttpSession session = registerWithCharacter(email);
		UUID weaponId = itemInstanceId(characterIdForEmail(email), ItemCodes.RUSTY_SWORD);

		mockMvc.perform(withCsrf(post("/api/v1/inventory/" + weaponId + "/use")).session(session))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.code").value("ITEM_NOT_USABLE"));
	}

	@Test
	void ownershipIsValidatedForEveryMutation() throws Exception {
		String ownerEmail = "inv-owner-" + System.nanoTime() + "@greyhaven.test";
		registerWithCharacter(ownerEmail);
		MockHttpSession thiefSession = registerWithCharacter("inv-thief-" + System.nanoTime() + "@greyhaven.test");
		UUID ownerCharacterId = characterIdForEmail(ownerEmail);
		UUID ownerWeaponId = itemInstanceId(ownerCharacterId, ItemCodes.RUSTY_SWORD);
		UUID ownerPotionId = itemInstanceId(ownerCharacterId, ItemCodes.HEALING_POTION);

		for (String path : new String[] {
				"/api/v1/inventory/" + ownerWeaponId + "/equip",
				"/api/v1/inventory/" + ownerWeaponId + "/unequip",
				"/api/v1/inventory/" + ownerPotionId + "/use" }) {
			mockMvc.perform(withCsrf(post(path)).session(thiefSession))
					.andExpect(status().isForbidden())
					.andExpect(jsonPath("$.code").value("ITEM_NOT_OWNED"));
		}

		Integer ownerPotions = jdbcTemplate.queryForObject(
				"select quantity from item_instances where id = ?",
				Integer.class,
				ownerPotionId);
		assertThat(ownerPotions).isEqualTo(2);
	}

	@Test
	void malformedItemIdIsRejectedAsClientError() throws Exception {
		MockHttpSession session = registerWithCharacter("inv-badid-" + System.nanoTime() + "@greyhaven.test");

		mockMvc.perform(withCsrf(post("/api/v1/inventory/not-a-uuid/equip")).session(session))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.code").value("MALFORMED_REQUEST"));
	}

	@Test
	void databaseRejectsEquipmentPointingAtAnotherCharactersItem() throws Exception {
		String ownerEmail = "inv-fk-owner-" + System.nanoTime() + "@greyhaven.test";
		String otherEmail = "inv-fk-other-" + System.nanoTime() + "@greyhaven.test";
		registerWithCharacter(ownerEmail);
		registerWithCharacter(otherEmail);
		UUID ownerCharacterId = characterIdForEmail(ownerEmail);
		UUID otherCharacterId = characterIdForEmail(otherEmail);

		// An unequipped weapon, so the insert below can only fail on the ownership foreign key.
		inventoryApplicationService.grantItems(ownerCharacterId, ItemCodes.OLD_DAGGER, 1);
		UUID foreignDaggerId = itemInstanceId(ownerCharacterId, ItemCodes.OLD_DAGGER);
		jdbcTemplate.update("delete from equipment where character_id = ?", otherCharacterId);

		assertThatThrownBy(() -> jdbcTemplate.update(
				"insert into equipment (id, character_id, slot, item_instance_id) values (?, ?, 'MAIN_HAND', ?)",
				UUID.randomUUID(),
				otherCharacterId,
				foreignDaggerId))
				.isInstanceOf(DataIntegrityViolationException.class)
				.hasMessageContaining("fk_equipment_owned_item");
	}

	@Test
	void inventoryItemsCarryServerDecidedActionFlags() throws Exception {
		MockHttpSession session = registerWithCharacter("inv-flags-" + System.nanoTime() + "@greyhaven.test");

		mockMvc.perform(get("/api/v1/inventory").session(session))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.items[?(@.equipmentSlot == 'MAIN_HAND')].code",
						containsInAnyOrder(ItemCodes.RUSTY_SWORD)))
				.andExpect(jsonPath("$.items[?(@.equipmentSlot == 'CHEST')].code",
						containsInAnyOrder(ItemCodes.WORN_LEATHER_ARMOR)))
				.andExpect(jsonPath("$.items[?(@.usable == true)].code",
						containsInAnyOrder(ItemCodes.HEALING_POTION)));
	}

	@Test
	void usingHealingPotionHealsAndRemovesConsumable() throws Exception {
		String email = "inv-potion-" + System.nanoTime() + "@greyhaven.test";
		MockHttpSession session = registerWithCharacter(email);
		UUID characterId = characterIdForEmail(email);
		UUID potionId = itemInstanceId(characterId, ItemCodes.HEALING_POTION);

		jdbcTemplate.update(
				"update characters set current_health = 100, last_recovery_at = now() where id = ?",
				characterId);

		mockMvc.perform(withCsrf(post("/api/v1/inventory/" + potionId + "/use")).session(session))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.items[?(@.code=='HEALING_POTION')].quantity", hasItem(1)));

		Integer health = jdbcTemplate.queryForObject(
				"select current_health from characters where id = ?",
				Integer.class,
				characterId);
		assertThat(health).isEqualTo(140);

		mockMvc.perform(withCsrf(post("/api/v1/inventory/" + potionId + "/use")).session(session))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.items[?(@.code=='HEALING_POTION')]").isEmpty());

		Integer remainingPotions = jdbcTemplate.queryForObject(
				"""
						select count(*) from item_instances i
						join item_definitions d on d.id = i.item_definition_id
						where i.owner_character_id = ? and d.code = ?
						""",
				Integer.class,
				characterId,
				ItemCodes.HEALING_POTION);
		assertThat(remainingPotions).isEqualTo(0);

		Integer clampedHealth = jdbcTemplate.queryForObject(
				"select current_health from characters where id = ?",
				Integer.class,
				characterId);
		assertThat(clampedHealth).isEqualTo(165);
	}

	@Test
	void inventoryCapacityIsEnforced() throws Exception {
		String email = "inv-cap-" + System.nanoTime() + "@greyhaven.test";
		registerWithCharacter(email);
		UUID characterId = characterIdForEmail(email);

		// Starter loadout already uses 3 slots. Fill to capacity with wolf pelts (stackable = 1 slot).
		inventoryApplicationService.grantItems(characterId, ItemCodes.WOLF_PELT, 1);
		assertThat(slotCount(characterId)).isEqualTo(4);

		// Fill remaining slots with non-stackable weapons (each dagger is one slot).
		int remaining = 40 - 4;
		inventoryApplicationService.grantItems(characterId, ItemCodes.OLD_DAGGER, remaining);
		assertThat(slotCount(characterId)).isEqualTo(40);

		assertThatThrownBy(() -> inventoryApplicationService.grantItems(characterId, ItemCodes.OLD_DAGGER, 1))
				.isInstanceOf(ApiException.class)
				.hasFieldOrPropertyWithValue("code", "INVENTORY_FULL");
	}

	@Test
	void grantingStackableItemsMergesIntoOneSlot() throws Exception {
		String email = "inv-stack-" + System.nanoTime() + "@greyhaven.test";
		registerWithCharacter(email);
		UUID characterId = characterIdForEmail(email);

		assertThat(slotCount(characterId)).isEqualTo(3);
		assertThat(itemQuantity(characterId, ItemCodes.HEALING_POTION)).isEqualTo(2);

		inventoryApplicationService.grantItems(characterId, ItemCodes.HEALING_POTION, 3);

		assertThat(slotCount(characterId)).isEqualTo(3);
		assertThat(itemQuantity(characterId, ItemCodes.HEALING_POTION)).isEqualTo(5);
		assertThat(stackRowCount(characterId, ItemCodes.HEALING_POTION)).isEqualTo(1);
	}

	@Test
	void databaseRejectsDuplicateStackableStacksForSameOwner() throws Exception {
		String email = "inv-stack-uq-" + System.nanoTime() + "@greyhaven.test";
		registerWithCharacter(email);
		UUID characterId = characterIdForEmail(email);
		UUID potionDefinitionId = jdbcTemplate.queryForObject(
				"select id from item_definitions where code = ?",
				UUID.class,
				ItemCodes.HEALING_POTION);

		assertThatThrownBy(() -> jdbcTemplate.update(
				"""
						insert into item_instances
						    (id, item_definition_id, owner_character_id, quantity, stackable, rarity, created_at)
						values (?, ?, ?, 1, true, 'COMMON', timestamptz '2026-01-01 00:00:00+00')
						""",
				UUID.randomUUID(),
				potionDefinitionId,
				characterId))
				.isInstanceOf(DataIntegrityViolationException.class)
				.hasMessageContaining("uq_item_instances_owner_stackable_definition");
	}

	@Test
	void equipOwnedItemEnforcesLevelRequirements() throws Exception {
		String email = "inv-owned-equip-" + System.nanoTime() + "@greyhaven.test";
		registerWithCharacter(email);
		UUID characterId = characterIdForEmail(email);

		inventoryApplicationService.grantItems(characterId, ItemCodes.IRON_SWORD, 1);
		UUID ironSwordId = itemInstanceId(characterId, ItemCodes.IRON_SWORD);

		assertThatThrownBy(() -> inventoryApplicationService.equipOwnedItem(characterId, ironSwordId))
				.isInstanceOf(ApiException.class)
				.hasFieldOrPropertyWithValue("code", "EQUIP_REQUIREMENTS_NOT_MET");
	}

	@Test
	void inventoryRequiresAuthentication() throws Exception {
		mockMvc.perform(get("/api/v1/inventory"))
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.code").value("UNAUTHENTICATED"));
	}

	private int slotCount(UUID characterId) {
		Integer count = jdbcTemplate.queryForObject(
				"select count(*) from item_instances where owner_character_id = ?",
				Integer.class,
				characterId);
		return count == null ? 0 : count;
	}

	private int itemQuantity(UUID characterId, String code) {
		Integer quantity = jdbcTemplate.queryForObject(
				"""
						select coalesce(sum(i.quantity), 0) from item_instances i
						join item_definitions d on d.id = i.item_definition_id
						where i.owner_character_id = ? and d.code = ?
						""",
				Integer.class,
				characterId,
				code);
		return quantity == null ? 0 : quantity;
	}

	private int stackRowCount(UUID characterId, String code) {
		Integer count = jdbcTemplate.queryForObject(
				"""
						select count(*) from item_instances i
						join item_definitions d on d.id = i.item_definition_id
						where i.owner_character_id = ? and d.code = ?
						""",
				Integer.class,
				characterId,
				code);
		return count == null ? 0 : count;
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

		String name = "Inv" + UUID.randomUUID().toString().replace("-", "").substring(0, 10);
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
}
