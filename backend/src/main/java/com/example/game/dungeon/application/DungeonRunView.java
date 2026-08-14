package com.example.game.dungeon.application;

import java.util.List;
import java.util.UUID;

import com.example.game.combat.application.EncounterSearchView;
import com.example.game.dungeon.domain.DungeonRoomKind;
import com.example.game.dungeon.domain.DungeonRunStatus;

public record DungeonRunView(
		UUID runId,
		String dungeonCode,
		String dungeonName,
		DungeonRunStatus status,
		boolean paused,
		String currentRoomCode,
		String currentRoomName,
		String currentRoomDescription,
		DungeonRoomKind currentRoomKind,
		String chosenBranch,
		boolean uniqueRewardGranted,
		List<DungeonRoomView> rooms,
		List<DungeonChoiceView> choices,
		EncounterSearchView encounter
) {
}
