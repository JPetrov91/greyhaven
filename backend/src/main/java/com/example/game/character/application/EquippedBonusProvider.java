package com.example.game.character.application;

import java.util.UUID;

/**
 * Port for reading equipment bonuses without coupling character reads to inventory persistence.
 */
public interface EquippedBonusProvider {

	EquippedBonuses bonusesFor(UUID characterId);
}
