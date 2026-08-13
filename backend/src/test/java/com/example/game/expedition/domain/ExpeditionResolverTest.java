package com.example.game.expedition.domain;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import com.example.game.item.domain.ItemCodes;
import com.example.game.shared.domain.ScriptedRandomProvider;

class ExpeditionResolverTest {

	@Test
	void emptyHaulStillAllowsInjury() {
		// injury chance 25% succeeds (10 < 25); empty haul 12% succeeds (5 < 12)
		ScriptedRandomProvider random = new ScriptedRandomProvider(10, 8, 5);

		ExpeditionResult result = ExpeditionResolver.resolve(
				ExpeditionType.FOREST_PATROL,
				ExpeditionStrategy.BALANCED,
				random);

		assertThat(result.xp()).isZero();
		assertThat(result.gold()).isZero();
		assertThat(result.injuryDamage()).isEqualTo(8);
		assertThat(result.items()).isEmpty();
	}

	@Test
	void balancedHaulRollsRewardsWithoutInjury() {
		// injury miss (90), empty miss (50), xp 20, gold 10,
		// wolf pelt hit (10) qty 1, potion miss (40), dagger miss (20), iron miss (10), leather miss (10)
		ScriptedRandomProvider random = new ScriptedRandomProvider(
				90, 50, 20, 10,
				10, 1,
				40,
				20,
				10,
				10);

		ExpeditionResult result = ExpeditionResolver.resolve(
				ExpeditionType.FOREST_PATROL,
				ExpeditionStrategy.BALANCED,
				random);

		assertThat(result.xp()).isEqualTo(20);
		assertThat(result.gold()).isEqualTo(10);
		assertThat(result.injuryDamage()).isZero();
		assertThat(result.items()).containsExactly(new ExpeditionLootDrop(ItemCodes.WOLF_PELT, 1));
	}

	@Test
	void aggressiveIncreasesRewardRanges() {
		ScriptedRandomProvider random = new ScriptedRandomProvider(
				90, 50, 40, 30,
				90,
				90,
				90,
				90,
				90);

		ExpeditionResult result = ExpeditionResolver.resolve(
				ExpeditionType.FOREST_PATROL,
				ExpeditionStrategy.AGGRESSIVE,
				random);

		assertThat(result.xp()).isEqualTo(40);
		assertThat(result.gold()).isEqualTo(30);
		assertThat(result.injuryDamage()).isZero();
		assertThat(result.items()).isEmpty();
	}

	@Test
	void cautiousStrategyCanApplyInjuryAndRollEveryLootCategory() {
		ScriptedRandomProvider random = new ScriptedRandomProvider(
				5, 4,
				99,
				10, 5,
				1, 2,
				1, 1,
				1, 1,
				1, 1,
				1, 1);

		ExpeditionResult result = ExpeditionResolver.resolve(
				ExpeditionType.FOREST_PATROL,
				ExpeditionStrategy.CAUTIOUS,
				random);

		assertThat(result.injuryDamage()).isEqualTo(4);
		assertThat(result.xp()).isEqualTo(10);
		assertThat(result.gold()).isEqualTo(5);
		assertThat(result.items()).containsExactly(
				new ExpeditionLootDrop(ItemCodes.WOLF_PELT, 2),
				new ExpeditionLootDrop(ItemCodes.HEALING_POTION, 1),
				new ExpeditionLootDrop(ItemCodes.OLD_DAGGER, 1),
				new ExpeditionLootDrop(ItemCodes.IRON_SWORD, 1),
				new ExpeditionLootDrop(ItemCodes.LEATHER_ARMOR, 1));
	}
}
