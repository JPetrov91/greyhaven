package com.example.game.inventory.api;

import java.util.List;
import java.util.UUID;

public record InventoryItemResponse(
		UUID id,
		UUID definitionId,
		String code,
		String name,
		String displayName,
		String description,
		String type,
		String rarity,
		int quantity,
		int requiredLevel,
		int requiredStrength,
		int requiredAgility,
		int requiredEndurance,
		int requiredPerception,
		int baseValue,
		int merchantBuyPrice,
		boolean equipped,
		boolean canEquip,
		boolean twoHanded,
		boolean legacy,
		String equipmentSlot,
		String weaponFamily,
		String armorCategory,
		boolean usable,
		int listedQuantity,
		Integer rolledWeaponDamage,
		Integer rolledArmorValue,
		Integer weaponDamage,
		Integer armorValue,
		Integer healAmount,
		List<ItemAffixResponse> affixes,
		ItemComparisonResponse comparison
) {
}
