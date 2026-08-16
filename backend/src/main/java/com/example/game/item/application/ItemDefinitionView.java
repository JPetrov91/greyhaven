package com.example.game.item.application;

import java.util.List;
import java.util.UUID;

import com.example.game.inventory.domain.EquipmentSlot;
import com.example.game.item.domain.ArmorCategory;
import com.example.game.item.domain.ItemRarity;
import com.example.game.item.domain.ItemType;
import com.example.game.item.domain.WeaponFamily;

/**
 * Identity of an item definition as seen by other modules.
 */
public record ItemDefinitionView(
		UUID id,
		String code,
		String name,
		String description,
		ItemType type,
		ItemRarity rarity,
		int baseValue,
		int requiredLevel,
		Integer weaponDamage,
		Integer weaponDamageMin,
		Integer weaponDamageMax,
		Integer blockSoakMin,
		Integer blockSoakMax,
		Integer armorValue,
		Integer healAmount,
		boolean twoHanded,
		EquipmentSlot equipmentSlot,
		WeaponFamily weaponFamily,
		ArmorCategory armorCategory,
		int requiredStrength,
		int requiredAgility,
		int requiredEndurance,
		int requiredPerception,
		List<ItemModifierView> modifiers
) {

	public com.example.game.item.domain.ItemDefinitionData toData() {
		return new com.example.game.item.domain.ItemDefinitionData(
				code,
				name,
				type,
				rarity,
				false,
				equipmentSlot,
				twoHanded,
				weaponFamily,
				armorCategory,
				weaponDamage,
				armorValue,
				requiredLevel,
				requiredStrength,
				requiredAgility,
				requiredEndurance,
				requiredPerception,
				weaponDamageMin,
				weaponDamageMax,
				blockSoakMin,
				blockSoakMax);
	}
}
