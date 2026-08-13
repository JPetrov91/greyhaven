package com.example.game.expedition.application;

import org.springframework.http.HttpStatus;

import com.example.game.shared.api.ApiException;

final class ExpeditionErrors {

	private ExpeditionErrors() {
	}

	static ApiException expeditionNotFound() {
		return new ApiException(
				"EXPEDITION_NOT_FOUND",
				"That expedition does not exist.",
				HttpStatus.NOT_FOUND);
	}

	static ApiException expeditionInProgress() {
		return new ApiException(
				"EXPEDITION_IN_PROGRESS",
				"You already have an expedition in progress.",
				HttpStatus.CONFLICT);
	}

	static ApiException expeditionNotReady() {
		return new ApiException(
				"EXPEDITION_NOT_READY",
				"That expedition has not finished yet.",
				HttpStatus.CONFLICT);
	}

	static ApiException expeditionAlreadyClaimed() {
		return new ApiException(
				"EXPEDITION_ALREADY_CLAIMED",
				"Those expedition rewards were already claimed.",
				HttpStatus.CONFLICT);
	}

	static ApiException locationCannotStartExpedition() {
		return new ApiException(
				"LOCATION_CANNOT_START_EXPEDITION",
				"You cannot start an expedition from this location.",
				HttpStatus.BAD_REQUEST);
	}

	static ApiException rewardsNeedInventorySpace() {
		return new ApiException(
				"INVENTORY_FULL",
				"Free inventory space before claiming expedition rewards.",
				HttpStatus.CONFLICT);
	}

	static ApiException itemDefinitionMissing(String itemCode) {
		return new ApiException(
				"ITEM_DEFINITION_MISSING",
				"Item definition missing for code: " + itemCode,
				HttpStatus.INTERNAL_SERVER_ERROR);
	}
}
