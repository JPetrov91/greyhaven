package com.example.game.pvp.application;

import org.springframework.http.HttpStatus;

import com.example.game.shared.api.ApiException;

final class PvpErrors {

	private PvpErrors() {
	}

	static ApiException notAtArena() {
		return new ApiException(
				"NOT_AT_ARENA",
				"You must be at the Arena to do that.",
				HttpStatus.CONFLICT);
	}

	static ApiException selfChallenge() {
		return new ApiException(
				"SELF_CHALLENGE",
				"You cannot challenge yourself.",
				HttpStatus.BAD_REQUEST);
	}

	static ApiException matchNotFound() {
		return new ApiException(
				"PVP_MATCH_NOT_FOUND",
				"That match does not exist.",
				HttpStatus.NOT_FOUND);
	}

	static ApiException matchNotActive() {
		return new ApiException(
				"PVP_MATCH_NOT_ACTIVE",
				"That match is not active.",
				HttpStatus.CONFLICT);
	}

	static ApiException staleMatchState() {
		return new ApiException(
				"STALE_PVP_STATE",
				"The match has advanced. Refresh before acting again.",
				HttpStatus.CONFLICT);
	}

	static ApiException outcomePending() {
		return new ApiException(
				"PVP_OUTCOME_PENDING",
				"Acknowledge the previous Arena result before starting another match.",
				HttpStatus.CONFLICT);
	}

	static ApiException opponentOutOfRange() {
		return new ApiException(
				"OPPONENT_OUT_OF_RANGE",
				"That opponent is outside your Arena rating band.",
				HttpStatus.BAD_REQUEST);
	}

	static ApiException dailyChallengeLimit() {
		return new ApiException(
				"ARENA_CHALLENGE_LIMIT",
				"You have reached the daily Arena challenge limit.",
				HttpStatus.CONFLICT);
	}

	static ApiException occupied() {
		return new ApiException(
				"COMBAT_IN_PROGRESS",
				"That action is unavailable while combat is in progress.",
				HttpStatus.CONFLICT);
	}

	static ApiException invalidDefense() {
		return new ApiException(
				"INVALID_DEFENSE_STRATEGY",
				"That Arena defense configuration is not valid for your current loadout.",
				HttpStatus.BAD_REQUEST);
	}

	static ApiException alreadyPendingAction() {
		return new ApiException(
				"DUEL_ACTION_ALREADY_SUBMITTED",
				"You have already submitted an action for this round.",
				HttpStatus.CONFLICT);
	}

	static ApiException duelNotPending() {
		return new ApiException(
				"DUEL_NOT_PENDING",
				"That duel is not waiting to be accepted.",
				HttpStatus.CONFLICT);
	}

	static ApiException opponentBusy() {
		return new ApiException(
				"OPPONENT_BUSY",
				"That player is already in a duel.",
				HttpStatus.CONFLICT);
	}

	static ApiException noPotion() {
		return new ApiException(
				"NO_POTION",
				"You have no healing potion charges remaining in this match.",
				HttpStatus.BAD_REQUEST);
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
				"That technique is not available in this match.",
				HttpStatus.BAD_REQUEST);
	}
}
