package com.example.game.dungeon.api;

import jakarta.validation.constraints.NotBlank;

public record DungeonAdvanceRequest(@NotBlank String edgeCode) {
}
