package com.example.game.inventory.application;

import java.util.List;
import java.util.UUID;

import com.example.game.inventory.domain.EquipmentSlot;
import com.example.game.item.domain.ArmorCategory;
import com.example.game.item.domain.ItemRarity;
import com.example.game.item.domain.ItemType;
import com.example.game.item.domain.WeaponFamily;

public record InventoryItemView(
		UUID id,
		UUID definitionId,
		String code,
		String name,
		String displayName,
		String description,
		ItemType type,
		ItemRarity rarity,
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
		EquipmentSlot equipmentSlot,
		WeaponFamily weaponFamily,
		ArmorCategory armorCategory,
		boolean usable,
		int listedQuantity,
		Integer rolledWeaponDamage,
		Integer rolledArmorValue,
		Integer weaponDamage,
		Integer armorValue,
		Integer healAmount,
		int accuracy,
		int criticalChance,
		int dodge,
		int strength,
		int agility,
		int endurance,
		int perception,
		int staminaCostReduction,
		List<ItemAffixView> affixes,
		ItemComparisonView comparison
) {
}
