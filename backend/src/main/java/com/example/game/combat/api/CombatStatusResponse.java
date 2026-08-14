package com.example.game.combat.api;

import com.example.game.combat.domain.StatusType;

public record CombatStatusResponse(StatusType type, int stacks, int remainingRounds) {
}
