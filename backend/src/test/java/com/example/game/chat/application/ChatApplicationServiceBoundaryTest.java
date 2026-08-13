package com.example.game.chat.application;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Constructor;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.example.game.chat.api.ChatController;
import com.example.game.chat.api.ChatMessageResponse;

class ChatApplicationServiceBoundaryTest {

	private static final String CHAT_MODULE = "com.example.game.chat.";

	@Test
	void applicationMethodsReturnViewsNotApiDtos() throws NoSuchMethodException {
		assertThat(ChatApplicationService.class
				.getMethod("listRecent", UUID.class)
				.getReturnType())
				.isEqualTo(List.class);
		assertThat(ChatApplicationService.class
				.getMethod("post", UUID.class, String.class)
				.getReturnType())
				.isEqualTo(ChatMessageView.class);
		assertThat(ChatMessageView.class.getPackageName()).doesNotContain(".api");
		assertThat(ChatMessageResponse.class.getPackageName()).contains(".api");
	}

	@Test
	void chatReachesOtherModulesThroughTheirApplicationLayer() {
		Constructor<?> constructor = ChatApplicationService.class.getDeclaredConstructors()[0];
		for (Class<?> dependency : constructor.getParameterTypes()) {
			String packageName = dependency.getPackageName();
			if (!packageName.startsWith("com.example.game.") || packageName.startsWith(CHAT_MODULE)) {
				continue;
			}
			assertThat(packageName)
					.describedAs(
							"%s must not depend on %s from another module's persistence layer",
							ChatApplicationService.class.getSimpleName(),
							dependency.getSimpleName())
					.doesNotEndWith(".infrastructure");
		}
	}

	@Test
	void chatControllerDoesNotDependOnChatInfrastructure() {
		Constructor<?> constructor = ChatController.class.getDeclaredConstructors()[0];
		for (Class<?> dependency : constructor.getParameterTypes()) {
			assertThat(dependency.getPackageName())
					.describedAs("%s must not be injected into %s", dependency.getSimpleName(), "ChatController")
					.doesNotEndWith(".infrastructure");
		}
	}
}
