package com.example.game.combat.application;

import java.util.UUID;

import com.example.game.combat.domain.CombatSessionStatus;

/**
 * Notified after an encounter is closed. Implementations must not live in other modules'
 * persistence packages.
 */
public interface EncounterResolutionListener {

	void onEncounterClosed(UUID characterId, UUID encounterId, EncounterCloseReason reason);
}
