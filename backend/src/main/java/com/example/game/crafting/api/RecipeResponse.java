package com.example.game.crafting.api;

import java.util.List;

import com.example.game.crafting.domain.Profession;
import com.example.game.item.domain.ItemRarity;

public record RecipeResponse(
		String code,
		String name,
		Profession profession,
		int requiredProfessionRank,
		int requiredCharacterLevel,
		int goldCost,
		int durationSeconds,
		String outputItemCode,
		String outputItemName,
		int outputQuantity,
		ItemRarity minRarity,
		ItemRarity maxRarity,
		int professionXp,
		boolean available,
		String unavailableReason,
		List<RecipeInputResponse> inputs
) {
}
