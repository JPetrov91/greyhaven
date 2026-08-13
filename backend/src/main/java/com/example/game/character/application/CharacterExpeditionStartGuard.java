package com.example.game.character.application;

import java.util.UUID;

/**
 * Character-module extension point for starting expeditions when combat/encounter state must be clear.
 */
public interface CharacterExpeditionStartGuard {

	void assertCanStartExpedition(UUID characterId);
}
