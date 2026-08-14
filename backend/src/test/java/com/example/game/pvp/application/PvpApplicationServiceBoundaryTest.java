package com.example.game.pvp.application;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Constructor;
import java.util.UUID;

import org.junit.jupiter.api.Test;

class PvpApplicationServiceBoundaryTest {

	@Test
	void applicationReturnsViewsNotApiDtos() throws NoSuchMethodException {
		assertThat(PvpInspectApplicationService.class.getMethod("inspect", UUID.class).getReturnType())
				.isEqualTo(PublicCharacterView.class);
		assertThat(PvpArenaApplicationService.class.getMethod("profile", UUID.class).getReturnType())
				.isEqualTo(ArenaProfileView.class);
	}

	@Test
	void pvpDoesNotDependOnOtherModulesPersistence() {
		assertOnlyOwnInfrastructure(PvpArenaApplicationService.class);
		assertOnlyOwnInfrastructure(PvpDuelApplicationService.class);
		assertOnlyOwnInfrastructure(PvpInspectApplicationService.class);
	}

	private static void assertOnlyOwnInfrastructure(Class<?> service) {
		Constructor<?> constructor = service.getDeclaredConstructors()[0];
		for (Class<?> dependency : constructor.getParameterTypes()) {
			String packageName = dependency.getPackageName();
			if (!packageName.startsWith("com.example.game.") || packageName.startsWith("com.example.game.pvp.")) {
				continue;
			}
			assertThat(packageName)
					.doesNotEndWith(".infrastructure");
		}
	}
}
