package com.example.game.character.domain;

import java.util.List;

import com.example.game.shared.balance.GameBalance;
import com.example.game.shared.balance.GameBalanceCatalog;

/**
 * Configurable out-of-combat recovery rates. Lookup is isolated so later modifiers can wrap
 * these percents without changing elapsed-time math.
 */
public final class CharacterRecoveryBalance {

	private static final List<GameBalance.RecoveryBand> BANDS = GameBalanceCatalog.get().recovery().bands();

	private CharacterRecoveryBalance() {
	}

	public static GameBalance.RecoveryBand ratesForLevel(int level) {
		if (level < CharacterBalance.STARTING_LEVEL) {
			throw new IllegalArgumentException("level below minimum");
		}
		GameBalance.RecoveryBand match = null;
		for (GameBalance.RecoveryBand band : BANDS) {
			if (level <= band.maxLevel()) {
				match = band;
				break;
			}
		}
		if (match == null) {
			match = BANDS.get(BANDS.size() - 1);
		}
		return match;
	}
}
