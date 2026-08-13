package com.example.game.character.domain;

/**
 * Isolated MVP balance constants and derived-stat formulas for characters.
 */
public final class CharacterBalance {

	public static final int STARTING_LEVEL = 1;
	public static final int STARTING_EXPERIENCE = 0;
	public static final int STARTING_STRENGTH = 5;
	public static final int STARTING_AGILITY = 5;
	public static final int STARTING_ENDURANCE = 5;
	public static final int STARTING_PERCEPTION = 5;
	public static final int STARTING_GOLD = 100;
	public static final int MAX_LEVEL = 10;

	public static final int BASE_MAX_HEALTH = 100;
	public static final int HEALTH_PER_ENDURANCE = 12;

	public static final int BASE_MAX_STAMINA = 50;
	public static final int STAMINA_PER_ENDURANCE = 4;
	public static final int STAMINA_PER_AGILITY = 2;

	/**
	 * Share of a maximum vital restored after a combat defeat. Office-first enough to keep the
	 * loop playable, but not a full free refill.
	 */
	public static final int DEFEAT_RECOVERY_PERCENT = 50;

	private CharacterBalance() {
	}

	public static int maxHealth(int endurance) {
		return BASE_MAX_HEALTH + (endurance * HEALTH_PER_ENDURANCE);
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
