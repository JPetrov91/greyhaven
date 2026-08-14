package com.example.game.market.api;

import java.util.UUID;

public record MerchantStockItemResponse(
		UUID itemDefinitionId,
		String itemCode,
		String itemName,
		String description,
		String itemType,
		String rarity,
		int sellPrice,
		String availabilityType,
		int requiredLevel,
		Integer weaponDamage,
		Integer armorValue,
		Integer healAmount,
		boolean twoHanded,
		String equipmentSlot,
		String weaponFamily,
		String armorCategory,
		int requiredStrength,
		int requiredAgility,
		int requiredEndurance,
		int requiredPerception,
		int accuracy,
		int criticalChance,
		int dodge,
		int strength,
		int agility,
		int endurance,
		int perception,
		int staminaCostReduction
) {
}
