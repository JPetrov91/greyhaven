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
}
