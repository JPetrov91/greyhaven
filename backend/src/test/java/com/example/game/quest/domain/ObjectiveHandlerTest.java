package com.example.game.quest.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;

import org.junit.jupiter.api.Test;

class ObjectiveHandlerTest {

	private final ItemQuantitySource empty = code -> 0;

	@Test
	void killIncrementsMatchingMonsterAndCaps() {
		KillObjectiveHandler handler = new KillObjectiveHandler();
		QuestObjectiveSpec spec = new QuestObjectiveSpec(QuestObjectiveType.KILL, "STREET_THUG", 2, false);
		ObjectiveProgress progress = new ObjectiveProgress(0, false);
		assertThat(handler.apply(spec, progress, new CombatVictoryFact("STREET_THUG", UUID.randomUUID()), empty)).isTrue();
		assertThat(progress.currentAmount()).isEqualTo(1);
		assertThat(handler.apply(spec, progress, new CombatVictoryFact("GIANT_RAT", UUID.randomUUID()), empty)).isFalse();
		assertThat(handler.apply(spec, progress, new CombatVictoryFact("STREET_THUG", UUID.randomUUID()), empty)).isTrue();
		assertThat(progress.completed()).isTrue();
		assertThat(handler.apply(spec, progress, new CombatVictoryFact("STREET_THUG", UUID.randomUUID()), empty)).isFalse();
	}

	@Test
	void collectRecomputesPossessionAndCanDrop() {
		CollectObjectiveHandler handler = new CollectObjectiveHandler();
		QuestObjectiveSpec spec = new QuestObjectiveSpec(QuestObjectiveType.COLLECT, "WOLF_PELT", 3, true);
		ObjectiveProgress progress = new ObjectiveProgress(0, false);
		assertThat(handler.apply(spec, progress, new InventoryChangedFact(), code -> 2)).isTrue();
		assertThat(progress.currentAmount()).isEqualTo(2);
		assertThat(progress.completed()).isFalse();
		assertThat(handler.apply(spec, progress, new InventoryChangedFact(), code -> 5)).isTrue();
		assertThat(progress.currentAmount()).isEqualTo(3);
		assertThat(progress.completed()).isTrue();
		assertThat(handler.apply(spec, progress, new InventoryChangedFact(), code -> 1)).isTrue();
		assertThat(progress.currentAmount()).isEqualTo(1);
		assertThat(progress.completed()).isFalse();
	}

	@Test
	void visitSetsOne() {
		VisitLocationObjectiveHandler handler = new VisitLocationObjectiveHandler();
		QuestObjectiveSpec spec = new QuestObjectiveSpec(QuestObjectiveType.VISIT_LOCATION, "OLD_TOWN", 1, false);
		ObjectiveProgress progress = new ObjectiveProgress(0, false);
		assertThat(handler.apply(spec, progress, new LocationVisitedFact("OLD_TOWN"), empty)).isTrue();
		assertThat(progress.completed()).isTrue();
		assertThat(handler.apply(spec, progress, new LocationVisitedFact("OLD_TOWN"), empty)).isFalse();
	}

	@Test
	void acquireAddsGrantedQuantity() {
		AcquireItemObjectiveHandler handler = new AcquireItemObjectiveHandler();
		QuestObjectiveSpec spec = new QuestObjectiveSpec(QuestObjectiveType.ACQUIRE_ITEM, "HEALING_POTION", 2, false);
		ObjectiveProgress progress = new ObjectiveProgress(0, false);
		assertThat(handler.apply(spec, progress, new ItemsGrantedFact("HEALING_POTION", 2, "g1"), empty)).isTrue();
		assertThat(progress.completed()).isTrue();
	}

	@Test
	void talkCraftExpeditionDungeonArenaIncrement() {
		TalkObjectiveHandler talk = new TalkObjectiveHandler();
		ObjectiveProgress talkProgress = new ObjectiveProgress(0, false);
		assertThat(talk.apply(
				new QuestObjectiveSpec(QuestObjectiveType.TALK_TO_NPC, "EDRIC_VARN", 1, false),
				talkProgress,
				new TalkFact("EDRIC_VARN"),
				empty)).isTrue();
		assertThat(talkProgress.completed()).isTrue();

		CraftItemObjectiveHandler craft = new CraftItemObjectiveHandler();
		ObjectiveProgress craftProgress = new ObjectiveProgress(0, false);
		assertThat(craft.apply(
				new QuestObjectiveSpec(QuestObjectiveType.CRAFT_ITEM, "CRAFT_LEATHER_ARMOR", 1, false),
				craftProgress,
				new CraftClaimedFact("CRAFT_LEATHER_ARMOR", UUID.randomUUID()),
				empty)).isTrue();

		CompleteExpeditionObjectiveHandler expedition = new CompleteExpeditionObjectiveHandler();
		ObjectiveProgress expeditionProgress = new ObjectiveProgress(0, false);
		assertThat(expedition.apply(
				new QuestObjectiveSpec(QuestObjectiveType.COMPLETE_EXPEDITION, "FOREST_PATROL", 1, false),
				expeditionProgress,
				new ExpeditionCompletedFact("FOREST_PATROL", UUID.randomUUID()),
				empty)).isTrue();

		CompleteDungeonObjectiveHandler dungeon = new CompleteDungeonObjectiveHandler();
		ObjectiveProgress dungeonProgress = new ObjectiveProgress(0, false);
		assertThat(dungeon.apply(
				new QuestObjectiveSpec(QuestObjectiveType.COMPLETE_DUNGEON, "RUINED_KEEP", 1, false),
				dungeonProgress,
				new DungeonCompletedFact("RUINED_KEEP", UUID.randomUUID()),
				empty)).isTrue();

		WinArenaMatchObjectiveHandler arena = new WinArenaMatchObjectiveHandler();
		ObjectiveProgress arenaProgress = new ObjectiveProgress(0, false);
		assertThat(arena.apply(
				new QuestObjectiveSpec(QuestObjectiveType.WIN_ARENA_MATCH, "ARENA", 1, false),
				arenaProgress,
				new ArenaWonFact("ARENA", UUID.randomUUID()),
				empty)).isTrue();
	}
}
