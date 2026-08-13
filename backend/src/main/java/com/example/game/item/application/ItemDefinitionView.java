package com.example.game.item.application;

import java.util.UUID;

/**
 * Identity of an item definition as seen by other modules.
 */
public record ItemDefinitionView(
		UUID id,
		String code,
		String name
) {
}
