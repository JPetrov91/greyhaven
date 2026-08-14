package com.example.game.inventory.application;

import com.example.game.item.domain.AffixKind;
import com.example.game.item.domain.AffixStat;

public record ItemAffixView(
		String code,
		AffixKind kind,
		String displayName,
		AffixStat stat,
		int magnitude
) {
}
