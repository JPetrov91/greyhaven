package com.example.game.sparring.domain;

import com.example.game.character.domain.CharacterBalance;
import com.example.game.character.domain.CharacterStatCalculator;
import com.example.game.character.domain.DerivedCombatStats;
import com.example.game.character.domain.ProgressionBalance;
import com.example.game.combat.domain.EnemyAiArchetype;
import com.example.game.shared.domain.RandomProvider;

/**
 * Constrained random drill-bot generation. Attribute budget matches player leveling; equipment
 * power is a narrow level corridor, not loot.
 */
public final class SparringBotGenerator {

	private SparringBotGenerator() {
	}

	public static SparringBotProfile generate(int level, RandomProvider random) {
		if (!SparringBots.isValidBotLevel(level)) {
			throw new IllegalArgumentException("sparring bot level must be 1-10");
		}
		int strength = CharacterBalance.STARTING_STRENGTH;
		int agility = CharacterBalance.STARTING_AGILITY;
		int endurance = CharacterBalance.STARTING_ENDURANCE;
		int perception = CharacterBalance.STARTING_PERCEPTION;
		int points = (level - CharacterBalance.STARTING_LEVEL) * ProgressionBalance.ATTRIBUTE_POINTS_PER_LEVEL;
		for (int i = 0; i < points; i++) {
			int pick = random.nextInt(0, 3);
			if (pick == 0 && strength < ProgressionBalance.MAX_ATTRIBUTE_VALUE) {
				strength++;
			}
			else if (pick == 1 && agility < ProgressionBalance.MAX_ATTRIBUTE_VALUE) {
				agility++;
			}
			else if (pick == 2 && endurance < ProgressionBalance.MAX_ATTRIBUTE_VALUE) {
				endurance++;
			}
			else if (perception < ProgressionBalance.MAX_ATTRIBUTE_VALUE) {
				perception++;
			}
			else if (strength < ProgressionBalance.MAX_ATTRIBUTE_VALUE) {
				strength++;
			}
			else if (agility < ProgressionBalance.MAX_ATTRIBUTE_VALUE) {
				agility++;
			}
			else if (endurance < ProgressionBalance.MAX_ATTRIBUTE_VALUE) {
				endurance++;
			}
		}
		int weaponDamage = 4 + level;
		int armorValue = level;
		DerivedCombatStats derived = CharacterStatCalculator.calculate(
				strength, agility, perception, weaponDamage, armorValue);
		int physical = derived.physicalDamage();
		return new SparringBotProfile(
				SparringBots.nameForLevel(level),
				level,
				strength,
				agility,
				endurance,
				perception,
				CharacterBalance.maxHealth(endurance, level),
				CharacterBalance.maxStamina(endurance, agility),
				Math.max(1, physical - 2),
				physical + 1,
				derived.armor(),
				derived.accuracy(),
				derived.dodge(),
				derived.criticalChance(),
				archetypeFor(level));
	}

	static EnemyAiArchetype archetypeFor(int level) {
		if (level <= 3) {
			return EnemyAiArchetype.AGGRESSIVE;
		}
		if (level <= 6) {
			return EnemyAiArchetype.DEFENSIVE;
		}
		return EnemyAiArchetype.ARMORED;
	}
}
