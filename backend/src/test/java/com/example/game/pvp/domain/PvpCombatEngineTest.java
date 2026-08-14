package com.example.game.pvp.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.example.game.combat.domain.CombatAction;
import com.example.game.combat.domain.CombatantStats;
import com.example.game.combat.domain.CombatEventType;
import com.example.game.shared.domain.ScriptedRandomProvider;

class PvpCombatEngineTest {

	@Test
	void attackerStrikeCanDefeatDefender() {
		PvpCombatState state = state(80, 80);
		PvpRoundResult result = PvpCombatEngine.resolve(
				state,
				CombatAction.QUICK_ATTACK,
				null,
				CombatAction.DEFEND,
				null,
				new ScriptedRandomProvider(1, 90, 90));
		assertThat(result.defenderHealth()).isLessThan(80);
		assertThat(result.events()).extracting(event -> event.type()).contains(CombatEventType.PLAYER_ATTACK);
	}

	@Test
	void snapshotPotionChargesAreConsumedNotInventories() {
		PvpCombatState state = state(20, 80);
		state = new PvpCombatState(
				state.roundNumber(),
				state.status(),
				withPotions(state.attacker(), 1, 30),
				state.defender(),
				20,
				state.attackerStamina(),
				state.defenderHealth(),
				state.defenderStamina(),
				1,
				0,
				state.attackerStatuses(),
				state.defenderStatuses(),
				false,
				false,
				state.defense());
		PvpRoundResult result = PvpCombatEngine.resolve(
				state,
				CombatAction.USE_POTION,
				null,
				CombatAction.DEFEND,
				null,
				new ScriptedRandomProvider(90));
		assertThat(result.attackerPotionCharges()).isZero();
		assertThat(result.attackerHealth()).isGreaterThan(20);
	}

	private static PvpCombatantSnapshot withPotions(PvpCombatantSnapshot base, int charges, int heal) {
		return new PvpCombatantSnapshot(
				base.name(),
				base.level(),
				base.strength(),
				base.agility(),
				base.endurance(),
				base.perception(),
				base.maxHealth(),
				base.maxStamina(),
				base.stats(),
				base.weaponFamily(),
				base.staminaCostReduction(),
				base.techniqueCodes(),
				base.techniqueSpecs(),
				base.masteryPassive(),
				charges,
				heal,
				base.equipment());
	}

	private static PvpCombatState state(int attackerHp, int defenderHp) {
		PvpCombatantSnapshot attacker = combatant("Aegon", new CombatantStats(18, 88, 6, 8, 8, 10));
		PvpCombatantSnapshot defender = combatant("Morrigan", new CombatantStats(14, 80, 8, 8, 10, 10));
		return new PvpCombatState(
				0,
				PvpMatchStatus.ACTIVE,
				attacker,
				defender,
				attackerHp,
				40,
				defenderHp,
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
				name,
				5,
				10,
				10,
				10,
				10,
				80,
				40,
				stats,
				null,
				0,
				List.of(),
				Map.of(),
				null,
				0,
				0,
				List.of());
	}
}
