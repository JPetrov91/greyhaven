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
				request.action()));
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
				view.roundNumber(),
				view.playerHealth(),
				view.playerMaxHealth(),
				view.playerStamina(),
				view.playerMaxStamina(),
				view.enemyHealth(),
				view.enemyMaxHealth(),
				toMonster(view.monster()),
				view.potionAvailable(),
				view.events().stream().map(CombatController::toEvent).toList(),
				toRewards(view.rewards()));
	}

	private static MonsterResponse toMonster(MonsterView monster) {
		return new MonsterResponse(
				monster.id(),
				monster.code(),
				monster.name(),
				monster.level(),
				monster.maxHealth());
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
				rewards.items().stream().map(CombatController::toRewardItem).toList());
	}

	private static CombatRewardItemResponse toRewardItem(CombatRewardItemView item) {
		return new CombatRewardItemResponse(
				item.itemCode(),
				item.itemName(),
				item.quantity());
	}
}
