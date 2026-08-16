package com.example.game.quest.application;

import java.util.UUID;

import com.example.game.quest.domain.ArenaWonFact;
import com.example.game.quest.domain.CombatVictoryFact;
import com.example.game.quest.domain.CraftClaimedFact;
import com.example.game.quest.domain.DungeonCompletedFact;
import com.example.game.quest.domain.ExpeditionCompletedFact;
import com.example.game.quest.domain.InventoryChangedFact;
import com.example.game.quest.domain.ItemsGrantedFact;
import com.example.game.quest.domain.LocationVisitedFact;
import com.example.game.quest.domain.LocationSearchedFact;
import com.example.game.quest.domain.QuestProgressFact;
import com.example.game.quest.domain.TalkFact;

/**
 * Gameplay modules report facts through these methods so they do not depend on quest fact types.
 */
public interface QuestProgressSink {

	void notify(UUID characterId, QuestProgressFact fact);

	default void onCombatVictory(UUID characterId, String monsterCode, UUID sessionId) {
		notify(characterId, new CombatVictoryFact(monsterCode, sessionId));
	}

	default void onLocationVisited(UUID characterId, String locationCode) {
		notify(characterId, new LocationVisitedFact(locationCode));
	}

	default void onLocationSearched(UUID characterId, String locationCode) {
		notify(characterId, new LocationSearchedFact(locationCode));
	}

	default void onItemsGranted(UUID characterId, String itemCode, int quantity, String grantKey) {
		notify(characterId, new ItemsGrantedFact(itemCode, quantity, grantKey));
	}

	default void onInventoryChanged(UUID characterId) {
		notify(characterId, new InventoryChangedFact());
	}

	default void onTalk(UUID characterId, String npcCode) {
		notify(characterId, new TalkFact(npcCode));
	}

	default void onCraftClaimed(UUID characterId, String recipeCode, UUID jobId) {
		notify(characterId, new CraftClaimedFact(recipeCode, jobId));
	}

	default void onExpeditionCompleted(UUID characterId, String expeditionType, UUID expeditionId) {
		notify(characterId, new ExpeditionCompletedFact(expeditionType, expeditionId));
	}

	default void onDungeonCompleted(UUID characterId, String dungeonCode, UUID runId) {
		notify(characterId, new DungeonCompletedFact(dungeonCode, runId));
	}

	default void onArenaWon(UUID characterId, String matchKind, UUID matchId) {
		notify(characterId, new ArenaWonFact(matchKind, matchId));
	}
}
