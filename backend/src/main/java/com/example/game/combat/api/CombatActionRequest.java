package com.example.game.combat.api;

import com.example.game.combat.domain.CombatAction;

import jakarta.validation.constraints.NotNull;

public record CombatActionRequest(@NotNull CombatAction action) {
}
