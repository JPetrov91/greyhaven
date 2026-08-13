package com.example.game.world.application;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.game.character.infrastructure.CharacterEntity;
import com.example.game.character.infrastructure.CharacterRepository;
import com.example.game.shared.api.ApiException;
import com.example.game.world.domain.LocationActions;
import com.example.game.world.domain.LocationConnectivity;
import com.example.game.world.infrastructure.LocationConnectionRepository;
import com.example.game.world.infrastructure.LocationEntity;
import com.example.game.world.infrastructure.LocationRepository;

@Service
public class WorldApplicationService {

	private final CharacterRepository characterRepository;
	private final LocationRepository locationRepository;
	private final LocationConnectionRepository locationConnectionRepository;
	private final Clock clock;

	public WorldApplicationService(
			CharacterRepository characterRepository,
			LocationRepository locationRepository,
			LocationConnectionRepository locationConnectionRepository,
			Clock clock) {
		this.characterRepository = characterRepository;
		this.locationRepository = locationRepository;
		this.locationConnectionRepository = locationConnectionRepository;
		this.clock = clock;
	}

	@Transactional(readOnly = true)
	public LocationView currentLocation(UUID accountId) {
		CharacterEntity character = requireCharacter(accountId);
		LocationEntity location = requireLocation(character.getCurrentLocationId());
		return toLocationView(location);
	}

	@Transactional(readOnly = true)
	public List<DestinationView> destinations(UUID accountId) {
		CharacterEntity character = requireCharacter(accountId);
		return locationConnectionRepository.findDestinationsFrom(character.getCurrentLocationId()).stream()
				.map(this::toDestinationView)
				.toList();
	}

	@Transactional(readOnly = true)
	public List<NearbyCharacterView> nearbyCharacters(UUID accountId) {
		CharacterEntity character = requireCharacter(accountId);
		return characterRepository
				.findByCurrentLocationIdAndIdNotOrderByNameAsc(character.getCurrentLocationId(), character.getId())
				.stream()
				.map(other -> new NearbyCharacterView(other.getId(), other.getName(), other.getLevel()))
				.toList();
	}

	@Transactional
	public LocationView move(UUID accountId, UUID destinationLocationId) {
		CharacterEntity character = requireCharacter(accountId);
		LocationEntity destination = requireLocation(destinationLocationId);

		UUID fromLocationId = character.getCurrentLocationId();
		boolean connected = locationConnectionRepository.existsByFromLocationIdAndToLocationId(
				fromLocationId,
				destination.getId());

		if (!LocationConnectivity.canMove(fromLocationId, destination.getId(), connected)) {
			throw new ApiException(
					"INVALID_MOVEMENT",
					"You cannot travel directly to that location from here.",
					HttpStatus.BAD_REQUEST);
		}

		character.moveTo(destination.getId(), Instant.now(clock));
		characterRepository.saveAndFlush(character);
		return toLocationView(destination);
	}

	private CharacterEntity requireCharacter(UUID accountId) {
		return characterRepository.findByAccountId(accountId)
				.orElseThrow(() -> new ApiException(
						"CHARACTER_NOT_FOUND",
						"No character exists for this account.",
						HttpStatus.NOT_FOUND));
	}

	private LocationEntity requireLocation(UUID locationId) {
		return locationRepository.findById(locationId)
				.orElseThrow(() -> new ApiException(
						"LOCATION_NOT_FOUND",
						"That location does not exist.",
						HttpStatus.NOT_FOUND));
	}

	private LocationView toLocationView(LocationEntity location) {
		return new LocationView(
				location.getId(),
				location.getCode(),
				location.getName(),
				location.getDescription(),
				location.getSafety(),
				location.getRegion(),
				LocationActions.forCode(location.getCode()));
	}

	private DestinationView toDestinationView(LocationEntity location) {
		return new DestinationView(
				location.getId(),
				location.getCode(),
				location.getName(),
				location.getSafety());
	}
}
