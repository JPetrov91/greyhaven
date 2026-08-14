package com.example.game.crafting.domain;

import java.util.List;
import java.util.Map;

/**
 * Pure recipe gate checks. Inventory quantities are supplied by the application layer.
 */
public final class RecipeValidator {

	private RecipeValidator() {
	}

	public record RecipeRequirement(
			Profession profession,
			int requiredProfessionRank,
			int requiredCharacterLevel,
			int goldCost,
			List<RecipeInput> inputs
	) {
	}

	public record RecipeInput(String itemCode, int quantity) {
	}

	public enum Failure {
		PROFESSION_RANK,
		CHARACTER_LEVEL,
		GOLD,
		MATERIALS
	}

	public static Failure validate(
			RecipeRequirement recipe,
			int professionRank,
			int characterLevel,
			int gold,
			Map<String, Integer> availableByCode) {
		if (recipe == null) {
			throw new IllegalArgumentException("recipe is required");
		}
		if (professionRank < recipe.requiredProfessionRank()) {
			return Failure.PROFESSION_RANK;
		}
		if (characterLevel < recipe.requiredCharacterLevel()) {
			return Failure.CHARACTER_LEVEL;
		}
		if (gold < recipe.goldCost()) {
			return Failure.GOLD;
		}
		for (RecipeInput input : recipe.inputs()) {
			int available = availableByCode.getOrDefault(input.itemCode(), 0);
			if (available < input.quantity()) {
				return Failure.MATERIALS;
			}
		}
		return null;
	}
}
