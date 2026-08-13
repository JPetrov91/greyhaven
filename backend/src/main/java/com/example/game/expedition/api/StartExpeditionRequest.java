package com.example.game.expedition.api;

import com.example.game.expedition.domain.ExpeditionStrategy;

import jakarta.validation.constraints.NotNull;

public record StartExpeditionRequest(
		@NotNull ExpeditionStrategy strategy
) {
}
