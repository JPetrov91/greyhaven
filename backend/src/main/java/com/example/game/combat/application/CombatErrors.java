package com.example.game.combat.application;

import org.springframework.http.HttpStatus;

import com.example.game.shared.api.ApiException;

final class CombatErrors {

	private CombatErrors() {
	}

	static ApiException encounterNotFound() {
		return new ApiException(
				"ENCOUNTER_NOT_FOUND",
				"That encounter does not exist.",
				HttpStatus.NOT_FOUND);
	}

	static ApiException combatNotFound() {
		return new ApiException(
				"COMBAT_NOT_FOUND",
				"That combat session does not exist.",
				HttpStatus.NOT_FOUND);
	}

	static ApiException combatNotActive() {
		return new ApiException(
				"COMBAT_NOT_ACTIVE",
				"Combat is not active.",
				HttpStatus.CONFLICT);
	}

	static ApiException combatStillActive() {
		return new ApiException(
				"COMBAT_STILL_ACTIVE",
				"Combat is still in progress.",
				HttpStatus.CONFLICT);
	}

	static ApiException staleCombatState() {
		return new ApiException(
				"STALE_COMBAT_STATE",
				"Combat has advanced. Refresh the current combat state before acting again.",
				HttpStatus.CONFLICT);
	}

	static ApiException outcomePending() {
		return new ApiException(
				"COMBAT_OUTCOME_PENDING",
				"Acknowledge the previous combat outcome before continuing.",
				HttpStatus.CONFLICT);
	}

	static ApiException unresolvedEncounter() {
		return new ApiException(
				"UNRESOLVED_ENCOUNTER",
				"You already have an unresolved encounter.",
				HttpStatus.CONFLICT);
	}

	static ApiException combatInProgress() {
		return new ApiException(
				"COMBAT_IN_PROGRESS",
				"You already have an active combat session.",
				HttpStatus.CONFLICT);
	}

	static ApiException locationNotDangerous() {
		return new ApiException(
				"LOCATION_NOT_DANGEROUS",
				"You can only search for encounters in dangerous locations.",
				HttpStatus.BAD_REQUEST);
	}

	static ApiException dungeonEncounterRequired() {
		return new ApiException(
				"DUNGEON_ENCOUNTER_REQUIRED",
				"You must fight this dungeon encounter.",
				HttpStatus.CONFLICT);
	}

	static ApiException encounterNotAvailable() {
		return new ApiException(
				"ENCOUNTER_NOT_AVAILABLE",
				"That encounter is no longer available.",
				HttpStatus.CONFLICT);
	}

	static ApiException insufficientStamina() {
		return new ApiException(
				"INSUFFICIENT_STAMINA",
				"You do not have enough stamina for that action.",
				HttpStatus.BAD_REQUEST);
	}

	static ApiException invalidTechnique() {
		return new ApiException(
				"INVALID_TECHNIQUE",
				"That technique cannot be used in this combat.",
				HttpStatus.BAD_REQUEST);
	}

	static ApiException noPotion() {
		return new ApiException(
				"NO_POTION",
				"You have no healing potion to use.",
				HttpStatus.BAD_REQUEST);
	}

	/**
	 * Victory is already persisted. Make room, then submit another action to claim the planned loot.
	 */
	static ApiException rewardsNeedInventorySpace() {
		return new ApiException(
				"INVENTORY_FULL",
				"Your inventory is full. Make room, then claim these rewards.",
				HttpStatus.CONFLICT);
	}
}
