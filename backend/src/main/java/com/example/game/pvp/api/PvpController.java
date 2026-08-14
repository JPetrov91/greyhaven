package com.example.game.pvp.api;

import java.util.Objects;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.game.account.infrastructure.AccountPrincipal;
import com.example.game.combat.api.CombatActionPreviewResponse;
import com.example.game.combat.api.CombatActionRequest;
import com.example.game.combat.api.CombatEventResponse;
import com.example.game.combat.api.CombatIntentResponse;
import com.example.game.combat.api.CombatStatusResponse;
import com.example.game.combat.api.CombatTechniqueOptionResponse;
import com.example.game.pvp.application.ArenaOpponentListView;
import com.example.game.pvp.application.ArenaProfileView;
import com.example.game.pvp.application.PvpArenaApplicationService;
import com.example.game.pvp.application.PvpDuelApplicationService;
import com.example.game.pvp.application.PvpHistoryPageView;
import com.example.game.pvp.application.PvpInspectApplicationService;
import com.example.game.pvp.application.PvpMatchView;
import com.example.game.pvp.application.PvpSettlementView;
import com.example.game.pvp.application.PublicCharacterView;
import com.example.game.pvp.domain.ArenaDefenseStrategy;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1")
public class PvpController {

	private final PvpInspectApplicationService inspectApplicationService;
	private final PvpArenaApplicationService arenaApplicationService;
	private final PvpDuelApplicationService duelApplicationService;

	public PvpController(
			PvpInspectApplicationService inspectApplicationService,
			PvpArenaApplicationService arenaApplicationService,
			PvpDuelApplicationService duelApplicationService) {
		this.inspectApplicationService = inspectApplicationService;
		this.arenaApplicationService = arenaApplicationService;
		this.duelApplicationService = duelApplicationService;
	}

	@GetMapping("/characters/{id}/public")
	public PublicCharacterResponse inspect(@PathVariable("id") UUID characterId) {
		return toPublic(inspectApplicationService.inspect(characterId));
	}

	@GetMapping("/pvp/arena")
	public ArenaProfileResponse arena(@AuthenticationPrincipal AccountPrincipal principal) {
		return toProfile(arenaApplicationService.profile(principal.getAccountId()));
	}

	@PutMapping("/pvp/arena/defense")
	public ArenaProfileResponse updateDefense(
			@AuthenticationPrincipal AccountPrincipal principal,
			@Valid @RequestBody ArenaDefenseRequest request) {
		return toProfile(arenaApplicationService.updateDefense(
				principal.getAccountId(),
				new ArenaDefenseStrategy(
						request.preferredAction(),
						request.preferredTechniqueCode(),
						request.healWhenHpPercentBelow(),
						request.defendWhenStaminaPercentBelow(),
						request.finisherWhenEnemyHpPercentBelow(),
						request.finisherTechniqueCode())));
	}

	@GetMapping("/pvp/arena/opponents")
	public ArenaOpponentListResponse opponents(
			@AuthenticationPrincipal AccountPrincipal principal,
			@RequestParam(name = "page", defaultValue = "0") int page) {
		ArenaOpponentListView view = arenaApplicationService.opponents(principal.getAccountId(), page);
		return new ArenaOpponentListResponse(
				view.opponents().stream()
						.map(row -> new ArenaOpponentListResponse.ArenaOpponentResponse(
								row.id(), row.name(), row.level(), row.rating()))
						.toList(),
				view.page(),
				view.size(),
				view.hasMore());
	}

	@PostMapping("/pvp/arena/challenges")
	public PvpMatchResponse challenge(
			@AuthenticationPrincipal AccountPrincipal principal,
			@Valid @RequestBody PvpChallengeRequest request) {
		return toMatch(Objects.requireNonNull(
				arenaApplicationService.challenge(principal.getAccountId(), request.defenderId())));
	}

	@GetMapping("/pvp/arena/matches/current")
	public ResponseEntity<PvpMatchResponse> currentArena(@AuthenticationPrincipal AccountPrincipal principal) {
		PvpMatchView view = arenaApplicationService.current(principal.getAccountId());
		if (view == null) {
			return ResponseEntity.noContent().build();
		}
		return ResponseEntity.ok(toMatch(view));
	}

	@PostMapping("/pvp/arena/matches/{id}/actions")
	public PvpMatchResponse arenaAction(
			@AuthenticationPrincipal AccountPrincipal principal,
			@PathVariable("id") UUID matchId,
			@Valid @RequestBody CombatActionRequest request) {
		return toMatch(arenaApplicationService.submitAction(
				principal.getAccountId(),
				matchId,
				request.action(),
				request.techniqueCode(),
				request.expectedRoundNumber()));
	}

	@PostMapping("/pvp/arena/matches/{id}/acknowledge")
	public void acknowledge(
			@AuthenticationPrincipal AccountPrincipal principal,
			@PathVariable("id") UUID matchId) {
		arenaApplicationService.acknowledge(principal.getAccountId(), matchId);
	}

	@GetMapping("/pvp/history")
	public PvpHistoryPageResponse history(
			@AuthenticationPrincipal AccountPrincipal principal,
			@RequestParam(name = "page", defaultValue = "0") int page) {
		PvpHistoryPageView view = arenaApplicationService.history(principal.getAccountId(), page);
		return new PvpHistoryPageResponse(
				view.entries().stream()
						.map(row -> new PvpHistoryPageResponse.PvpHistoryEntryResponse(
								row.matchId(),
								row.matchKind(),
								row.opponentName(),
								row.opponentId(),
								row.result(),
								row.ratingDelta(),
								row.marksAwarded(),
								row.createdAt()))
						.toList(),
				view.page(),
				view.size(),
				view.hasMore());
	}

	@PostMapping("/pvp/duels")
	public PvpMatchResponse createDuel(
			@AuthenticationPrincipal AccountPrincipal principal,
			@Valid @RequestBody PvpChallengeRequest request) {
		return toMatch(duelApplicationService.challenge(principal.getAccountId(), request.defenderId()));
	}

	@PostMapping("/pvp/duels/{id}/accept")
	public PvpMatchResponse acceptDuel(
			@AuthenticationPrincipal AccountPrincipal principal,
			@PathVariable("id") UUID matchId) {
		return toMatch(duelApplicationService.accept(principal.getAccountId(), matchId));
	}

	@PostMapping("/pvp/duels/{id}/decline")
	public PvpMatchResponse declineDuel(
			@AuthenticationPrincipal AccountPrincipal principal,
			@PathVariable("id") UUID matchId) {
		return toMatch(duelApplicationService.decline(principal.getAccountId(), matchId));
	}

	@GetMapping("/pvp/duels/current")
	public ResponseEntity<PvpMatchResponse> currentDuel(@AuthenticationPrincipal AccountPrincipal principal) {
		PvpMatchView view = duelApplicationService.current(principal.getAccountId());
		if (view == null) {
			return ResponseEntity.noContent().build();
		}
		return ResponseEntity.ok(toMatch(view));
	}

	@PostMapping("/pvp/duels/{id}/actions")
	public PvpMatchResponse duelAction(
			@AuthenticationPrincipal AccountPrincipal principal,
			@PathVariable("id") UUID matchId,
			@Valid @RequestBody CombatActionRequest request) {
		return toMatch(duelApplicationService.submitAction(
				principal.getAccountId(),
				matchId,
				request.action(),
				request.techniqueCode(),
				request.expectedRoundNumber()));
	}

	private static PublicCharacterResponse toPublic(PublicCharacterView view) {
		return new PublicCharacterResponse(
				view.id(),
				view.name(),
				view.level(),
				view.strength(),
				view.agility(),
				view.endurance(),
				view.perception(),
				view.arenaRating(),
				view.weaponFamily(),
				view.weaponMasteryLevel(),
				view.techniqueLoadout(),
				view.equipment().stream()
						.map(item -> new PublicCharacterResponse.PublicEquippedItemResponse(
								item.slot(),
								item.code(),
								item.displayName(),
								item.rarity(),
								item.weaponDamage(),
								item.armorValue(),
								item.affixes().stream()
										.map(affix -> new PublicCharacterResponse.PublicAffixResponse(
												affix.code(),
												affix.displayName(),
												affix.stat(),
												affix.magnitude()))
										.toList()))
						.toList());
	}

	private static ArenaProfileResponse toProfile(ArenaProfileView view) {
		return new ArenaProfileResponse(
				view.characterId(),
				view.rating(),
				view.marks(),
				view.defense(),
				view.preferredActionOptions());
	}

	private static PvpMatchResponse toMatch(PvpMatchView view) {
		return new PvpMatchResponse(
				view.id(),
				view.matchKind(),
				view.status(),
				view.roundNumber(),
				view.attackerName(),
				view.defenderName(),
				view.attackerId(),
				view.defenderId(),
				view.attackerHealth(),
				view.attackerMaxHealth(),
				view.attackerStamina(),
				view.attackerMaxStamina(),
				view.defenderHealth(),
				view.defenderMaxHealth(),
				view.defenderStamina(),
				view.defenderMaxStamina(),
				view.potionAvailable(),
				view.attackerStatuses().stream()
						.map(status -> new CombatStatusResponse(status.type(), status.stacks(), status.remainingRounds()))
						.toList(),
				view.defenderStatuses().stream()
						.map(status -> new CombatStatusResponse(status.type(), status.stacks(), status.remainingRounds()))
						.toList(),
				view.techniques().stream()
						.map(technique -> new CombatTechniqueOptionResponse(
								technique.code(),
								technique.name(),
								technique.description(),
								technique.staminaCost(),
								technique.disabledReason()))
						.toList(),
				view.events().stream()
						.map(event -> new CombatEventResponse(
								event.roundNumber(),
								event.sequenceNumber(),
								event.type(),
								event.message()))
						.toList(),
				view.defenderIntent() == null
						? null
						: new CombatIntentResponse(view.defenderIntent().kind(), view.defenderIntent().label()),
				view.actionPreviews().stream()
						.map(preview -> new CombatActionPreviewResponse(
								preview.action(),
								preview.techniqueCode(),
								preview.name(),
								preview.description(),
								preview.staminaCost(),
								preview.hitChancePercent(),
								preview.disabledReason()))
						.toList(),
				toSettlement(view.settlement()),
				view.waitingForOpponent(),
				view.yourPendingAction(),
				view.outcomeAcknowledged());
	}

	private static PvpSettlementResponse toSettlement(PvpSettlementView view) {
		if (view == null) {
			return null;
		}
		return new PvpSettlementResponse(
				view.attackerRatingDelta(),
				view.defenderRatingDelta(),
				view.attackerMarks(),
				view.defenderMarks(),
				view.applied());
	}
}
