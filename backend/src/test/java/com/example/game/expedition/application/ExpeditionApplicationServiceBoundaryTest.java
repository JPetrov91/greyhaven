package com.example.game.expedition.application;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Constructor;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.example.game.expedition.domain.ExpeditionStrategy;

class ExpeditionApplicationServiceBoundaryTest {

	private static final String EXPEDITION_MODULE = "com.example.game.expedition.";

	@Test
	void applicationMethodsReturnViewsNotApiDtos() throws NoSuchMethodException {
		assertThat(ExpeditionApplicationService.class
				.getMethod("current", UUID.class)
				.getReturnType())
				.isEqualTo(ExpeditionView.class);
		assertThat(ExpeditionApplicationService.class
				.getMethod("start", UUID.class, ExpeditionStrategy.class)
				.getReturnType())
				.isEqualTo(ExpeditionView.class);
		assertThat(ExpeditionApplicationService.class
				.getMethod("claim", UUID.class, UUID.class)
				.getReturnType())
				.isEqualTo(ExpeditionView.class);
	}

	@Test
	void expeditionReachesOtherModulesThroughTheirApplicationLayer() {
		Constructor<?> constructor = ExpeditionApplicationService.class.getDeclaredConstructors()[0];
		for (Class<?> dependency : constructor.getParameterTypes()) {
			String packageName = dependency.getPackageName();
			if (!packageName.startsWith("com.example.game.") || packageName.startsWith(EXPEDITION_MODULE)) {
				continue;
			}
			assertThat(packageName)
					.describedAs(
							"%s must not depend on %s from another module's persistence layer",
							ExpeditionApplicationService.class.getSimpleName(),
							dependency.getSimpleName())
					.doesNotEndWith(".infrastructure");
		}
	}
}
