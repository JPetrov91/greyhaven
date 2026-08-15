package com.example.game.quest.api;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.game.account.infrastructure.AccountPrincipal;
import com.example.game.quest.application.QuestApplicationService;
import com.example.game.quest.application.QuestObjectiveView;
import com.example.game.quest.application.QuestRewardView;
import com.example.game.quest.application.QuestView;

@RestController
@RequestMapping("/api/v1/quests")
public class QuestController {

	private final QuestApplicationService questApplicationService;

	public QuestController(QuestApplicationService questApplicationService) {
		this.questApplicationService = questApplicationService;
	}

	@GetMapping
	public QuestListResponse list(@AuthenticationPrincipal AccountPrincipal principal) {
		return new QuestListResponse(
				questApplicationService.list(principal.getAccountId()).stream().map(QuestController::toResponse).toList());
	}

	@GetMapping("/{code}")
	public QuestResponse get(
			@AuthenticationPrincipal AccountPrincipal principal,
			@PathVariable("code") String code) {
		return toResponse(questApplicationService.get(principal.getAccountId(), code));
	}

	@PostMapping("/{code}/accept")
	public QuestResponse accept(
			@AuthenticationPrincipal AccountPrincipal principal,
			@PathVariable("code") String code) {
		return toResponse(questApplicationService.accept(principal.getAccountId(), code));
	}

	@PostMapping("/{code}/turn-in")
	public QuestResponse turnIn(
			@AuthenticationPrincipal AccountPrincipal principal,
			@PathVariable("code") String code) {
		return toResponse(questApplicationService.turnIn(principal.getAccountId(), code));
	}

	@PostMapping("/{code}/track")
	public QuestResponse track(
			@AuthenticationPrincipal AccountPrincipal principal,
			@PathVariable("code") String code) {
		return toResponse(questApplicationService.track(principal.getAccountId(), code));
	}

	@DeleteMapping("/{code}/track")
	public QuestResponse untrack(
			@AuthenticationPrincipal AccountPrincipal principal,
			@PathVariable("code") String code) {
		return toResponse(questApplicationService.untrack(principal.getAccountId(), code));
	}

	static QuestResponse toResponse(QuestView view) {
		return new QuestResponse(
				view.code(),
				view.name(),
				view.description(),
				view.category(),
				view.status(),
				view.recommendedLevel(),
				view.startNpcCode(),
				view.turnInNpcCode(),
				view.nextQuestCode(),
				view.tracked(),
				view.objectives().stream().map(QuestController::toObjective).toList(),
				view.rewards().stream().map(QuestController::toReward).toList(),
				view.unlocks());
	}

	private static QuestObjectiveResponse toObjective(QuestObjectiveView view) {
		return new QuestObjectiveResponse(
				view.type(),
				view.targetCode(),
				view.requiredAmount(),
				view.currentAmount(),
				view.completed(),
				view.displayText(),
				view.consumeOnTurnIn());
	}

	private static QuestRewardResponse toReward(QuestRewardView view) {
		return new QuestRewardResponse(view.kind(), view.amount(), view.itemCode(), view.unlockCode());
	}
}
