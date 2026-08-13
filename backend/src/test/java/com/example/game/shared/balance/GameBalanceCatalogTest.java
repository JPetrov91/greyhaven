package com.example.game.shared.balance;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import com.example.game.character.domain.CharacterBalance;
import com.example.game.character.domain.ProgressionBalance;
import com.example.game.inventory.domain.InventoryBalance;

class GameBalanceCatalogTest {

	@Test
	void classpathYamlMatchesPhase1Facades() {
		GameBalance balance = GameBalanceCatalog.get();

		assertThat(balance.character().maxLevel()).isEqualTo(10);
		assertThat(balance.character().startingGold()).isEqualTo(100);
		assertThat(balance.progression().attributePointsPerLevel()).isEqualTo(2);
		assertThat(balance.progression().cumulativeXpToReachLevel()).containsExactly(
				0, 0, 100, 350, 800, 1500, 2500, 3700, 5100, 6700, 8500);
		assertThat(balance.inventory().defaultCapacity()).isEqualTo(40);

		assertThat(CharacterBalance.MAX_LEVEL).isEqualTo(10);
		assertThat(ProgressionBalance.cumulativeXpForLevel(10)).isEqualTo(8500);
		assertThat(InventoryBalance.DEFAULT_CAPACITY).isEqualTo(40);
	}
}
