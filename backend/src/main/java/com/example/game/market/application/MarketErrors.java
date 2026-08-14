package com.example.game.market.application;

import org.springframework.http.HttpStatus;

import com.example.game.shared.api.ApiException;

final class MarketErrors {

	private MarketErrors() {
	}

	static ApiException insufficientGold() {
		return new ApiException(
				"INSUFFICIENT_GOLD",
				"You do not have enough gold to purchase this item.",
				HttpStatus.BAD_REQUEST);
	}

	static ApiException listingNotFound() {
		return new ApiException(
				"LISTING_NOT_FOUND",
				"That marketplace listing does not exist.",
				HttpStatus.NOT_FOUND);
	}

	static ApiException listingNotActive() {
		return new ApiException(
				"LISTING_NOT_ACTIVE",
				"That listing is no longer available.",
				HttpStatus.CONFLICT);
	}

	static ApiException cannotBuyOwnListing() {
		return new ApiException(
				"CANNOT_BUY_OWN_LISTING",
				"You cannot buy your own marketplace listing.",
				HttpStatus.BAD_REQUEST);
	}

	static ApiException 		cannotSellEquippedItem() {
		return new ApiException(
				"CANNOT_SELL_EQUIPPED_ITEM",
				"Unequip that item before selling it.",
				HttpStatus.BAD_REQUEST);
	}

	static ApiException invalidListingQuantity() {
		return new ApiException(
				"INVALID_LISTING_QUANTITY",
				"That quantity is not available to list.",
				HttpStatus.BAD_REQUEST);
	}

	static ApiException invalidPrice() {
		return new ApiException(
				"INVALID_PRICE",
				"Listing price must be at least 1 gold.",
				HttpStatus.BAD_REQUEST);
	}

	static ApiException listingNotOwned() {
		return new ApiException(
				"LISTING_NOT_OWNED",
				"You can only cancel your own marketplace listings.",
				HttpStatus.FORBIDDEN);
	}

	static ApiException locationCannotUseMarket() {
		return new ApiException(
				"LOCATION_CANNOT_USE_MARKET",
				"You can only use the marketplace at the Market.",
				HttpStatus.BAD_REQUEST);
	}

	static ApiException buyerInventoryFull() {
		return new ApiException(
				"INVENTORY_FULL",
				"Free inventory space before buying this listing.",
				HttpStatus.CONFLICT);
	}

	static ApiException merchantNotFound() {
		return new ApiException(
				"MERCHANT_NOT_FOUND",
				"That merchant does not exist.",
				HttpStatus.NOT_FOUND);
	}

	static ApiException itemNotSoldByMerchant() {
		return new ApiException(
				"ITEM_NOT_SOLD_BY_MERCHANT",
				"That merchant does not sell this item.",
				HttpStatus.BAD_REQUEST);
	}

	static ApiException invalidMerchantQuantity() {
		return new ApiException(
				"INVALID_MERCHANT_QUANTITY",
				"That quantity cannot be traded with a merchant.",
				HttpStatus.BAD_REQUEST);
	}

	static ApiException cannotSellListedItem() {
		return new ApiException(
				"CANNOT_SELL_LISTED_ITEM",
				"Cancel the marketplace listing before selling that item to a merchant.",
				HttpStatus.CONFLICT);
	}

	static ApiException merchantInventoryFull() {
		return new ApiException(
				"INVENTORY_FULL",
				"Free inventory space before buying from this merchant.",
				HttpStatus.CONFLICT);
	}
}
