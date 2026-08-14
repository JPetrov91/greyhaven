package com.example.game.crafting.api;

public record RecipeInputResponse(
		String itemCode,
		String itemName,
		int quantity,
		int availableQuantity
) {
}
