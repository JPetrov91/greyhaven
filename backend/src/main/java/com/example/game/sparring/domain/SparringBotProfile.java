package com.example.game.sparring.domain;

import com.example.game.combat.domain.EnemyAiArchetype;
import com.example.game.combat.domain.MonsterCombatProfile;
import com.example.game.combat.domain.MonsterTier;
import com.example.game.combat.domain.StatusType;

public record SparringBotProfile(
		String name,
		int level,
		int strength,
		int agility,
		int endurance,
		int perception,
		int maxHealth,
		int maxStamina,
		int damageMin,
		int damageMax,
		int armor,
		int accuracy,
		int dodge,
		int criticalChance,
		EnemyAiArchetype archetype
) {
	public MonsterCombatProfile toMonsterProfile() {
		return new MonsterCombatProfile(
				name,
				level,
				damageMin,
				damageMax,
				armor,
				accuracy,
				dodge,
				criticalChance,
				maxHealth,
				maxStamina,
				archetype,
				(StatusType) null,
				MonsterTier.NORMAL);
	}
}
