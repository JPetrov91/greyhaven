package com.example.game.market.application;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.example.game.market.api.MarketListingResponse;
import com.example.game.market.api.MarketListingsResponse;

class MarketApplicationServiceBoundaryTest {

	@Test
	void applicationMethodsReturnViewsNotApiDtos() {
		assertThat(returnType("listActive")).isEqualTo(MarketListingPage.class);
		assertThat(returnType("create")).isEqualTo(MarketListingView.class);
		assertThat(returnType("buy")).isEqualTo(MarketListingView.class);
		assertThat(returnType("cancel")).isEqualTo(MarketListingView.class);
		assertThat(returnType("createBuyOrder")).isEqualTo(MarketBuyOrderView.class);
		assertThat(returnType("fulfillBuyOrder")).isEqualTo(MarketBuyOrderView.class);

		assertThat(MarketListingView.class.getPackageName()).doesNotContain(".api");
		assertThat(MarketListingPage.class.getPackageName()).doesNotContain(".api");
		assertThat(MarketListingsResponse.class.getPackageName()).contains(".api");
		assertThat(MarketListingResponse.class.getPackageName()).contains(".api");
	}

	@Test
	void pageFlagsOlderRowsAsTruncated() {
		MarketListingPage page = new MarketListingPage(java.util.List.of(), 0, 20, 40, true);
		assertThat(page.truncated()).isTrue();
		assertThat(page.total()).isEqualTo(40);
	}

	private static Class<?> returnType(String name) {
		return Arrays.stream(MarketApplicationService.class.getMethods())
				.filter(method -> method.getName().equals(name))
				.map(Method::getReturnType)
				.findFirst()
				.orElseThrow();
	}
}
