package com.example.game.world.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.game.character.application.CharacterAtLocationView;
import com.example.game.character.application.CharacterLocationService;
import com.example.game.character.application.CharacterLocationView;
import com.example.game.shared.api.ApiException;
import com.example.game.world.domain.LocationCodes;
import com.example.game.world.domain.LocationSafety;
import com.example.game.world.infrastructure.LocationConnectionRepository;
import com.example.game.world.infrastructure.LocationEntity;
import com.example.game.world.infrastructure.LocationRepository;

@ExtendWith(MockitoExtension.class)
class WorldApplicationServiceTest {

	private static final UUID ACCOUNT_ID = UUID.fromString("c0000000-0000-4000-8000-000000000001");
	private static final UUID CHARACTER_ID = UUID.fromString("c0000000-0000-4000-8000-000000000002");
	private static final UUID CITY_SQUARE_ID = UUID.fromString("a0000000-0000-4000-8000-000000000001");
	private static final UUID FOREST_ID = UUID.fromString("a0000000-0000-4000-8000-000000000005");

	@Mock
	private CharacterLocationService characterLocationService;

	@Mock
	private LocationRepository locationRepository;

	@Mock
	private LocationConnectionRepository locationConnectionRepository;

	@Mock
	private CharacterTravelGuard characterTravelGuard;

	private WorldApplicationService worldApplicationService;

	@BeforeEach
	void setUp() {
		worldApplicationService = new WorldApplicationService(
				characterLocationService,
				locationRepository,
				locationConnectionRepository,
				characterTravelGuard);
	}

	@Test
	void movementReadsTheCharacterUnderLockBeforeRelocating() {
		when(characterLocationService.lockLocationOf(ACCOUNT_ID))
				.thenReturn(new CharacterLocationView(CHARACTER_ID, CITY_SQUARE_ID));
		when(locationRepository.findById(FOREST_ID)).thenReturn(Optional.of(forest()));
		when(locationConnectionRepository.existsByFromLocationIdAndToLocationId(CITY_SQUARE_ID, FOREST_ID))
				.thenReturn(true);

		LocationView destination = worldApplicationService.move(ACCOUNT_ID, FOREST_ID);

		assertThat(destination.code()).isEqualTo(LocationCodes.FOREST);
		verify(characterLocationService).lockLocationOf(ACCOUNT_ID);
		verify(characterLocationService).relocate(ACCOUNT_ID, FOREST_ID);
	}

	@Test
	void unconnectedDestinationIsRejectedWithoutWriting() {
		when(characterLocationService.lockLocationOf(ACCOUNT_ID))
				.thenReturn(new CharacterLocationView(CHARACTER_ID, CITY_SQUARE_ID));
		when(locationRepository.findById(FOREST_ID)).thenReturn(Optional.of(forest()));
		when(locationConnectionRepository.existsByFromLocationIdAndToLocationId(CITY_SQUARE_ID, FOREST_ID))
				.thenReturn(false);

		assertThatThrownBy(() -> worldApplicationService.move(ACCOUNT_ID, FOREST_ID))
				.isInstanceOf(ApiException.class)
				.hasFieldOrPropertyWithValue("code", "INVALID_MOVEMENT");

		verify(characterLocationService, never()).relocate(any(), any());
	}

	@Test
	void nearbyCharactersUnderTheCapAreNotFlaggedAsTruncated() {
		when(characterLocationService.locationOf(ACCOUNT_ID))
				.thenReturn(new CharacterLocationView(CHARACTER_ID, CITY_SQUARE_ID));
		when(characterLocationService.othersAt(eq(CITY_SQUARE_ID), eq(CHARACTER_ID), anyInt()))
				.thenReturn(companions(WorldApplicationService.NEARBY_CHARACTER_LIMIT));

		NearbyCharactersView nearby = worldApplicationService.nearbyCharacters(ACCOUNT_ID);

		assertThat(nearby.characters()).hasSize(WorldApplicationService.NEARBY_CHARACTER_LIMIT);
		assertThat(nearby.truncated()).isFalse();
	}

	@Test
	void nearbyCharactersBeyondTheCapAreTrimmedAndFlagged() {
		when(characterLocationService.locationOf(ACCOUNT_ID))
				.thenReturn(new CharacterLocationView(CHARACTER_ID, CITY_SQUARE_ID));
		when(characterLocationService.othersAt(
				CITY_SQUARE_ID,
				CHARACTER_ID,
				WorldApplicationService.NEARBY_CHARACTER_LIMIT + 1))
				.thenReturn(companions(WorldApplicationService.NEARBY_CHARACTER_LIMIT + 1));

		NearbyCharactersView nearby = worldApplicationService.nearbyCharacters(ACCOUNT_ID);

		assertThat(nearby.characters()).hasSize(WorldApplicationService.NEARBY_CHARACTER_LIMIT);
		assertThat(nearby.truncated()).isTrue();
	}

	private static List<CharacterAtLocationView> companions(int count) {
		List<CharacterAtLocationView> companions = new ArrayList<>(count);
		for (int index = 0; index < count; index++) {
			companions.add(new CharacterAtLocationView(UUID.randomUUID(), "Traveler" + index, 1));
		}
		return companions;
	}

	private static LocationEntity forest() {
		return new LocationEntity(
				FOREST_ID,
				LocationCodes.FOREST,
				"Forest",
				"Dense woods press close to the road.",
				LocationSafety.DANGEROUS,
				"Greyhaven",
				Instant.parse("2026-01-01T00:00:00Z"));
	}
}
