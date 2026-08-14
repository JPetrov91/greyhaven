package com.example.game.shared.balance;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import com.example.game.character.domain.CharacterBalance;
import com.example.game.character.domain.ProgressionBalance;
import com.example.game.inventory.domain.InventoryBalance;

class GameBalanceCatalogTest {

	@Test
	void classpathYamlMatchesPhase2Facades() {
		GameBalance balance = GameBalanceCatalog.get();

		assertThat(balance.character().maxLevel()).isEqualTo(30);
		assertThat(balance.character().healthPerLevel()).isEqualTo(5);
		assertThat(balance.character().staminaPerAgility()).isEqualTo(3);
		assertThat(balance.progression().attributePointsPerLevel()).isEqualTo(2);
		assertThat(balance.progression().cumulativeXpToReachLevel()).hasSize(31);
		assertThat(balance.progression().cumulativeXpToReachLevel()[30]).isEqualTo(184830);
		assertThat(balance.progression().freeRespecMaxLevel()).isEqualTo(10);
		assertThat(balance.combat().baseAccuracy()).isEqualTo(75);
		assertThat(balance.recovery().bands()).hasSize(4);
		assertThat(balance.recovery().bands().get(0).maxLevel()).isEqualTo(5);
		assertThat(balance.recovery().bands().get(0).healthPercentPerMinute()).isEqualTo(20.0);
		assertThat(balance.recovery().bands().get(0).staminaPercentPerMinute()).isEqualTo(40.0);
		assertThat(balance.recovery().bands().get(3).maxLevel()).isEqualTo(30);
		assertThat(balance.recovery().bands().get(3).healthPercentPerMinute()).isEqualTo(7.5);
		assertThat(balance.inventory().defaultCapacity()).isEqualTo(40);

		assertThat(CharacterBalance.MAX_LEVEL).isEqualTo(30);
		assertThat(ProgressionBalance.cumulativeXpForLevel(11)).isEqualTo(7230);
		assertThat(ProgressionBalance.xpToNextLevel(11)).isEqualTo(2000);
		assertThat(InventoryBalance.DEFAULT_CAPACITY).isEqualTo(40);
	}
}
