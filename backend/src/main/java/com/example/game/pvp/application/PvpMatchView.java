package com.example.game.pvp.application;

import java.util.List;
import java.util.UUID;

import com.example.game.combat.application.CombatActionPreviewView;
import com.example.game.combat.application.CombatEventView;
import com.example.game.combat.application.CombatIntentView;
import com.example.game.combat.application.CombatStatusView;
import com.example.game.combat.application.CombatTechniqueOptionView;
import com.example.game.combat.domain.CombatAction;
import com.example.game.pvp.domain.PvpMatchKind;
import com.example.game.pvp.domain.PvpMatchStatus;

public record PvpMatchView(
		UUID id,
		PvpMatchKind matchKind,
		PvpMatchStatus status,
		int roundNumber,
		String attackerName,
		String defenderName,
		UUID attackerId,
		UUID defenderId,
		int attackerHealth,
		int attackerMaxHealth,
		int attackerStamina,
		int attackerMaxStamina,
		int defenderHealth,
		int defenderMaxHealth,
		int defenderStamina,
		int defenderMaxStamina,
		boolean potionAvailable,
		List<CombatStatusView> attackerStatuses,
		List<CombatStatusView> defenderStatuses,
		List<CombatTechniqueOptionView> techniques,
		List<CombatEventView> events,
		CombatIntentView defenderIntent,
		List<CombatActionPreviewView> actionPreviews,
		PvpSettlementView settlement,
		boolean waitingForOpponent,
		CombatAction yourPendingAction,
		boolean outcomeAcknowledged
) {
}
