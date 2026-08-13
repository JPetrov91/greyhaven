package com.example.game.character.application;

import java.util.UUID;

/**
 * Port for granting starter equipment when a character is created.
 */
public interface StarterLoadoutGranter {

	void grantStarterLoadout(UUID characterId);
}
