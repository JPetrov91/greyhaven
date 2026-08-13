package com.example.game.market.api;

import java.util.UUID;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record CreateMarketListingRequest(
		@NotNull UUID itemInstanceId,
		@Min(1) int quantity,
		@Min(1) int price
) {
}
