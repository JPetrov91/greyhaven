package com.example.game.combat.api;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.game.account.infrastructure.AccountPrincipal;
import com.example.game.combat.application.CombatApplicationService;
import com.example.game.combat.application.CombatEventView;
import com.example.game.combat.application.CombatRewardItemView;
import com.example.game.combat.application.CombatRewardsView;
import com.example.game.combat.application.CombatStatusView;
import com.example.game.combat.application.CombatTechniqueOptionView;
import com.example.game.combat.application.CombatView;
import com.example.game.combat.application.MonsterView;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/combat")
public class CombatController {

	private final CombatApplicationService combatApplicationService;

	public CombatController(CombatApplicationService combatApplicationService) {
		this.combatApplicationService = combatApplicationService;
	}

	@GetMapping("/current")
	public ResponseEntity<CombatResponse> current(@AuthenticationPrincipal AccountPrincipal principal) {
		CombatView view = combatApplicationService.current(principal.getAccountId());
		if (view == null) {
			return ResponseEntity.noContent().build();
		}
		return ResponseEntity.ok(toResponse(view));
	}

	@PostMapping("/{id}/actions")
	public CombatResponse submitAction(
			@AuthenticationPrincipal AccountPrincipal principal,
			@PathVariable("id") UUID combatId,
			@Valid @RequestBody CombatActionRequest request) {
		return toResponse(combatApplicationService.submitAction(
				principal.getAccountId(),
				combatId,
				request.action(),
				request.techniqueCode(),
				request.expectedRoundNumber()));
	}

	@PostMapping("/{id}/acknowledge")
	public ResponseEntity<Void> acknowledge(
			@AuthenticationPrincipal AccountPrincipal principal,
			@PathVariable("id") UUID combatId) {
		combatApplicationService.acknowledgeOutcome(principal.getAccountId(), combatId);
		return ResponseEntity.noContent().build();
	}

	static CombatResponse toResponse(CombatView view) {
		return new CombatResponse(
				view.id(),
				view.encounterId(),
				view.status(),
				view.rulesVersion(),
				view.roundNumber(),
				view.playerHealth(),
				view.playerMaxHealth(),
				view.playerStamina(),
				view.playerMaxStamina(),
				view.enemyHealth(),
				view.enemyMaxHealth(),
				view.enemyStamina(),
				view.enemyMaxStamina(),
				toMonster(view.monster()),
				view.potionAvailable(),
				view.playerStunned(),
				view.playerStatuses().stream().map(CombatController::toStatus).toList(),
				view.enemyStatuses().stream().map(CombatController::toStatus).toList(),
				view.techniques().stream().map(CombatController::toTechnique).toList(),
				new CoreActionCostsResponse(
						view.coreActionCosts().quickAttack(),
						view.coreActionCosts().heavyAttack(),
						view.coreActionCosts().preciseAttack()),
				view.events().stream().map(CombatController::toEvent).toList(),
				toRewards(view.rewards()),
				view.enemyIntent() == null
						? null
						: new CombatIntentResponse(view.enemyIntent().kind(), view.enemyIntent().label()),
				view.actionPreviews().stream().map(CombatController::toActionPreview).toList(),
				view.possibleLoot().stream()
						.map(item -> new CombatLootPreviewResponse(item.itemName(), item.dropChancePercent()))
						.toList());
	}

	private static CombatActionPreviewResponse toActionPreview(
			com.example.game.combat.application.CombatActionPreviewView preview) {
		return new CombatActionPreviewResponse(
				preview.action(),
				preview.techniqueCode(),
				preview.name(),
				preview.description(),
				preview.staminaCost(),
				preview.hitChancePercent(),
				preview.disabledReason());
	}

	private static MonsterResponse toMonster(MonsterView monster) {
		return new MonsterResponse(
				monster.id(),
				monster.code(),
				monster.name(),
				monster.level(),
				monster.maxHealth(),
				monster.archetype(),
				monster.tier());
	}

	private static CombatStatusResponse toStatus(CombatStatusView status) {
		return new CombatStatusResponse(status.type(), status.stacks(), status.remainingRounds());
	}

	private static CombatTechniqueOptionResponse toTechnique(CombatTechniqueOptionView technique) {
		return new CombatTechniqueOptionResponse(
				technique.code(),
				technique.name(),
				technique.description(),
				technique.staminaCost(),
				technique.disabledReason());
	}

	private static CombatEventResponse toEvent(CombatEventView event) {
		return new CombatEventResponse(
				event.roundNumber(),
				event.sequenceNumber(),
				event.type(),
				event.message());
	}

	private static CombatRewardsResponse toRewards(CombatRewardsView rewards) {
		if (rewards == null) {
			return null;
		}
		return new CombatRewardsResponse(
				rewards.xp(),
				rewards.gold(),
				rewards.previousLevel(),
				rewards.newLevel(),
				rewards.attributePointsGained(),
				rewards.items().stream().map(CombatController::toRewardItem).toList());
	}

	private static CombatRewardItemResponse toRewardItem(CombatRewardItemView item) {
		return new CombatRewardItemResponse(
				item.itemCode(),
				item.itemName(),
				item.quantity());
	}
}
