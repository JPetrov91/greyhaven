package com.example.game.market.application;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.example.game.item.domain.ItemRarity;
import com.example.game.item.domain.ItemType;
import com.example.game.market.api.MarketListingResponse;
import com.example.game.market.api.MarketListingsResponse;
import com.example.game.market.domain.MarketListingStatus;

class MarketApplicationServiceBoundaryTest {

	@Test
	void applicationMethodsReturnViewsNotApiDtos() throws NoSuchMethodException {
		assertThat(MarketApplicationService.class
				.getMethod("listActive", UUID.class, ItemType.class, boolean.class)
				.getReturnType())
				.isEqualTo(MarketListingPage.class);
		assertThat(MarketApplicationService.class
				.getMethod("create", UUID.class, UUID.class, int.class, int.class)
				.getReturnType())
				.isEqualTo(MarketListingView.class);
		assertThat(MarketApplicationService.class
				.getMethod("buy", UUID.class, UUID.class)
				.getReturnType())
				.isEqualTo(MarketListingView.class);
		assertThat(MarketApplicationService.class
				.getMethod("cancel", UUID.class, UUID.class)
				.getReturnType())
				.isEqualTo(MarketListingView.class);

		assertThat(MarketListingView.class.getPackageName()).doesNotContain(".api");
		assertThat(MarketListingPage.class.getPackageName()).doesNotContain(".api");
		assertThat(MarketListingsResponse.class.getPackageName()).contains(".api");
		assertThat(MarketListingResponse.class.getPackageName()).contains(".api");
	}

	@Test
	void newestPageIsTrimmedAndFlaggedWhenTheFetchExceedsTheLimit() {
		List<MarketListingView> fetched = views(3);

		MarketListingPage page = MarketListingPage.ofNewest(fetched, 2);

		assertThat(page.truncated()).isTrue();
		assertThat(page.listings()).containsExactly(fetched.get(0), fetched.get(1));
	}

	@Test
	void newestPageIsNotTruncatedAtTheCap() {
		List<MarketListingView> fetched = views(2);

		MarketListingPage page = MarketListingPage.ofNewest(fetched, 2);

		assertThat(page.truncated()).isFalse();
		assertThat(page.listings()).hasSize(2);
	}

	private static List<MarketListingView> views(int count) {
		List<MarketListingView> views = new ArrayList<>(count);
		for (int index = 0; index < count; index++) {
			views.add(new MarketListingView(
					UUID.randomUUID(),
					UUID.randomUUID(),
					"Seller",
					UUID.randomUUID(),
					"WOLF_PELT",
					"Wolf Pelt",
					ItemType.MATERIAL,
					ItemRarity.COMMON,
					1,
					5,
					MarketListingStatus.ACTIVE,
					null,
					null,
					false));
		}
		return views;
	}
}
