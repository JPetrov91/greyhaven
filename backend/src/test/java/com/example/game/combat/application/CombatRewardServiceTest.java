package com.example.game.combat.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.Arrays;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.game.activity.application.ActivityApplicationService;
import com.example.game.character.application.CharacterVitalsService;
import com.example.game.combat.infrastructure.CharacterUniqueDropRepository;
import com.example.game.combat.infrastructure.CombatRewardItemRepository;
import com.example.game.combat.infrastructure.CombatSessionEntity;
import com.example.game.combat.infrastructure.CombatSessionRepository;
import com.example.game.combat.infrastructure.MonsterDefinitionEntity;
import com.example.game.combat.infrastructure.MonsterLootEntryRepository;
import com.example.game.inventory.application.InventoryApplicationService;
import com.example.game.item.application.ItemCatalogService;
import com.example.game.mastery.application.MasteryApplicationService;
import com.example.game.shared.domain.RandomProvider;
import com.example.game.telemetry.application.GameTelemetryRecorder;

@ExtendWith(MockitoExtension.class)
class CombatRewardServiceTest {

	@Mock
	private CharacterVitalsService characterVitalsService;

	@Mock
	private InventoryApplicationService inventoryApplicationService;

	@Mock
	private ActivityApplicationService activityApplicationService;

	@Mock
	private CombatSessionRepository combatSessionRepository;

	@Mock
	private CombatRewardItemRepository combatRewardItemRepository;

	@Mock
	private MonsterLootEntryRepository monsterLootEntryRepository;

	@Mock
	private CharacterUniqueDropRepository characterUniqueDropRepository;

	@Mock
	private ItemCatalogService itemCatalogService;

	@Mock
	private MasteryApplicationService masteryApplicationService;

	@Mock
	private GameTelemetryRecorder gameTelemetryRecorder;

	@Mock
	private RandomProvider randomProvider;

	private CombatRewardService combatRewardService;

	@BeforeEach
	void setUp() {
		combatRewardService = new CombatRewardService(
				characterVitalsService,
				inventoryApplicationService,
				activityApplicationService,
				combatSessionRepository,
				combatRewardItemRepository,
				monsterLootEntryRepository,
				characterUniqueDropRepository,
				itemCatalogService,
				masteryApplicationService,
				gameTelemetryRecorder,
				randomProvider);
	}

	@Test
	void applyRewardsExactlyOnceIsNoOpWhenAlreadyApplied() {
		CombatSessionEntity session = mock(CombatSessionEntity.class);
		when(session.isRewardsApplied()).thenReturn(true);

		combatRewardService.applyRewardsExactlyOnce(
				session,
				mock(MonsterDefinitionEntity.class),
				Instant.parse("2026-08-14T08:00:00Z"));

		verify(characterVitalsService, never()).grantCombatRewards(any(), anyInt(), anyInt());
		verify(inventoryApplicationService, never()).grantItems(any(), any(), anyInt());
		verify(combatSessionRepository, never()).saveAndFlush(session);
	}

	@Test
	void createRewardPlanIsNoOpWhenPlanAlreadyExists() {
		CombatSessionEntity session = mock(CombatSessionEntity.class);
		when(session.isRewardPlanCreated()).thenReturn(true);

		combatRewardService.createRewardPlan(
				session,
				mock(MonsterDefinitionEntity.class),
				Instant.parse("2026-08-14T08:00:00Z"));

		verify(combatRewardItemRepository, never()).saveAll(any());
		verify(session, never()).markRewardPlan(anyInt(), anyInt(), any());
	}

	@Test
	void combatApplicationServiceDelegatesRewardsToCombatRewardService() {
		assertThat(constructorTakes(CombatApplicationService.class, CombatRewardService.class)).isTrue();
		assertThat(constructorTakes(CombatRewardService.class, ItemCatalogService.class)).isTrue();
	}

	private static boolean constructorTakes(Class<?> type, Class<?> dependency) {
		return Arrays.stream(type.getConstructors())
				.flatMap(constructor -> Arrays.stream(constructor.getParameterTypes()))
				.anyMatch(parameter -> parameter == dependency);
	}
}
