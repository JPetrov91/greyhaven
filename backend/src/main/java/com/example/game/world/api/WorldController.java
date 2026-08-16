package com.example.game.world.api;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.game.account.infrastructure.AccountPrincipal;
import com.example.game.quest.api.QuestRewardResponse;
import com.example.game.quest.application.QuestApplicationService;
import com.example.game.quest.application.QuestBoardEntryView;
import com.example.game.quest.application.QuestRewardView;
import com.example.game.world.application.DestinationView;
import com.example.game.world.application.LocationView;
import com.example.game.world.application.NearbyCharacterView;
import com.example.game.world.application.NearbyCharactersView;
import com.example.game.world.application.WorldApplicationService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/world")
public class WorldController {

	private final WorldApplicationService worldApplicationService;
	private final QuestApplicationService questApplicationService;

	public WorldController(
			WorldApplicationService worldApplicationService,
			QuestApplicationService questApplicationService) {
		this.worldApplicationService = worldApplicationService;
		this.questApplicationService = questApplicationService;
	}

	@GetMapping("/location")
	public LocationResponse currentLocation(@AuthenticationPrincipal AccountPrincipal principal) {
		return toLocationResponse(worldApplicationService.currentLocation(principal.getAccountId()));
	}

	@GetMapping("/destinations")
	public DestinationsResponse destinations(@AuthenticationPrincipal AccountPrincipal principal) {
		return new DestinationsResponse(
				worldApplicationService.destinations(principal.getAccountId()).stream()
						.map(WorldController::toDestinationResponse)
						.toList());
	}

	@GetMapping("/locations/{locationCode}/quest-board")
	public QuestBoardResponse questBoard(
			@AuthenticationPrincipal AccountPrincipal principal,
			@PathVariable("locationCode") String locationCode) {
		return new QuestBoardResponse(
				locationCode,
				questApplicationService.board(principal.getAccountId(), locationCode).stream()
						.map(WorldController::toBoardEntry)
						.toList());
	}

	@GetMapping("/nearby")
	public NearbyCharactersResponse nearby(@AuthenticationPrincipal AccountPrincipal principal) {
		NearbyCharactersView nearby = worldApplicationService.nearbyCharacters(principal.getAccountId());
		return new NearbyCharactersResponse(
				nearby.characters().stream()
						.map(WorldController::toNearbyResponse)
						.toList(),
				nearby.truncated());
	}

	@PostMapping("/move")
	public LocationResponse move(
			@AuthenticationPrincipal AccountPrincipal principal,
			@Valid @RequestBody MoveRequest request) {
		return toLocationResponse(
				worldApplicationService.move(principal.getAccountId(), request.destinationLocationId()));
	}

	private static QuestBoardEntryResponse toBoardEntry(QuestBoardEntryView view) {
		return new QuestBoardEntryResponse(
				view.code(),
				view.name(),
				view.shortDescription(),
				view.questType(),
				view.listState(),
				view.recommendedLevel(),
				view.difficulty(),
				view.rewards().stream().map(WorldController::toReward).toList());
	}

	private static QuestRewardResponse toReward(QuestRewardView view) {
		return new QuestRewardResponse(view.kind(), view.amount(), view.itemCode(), view.itemName(), view.unlockCode());
	}

	private static LocationResponse toLocationResponse(LocationView location) {
		return new LocationResponse(
				location.id(),
				location.code(),
				location.name(),
				location.description(),
				location.safety(),
				location.region(),
				location.recommendedLevelMin(),
				location.recommendedLevelMax(),
				location.actions());
	}

	private static DestinationResponse toDestinationResponse(DestinationView destination) {
		return new DestinationResponse(
				destination.id(),
				destination.code(),
				destination.name(),
				destination.safety(),
				destination.recommendedLevelMin(),
				destination.recommendedLevelMax());
	}

	private static NearbyCharacterResponse toNearbyResponse(NearbyCharacterView character) {
		return new NearbyCharacterResponse(character.id(), character.name(), character.level());
	}
}
