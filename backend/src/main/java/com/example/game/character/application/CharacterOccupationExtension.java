package com.example.game.character.application;

import java.util.UUID;

/**
 * Extra occupation sources (Arena, duels) that block the same mutations as PvE combat.
 */
public interface CharacterOccupationExtension {

	boolean occupied(UUID characterId);
}
