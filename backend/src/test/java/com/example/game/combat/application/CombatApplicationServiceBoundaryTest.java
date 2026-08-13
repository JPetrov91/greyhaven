package com.example.game.combat.application;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Constructor;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.example.game.combat.domain.CombatAction;

/**
 * Guards the modular boundary: combat returns views rather than API DTOs, and reaches other
 * modules through their application layer instead of their persistence layer.
 */
class CombatApplicationServiceBoundaryTest {

	private static final String COMBAT_MODULE = "com.example.game.combat.";

	@Test
	void applicationMethodsReturnViewsNotApiDtos() throws NoSuchMethodException {
		assertThat(CombatApplicationService.class
				.getMethod("current", UUID.class)
				.getReturnType())
				.isEqualTo(CombatView.class);
		assertThat(CombatApplicationService.class
				.getMethod("submitAction", UUID.class, UUID.class, CombatAction.class)
				.getReturnType())
				.isEqualTo(CombatView.class);
		assertThat(EncounterApplicationService.class
				.getMethod("search", UUID.class)
				.getReturnType())
				.isEqualTo(EncounterSearchView.class);
	}

	@Test
	void combatReachesOtherModulesThroughTheirApplicationLayer() {
		assertOnlyOwnInfrastructure(CombatApplicationService.class);
		assertOnlyOwnInfrastructure(EncounterApplicationService.class);
	}

	private static void assertOnlyOwnInfrastructure(Class<?> service) {
		Constructor<?> constructor = service.getDeclaredConstructors()[0];
		for (Class<?> dependency : constructor.getParameterTypes()) {
			String packageName = dependency.getPackageName();
			if (!packageName.startsWith("com.example.game.") || packageName.startsWith(COMBAT_MODULE)) {
				continue;
			}
			assertThat(packageName)
					.describedAs(
							"%s must not depend on %s from another module's persistence layer",
							service.getSimpleName(),
							dependency.getSimpleName())
					.doesNotEndWith(".infrastructure");
		}
	}
}
