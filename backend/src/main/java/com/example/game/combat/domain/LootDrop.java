package com.example.game.combat.domain;

import java.util.UUID;

public record LootDrop(UUID itemDefinitionId, String itemCode, int quantity) {
}
