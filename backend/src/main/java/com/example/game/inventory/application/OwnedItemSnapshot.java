package com.example.game.inventory.application;

import java.util.UUID;

public record OwnedItemSnapshot(
		UUID itemInstanceId,
		UUID itemDefinitionId,
		int quantity,
		int unreservedQuantity,
		boolean equipped
) {
}
