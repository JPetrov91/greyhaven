package com.example.game.market.application;

import java.util.UUID;

import com.example.game.inventory.domain.EquipmentSlot;
import com.example.game.item.domain.ArmorCategory;
import com.example.game.item.domain.ItemRarity;
import com.example.game.item.domain.ItemType;
import com.example.game.item.domain.WeaponFamily;
import com.example.game.market.domain.MerchantAvailabilityType;

public record MerchantStockItemView(
		UUID itemDefinitionId,
		String itemCode,
		String itemName,
		String description,
		ItemType itemType,
		ItemRarity rarity,
		int sellPrice,
		MerchantAvailabilityType availabilityType,
		int requiredLevel,
		Integer weaponDamage,
		Integer armorValue,
		Integer healAmount,
		boolean twoHanded,
		EquipmentSlot equipmentSlot,
		WeaponFamily weaponFamily,
		ArmorCategory armorCategory,
		int requiredStrength,
		int requiredAgility,
		int requiredEndurance,
		int requiredPerception
) {
}
