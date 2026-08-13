package com.example.game.market.infrastructure;

import java.util.UUID;

/**
 * How much of one item instance is held by active listings.
 */
public record ItemInstanceReservation(
		UUID itemInstanceId,
		Long reservedQuantity
) {
}
