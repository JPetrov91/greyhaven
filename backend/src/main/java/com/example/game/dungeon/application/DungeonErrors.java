package com.example.game.dungeon.application;

import org.springframework.http.HttpStatus;

import com.example.game.shared.api.ApiException;

final class DungeonErrors {

	private DungeonErrors() {
	}

	static ApiException notAtEntrance() {
		return new ApiException(
				"DUNGEON_NOT_AT_ENTRANCE",
				"You must be at the Ancient Ruins to enter the keep.",
				HttpStatus.BAD_REQUEST);
	}

	static ApiException noActiveRun() {
		return new ApiException(
				"DUNGEON_RUN_NOT_FOUND",
				"You have no active dungeon expedition.",
				HttpStatus.NOT_FOUND);
	}

	static ApiException invalidAdvance() {
		return new ApiException(
				"INVALID_DUNGEON_PATH",
				"You cannot take that path from here.",
				HttpStatus.BAD_REQUEST);
	}

	static ApiException roomLocked() {
		return new ApiException(
				"DUNGEON_ROOM_LOCKED",
				"That chamber is not open yet.",
				HttpStatus.CONFLICT);
	}

	static ApiException encounterOutstanding() {
		return new ApiException(
				"UNRESOLVED_ENCOUNTER",
				"Resolve the current encounter before advancing.",
				HttpStatus.CONFLICT);
	}

	static ApiException alreadyComplete() {
		return new ApiException(
				"DUNGEON_ALREADY_COMPLETE",
				"This expedition is already finished.",
				HttpStatus.CONFLICT);
	}
}
