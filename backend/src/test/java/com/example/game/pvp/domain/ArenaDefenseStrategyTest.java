package com.example.game.pvp.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.example.game.combat.domain.CombatAction;
import com.example.game.mastery.domain.TechniqueEffectSpec;

class ArenaDefenseStrategyTest {

	@Test
	void healsWhenHpIsLowAndPotionsRemain() {
		ArenaDefenseStrategy strategy = ArenaDefenseStrategy.defaults();
		ChosenPvpAction choice = strategy.choose(view(20, 100, 50, 50, 80, 100, 2), 0, Map.of());
		assertThat(choice.action()).isEqualTo(CombatAction.USE_POTION);
	}

	@Test
	void defendsWhenStaminaIsLow() {
		ArenaDefenseStrategy strategy = ArenaDefenseStrategy.defaults();
		ChosenPvpAction choice = strategy.choose(view(90, 100, 5, 50, 80, 100, 0), 0, Map.of());
		assertThat(choice.action()).isEqualTo(CombatAction.DEFEND);
	}

	@Test
	void usesFinisherAgainstLowHealthEnemy() {
		ArenaDefenseStrategy strategy = new ArenaDefenseStrategy(
				CombatAction.QUICK_ATTACK, null, 10, 5, 40, "FINISH");
		TechniqueEffectSpec spec = new TechniqueEffectSpec("FINISH", 8, 0, 20, null, 0, 0, "");
		ChosenPvpAction choice = strategy.choose(
				new ArenaDefenseView(90, 100, 40, 50, 20, 100, 0, List.of(), List.of("FINISH")),
				0,
				Map.of("FINISH", spec));
		assertThat(choice.action()).isEqualTo(CombatAction.USE_TECHNIQUE);
		assertThat(choice.techniqueCode()).isEqualTo("FINISH");
	}

	private static ArenaDefenseView view(
			int hp, int maxHp, int sta, int maxSta, int enemyHp, int enemyMax, int potions) {
		return new ArenaDefenseView(hp, maxHp, sta, maxSta, enemyHp, enemyMax, potions, List.of(), List.of());
	}
}
