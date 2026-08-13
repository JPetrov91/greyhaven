package com.example.game.world.application;

import java.util.UUID;

/**
 * Port so world travel can refuse movement while combat/encounters block the character.
 */
public interface CharacterTravelGuard {

	void assertCanTravel(UUID characterId);
}
