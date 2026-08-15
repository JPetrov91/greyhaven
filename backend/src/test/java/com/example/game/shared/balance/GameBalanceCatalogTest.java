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
		assertThat(balance.market().merchantBuyMultiplier()).isEqualTo(0.55);
		assertThat(balance.market().merchantSellMultiplier()).isEqualTo(1.30);
		assertThat(balance.market().affixValuePerAffix()).isEqualTo(0.08);
		assertThat(balance.market().maxMerchantPurchaseQuantity()).isEqualTo(99);
		assertThat(balance.market().commonRarityModifier()).isEqualTo(1.0);
		assertThat(balance.market().epicRarityModifier()).isEqualTo(1.60);
		assertThat(balance.market().listingFeePercent()).isEqualTo(0.02);
		assertThat(balance.market().saleFeePercent()).isEqualTo(0.08);
		assertThat(balance.crafting().rankRarityBonusPerRank()).isEqualTo(1);
		assertThat(balance.crafting().maxRank()).isEqualTo(10);
		assertThat(balance.crafting().xpPerRecipe()).isEqualTo(15);
		assertThat(balance.crafting().cumulativeXpToReachRank()).hasSize(11);
		assertThat(com.example.game.crafting.domain.CraftingBalance.MAX_RANK).isEqualTo(10);
		assertThat(com.example.game.crafting.domain.CraftingBalance.cumulativeXpForRank(2)).isEqualTo(40);
		assertThat(balance.pvp().startingRating()).isEqualTo(1000);
		assertThat(balance.pvp().ratingKFactor()).isEqualTo(24);
		assertThat(balance.pvp().marksPerWin()).isEqualTo(8);
		assertThat(balance.pvp().maxArenaChallengesPerDay()).isEqualTo(20);
		assertThat(balance.expedition().forestPatrolDurationMinutes()).isEqualTo(20);
		assertThat(balance.expedition().balanced().injuryChancePercent()).isEqualTo(25);
		assertThat(com.example.game.expedition.domain.ExpeditionBalance.FOREST_PATROL_DURATION.toMinutes())
				.isEqualTo(20);

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
