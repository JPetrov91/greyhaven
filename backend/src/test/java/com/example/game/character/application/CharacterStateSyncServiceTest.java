package com.example.game.character.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.game.activity.application.ActivityApplicationService;
import com.example.game.character.infrastructure.CharacterEntity;
import com.example.game.character.infrastructure.CharacterRepository;
import com.example.game.shared.domain.MutableClock;

@ExtendWith(MockitoExtension.class)
class CharacterStateSyncServiceTest {

	private static final Instant T0 = Instant.parse("2026-08-14T08:00:00Z");
	private static final UUID CHARACTER_ID = UUID.fromString("c0000000-0000-4000-8000-000000000001");

	@Mock
	private CharacterRepository characterRepository;

	@Mock
	private CharacterCombatGuard characterCombatGuard;

	@Mock
	private ActivityApplicationService activityApplicationService;

	@Mock
	private CharacterEntity character;

	private MutableClock clock;
	private CharacterStateSyncService syncService;

	@BeforeEach
	void setUp() {
		clock = new MutableClock(T0);
		syncService = new CharacterStateSyncService(
				characterRepository,
				characterCombatGuard,
				activityApplicationService,
				clock);
	}

	@Test
	void wouldMutateIsFalseWhenVitalsAreCurrent() {
		stubHealthyCharacter();
		when(characterCombatGuard.inActiveCombat(CHARACTER_ID)).thenReturn(false);

		assertThat(syncService.wouldMutate(character)).isFalse();
		assertThat(syncService.sync(character)).isFalse();
		verify(characterRepository, never()).saveAndFlush(character);
	}

	@Test
	void syncWritesWhenPassiveRecoveryApplies() {
		stubHealthyCharacter();
		when(characterCombatGuard.inActiveCombat(CHARACTER_ID)).thenReturn(false);
		when(character.getCurrentHealth()).thenReturn(50);
		when(character.getCurrentStamina()).thenReturn(10);
		when(character.getLastRecoveryAt()).thenReturn(T0);
		clock.advanceSeconds(60);

		assertThat(syncService.wouldMutate(character)).isTrue();
		assertThat(syncService.sync(character)).isTrue();
		verify(character).checkpointRecovery(83, 44, T0.plusSeconds(60));
		verify(characterRepository).saveAndFlush(character);
	}

	@Test
	void wouldMutateIsTrueForLegacyExperienceCatchUp() {
		when(character.getLevel()).thenReturn(5);
		when(character.getExperience()).thenReturn(1600);

		assertThat(syncService.wouldMutate(character)).isTrue();
	}

	private void stubHealthyCharacter() {
		when(character.getId()).thenReturn(CHARACTER_ID);
		when(character.getLevel()).thenReturn(1);
		when(character.getExperience()).thenReturn(0);
		when(character.getMaxHealth()).thenReturn(165);
		when(character.getMaxStamina()).thenReturn(85);
		when(character.getCurrentHealth()).thenReturn(165);
		when(character.getCurrentStamina()).thenReturn(85);
		when(character.getLastRecoveryAt()).thenReturn(T0);
	}
}
