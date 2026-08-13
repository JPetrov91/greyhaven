package com.example.game.character.application;

import java.util.UUID;

/**
 * Character-module extension point for mutations that are forbidden during active combat.
 */
public interface CharacterCombatGuard {

	void assertNotInActiveCombat(UUID characterId);
}
