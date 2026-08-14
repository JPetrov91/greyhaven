package com.example.game.market.api;

import java.util.UUID;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record FulfillBuyOrderRequest(
		@NotNull UUID itemInstanceId,
		@Min(1) int quantity
) {
}
