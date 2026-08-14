package com.example.game.pvp.api;

import java.util.List;
import java.util.UUID;

import com.example.game.combat.api.CombatActionPreviewResponse;
import com.example.game.combat.api.CombatEventResponse;
import com.example.game.combat.api.CombatIntentResponse;
import com.example.game.combat.api.CombatStatusResponse;
import com.example.game.combat.api.CombatTechniqueOptionResponse;
import com.example.game.combat.domain.CombatAction;
import com.example.game.pvp.domain.PvpMatchKind;
import com.example.game.pvp.domain.PvpMatchStatus;

public record PvpMatchResponse(
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
		List<CombatStatusResponse> attackerStatuses,
		List<CombatStatusResponse> defenderStatuses,
		List<CombatTechniqueOptionResponse> techniques,
		List<CombatEventResponse> events,
		CombatIntentResponse defenderIntent,
		List<CombatActionPreviewResponse> actionPreviews,
		PvpSettlementResponse settlement,
		boolean waitingForOpponent,
		CombatAction yourPendingAction,
		boolean outcomeAcknowledged
) {
}
