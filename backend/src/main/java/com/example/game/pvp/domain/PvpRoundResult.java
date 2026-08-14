package com.example.game.pvp.domain;

import java.util.List;

import com.example.game.combat.domain.CombatEvent;
import com.example.game.combat.domain.StatusInstance;

public record PvpRoundResult(
		int roundNumber,
		PvpMatchStatus status,
		int attackerHealth,
		int attackerStamina,
		int defenderHealth,
		int defenderStamina,
		int attackerPotionCharges,
		int defenderPotionCharges,
		List<StatusInstance> attackerStatuses,
		List<StatusInstance> defenderStatuses,
		boolean lastDefenderMissed,
		boolean lastAttackerGuarded,
		List<CombatEvent> events
) {
}
