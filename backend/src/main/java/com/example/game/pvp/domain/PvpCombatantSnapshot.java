package com.example.game.pvp.domain;

import java.util.List;
import java.util.Map;

import com.example.game.combat.domain.CombatantStats;
import com.example.game.item.domain.WeaponFamily;
import com.example.game.mastery.domain.TechniqueEffectSpec;

public record PvpCombatantSnapshot(
		String name,
		int level,
		int strength,
		int agility,
		int endurance,
		int perception,
		int maxHealth,
		int maxStamina,
		CombatantStats stats,
		WeaponFamily weaponFamily,
		int staminaCostReduction,
		List<String> techniqueCodes,
		Map<String, TechniqueEffectSpec> techniqueSpecs,
		TechniqueEffectSpec masteryPassive,
		int potionCharges,
		int potionHealAmount,
		List<PvpEquippedItemSnapshot> equipment
) {
}
