package com.example.game.character.application;

import org.springframework.http.HttpStatus;

import com.example.game.shared.api.ApiException;

final class CharacterErrors {

	private CharacterErrors() {
	}

	static ApiException characterNotFound() {
		return new ApiException(
				"CHARACTER_NOT_FOUND",
				"No character exists for this account.",
				HttpStatus.NOT_FOUND);
	}

	static ApiException insufficientGold() {
		return new ApiException(
				"INSUFFICIENT_GOLD",
				"You do not have enough gold to purchase this item.",
				HttpStatus.BAD_REQUEST);
	}

	static ApiException insufficientGoldForRespec() {
		return new ApiException(
				"INSUFFICIENT_GOLD",
				"You do not have enough gold to respec.",
				HttpStatus.BAD_REQUEST);
	}
}
