package com.example.game.character.domain;

/**
 * Backend-calculated combat statistics derived from attributes and equipment.
 *
 * <p>Max health and max stamina are deliberately absent. Those are persisted on the character
 * (see {@code characters.max_health} / {@code characters.max_stamina}, written via
 * {@link CharacterBalance}) because current health and stamina are clamped against them, and a
 * value cannot have two owners.
 */
public record DerivedCombatStats(
		int physicalDamage,
		int accuracy,
		int dodge,
		int criticalChance,
		int armor
) {
}
