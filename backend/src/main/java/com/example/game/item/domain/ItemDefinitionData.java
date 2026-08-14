package com.example.game.item.domain;

import com.example.game.inventory.domain.EquipmentSlot;

/**
 * Static definition fields needed by generators and validators. Not a persistence entity.
 */
public record ItemDefinitionData(
		String code,
		String name,
		ItemType type,
		ItemRarity catalogRarity,
		boolean legacy,
		EquipmentSlot equipmentSlot,
		boolean twoHanded,
		WeaponFamily weaponFamily,
		ArmorCategory armorCategory,
		Integer weaponDamage,
		Integer armorValue,
		int requiredLevel,
		int requiredStrength,
		int requiredAgility,
		int requiredEndurance,
		int requiredPerception
) {
}
