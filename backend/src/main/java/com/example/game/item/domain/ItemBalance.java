package com.example.game.item.domain;

import com.example.game.shared.balance.GameBalance;
import com.example.game.shared.balance.GameBalanceCatalog;

/**
 * Item generation and armor-category modifiers. Loaded from {@code game-balance.yml}.
 */
public final class ItemBalance {

	private static final GameBalance.Items VALUES = GameBalanceCatalog.get().items();

	public static final int BASE_ROLL_PERCENT_MIN = VALUES.baseRollPercentMin();
	public static final int BASE_ROLL_PERCENT_MAX = VALUES.baseRollPercentMax();

	private ItemBalance() {
	}

	public static int rarityWeight(ItemRarity rarity) {
		return switch (rarity) {
			case COMMON -> VALUES.commonWeight();
			case UNCOMMON -> VALUES.uncommonWeight();
			case RARE -> VALUES.rareWeight();
			case EPIC -> VALUES.epicWeight();
		};
	}

	public static int affixCount(ItemRarity rarity) {
		return switch (rarity) {
			case COMMON -> VALUES.commonAffixes();
			case UNCOMMON -> VALUES.uncommonAffixes();
			case RARE -> VALUES.rareAffixes();
			case EPIC -> VALUES.epicAffixes();
		};
	}

	public static int armorDodge(ArmorCategory category) {
		if (category == null) {
			return 0;
		}
		return switch (category) {
			case LIGHT -> VALUES.lightDodge();
			case MEDIUM -> VALUES.mediumDodge();
			case HEAVY -> VALUES.heavyDodge();
		};
	}
}
