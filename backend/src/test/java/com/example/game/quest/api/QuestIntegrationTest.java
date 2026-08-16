package com.example.game.quest.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

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
import com.example.game.inventory.domain.InventoryBalance;
import com.example.game.item.domain.ItemCodes;
import com.example.game.quest.domain.QuestCodes;
import com.example.game.quest.domain.UnlockCodes;
import com.example.game.shared.domain.MutableRandomProvider;
import com.example.game.shared.domain.RandomProvider;
import com.example.game.world.domain.LocationCodes;
import com.jayway.jsonpath.JsonPath;

import jakarta.servlet.http.Cookie;

@Import({ TestcontainersConfiguration.class, QuestIntegrationTest.RandomTestConfig.class })
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class QuestIntegrationTest {

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
		MvcResult bootstrap = mockMvc.perform(get("/api/v1/bootstrap")).andExpect(status().isOk()).andReturn();
		csrfCookie = bootstrap.getResponse().getCookie("XSRF-TOKEN");
		assertThat(csrfCookie).isNotNull();
	}

	@Test
	void issuedSteelAutoStartsKitSearchAndTurnInUnlocksChain() throws Exception {
		String email = "quest-e2e-" + System.nanoTime() + "@greyhaven.test";
		MockHttpSession session = registerWithCharacter(email);
		UUID characterId = characterIdForEmail(email);
		int xpBefore = xpOf(characterId);
		int goldBefore = goldOf(characterId);
		int potionsBefore = inventoryApplicationService.unreservedQuantityByCode(characterId, ItemCodes.HEALING_POTION);

		mockMvc.perform(get("/api/v1/quests").session(session))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.quests[?(@.code=='QST_MILITIA_NOTICE')].status").value("ACTIVE"))
				.andExpect(jsonPath("$.quests[?(@.code=='QST_MILITIA_NOTICE')].tracked").value(true))
				.andExpect(jsonPath("$.quests[?(@.code=='QST_MILITIA_NOTICE')].name").value("Issued Steel"))
				.andExpect(jsonPath("$.quests[?(@.code=='QST_ARM_THE_WATCH')]").isEmpty());

		mockMvc.perform(get("/api/v1/world/npcs").session(session))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.npcs[?(@.code=='MILITIA_OFFICER')].questBadges[0]").value("ACTIVE"));

		mockMvc.perform(withCsrf(post("/api/v1/world/npcs/MILITIA_OFFICER/talk")).session(session)
						.contentType(MediaType.APPLICATION_JSON)
						.content("{}"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.actions[?(@.type=='ACCEPT')]").isEmpty())
				.andExpect(jsonPath("$.actions[?(@.action=='WALK_OLD_TOWN')].type").value("DIALOGUE"));

		talk(session, "WALK_OLD_TOWN", null)
				.andExpect(jsonPath("$.text").value("What can you hold?"))
				.andExpect(jsonPath("$.actions[?(@.type=='CHOOSE_KIT')].action", org.hamcrest.Matchers.hasItems(
						"SWORD", "AXE", "MACE", "DAGGERS")))
				.andExpect(jsonPath("$.actions[?(@.action=='BOW')]").isEmpty());

		talk(session, "CHOOSE_KIT", "SWORD").andExpect(status().isOk());
		talk(session, "CHOOSE_KIT", "AXE").andExpect(status().isOk());
		assertThat(itemCount(characterId, ItemCodes.RUSTY_SWORD)).isEqualTo(1);
		assertThat(equippedCount(characterId, ItemCodes.RUSTY_SHIELD)).isEqualTo(1);
		assertThat(itemCount(characterId, ItemCodes.RUSTY_AXE)).isZero();
		assertThat(itemCount(characterId, ItemCodes.HUNTING_BOW)).isZero();

		mockMvc.perform(get("/api/v1/quests/" + QuestCodes.MILITIA_NOTICE).session(session))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.kitFamily").value("SWORD"))
				.andExpect(jsonPath("$.objectives[0].completed").value(true));

		moveTo(session, LocationCodes.OLD_TOWN);
		mockMvc.perform(get("/api/v1/quests/" + QuestCodes.MILITIA_NOTICE).session(session))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.objectives[1].completed").value(true));

		searchAndIgnore(session);
		mockMvc.perform(get("/api/v1/quests/" + QuestCodes.MILITIA_NOTICE).session(session))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value("READY_TO_TURN_IN"))
				.andExpect(jsonPath("$.objectives[2].completed").value(true))
				.andExpect(jsonPath("$.lastSearchOutcome").value("NO_COMBAT"));

		moveTo(session, LocationCodes.CITY_SQUARE);
		mockMvc.perform(withCsrf(post("/api/v1/world/npcs/MILITIA_OFFICER/talk")).session(session)
						.contentType(MediaType.APPLICATION_JSON)
						.content("{}"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.text").value(org.hamcrest.Matchers.containsString("You walked it and came back")));

		int swordsBeforeTurnIn = itemCount(characterId, ItemCodes.RUSTY_SWORD);
		mockMvc.perform(withCsrf(post("/api/v1/quests/" + QuestCodes.MILITIA_NOTICE + "/turn-in")).session(session))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value("COMPLETED"))
				.andExpect(jsonPath("$.startNpcName").value("Watch-Sergeant Bren"))
				.andExpect(jsonPath("$.nextQuestName").value("Arm the Watch"))
				.andExpect(jsonPath("$.rewards[?(@.kind=='ITEM')].itemName", org.hamcrest.Matchers.hasItems(
						"Healing Potion", "Rusty Sword", "Rusty Shield")))
				.andExpect(jsonPath("$.completeText").value(org.hamcrest.Matchers.containsString("The rust is yours")))
				.andExpect(jsonPath("$.unlocks").isEmpty());
		assertThat(itemCount(characterId, ItemCodes.RUSTY_SWORD)).isEqualTo(swordsBeforeTurnIn);

		assertThat(xpOf(characterId)).isEqualTo(xpBefore + 40);
		assertThat(goldOf(characterId)).isGreaterThanOrEqualTo(goldBefore + 15);
		assertThat(inventoryApplicationService.unreservedQuantityByCode(characterId, ItemCodes.HEALING_POTION))
				.isEqualTo(potionsBefore + 1);

		mockMvc.perform(withCsrf(post("/api/v1/quests/" + QuestCodes.MILITIA_NOTICE + "/turn-in")).session(session))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value("COMPLETED"));
		assertThat(inventoryApplicationService.unreservedQuantityByCode(characterId, ItemCodes.HEALING_POTION))
				.isEqualTo(potionsBefore + 1);

		mockMvc.perform(get("/api/v1/quests").session(session))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.quests[?(@.code=='QST_ARM_THE_WATCH')].status").value("AVAILABLE"));
	}

	@Test
	void issuedSteelDaggersNeverGrantShield() throws Exception {
		String email = "quest-daggers-" + System.nanoTime() + "@greyhaven.test";
		MockHttpSession session = registerWithCharacter(email);
		UUID characterId = characterIdForEmail(email);
		talk(session, "WALK_OLD_TOWN", null);
		talk(session, "CHOOSE_KIT", "DAGGERS").andExpect(status().isOk());
		assertThat(itemCount(characterId, ItemCodes.RUSTY_DAGGER)).isEqualTo(1);
		assertThat(itemCount(characterId, ItemCodes.RUSTY_SHIELD)).isZero();
		assertThat(itemCount(characterId, ItemCodes.RUSTY_SWORD)).isZero();
	}

	@Test
	void issuedSteelVictorySearchBranchesCopy() throws Exception {
		String email = "quest-victory-" + System.nanoTime() + "@greyhaven.test";
		MockHttpSession session = registerWithCharacter(email);
		talk(session, "WALK_OLD_TOWN", null);
		talk(session, "CHOOSE_KIT", "AXE").andExpect(status().isOk());
		moveTo(session, LocationCodes.OLD_TOWN);
		winStreetThug(session);
		mockMvc.perform(get("/api/v1/quests/" + QuestCodes.MILITIA_NOTICE).session(session))
				.andExpect(jsonPath("$.status").value("READY_TO_TURN_IN"))
				.andExpect(jsonPath("$.lastSearchOutcome").value("VICTORY"));
		moveTo(session, LocationCodes.CITY_SQUARE);
		mockMvc.perform(withCsrf(post("/api/v1/world/npcs/MILITIA_OFFICER/talk")).session(session)
						.contentType(MediaType.APPLICATION_JSON)
						.content("{}"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.text").value(org.hamcrest.Matchers.containsString("You came back louder")));
	}

	@Test
	void issuedSteelRetreatSearchCompletesAndBranchesCopy() throws Exception {
		String email = "quest-retreat-" + System.nanoTime() + "@greyhaven.test";
		MockHttpSession session = registerWithCharacter(email);
		talk(session, "WALK_OLD_TOWN", null);
		talk(session, "CHOOSE_KIT", "MACE").andExpect(status().isOk());
		moveTo(session, LocationCodes.OLD_TOWN);
		retreatSearch(session);
		mockMvc.perform(get("/api/v1/quests/" + QuestCodes.MILITIA_NOTICE).session(session))
				.andExpect(jsonPath("$.status").value("READY_TO_TURN_IN"))
				.andExpect(jsonPath("$.lastSearchOutcome").value("RETREAT"));
		moveTo(session, LocationCodes.CITY_SQUARE);
		mockMvc.perform(withCsrf(post("/api/v1/world/npcs/MILITIA_OFFICER/talk")).session(session)
						.contentType(MediaType.APPLICATION_JSON)
						.content("{}"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.text").value(org.hamcrest.Matchers.containsString("Alive is a report")));
	}

	@Test
	void acceptRejectedWhenAlreadyActiveOrNotAvailable() throws Exception {
		MockHttpSession session = registerWithCharacter("quest-loc-" + System.nanoTime() + "@greyhaven.test");
		mockMvc.perform(withCsrf(post("/api/v1/quests/" + QuestCodes.MILITIA_NOTICE + "/accept")).session(session))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.code").value("QUEST_ALREADY_ACCEPTED"));
		moveTo(session, LocationCodes.MARKET);
		mockMvc.perform(withCsrf(post("/api/v1/quests/" + QuestCodes.ARM_THE_WATCH + "/accept")).session(session))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.code").value("QUEST_NOT_AVAILABLE"));
	}

	@Test
	void collectGrantUpdatesProgressAndTurnInConsumes() throws Exception {
		String email = "quest-pelt-ok-" + System.nanoTime() + "@greyhaven.test";
		MockHttpSession session = registerWithCharacter(email);
		UUID characterId = characterIdForEmail(email);
		insertCollectQuest();
		mockMvc.perform(withCsrf(post("/api/v1/quests/QST_WATCH_TITHE/accept")).session(session))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value("ACTIVE"));
		inventoryApplicationService.grantItems(characterId, ItemCodes.WOLF_PELT, 2);
		mockMvc.perform(get("/api/v1/quests/QST_WATCH_TITHE").session(session))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value("READY_TO_TURN_IN"))
				.andExpect(jsonPath("$.objectives[0].currentAmount").value(2))
				.andExpect(jsonPath("$.objectives[0].completed").value(true));
		mockMvc.perform(withCsrf(post("/api/v1/quests/QST_WATCH_TITHE/turn-in")).session(session))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value("COMPLETED"));
		assertThat(inventoryApplicationService.unreservedQuantityByCode(characterId, ItemCodes.WOLF_PELT)).isZero();
	}

	@Test
	void collectDropsWhenItemsLeaveAndTurnInRevalidates() throws Exception {
		String email = "quest-pelt-" + System.nanoTime() + "@greyhaven.test";
		MockHttpSession session = registerWithCharacter(email);
		UUID characterId = characterIdForEmail(email);
		insertCollectQuest();
		mockMvc.perform(withCsrf(post("/api/v1/quests/QST_WATCH_TITHE/accept")).session(session))
				.andExpect(status().isOk());
		inventoryApplicationService.grantItems(characterId, ItemCodes.WOLF_PELT, 2);
		mockMvc.perform(get("/api/v1/quests/QST_WATCH_TITHE").session(session))
				.andExpect(jsonPath("$.status").value("READY_TO_TURN_IN"));
		jdbcTemplate.update(
				"""
						delete from item_instances i
						using item_definitions d
						where i.item_definition_id = d.id
						and i.owner_character_id = ? and d.code = 'WOLF_PELT'
						""",
				characterId);
		mockMvc.perform(withCsrf(post("/api/v1/quests/QST_WATCH_TITHE/turn-in")).session(session))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.code").value("QUEST_NOT_READY"));
		mockMvc.perform(get("/api/v1/quests/QST_WATCH_TITHE").session(session))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value("READY_TO_TURN_IN"))
				.andExpect(jsonPath("$.objectives[0].completed").value(true));
	}

	@Test
	void acquireItemCountsSeparateGrants() throws Exception {
		String email = "quest-acquire-" + System.nanoTime() + "@greyhaven.test";
		MockHttpSession session = registerWithCharacter(email);
		UUID characterId = characterIdForEmail(email);
		insertAcquireQuest();
		mockMvc.perform(withCsrf(post("/api/v1/quests/QST_POTION_RUN/accept")).session(session))
				.andExpect(status().isOk());
		inventoryApplicationService.grantItems(characterId, ItemCodes.HEALING_POTION, 1);
		mockMvc.perform(get("/api/v1/quests/QST_POTION_RUN").session(session))
				.andExpect(jsonPath("$.objectives[0].currentAmount").value(1))
				.andExpect(jsonPath("$.status").value("ACTIVE"));
		inventoryApplicationService.grantItems(characterId, ItemCodes.HEALING_POTION, 1);
		mockMvc.perform(get("/api/v1/quests/QST_POTION_RUN").session(session))
				.andExpect(jsonPath("$.objectives[0].currentAmount").value(2))
				.andExpect(jsonPath("$.status").value("READY_TO_TURN_IN"));
	}

	@Test
	void autoCompleteFullInventoryMarksReadyInsteadOfFailing() throws Exception {
		String email = "quest-auto-full-" + System.nanoTime() + "@greyhaven.test";
		MockHttpSession session = registerWithCharacter(email);
		UUID characterId = characterIdForEmail(email);
		insertAutoCompleteSwordQuest();
		int used = inventoryApplicationService.usedCapacity(characterId);
		int remaining = InventoryBalance.DEFAULT_CAPACITY - used;
		if (remaining > 0) {
			inventoryApplicationService.grantItems(characterId, ItemCodes.IRON_SWORD, remaining);
		}
		mockMvc.perform(withCsrf(post("/api/v1/quests/QST_AUTO_SWORD/accept")).session(session))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value("READY_TO_TURN_IN"));
		assertThat(jdbcTemplate.queryForObject(
				"""
						select q.rewards_applied from character_quest q
						join quest_definition d on d.id = q.quest_id
						where q.character_id = ? and d.code = 'QST_AUTO_SWORD'
						""",
				Boolean.class,
				characterId)).isFalse();
	}

	@Test
	void inventoryFullItemRewardRollsBackTurnIn() throws Exception {
		String email = "quest-full-" + System.nanoTime() + "@greyhaven.test";
		MockHttpSession session = registerWithCharacter(email);
		UUID characterId = characterIdForEmail(email);
		insertSwordRewardQuest();
		mockMvc.perform(withCsrf(post("/api/v1/quests/QST_PACK_CHECK/accept")).session(session))
				.andExpect(status().isOk());
		jdbcTemplate.update(
				"""
						update character_quest_objective o
						set current_amount = 1, completed = true
						from character_quest q, quest_definition d
						where o.character_quest_id = q.id and q.quest_id = d.id
						and q.character_id = ? and d.code = 'QST_PACK_CHECK'
						""",
				characterId);
		jdbcTemplate.update(
				"""
						update character_quest q
						set status = 'READY_TO_TURN_IN'
						from quest_definition d
						where q.quest_id = d.id and q.character_id = ? and d.code = 'QST_PACK_CHECK'
						""",
				characterId);
		int used = inventoryApplicationService.usedCapacity(characterId);
		int remaining = InventoryBalance.DEFAULT_CAPACITY - used;
		if (remaining > 0) {
			inventoryApplicationService.grantItems(characterId, ItemCodes.IRON_SWORD, remaining);
		}
		mockMvc.perform(withCsrf(post("/api/v1/quests/QST_PACK_CHECK/turn-in")).session(session))
				.andExpect(status().isConflict());
		assertThat(jdbcTemplate.queryForObject(
				"""
						select q.status from character_quest q
						join quest_definition d on d.id = q.quest_id
						where q.character_id = ? and d.code = 'QST_PACK_CHECK'
						""",
				String.class,
				characterId)).isEqualTo("READY_TO_TURN_IN");
	}

	@Test
	void concurrentTurnInGrantsOnce() throws Exception {
		String email = "quest-race-" + System.nanoTime() + "@greyhaven.test";
		MockHttpSession session = registerWithCharacter(email);
		UUID characterId = characterIdForEmail(email);
		jdbcTemplate.update(
				"""
						update character_quest_objective o
						set current_amount = o.current_amount, completed = true
						from character_quest q, quest_definition d
						where o.character_quest_id = q.id and q.quest_id = d.id
						and q.character_id = ? and d.code = 'QST_MILITIA_NOTICE'
						""",
				characterId);
		jdbcTemplate.update(
				"""
						update character_quest q
						set status = 'READY_TO_TURN_IN'
						from quest_definition d
						where q.quest_id = d.id and q.character_id = ? and d.code = 'QST_MILITIA_NOTICE'
						""",
				characterId);
		int potionsBefore = inventoryApplicationService.unreservedQuantityByCode(characterId, ItemCodes.HEALING_POTION);
		CyclicBarrier barrier = new CyclicBarrier(2);
		ExecutorService executor = Executors.newFixedThreadPool(2);
		Callable<Integer> turnIn = () -> {
			barrier.await();
			return mockMvc.perform(withCsrf(post("/api/v1/quests/" + QuestCodes.MILITIA_NOTICE + "/turn-in")).session(session))
					.andReturn()
					.getResponse()
					.getStatus();
		};
		Future<Integer> first = executor.submit(turnIn);
		Future<Integer> second = executor.submit(turnIn);
		assertThat(first.get()).isIn(200, 409);
		assertThat(second.get()).isIn(200, 409);
		executor.shutdown();
		assertThat(inventoryApplicationService.unreservedQuantityByCode(characterId, ItemCodes.HEALING_POTION))
				.isEqualTo(potionsBefore + 1);
	}

	@Test
	void starterGrantsDoNotRecordProgressSourcesWithoutOpenQuests() throws Exception {
		String email = "quest-source-" + System.nanoTime() + "@greyhaven.test";
		registerWithCharacter(email);
		UUID characterId = characterIdForEmail(email);
		assertThat(jdbcTemplate.queryForObject(
				"select count(*) from character_quest_progress_source where character_id = ?",
				Integer.class,
				characterId)).isZero();
	}

	@Test
	void repeatableQuestCanBeAcceptedAgain() throws Exception {
		String email = "quest-repeat-" + System.nanoTime() + "@greyhaven.test";
		MockHttpSession session = registerWithCharacter(email);
		UUID characterId = characterIdForEmail(email);
		insertRepeatableVisitQuest();
		mockMvc.perform(withCsrf(post("/api/v1/quests/QST_SQUARE_DRILL/accept")).session(session))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value("COMPLETED"));
		mockMvc.perform(withCsrf(post("/api/v1/world/npcs/MILITIA_OFFICER/talk")).session(session)
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"questCode\":\"QST_SQUARE_DRILL\"}"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.actions[?(@.type=='ACCEPT')].questCode").value("QST_SQUARE_DRILL"));
		mockMvc.perform(withCsrf(post("/api/v1/quests/QST_SQUARE_DRILL/accept")).session(session))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value("COMPLETED"));
		assertThat(jdbcTemplate.queryForObject(
				"""
						select count(*) from character_quest q
						join quest_definition d on d.id = q.quest_id
						where q.character_id = ? and d.code = 'QST_SQUARE_DRILL'
						""",
				Integer.class,
				characterId)).isEqualTo(1);
	}

	@Test
	void completedQuestUnlocksAreOnlyThatQuest() throws Exception {
		String email = "quest-unlock-" + System.nanoTime() + "@greyhaven.test";
		MockHttpSession session = registerWithCharacter(email);
		insertUnlockQuest();
		mockMvc.perform(withCsrf(post("/api/v1/quests/QST_YARD_PASS/accept")).session(session))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value("COMPLETED"))
				.andExpect(jsonPath("$.unlocks[0]").value(UnlockCodes.TRAINING_GROUNDS));
	}

	private org.springframework.test.web.servlet.ResultActions talk(
			MockHttpSession session,
			String action,
			String kitFamily) throws Exception {
		String body = kitFamily == null
				? """
						{"questCode":"%s","action":"%s"}
						""".formatted(QuestCodes.MILITIA_NOTICE, action)
				: """
						{"questCode":"%s","action":"%s","kitFamily":"%s"}
						""".formatted(QuestCodes.MILITIA_NOTICE, action, kitFamily);
		return mockMvc.perform(withCsrf(post("/api/v1/world/npcs/MILITIA_OFFICER/talk")).session(session)
				.contentType(MediaType.APPLICATION_JSON)
				.content(body));
	}

	private void searchAndIgnore(MockHttpSession session) throws Exception {
		mutableRandomProvider.queue(1);
		MvcResult search = mockMvc.perform(withCsrf(post("/api/v1/encounters/search")).session(session))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.found").value(true))
				.andExpect(jsonPath("$.flavour").value(org.hamcrest.Matchers.anyOf(
						org.hamcrest.Matchers.containsString("A shutter slams"),
						org.hamcrest.Matchers.containsString("dropped cap"),
						org.hamcrest.Matchers.containsString("Boots at the corner"))))
				.andReturn();
		UUID encounterId = UUID.fromString(JsonPath.read(search.getResponse().getContentAsString(), "$.encounterId"));
		mockMvc.perform(withCsrf(post("/api/v1/encounters/" + encounterId + "/ignore")).session(session))
				.andExpect(status().isOk());
	}

	private void retreatSearch(MockHttpSession session) throws Exception {
		mutableRandomProvider.queue(1);
		MvcResult search = mockMvc.perform(withCsrf(post("/api/v1/encounters/search")).session(session))
				.andExpect(status().isOk())
				.andReturn();
		UUID encounterId = UUID.fromString(JsonPath.read(search.getResponse().getContentAsString(), "$.encounterId"));
		MvcResult fight = mockMvc.perform(withCsrf(post("/api/v1/encounters/" + encounterId + "/fight")).session(session))
				.andExpect(status().isOk())
				.andReturn();
		UUID combatId = UUID.fromString(JsonPath.read(fight.getResponse().getContentAsString(), "$.id"));
		mutableRandomProvider.queue(0);
		mockMvc.perform(withCsrf(post("/api/v1/combat/" + combatId + "/actions"))
						.session(session)
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"action\":\"RETREAT\",\"expectedRoundNumber\":0}"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value("PLAYER_ESCAPED"));
	}

	private int itemCount(UUID characterId, String code) {
		Integer count = jdbcTemplate.queryForObject(
				"""
						select coalesce(sum(i.quantity), 0) from item_instances i
						join item_definitions d on d.id = i.item_definition_id
						where i.owner_character_id = ? and d.code = ?
						""",
				Integer.class,
				characterId,
				code);
		return count == null ? 0 : count;
	}

	private int equippedCount(UUID characterId, String code) {
		Integer count = jdbcTemplate.queryForObject(
				"""
						select count(*) from equipment e
						join item_instances i on i.id = e.item_instance_id
						join item_definitions d on d.id = i.item_definition_id
						where i.owner_character_id = ? and d.code = ?
						""",
				Integer.class,
				characterId,
				code);
		return count == null ? 0 : count;
	}

	private void winStreetThug(MockHttpSession session) throws Exception {
		mutableRandomProvider.queue(1);
		MvcResult search = mockMvc.perform(withCsrf(post("/api/v1/encounters/search")).session(session))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.monster.code").value("STREET_THUG"))
				.andReturn();
		UUID encounterId = UUID.fromString(JsonPath.read(search.getResponse().getContentAsString(), "$.encounterId"));
		MvcResult fight = mockMvc.perform(withCsrf(post("/api/v1/encounters/" + encounterId + "/fight")).session(session))
				.andExpect(status().isOk())
				.andReturn();
		UUID combatId = UUID.fromString(JsonPath.read(fight.getResponse().getContentAsString(), "$.id"));
		jdbcTemplate.update("update combat_sessions set enemy_health = 1 where id = ?", combatId);
		mutableRandomProvider.queue(5, 6, 90);
		mockMvc.perform(withCsrf(post("/api/v1/combat/" + combatId + "/actions"))
						.session(session)
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"action\":\"QUICK_ATTACK\",\"expectedRoundNumber\":0}"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value("PLAYER_WON"));
	}

	private void insertCollectQuest() {
		jdbcTemplate.update(
				"""
						insert into quest_definition
						(id, code, name, description, category, recommended_level, min_level, start_npc_code, turn_in_npc_code,
						 prerequisite_quest_code, next_quest_code, repeatable, sort_order, offer_text, progress_text, complete_text)
						values ('e0000000-0000-4000-8000-000000000201', 'QST_WATCH_TITHE', 'Watch Tithe', 'Bring pelts.',
						 'SIDE', 1, 1, 'MILITIA_OFFICER', 'MILITIA_OFFICER', null, null, false, 90,
						 'Bring two pelts.', 'Still waiting.', 'Paid.')
						on conflict do nothing
						""");
		jdbcTemplate.update(
				"""
						insert into quest_objective_definition
						(id, quest_id, sort_order, type, target_code, required_amount, display_text, consume_on_turn_in)
						values ('e0000000-0000-4000-8000-000000000211', 'e0000000-0000-4000-8000-000000000201', 1,
						 'COLLECT', 'WOLF_PELT', 2, 'Collect Wolf Pelts', true)
						on conflict do nothing
						""");
	}

	private void insertAcquireQuest() {
		jdbcTemplate.update(
				"""
						insert into quest_definition
						(id, code, name, description, category, recommended_level, min_level, start_npc_code, turn_in_npc_code,
						 prerequisite_quest_code, next_quest_code, repeatable, sort_order, offer_text, progress_text, complete_text)
						values ('e0000000-0000-4000-8000-000000000203', 'QST_POTION_RUN', 'Potion Run', 'Find draughts.',
						 'SIDE', 1, 1, 'MILITIA_OFFICER', 'MILITIA_OFFICER', null, null, false, 92,
						 'Bring two draughts.', 'Still waiting.', 'Good.')
						on conflict do nothing
						""");
		jdbcTemplate.update(
				"""
						insert into quest_objective_definition
						(id, quest_id, sort_order, type, target_code, required_amount, display_text, consume_on_turn_in)
						values ('e0000000-0000-4000-8000-000000000213', 'e0000000-0000-4000-8000-000000000203', 1,
						 'ACQUIRE_ITEM', 'HEALING_POTION', 2, 'Acquire Healing Potions', false)
						on conflict do nothing
						""");
	}

	private void insertAutoCompleteSwordQuest() {
		jdbcTemplate.update(
				"""
						insert into quest_definition
						(id, code, name, description, category, recommended_level, min_level, start_npc_code, turn_in_npc_code,
						 prerequisite_quest_code, next_quest_code, repeatable, sort_order, offer_text, progress_text, complete_text)
						values ('e0000000-0000-4000-8000-000000000204', 'QST_AUTO_SWORD', 'Auto Sword', 'A sword.',
						 'SIDE', 1, 1, null, null, null, null, false, 93,
						 'Take this.', 'Go on.', 'Here.')
						on conflict do nothing
						""");
		jdbcTemplate.update(
				"""
						insert into quest_objective_definition
						(id, quest_id, sort_order, type, target_code, required_amount, display_text, consume_on_turn_in)
						values ('e0000000-0000-4000-8000-000000000214', 'e0000000-0000-4000-8000-000000000204', 1,
						 'VISIT_LOCATION', 'CITY_SQUARE', 1, 'Stand in the Square', false)
						on conflict do nothing
						""");
		jdbcTemplate.update(
				"""
						insert into quest_reward_definition
						(id, quest_id, kind, amount, item_code, unlock_code, sort_order)
						values ('e0000000-0000-4000-8000-000000000234', 'e0000000-0000-4000-8000-000000000204',
						 'ITEM', 1, 'IRON_SWORD', null, 1)
						on conflict do nothing
						""");
	}

	private void insertRepeatableVisitQuest() {
		jdbcTemplate.update(
				"""
						insert into quest_definition
						(id, code, name, description, category, recommended_level, min_level, start_npc_code, turn_in_npc_code,
						 prerequisite_quest_code, next_quest_code, repeatable, sort_order, offer_text, progress_text, complete_text)
						values ('e0000000-0000-4000-8000-000000000205', 'QST_SQUARE_DRILL', 'Square Drill', 'Stand in the square.',
						 'SIDE', 1, 1, 'MILITIA_OFFICER', null, null, null, true, 94,
						 'Stand here.', 'Go on.', 'Again.')
						on conflict do nothing
						""");
		jdbcTemplate.update(
				"""
						insert into quest_objective_definition
						(id, quest_id, sort_order, type, target_code, required_amount, display_text, consume_on_turn_in)
						values ('e0000000-0000-4000-8000-000000000215', 'e0000000-0000-4000-8000-000000000205', 1,
						 'VISIT_LOCATION', 'CITY_SQUARE', 1, 'Stand in the Square', false)
						on conflict do nothing
						""");
		jdbcTemplate.update(
				"""
						insert into quest_reward_definition
						(id, quest_id, kind, amount, item_code, unlock_code, sort_order)
						values ('e0000000-0000-4000-8000-000000000235', 'e0000000-0000-4000-8000-000000000205',
						 'GOLD', 1, null, null, 1)
						on conflict do nothing
						""");
	}

	private void insertUnlockQuest() {
		jdbcTemplate.update(
				"""
						insert into quest_definition
						(id, code, name, description, category, recommended_level, min_level, start_npc_code, turn_in_npc_code,
						 prerequisite_quest_code, next_quest_code, repeatable, sort_order, offer_text, progress_text, complete_text)
						values ('e0000000-0000-4000-8000-000000000206', 'QST_YARD_PASS', 'Yard Pass', 'The yard.',
						 'SIDE', 1, 1, 'MILITIA_OFFICER', null, null, null, false, 95,
						 'Take this.', 'Go on.', 'Here.')
						on conflict do nothing
						""");
		jdbcTemplate.update(
				"""
						insert into quest_objective_definition
						(id, quest_id, sort_order, type, target_code, required_amount, display_text, consume_on_turn_in)
						values ('e0000000-0000-4000-8000-000000000216', 'e0000000-0000-4000-8000-000000000206', 1,
						 'VISIT_LOCATION', 'CITY_SQUARE', 1, 'Stand in the Square', false)
						on conflict do nothing
						""");
		jdbcTemplate.update(
				"""
						insert into quest_reward_definition
						(id, quest_id, kind, amount, item_code, unlock_code, sort_order)
						values ('e0000000-0000-4000-8000-000000000236', 'e0000000-0000-4000-8000-000000000206',
						 'UNLOCK', 1, null, 'TRAINING_GROUNDS', 1)
						on conflict do nothing
						""");
	}

	private void insertSwordRewardQuest() {
		jdbcTemplate.update(
				"""
						insert into quest_definition
						(id, code, name, description, category, recommended_level, min_level, start_npc_code, turn_in_npc_code,
						 prerequisite_quest_code, next_quest_code, repeatable, sort_order, offer_text, progress_text, complete_text)
						values ('e0000000-0000-4000-8000-000000000202', 'QST_PACK_CHECK', 'Pack Check', 'A sword.',
						 'SIDE', 1, 1, 'MILITIA_OFFICER', 'MILITIA_OFFICER', null, null, false, 91,
						 'Take this.', 'Go on.', 'Here.')
						on conflict do nothing
						""");
		jdbcTemplate.update(
				"""
						insert into quest_objective_definition
						(id, quest_id, sort_order, type, target_code, required_amount, display_text, consume_on_turn_in)
						values ('e0000000-0000-4000-8000-000000000212', 'e0000000-0000-4000-8000-000000000202', 1,
						 'VISIT_LOCATION', 'CITY_SQUARE', 1, 'Stand in the Square', false)
						on conflict do nothing
						""");
		jdbcTemplate.update(
				"""
						insert into quest_reward_definition
						(id, quest_id, kind, amount, item_code, unlock_code, sort_order)
						values ('e0000000-0000-4000-8000-000000000232', 'e0000000-0000-4000-8000-000000000202',
						 'ITEM', 1, 'IRON_SWORD', null, 1)
						on conflict do nothing
						""");
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

	private int xpOf(UUID characterId) {
		return jdbcTemplate.queryForObject("select experience from characters where id = ?", Integer.class, characterId);
	}

	private int goldOf(UUID characterId) {
		return jdbcTemplate.queryForObject("select gold from characters where id = ?", Integer.class, characterId);
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
		String name = "Q" + UUID.randomUUID().toString().replace("-", "").substring(0, 10);
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
