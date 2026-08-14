package com.example.game.market.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

import com.example.game.item.domain.ItemRarity;

class MerchantPriceCalculatorTest {

	@Test
	void buyPriceIsBelowBaseAndSellPriceIsAbove() {
		assertThat(MerchantPriceCalculator.merchantBuyPrice(100, ItemRarity.COMMON, 0)).isEqualTo(55);
		assertThat(MerchantPriceCalculator.merchantSellPrice(100, ItemRarity.COMMON)).isEqualTo(130);
		assertThat(MerchantBalance.BUY_MULTIPLIER).isEqualTo(0.55);
		assertThat(MerchantBalance.SELL_MULTIPLIER).isEqualTo(1.30);
	}

	@Test
	void pricesRoundToNearestGoldAndNeverDropBelowOne() {
		assertThat(MerchantPriceCalculator.merchantBuyPrice(5, ItemRarity.COMMON, 0)).isEqualTo(3);
		assertThat(MerchantPriceCalculator.merchantSellPrice(5, ItemRarity.COMMON)).isEqualTo(7);
		assertThat(MerchantPriceCalculator.merchantBuyPrice(1, ItemRarity.COMMON, 0)).isEqualTo(1);
	}

	@Test
	void catalogRarityIsNotAppliedTwiceOnTopOfBaseValue() {
		assertThat(MerchantPriceCalculator.merchantBuyPrice(100, ItemRarity.UNCOMMON, 0)).isEqualTo(55);
		assertThat(MerchantPriceCalculator.merchantBuyPrice(100, ItemRarity.EPIC, 0)).isEqualTo(55);
		assertThat(MerchantPriceCalculator.merchantSellPrice(25, ItemRarity.UNCOMMON)).isEqualTo(33);
		assertThat(MerchantPriceCalculator.merchantSellPrice(25, ItemRarity.COMMON)).isEqualTo(33);
	}

	@Test
	void affixesIncreaseBuyValueWithoutCrossingBaseOrShopSell() {
		assertThat(MerchantPriceCalculator.merchantBuyPrice(100, ItemRarity.COMMON, 1)).isEqualTo(59);
		assertThat(MerchantPriceCalculator.merchantBuyPrice(100, ItemRarity.RARE, 2)).isEqualTo(64);
		assertThat(MerchantPriceCalculator.merchantBuyPrice(100, ItemRarity.EPIC, 3)).isEqualTo(68);
		assertThat(MerchantPriceCalculator.merchantBuyPrice(100, ItemRarity.EPIC, 3))
				.isLessThan(100)
				.isLessThan(MerchantPriceCalculator.merchantSellPrice(100, ItemRarity.COMMON));
	}

	@Test
	void buyStaysBelowBaseAndBaseStaysBelowSellForTypicalCatalogValues() {
		int[] bases = { 2, 5, 6, 9, 10, 12, 14, 18, 25, 28, 45, 90, 100 };
		for (int base : bases) {
			for (ItemRarity rarity : ItemRarity.values()) {
				int buy = MerchantPriceCalculator.merchantBuyPrice(base, rarity, 3);
				int sell = MerchantPriceCalculator.merchantSellPrice(base, rarity);
				assertThat(buy).as("buy %s base %s", rarity, base).isLessThan(base);
				assertThat(base).as("base %s", base).isLessThan(sell);
				assertThat(buy).isLessThan(sell);
			}
		}
	}

	@Test
	void zeroBaseValueStaysZero() {
		assertThat(MerchantPriceCalculator.merchantBuyPrice(0, ItemRarity.COMMON, 0)).isZero();
		assertThat(MerchantPriceCalculator.merchantSellPrice(0, ItemRarity.EPIC)).isZero();
	}

	@Test
	void rejectsInvalidInputs() {
		assertThatThrownBy(() -> MerchantPriceCalculator.merchantBuyPrice(-1, ItemRarity.COMMON, 0))
				.isInstanceOf(IllegalArgumentException.class);
		assertThatThrownBy(() -> MerchantPriceCalculator.merchantBuyPrice(10, ItemRarity.COMMON, -1))
				.isInstanceOf(IllegalArgumentException.class);
		assertThatThrownBy(() -> MerchantPriceCalculator.merchantSellPrice(10, null))
				.isInstanceOf(IllegalArgumentException.class);
	}
}
