package com.example.game.character.application;

import java.util.List;
import java.util.UUID;

public interface CharacterUnlockQuery {

	List<String> unlockCodesOf(UUID characterId);
}
