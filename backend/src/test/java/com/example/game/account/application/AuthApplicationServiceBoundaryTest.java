package com.example.game.account.application;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Constructor;
import java.lang.reflect.Parameter;
import java.util.Arrays;

import org.junit.jupiter.api.Test;

/**
 * Guards the modular boundary fixed in the Task 2 review: account application must not
 * depend on the character application service.
 */
class AuthApplicationServiceBoundaryTest {

	@Test
	void constructorDoesNotDependOnCharacterApplicationTypes() {
		Constructor<?>[] constructors = AuthApplicationService.class.getDeclaredConstructors();
		assertThat(constructors).hasSize(1);

		Parameter[] parameters = constructors[0].getParameters();
		assertThat(Arrays.stream(parameters).map(Parameter::getType).map(Class::getName))
				.noneMatch(typeName -> typeName.startsWith("com.example.game.character."));
	}

	@Test
	void accountViewIsTheApplicationReturnType() throws NoSuchMethodException {
		assertThat(AuthApplicationService.class
				.getMethod("register", String.class, String.class)
				.getReturnType())
				.isEqualTo(AccountView.class);
		assertThat(AuthApplicationService.class
				.getMethod("currentUser", java.util.UUID.class)
				.getReturnType())
				.isEqualTo(AccountView.class);
	}
}
