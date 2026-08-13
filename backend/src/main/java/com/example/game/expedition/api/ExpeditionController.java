package com.example.game.expedition.api;

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
import com.example.game.expedition.application.ExpeditionApplicationService;
import com.example.game.expedition.application.ExpeditionRewardItemView;
import com.example.game.expedition.application.ExpeditionRewardsView;
import com.example.game.expedition.application.ExpeditionView;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/expeditions")
public class ExpeditionController {

	private final ExpeditionApplicationService expeditionApplicationService;

	public ExpeditionController(ExpeditionApplicationService expeditionApplicationService) {
		this.expeditionApplicationService = expeditionApplicationService;
	}

	@GetMapping("/current")
	public ResponseEntity<ExpeditionResponse> current(@AuthenticationPrincipal AccountPrincipal principal) {
		ExpeditionView view = expeditionApplicationService.current(principal.getAccountId());
		if (view == null) {
			return ResponseEntity.noContent().build();
		}
		return ResponseEntity.ok(toResponse(view));
	}

	@PostMapping
	public ExpeditionResponse start(
			@AuthenticationPrincipal AccountPrincipal principal,
			@Valid @RequestBody StartExpeditionRequest request) {
		return toResponse(expeditionApplicationService.start(principal.getAccountId(), request.strategy()));
	}

	@PostMapping("/{id}/claim")
	public ExpeditionResponse claim(
			@AuthenticationPrincipal AccountPrincipal principal,
			@PathVariable("id") UUID expeditionId) {
		return toResponse(expeditionApplicationService.claim(principal.getAccountId(), expeditionId));
	}

	static ExpeditionResponse toResponse(ExpeditionView view) {
		return new ExpeditionResponse(
				view.id(),
				view.expeditionType(),
				view.expeditionName(),
				view.strategy(),
				view.status(),
				view.startedAt(),
				view.completesAt(),
				view.claimedAt(),
				view.resultReady(),
				toRewards(view.rewards()));
	}

	private static ExpeditionRewardsResponse toRewards(ExpeditionRewardsView rewards) {
		if (rewards == null) {
			return null;
		}
		return new ExpeditionRewardsResponse(
				rewards.xp(),
				rewards.gold(),
				rewards.injuryDamage(),
				rewards.items().stream().map(ExpeditionController::toRewardItem).toList());
	}

	private static ExpeditionRewardItemResponse toRewardItem(ExpeditionRewardItemView item) {
		return new ExpeditionRewardItemResponse(item.itemCode(), item.itemName(), item.quantity());
	}
}
