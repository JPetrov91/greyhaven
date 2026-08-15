package com.example.game.character.application;

import org.springframework.http.HttpStatus;

import com.example.game.shared.api.ApiException;

final class CharacterErrors {

	private CharacterErrors() {
	}

	static ApiException invalidAppearance() {
		return new ApiException(
				"INVALID_CHARACTER_APPEARANCE",
				"Avatar must belong to the selected gender.",
				HttpStatus.BAD_REQUEST);
	}

	static ApiException characterNotFound() {
		return new ApiException(
				"CHARACTER_NOT_FOUND",
				"No character exists for this account.",
				HttpStatus.NOT_FOUND);
	}

	static ApiException noActiveCharacter() {
		return new ApiException(
				"NO_ACTIVE_CHARACTER",
				"Select a character before entering the world.",
				HttpStatus.CONFLICT);
	}

	static ApiException characterSlotsFull() {
		return new ApiException(
				"CHARACTER_SLOTS_FULL",
				"This account already has the maximum number of characters.",
				HttpStatus.CONFLICT);
	}

	static ApiException slotOccupied() {
		return new ApiException(
				"CHARACTER_SLOT_OCCUPIED",
				"That character slot is already occupied.",
				HttpStatus.CONFLICT);
	}

	static ApiException invalidSlot() {
		return new ApiException(
				"INVALID_CHARACTER_SLOT",
				"Character slot must be between 0 and 2.",
				HttpStatus.BAD_REQUEST);
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
