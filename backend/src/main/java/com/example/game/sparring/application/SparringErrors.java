package com.example.game.sparring.application;

import org.springframework.http.HttpStatus;

import com.example.game.shared.api.ApiException;

final class SparringErrors {

	private SparringErrors() {
	}

	static ApiException notAtYard() {
		return new ApiException(
				"NOT_AT_SPARRING_YARD",
				"You must be at the Sparring Yard to do that.",
				HttpStatus.CONFLICT);
	}

	static ApiException playerLevelTooHigh() {
		return new ApiException(
				"SPARRING_LEVEL_REQUIRED",
				"The Sparring Yard is for characters level 10 and below.",
				HttpStatus.CONFLICT);
	}

	static ApiException invalidBotLevel() {
		return new ApiException(
				"INVALID_BOT_LEVEL",
				"Choose a drill partner between levels 1 and 10.",
				HttpStatus.BAD_REQUEST);
	}

	static ApiException combatInProgress() {
		return new ApiException(
				"COMBAT_IN_PROGRESS",
				"You already have an active combat session.",
				HttpStatus.CONFLICT);
	}

	static ApiException unresolvedEncounter() {
		return new ApiException(
				"UNRESOLVED_ENCOUNTER",
				"You already have an unresolved encounter.",
				HttpStatus.CONFLICT);
	}

	static ApiException outcomePending() {
		return new ApiException(
				"COMBAT_OUTCOME_PENDING",
				"Acknowledge the previous combat outcome before continuing.",
				HttpStatus.CONFLICT);
	}
}
