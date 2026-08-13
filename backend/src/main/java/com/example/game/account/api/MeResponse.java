package com.example.game.account.api;

import java.util.UUID;

public record MeResponse(
		UUID accountId,
		String email,
		boolean hasCharacter
) {
}
