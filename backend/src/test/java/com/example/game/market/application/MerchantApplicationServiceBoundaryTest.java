package com.example.game.market.application;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.example.game.market.api.MerchantListResponse;
import com.example.game.market.api.MerchantPurchaseRequest;
import com.example.game.market.api.MerchantPurchaseResponse;
import com.example.game.market.api.MerchantResponse;
import com.example.game.market.api.MerchantSaleRequest;
import com.example.game.market.api.MerchantSaleResponse;

class MerchantApplicationServiceBoundaryTest {

	@Test
	void applicationMethodsReturnViewsNotApiDtos() throws NoSuchMethodException {
		assertThat(MerchantApplicationService.class.getMethod("listMerchants").getReturnType())
				.isEqualTo(List.class);
		assertThat(MerchantApplicationService.class.getMethod("getMerchant", UUID.class).getReturnType())
				.isEqualTo(MerchantView.class);
		assertThat(MerchantApplicationService.class
				.getMethod("purchase", UUID.class, UUID.class, UUID.class, int.class)
				.getReturnType())
				.isEqualTo(MerchantPurchaseView.class);
		assertThat(MerchantApplicationService.class.getMethod("sell", UUID.class, UUID.class, int.class).getReturnType())
				.isEqualTo(MerchantSaleView.class);

		assertThat(MerchantView.class.getPackageName()).doesNotContain(".api");
		assertThat(MerchantStockItemView.class.getPackageName()).doesNotContain(".api");
		assertThat(MerchantListResponse.class.getPackageName()).contains(".api");
		assertThat(MerchantResponse.class.getPackageName()).contains(".api");
		assertThat(MerchantPurchaseRequest.class.getPackageName()).contains(".api");
		assertThat(MerchantPurchaseResponse.class.getPackageName()).contains(".api");
		assertThat(MerchantSaleRequest.class.getPackageName()).contains(".api");
		assertThat(MerchantSaleResponse.class.getPackageName()).contains(".api");
	}
}
