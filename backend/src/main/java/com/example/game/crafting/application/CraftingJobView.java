package com.example.game.crafting.application;

import java.time.Instant;
import java.util.UUID;

import com.example.game.crafting.domain.CraftingJobStatus;
import com.example.game.crafting.domain.Profession;
import com.example.game.item.domain.ItemRarity;

public record CraftingJobView(
		UUID id,
		Profession profession,
		String recipeCode,
		String recipeName,
		CraftingJobStatus status,
		Instant startedAt,
		Instant completesAt,
		Instant claimedAt,
		boolean resultReady,
		String outputItemCode,
		String outputItemName,
		int outputQuantity,
		ItemRarity rarity,
		int professionXp
) {
}
