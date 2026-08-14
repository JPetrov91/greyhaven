package com.example.game.market.domain;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class MerchantRulesTest {

	@Test
	void saleQuantityMustBeAvailable() {
		assertThat(MerchantRules.isValidQuantity(1, 1)).isTrue();
		assertThat(MerchantRules.isValidQuantity(3, 5)).isTrue();
		assertThat(MerchantRules.isValidQuantity(0, 5)).isFalse();
		assertThat(MerchantRules.isValidQuantity(6, 5)).isFalse();
		assertThat(MerchantRules.isValidQuantity(-1, 5)).isFalse();
	}

	@Test
	void nonStackablePurchasesAreSingleItems() {
		assertThat(MerchantRules.isValidPurchaseQuantity(1, false)).isTrue();
		assertThat(MerchantRules.isValidPurchaseQuantity(2, false)).isFalse();
		assertThat(MerchantRules.isValidPurchaseQuantity(5, true)).isTrue();
		assertThat(MerchantRules.isValidPurchaseQuantity(0, true)).isFalse();
	}

	@Test
	void stackablePurchasesRespectConfiguredMaximum() {
		assertThat(MerchantBalance.MAX_PURCHASE_QUANTITY).isEqualTo(99);
		assertThat(MerchantRules.isValidPurchaseQuantity(99, true)).isTrue();
		assertThat(MerchantRules.isValidPurchaseQuantity(100, true)).isFalse();
	}
}
