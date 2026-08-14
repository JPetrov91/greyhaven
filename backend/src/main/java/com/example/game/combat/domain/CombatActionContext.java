package com.example.game.combat.domain;

/**
 * Potion availability and heal amount for a single submitted action. Consumption is done in the
 * application layer before the engine runs.
 */
public record CombatActionContext(boolean potionAvailable, int potionHealAmount) {
}
