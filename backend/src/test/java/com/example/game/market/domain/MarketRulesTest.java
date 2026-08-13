package com.example.game.market.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.UUID;

import org.junit.jupiter.api.Test;

class MarketRulesTest {

	@Test
	void priceMustBeAtLeastOneGold() {
		assertThat(MarketRules.isValidPrice(1)).isTrue();
		assertThat(MarketRules.isValidPrice(80)).isTrue();
		assertThat(MarketRules.isValidPrice(0)).isFalse();
		assertThat(MarketRules.isValidPrice(-5)).isFalse();
	}

	@Test
	void listingQuantityCannotExceedUnreservedStack() {
		assertThat(MarketRules.availableQuantity(5, 2)).isEqualTo(3);
		assertThat(MarketRules.isValidQuantity(3, 3)).isTrue();
		assertThat(MarketRules.isValidQuantity(4, 3)).isFalse();
		assertThat(MarketRules.isValidQuantity(0, 3)).isFalse();
	}

	@Test
	void buyersCannotPurchaseTheirOwnListing() {
		UUID characterId = UUID.fromString("11111111-1111-4111-8111-111111111111");
		assertThat(MarketRules.isOwnListing(characterId, characterId)).isTrue();
		assertThat(MarketRules.isOwnListing(
				characterId,
				UUID.fromString("22222222-2222-4222-8222-222222222222"))).isFalse();
	}

	@Test
	void availableQuantityRejectsNegatives() {
		assertThatThrownBy(() -> MarketRules.availableQuantity(-1, 0))
				.isInstanceOf(IllegalArgumentException.class);
	}
}
