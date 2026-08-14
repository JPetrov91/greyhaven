package com.example.game.market.domain;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class MarketFeeAndBuyOrderRulesTest {

	@Test
	void listingAndSaleFeesUseCeiling() {
		assertThat(MarketFeeCalculator.listingFee(1)).isEqualTo(1);
		assertThat(MarketFeeCalculator.listingFee(100)).isEqualTo(2);
		assertThat(MarketFeeCalculator.buyOrderPostingFee(40)).isEqualTo(1);
		assertThat(MarketFeeCalculator.saleFee(8)).isEqualTo(1);
		assertThat(MarketFeeCalculator.saleFee(20)).isEqualTo(2);
		assertThat(MarketFeeCalculator.sellerProceeds(20)).isEqualTo(18);
	}

	@Test
	void buyOrderEscrowAndPartialFillReduceRemainingAndReservedGold() {
		assertThat(BuyOrderRules.escrowGold(10, 12)).isEqualTo(120);
		BuyOrderRules.Fill fill = BuyOrderRules.applyFill(10, 120, 12, 4);
		assertThat(fill.filledQuantity()).isEqualTo(4);
		assertThat(fill.remainingQuantity()).isEqualTo(6);
		assertThat(fill.grossGold()).isEqualTo(48);
		assertThat(fill.reservedGoldAfter()).isEqualTo(72);
		assertThat(fill.completed()).isFalse();

		BuyOrderRules.Fill rest = BuyOrderRules.applyFill(6, 72, 12, 6);
		assertThat(rest.completed()).isTrue();
		assertThat(rest.reservedGoldAfter()).isZero();
	}
}
