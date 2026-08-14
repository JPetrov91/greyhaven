package com.example.game.crafting.application;

public record RecipeInputView(
		String itemCode,
		String itemName,
		int quantity,
		int availableQuantity
) {
}
