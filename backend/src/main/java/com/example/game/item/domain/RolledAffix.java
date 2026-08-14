package com.example.game.item.domain;

public record RolledAffix(
		String affixCode,
		AffixKind kind,
		int ordinal,
		int magnitude
) {
}
