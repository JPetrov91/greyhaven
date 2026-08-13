package com.example.game.world.application;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.example.game.character.application.StartingLocationProvider;
import com.example.game.shared.api.ApiException;
import com.example.game.world.domain.LocationCodes;
import com.example.game.world.infrastructure.LocationRepository;

/**
 * Places new characters in City Square, the safe hub every road in Greyhaven meets at.
 */
@Service
public class GreyhavenStartingLocationProvider implements StartingLocationProvider {

	private final LocationRepository locationRepository;

	public GreyhavenStartingLocationProvider(LocationRepository locationRepository) {
		this.locationRepository = locationRepository;
	}

	@Override
	public UUID startingLocationId() {
		return locationRepository.findByCode(LocationCodes.CITY_SQUARE)
				.orElseThrow(() -> new ApiException(
						"STARTING_LOCATION_MISSING",
						"Greyhaven starting location is not seeded.",
						HttpStatus.INTERNAL_SERVER_ERROR))
				.getId();
	}
}
