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

	static ApiException noPotion() {
		return new ApiException(
				"NO_POTION",
				"You have no healing potion to use.",
				HttpStatus.BAD_REQUEST);
	}

	/**
	 * Victory rewards are all-or-nothing, so a full inventory aborts the round rather than
	 * discarding loot the player earned.
	 */
	static ApiException rewardsNeedInventorySpace() {
		return new ApiException(
				"INVENTORY_FULL",
				"Your inventory is full. Retreat and make room before finishing this fight.",
				HttpStatus.CONFLICT);
	}
}
