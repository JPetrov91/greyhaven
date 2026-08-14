package com.example.game.pvp.api;

import java.util.UUID;

import jakarta.validation.constraints.NotNull;

public record PvpChallengeRequest(@NotNull UUID defenderId) {
}
