package com.example.game.pvp.api;

import java.util.List;
import java.util.UUID;

import com.example.game.inventory.domain.EquipmentSlot;
import com.example.game.item.domain.ItemRarity;
import com.example.game.item.domain.WeaponFamily;

public record PublicCharacterResponse(
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
		List<PublicEquippedItemResponse> equipment
) {
	public record PublicEquippedItemResponse(
			EquipmentSlot slot,
			String code,
			String displayName,
			ItemRarity rarity,
			Integer weaponDamage,
			Integer armorValue,
			List<PublicAffixResponse> affixes
	) {
	}

	public record PublicAffixResponse(
			String code,
			String displayName,
			String stat,
			int magnitude
	) {
	}
}
