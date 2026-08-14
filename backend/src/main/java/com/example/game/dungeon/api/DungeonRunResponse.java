package com.example.game.dungeon.api;

import java.util.List;
import java.util.UUID;

import com.example.game.combat.api.EncounterSearchResponse;
import com.example.game.combat.api.MonsterResponse;
import com.example.game.combat.application.EncounterSearchView;
import com.example.game.combat.application.MonsterView;
import com.example.game.dungeon.application.DungeonChoiceView;
import com.example.game.dungeon.application.DungeonRoomView;
import com.example.game.dungeon.application.DungeonRunView;
import com.example.game.dungeon.domain.DungeonRoomKind;
import com.example.game.dungeon.domain.DungeonRoomState;
import com.example.game.dungeon.domain.DungeonRunStatus;

public record DungeonRunResponse(
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
		List<DungeonRoomResponse> rooms,
		List<DungeonChoiceResponse> choices,
		EncounterSearchResponse encounter
) {
	static DungeonRunResponse from(DungeonRunView view) {
		return new DungeonRunResponse(
				view.runId(),
				view.dungeonCode(),
				view.dungeonName(),
				view.status(),
				view.paused(),
				view.currentRoomCode(),
				view.currentRoomName(),
				view.currentRoomDescription(),
				view.currentRoomKind(),
				view.chosenBranch(),
				view.uniqueRewardGranted(),
				view.rooms().stream().map(DungeonRunResponse::toRoom).toList(),
				view.choices().stream().map(DungeonRunResponse::toChoice).toList(),
				toEncounter(view.encounter()));
	}

	private static DungeonRoomResponse toRoom(DungeonRoomView room) {
		return new DungeonRoomResponse(room.code(), room.name(), room.kind(), room.state());
	}

	private static DungeonChoiceResponse toChoice(DungeonChoiceView choice) {
		return new DungeonChoiceResponse(choice.edgeCode(), choice.roomCode(), choice.roomName(), choice.optional());
	}

	private static EncounterSearchResponse toEncounter(EncounterSearchView encounter) {
		if (encounter == null || !encounter.found()) {
			return null;
		}
		return new EncounterSearchResponse(true, encounter.encounterId(), toMonster(encounter.monster()));
	}

	private static MonsterResponse toMonster(MonsterView monster) {
		if (monster == null) {
			return null;
		}
		return new MonsterResponse(
				monster.id(),
				monster.code(),
				monster.name(),
				monster.level(),
				monster.maxHealth(),
				monster.archetype(),
				monster.tier());
	}
}

record DungeonRoomResponse(String code, String name, DungeonRoomKind kind, DungeonRoomState state) {
}

record DungeonChoiceResponse(String edgeCode, String roomCode, String roomName, boolean optional) {
}
