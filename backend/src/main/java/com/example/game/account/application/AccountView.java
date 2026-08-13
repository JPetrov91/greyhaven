package com.example.game.account.application;

import java.util.UUID;

/**
 * Application-layer account snapshot. Controllers map this to API DTOs.
 */
public record AccountView(
		UUID accountId,
		String email
) {
}
