package com.example.game.inventory.application;

import org.springframework.http.HttpStatus;

import com.example.game.shared.api.ApiException;

final class InventoryErrors {

	private InventoryErrors() {
	}

	static ApiException itemNotFound() {
		return new ApiException(
				"ITEM_NOT_FOUND",
				"That item was not found in your inventory.",
				HttpStatus.NOT_FOUND);
	}

	static ApiException itemNotOwned() {
		return new ApiException(
				"ITEM_NOT_OWNED",
				"You do not own that item.",
				HttpStatus.FORBIDDEN);
	}

	static ApiException itemNotEquippable() {
		return new ApiException(
				"ITEM_NOT_EQUIPPABLE",
				"That item cannot be equipped.",
				HttpStatus.BAD_REQUEST);
	}

	static ApiException itemNotEquipped() {
		return new ApiException(
				"ITEM_NOT_EQUIPPED",
				"That item is not currently equipped.",
				HttpStatus.BAD_REQUEST);
	}

	static ApiException equipRequirementsNotMet() {
		return new ApiException(
				"EQUIP_REQUIREMENTS_NOT_MET",
				"You do not meet the requirements to equip that item.",
				HttpStatus.BAD_REQUEST);
	}

	static ApiException twoHandedBlocksOffHand() {
		return new ApiException(
				"EQUIP_TWO_HANDED_BLOCKS_OFF_HAND",
				"Unequip your two-handed weapon before using the off-hand slot.",
				HttpStatus.BAD_REQUEST);
	}

	static ApiException itemNotUsable() {
		return new ApiException(
				"ITEM_NOT_USABLE",
				"That item cannot be used.",
				HttpStatus.BAD_REQUEST);
	}

	static ApiException itemListed() {
		return new ApiException(
				"ITEM_LISTED",
				"That item is listed on the marketplace.",
				HttpStatus.CONFLICT);
	}

	static InventoryFullException inventoryFull() {
		return new InventoryFullException("Your inventory is full.");
	}

	static ApiException itemDefinitionMissing(String code) {
		return new ApiException(
				"ITEM_DEFINITION_MISSING",
				"Required item definition is missing: " + code,
				HttpStatus.INTERNAL_SERVER_ERROR);
	}
}
