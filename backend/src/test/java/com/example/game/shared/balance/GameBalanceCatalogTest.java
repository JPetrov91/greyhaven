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
		assertThat(balance.combat().armorK()).isEqualTo(50);
		assertThat(balance.combat().critChanceCap()).isEqualTo(35);
		assertThat(balance.recovery().bands()).hasSize(4);
		assertThat(balance.recovery().bands().get(0).maxLevel()).isEqualTo(5);
		assertThat(balance.recovery().bands().get(0).healthPercentPerMinute()).isEqualTo(20.0);
		assertThat(balance.recovery().bands().get(0).staminaPercentPerMinute()).isEqualTo(40.0);
		assertThat(balance.recovery().bands().get(3).maxLevel()).isEqualTo(30);
		assertThat(balance.recovery().bands().get(3).healthPercentPerMinute()).isEqualTo(7.5);
		assertThat(balance.inventory().defaultCapacity()).isEqualTo(40);
		assertThat(balance.mastery().maxLevel()).isEqualTo(10);
		assertThat(balance.mastery().xpPerVictory()).isEqualTo(12);
		assertThat(balance.mastery().unlockLevels()).containsExactly(2, 4, 6, 8, 10);
		assertThat(balance.mastery().cumulativeXpToReachLevel()).hasSize(11);
		assertThat(balance.mastery().cumulativeXpToReachLevel()[10]).isEqualTo(15000);
		assertThat(balance.items().baseRollPercentMin()).isEqualTo(95);
		assertThat(balance.items().commonAffixes()).isZero();
		assertThat(balance.items().epicAffixes()).isEqualTo(3);
		assertThat(balance.items().lightDodge()).isEqualTo(2);

		assertThat(CharacterBalance.MAX_LEVEL).isEqualTo(30);
		assertThat(ProgressionBalance.cumulativeXpForLevel(11)).isEqualTo(7230);
		assertThat(ProgressionBalance.xpToNextLevel(11)).isEqualTo(2000);
		assertThat(InventoryBalance.DEFAULT_CAPACITY).isEqualTo(40);
		assertThat(com.example.game.mastery.domain.MasteryBalance.MAX_LEVEL).isEqualTo(10);
		assertThat(com.example.game.mastery.domain.MasteryBalance.unlockLevels()).containsExactly(2, 4, 6, 8, 10);
		assertThat(com.example.game.mastery.domain.MasteryBalance.cumulativeXpForLevel(10)).isEqualTo(15000);
		assertThat(com.example.game.mastery.domain.MasteryBalance.cumulativeXpForLevel(2)).isEqualTo(200);
	}
}
