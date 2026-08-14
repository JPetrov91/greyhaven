package com.example.game.pvp.domain;

import com.example.game.combat.domain.CombatAction;

public record ChosenPvpAction(CombatAction action, String techniqueCode) {
}
