package com.example.game.combat.application;

import com.example.game.combat.domain.StatusType;

public record CombatStatusView(StatusType type, int stacks, int remainingRounds) {
}
