package com.example.game.inventory.application;

import java.util.UUID;

/**
 * Reserves healing potions into a combat snapshot so the same bottles cannot be reused.
 */
public interface HealingPotionConsumption {

	HealingPotionStock consumeUpTo(UUID characterId, int maxCharges);
}
