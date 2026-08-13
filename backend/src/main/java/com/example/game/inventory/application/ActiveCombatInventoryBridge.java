package com.example.game.inventory.application;

import java.util.UUID;

/**
 * Optional bridge so inventory heals stay aligned with an active combat session.
 * Implemented by the combat module; absent implementations are no-ops for non-combat flows.
 */
public interface ActiveCombatInventoryBridge {

	/**
	 * When the character is in ACTIVE combat, overwrite session player health after an inventory heal.
	 */
	void syncPlayerHealthIfInCombat(UUID characterId, int currentHealth);
}
