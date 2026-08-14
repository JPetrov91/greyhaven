package com.example.game.character.domain;

import com.example.game.shared.balance.GameBalance;
import com.example.game.shared.balance.GameBalanceCatalog;

/**
 * Isolated balance constants and derived-stat formulas for characters.
 */
public final class CharacterBalance {

	private static final GameBalance.Character VALUES = GameBalanceCatalog.get().character();

	public static final int STARTING_LEVEL = VALUES.startingLevel();
	public static final int STARTING_EXPERIENCE = VALUES.startingExperience();
	public static final int STARTING_STRENGTH = VALUES.startingStrength();
	public static final int STARTING_AGILITY = VALUES.startingAgility();
	public static final int STARTING_ENDURANCE = VALUES.startingEndurance();
	public static final int STARTING_PERCEPTION = VALUES.startingPerception();
	public static final int STARTING_GOLD = VALUES.startingGold();
	public static final int MAX_LEVEL = VALUES.maxLevel();

	public static final int BASE_MAX_HEALTH = VALUES.baseMaxHealth();
	public static final int HEALTH_PER_ENDURANCE = VALUES.healthPerEndurance();
	public static final int HEALTH_PER_LEVEL = VALUES.healthPerLevel();

	public static final int BASE_MAX_STAMINA = VALUES.baseMaxStamina();
	public static final int STAMINA_PER_ENDURANCE = VALUES.staminaPerEndurance();
	public static final int STAMINA_PER_AGILITY = VALUES.staminaPerAgility();

	/**
	 * Share of a maximum vital restored after a combat defeat. Office-first enough to keep the
	 * loop playable, but not a full free refill.
	 */
	public static final int DEFEAT_RECOVERY_PERCENT = VALUES.defeatRecoveryPercent();

	private CharacterBalance() {
	}

	public static int maxHealth(int endurance, int level) {
		return BASE_MAX_HEALTH + (endurance * HEALTH_PER_ENDURANCE) + (level * HEALTH_PER_LEVEL);
	}

	public static int maxStamina(int endurance, int agility) {
		return BASE_MAX_STAMINA + (endurance * STAMINA_PER_ENDURANCE) + (agility * STAMINA_PER_AGILITY);
	}

	/**
	 * Health or stamina a defeated character is restored to, never below 1.
	 */
	public static int defeatRecovery(int maximum) {
		return Math.max(1, maximum * DEFEAT_RECOVERY_PERCENT / 100);
	}
}
