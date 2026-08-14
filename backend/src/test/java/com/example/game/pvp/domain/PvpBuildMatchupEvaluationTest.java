package com.example.game.pvp.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.example.game.combat.domain.CombatAction;
import com.example.game.combat.domain.CombatantStats;
import com.example.game.shared.domain.ScriptedRandomProvider;

class PvpBuildMatchupEvaluationTest {

	@Test
	void archetypesAreNotDominatedByASingleBuild() {
		PvpCombatantSnapshot str = combatant("STR", new CombatantStats(24, 60, 6, 8, 16, 8));
		PvpCombatantSnapshot agi = combatant("AGI", new CombatantStats(12, 70, 28, 8, 4, 20));
		PvpCombatantSnapshot per = combatant("PER", new CombatantStats(14, 92, 8, 12, 6, 10));

		int strVsAgiHits = hitsLanded(str, agi, new ScriptedRandomProvider(55, 90, 55, 90, 55, 90));
		int perVsAgiHits = hitsLanded(per, agi, new ScriptedRandomProvider(55, 90, 55, 90, 55, 90));
		int strVsPerDamage = damageDealt(str, per, new ScriptedRandomProvider(1, 90, 1, 90, 12));
		int agiVsPerDamage = damageDealt(agi, per, new ScriptedRandomProvider(1, 90, 1, 90, 12));

		assertThat(perVsAgiHits).isGreaterThan(strVsAgiHits);
		assertThat(strVsPerDamage).isGreaterThan(agiVsPerDamage);
		assertThat(strVsAgiHits).isNotEqualTo(perVsAgiHits);
	}

	private static int hitsLanded(
			PvpCombatantSnapshot attacker,
			PvpCombatantSnapshot defender,
			ScriptedRandomProvider random) {
		int hits = 0;
		int defenderHealth = 80;
		for (int round = 0; round < 3; round++) {
			PvpCombatState state = state(attacker, defender, defenderHealth);
			PvpRoundResult result = PvpCombatEngine.resolve(
					state, CombatAction.QUICK_ATTACK, null, CombatAction.DEFEND, null, random);
			if (result.defenderHealth() < defenderHealth) {
				hits++;
			}
			defenderHealth = result.defenderHealth();
		}
		return hits;
	}

	private static int damageDealt(
			PvpCombatantSnapshot attacker,
			PvpCombatantSnapshot defender,
			ScriptedRandomProvider random) {
		PvpRoundResult result = PvpCombatEngine.resolve(
				state(attacker, defender, 80),
				CombatAction.HEAVY_ATTACK,
				null,
				CombatAction.DEFEND,
				null,
				random);
		return 80 - result.defenderHealth();
	}

	private static PvpCombatState state(
			PvpCombatantSnapshot attacker,
			PvpCombatantSnapshot defender,
			int defenderHealth) {
		return new PvpCombatState(
				0,
				PvpMatchStatus.ACTIVE,
				attacker,
				defender,
				80,
				40,
				defenderHealth,
				40,
				0,
				0,
				List.of(),
				List.of(),
				false,
				false,
				ArenaDefenseStrategy.defaults());
	}

	private static PvpCombatantSnapshot combatant(String name, CombatantStats stats) {
		return new PvpCombatantSnapshot(
				name, 8, 10, 10, 10, 10, 80, 40, stats, null, 0, List.of(), Map.of(), null, 0, 0, List.of());
	}
}
