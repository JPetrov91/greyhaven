package com.example.game.pvp.application;

import java.util.List;
import java.util.UUID;

import com.example.game.inventory.domain.EquipmentSlot;
import com.example.game.item.domain.ItemRarity;
import com.example.game.item.domain.WeaponFamily;

public record PublicCharacterView(
		UUID id,
		String name,
		int level,
		int strength,
		int agility,
		int endurance,
		int perception,
		int arenaRating,
		WeaponFamily weaponFamily,
		Integer weaponMasteryLevel,
		List<String> techniqueLoadout,
		List<PublicEquippedItemView> equipment
) {
	public record PublicEquippedItemView(
			EquipmentSlot slot,
			String code,
			String displayName,
			ItemRarity rarity,
			Integer weaponDamage,
			Integer armorValue,
			List<PublicAffixView> affixes
	) {
	}

	public record PublicAffixView(
			String code,
			String displayName,
			String stat,
			int magnitude
	) {
	}
}
