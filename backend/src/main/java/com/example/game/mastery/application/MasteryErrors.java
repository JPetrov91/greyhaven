package com.example.game.mastery.application;

import org.springframework.http.HttpStatus;

import com.example.game.shared.api.ApiException;

final class MasteryErrors {

	private MasteryErrors() {
	}

	static ApiException invalidLoadout(String reason) {
		return new ApiException("INVALID_TECHNIQUE_LOADOUT", reason, HttpStatus.BAD_REQUEST);
	}
}
