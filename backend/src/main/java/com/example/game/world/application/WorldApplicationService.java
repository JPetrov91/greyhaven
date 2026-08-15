package com.example.game.world.application;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.game.character.application.CharacterAtLocationView;
import com.example.game.character.application.CharacterLocationService;
import com.example.game.character.application.CharacterLocationView;
import com.example.game.quest.application.QuestProgressSink;
import com.example.game.shared.api.ApiException;
import com.example.game.world.domain.LocationActions;
import com.example.game.world.domain.LocationConnectivity;
import com.example.game.world.infrastructure.LocationConnectionRepository;
import com.example.game.world.infrastructure.LocationEntity;
import com.example.game.world.infrastructure.LocationRepository;

@Service
public class WorldApplicationService {

	/**
	 * Every account starts in City Square, so an uncapped nearby list would grow with total
	 * registrations. Clients are told when the list was cut short.
	 */
	static final int NEARBY_CHARACTER_LIMIT = 50;

	private final CharacterLocationService characterLocationService;
	private final LocationRepository locationRepository;
	private final LocationConnectionRepository locationConnectionRepository;
	private final List<CharacterTravelGuard> characterTravelGuards;
	private final QuestProgressSink questProgressSink;

	public WorldApplicationService(
			CharacterLocationService characterLocationService,
			LocationRepository locationRepository,
			LocationConnectionRepository locationConnectionRepository,
			List<CharacterTravelGuard> characterTravelGuards,
			QuestProgressSink questProgressSink) {
		this.characterLocationService = characterLocationService;
		this.locationRepository = locationRepository;
		this.locationConnectionRepository = locationConnectionRepository;
		this.characterTravelGuards = List.copyOf(characterTravelGuards);
		this.questProgressSink = questProgressSink;
	}

	@Transactional(readOnly = true)
	public LocationView currentLocation(UUID accountId) {
		CharacterLocationView character = characterLocationService.locationOf(accountId);
		return toLocationView(requireLocation(character.currentLocationId()));
	}

	@Transactional(readOnly = true)
	public List<DestinationView> destinations(UUID accountId) {
		CharacterLocationView character = characterLocationService.locationOf(accountId);
		return locationConnectionRepository.findDestinationsFrom(character.currentLocationId()).stream()
				.map(WorldApplicationService::toDestinationView)
				.toList();
	}

	@Transactional(readOnly = true)
	public NearbyCharactersView nearbyCharacters(UUID accountId) {
		CharacterLocationView character = characterLocationService.locationOf(accountId);

		// One extra row distinguishes "exactly at the cap" from "more remain".
		List<CharacterAtLocationView> found = characterLocationService.othersAt(
				character.currentLocationId(),
				character.characterId(),
				NEARBY_CHARACTER_LIMIT + 1);

		List<NearbyCharacterView> characters = found.stream()
				.limit(NEARBY_CHARACTER_LIMIT)
				.map(other -> new NearbyCharacterView(other.characterId(), other.name(), other.level()))
				.toList();
		return new NearbyCharactersView(characters, found.size() > NEARBY_CHARACTER_LIMIT);
	}

	@Transactional
	public LocationView move(UUID accountId, UUID destinationLocationId) {
		CharacterLocationView character = characterLocationService.lockLocationOf(accountId);
		for (CharacterTravelGuard guard : characterTravelGuards) {
			guard.assertCanTravel(character.characterId());
		}
		LocationEntity destination = requireLocation(destinationLocationId);

		UUID fromLocationId = character.currentLocationId();
		boolean connected = locationConnectionRepository.existsByFromLocationIdAndToLocationId(
				fromLocationId,
				destination.getId());

		if (!LocationConnectivity.canMove(fromLocationId, destination.getId(), connected)) {
			throw new ApiException(
					"INVALID_MOVEMENT",
					"You cannot travel directly to that location from here.",
					HttpStatus.BAD_REQUEST);
		}

		characterLocationService.relocate(accountId, destination.getId());
		questProgressSink.onLocationVisited(character.characterId(), destination.getCode());
		return toLocationView(destination);
	}

	private LocationEntity requireLocation(UUID locationId) {
		return locationRepository.findById(locationId)
				.orElseThrow(() -> new ApiException(
						"LOCATION_NOT_FOUND",
						"That location does not exist.",
						HttpStatus.NOT_FOUND));
	}

	private static LocationView toLocationView(LocationEntity location) {
		return new LocationView(
				location.getId(),
				location.getCode(),
				location.getName(),
				location.getDescription(),
				location.getSafety(),
				location.getRegion(),
				location.getRecommendedLevelMin(),
				location.getRecommendedLevelMax(),
				LocationActions.forCode(location.getCode()));
	}

	private static DestinationView toDestinationView(LocationEntity location) {
		return new DestinationView(
				location.getId(),
				location.getCode(),
				location.getName(),
				location.getSafety(),
				location.getRecommendedLevelMin(),
				location.getRecommendedLevelMax());
	}
}
