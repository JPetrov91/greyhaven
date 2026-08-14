package com.example.game.crafting.application;

import org.springframework.http.HttpStatus;

import com.example.game.shared.api.ApiException;

final class CraftingErrors {

	private CraftingErrors() {
	}

	static ApiException recipeNotFound() {
		return new ApiException(
				"RECIPE_NOT_FOUND",
				"That recipe does not exist.",
				HttpStatus.NOT_FOUND);
	}

	static ApiException professionRankTooLow() {
		return new ApiException(
				"PROFESSION_RANK_TOO_LOW",
				"Your profession rank is too low for that recipe.",
				HttpStatus.BAD_REQUEST);
	}

	static ApiException characterLevelTooLow() {
		return new ApiException(
				"CHARACTER_LEVEL_TOO_LOW",
				"Your character level is too low for that recipe.",
				HttpStatus.BAD_REQUEST);
	}

	static ApiException insufficientGold() {
		return new ApiException(
				"INSUFFICIENT_GOLD",
				"You do not have enough gold to craft that recipe.",
				HttpStatus.BAD_REQUEST);
	}

	static ApiException missingMaterials() {
		return new ApiException(
				"MISSING_MATERIALS",
				"You do not have the required materials.",
				HttpStatus.BAD_REQUEST);
	}

	static ApiException jobInProgress() {
		return new ApiException(
				"CRAFTING_JOB_IN_PROGRESS",
				"Claim your current crafting job before starting another.",
				HttpStatus.CONFLICT);
	}

	static ApiException jobNotFound() {
		return new ApiException(
				"CRAFTING_JOB_NOT_FOUND",
				"That crafting job does not exist.",
				HttpStatus.NOT_FOUND);
	}

	static ApiException jobNotReady() {
		return new ApiException(
				"CRAFTING_JOB_NOT_READY",
				"That crafting job has not finished yet.",
				HttpStatus.CONFLICT);
	}

	static ApiException jobAlreadyClaimed() {
		return new ApiException(
				"CRAFTING_JOB_ALREADY_CLAIMED",
				"That crafting result was already claimed.",
				HttpStatus.CONFLICT);
	}

	static ApiException locationCannotCraft() {
		return new ApiException(
				"LOCATION_CANNOT_CRAFT",
				"You can only craft at the Craftsmen Ward.",
				HttpStatus.BAD_REQUEST);
	}

	static ApiException cannotSalvageEquipped() {
		return new ApiException(
				"CANNOT_SALVAGE_EQUIPPED_ITEM",
				"Unequip that item before salvaging it.",
				HttpStatus.BAD_REQUEST);
	}

	static ApiException cannotSalvageListed() {
		return new ApiException(
				"CANNOT_SALVAGE_LISTED_ITEM",
				"Cancel the marketplace listing before salvaging that item.",
				HttpStatus.CONFLICT);
	}

	static ApiException cannotSalvageItem() {
		return new ApiException(
				"CANNOT_SALVAGE_ITEM",
				"Only unequipped equipment can be salvaged.",
				HttpStatus.BAD_REQUEST);
	}

	static ApiException salvageNeedsInventorySpace() {
		return new ApiException(
				"INVENTORY_FULL",
				"Free inventory space before salvaging.",
				HttpStatus.CONFLICT);
	}

	static ApiException rewardsNeedInventorySpace() {
		return new ApiException(
				"INVENTORY_FULL",
				"Free inventory space before claiming this craft.",
				HttpStatus.CONFLICT);
	}

	static ApiException itemDefinitionMissing(String itemCode) {
		return new ApiException(
				"ITEM_DEFINITION_MISSING",
				"Item definition missing for code: " + itemCode,
				HttpStatus.INTERNAL_SERVER_ERROR);
	}
}
