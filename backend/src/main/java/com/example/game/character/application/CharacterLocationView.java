package com.example.game.character.application;

import java.util.UUID;

public record CharacterLocationView(
		UUID characterId,
		UUID currentLocationId
) {
}
