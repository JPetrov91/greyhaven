package com.example.game.quest.domain;

public sealed interface QuestProgressFact
		permits CombatVictoryFact,
		LocationVisitedFact,
		LocationSearchedFact,
		ItemsGrantedFact,
		InventoryChangedFact,
		TalkFact,
		CraftClaimedFact,
		ExpeditionCompletedFact,
		DungeonCompletedFact,
		ArenaWonFact {

	QuestProgressSourceKind dedupeKind();

	String dedupeId();
}
