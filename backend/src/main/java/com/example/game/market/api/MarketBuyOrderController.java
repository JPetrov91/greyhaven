package com.example.game.market.api;

import java.util.UUID;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.game.account.infrastructure.AccountPrincipal;
import com.example.game.market.application.MarketApplicationService;
import com.example.game.market.application.MarketBuyOrderPage;
import com.example.game.market.application.MarketBuyOrderView;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/market/buy-orders")
public class MarketBuyOrderController {

	private final MarketApplicationService marketApplicationService;

	public MarketBuyOrderController(MarketApplicationService marketApplicationService) {
		this.marketApplicationService = marketApplicationService;
	}

	@GetMapping
	public MarketBuyOrdersResponse list(
			@AuthenticationPrincipal AccountPrincipal principal,
			@RequestParam(name = "mine", defaultValue = "false") boolean mine,
			@RequestParam(name = "itemDefinitionId", required = false) UUID itemDefinitionId,
			@RequestParam(name = "page", defaultValue = "0") int page,
			@RequestParam(name = "size", defaultValue = "20") int size) {
		MarketBuyOrderPage orderPage = marketApplicationService.listBuyOrders(
				principal.getAccountId(),
				mine,
				itemDefinitionId,
				page,
				size);
		return new MarketBuyOrdersResponse(
				orderPage.orders().stream().map(MarketBuyOrderController::toResponse).toList(),
				orderPage.truncated(),
				orderPage.page(),
				orderPage.size(),
				orderPage.total());
	}

	@PostMapping
	public MarketBuyOrderResponse create(
			@AuthenticationPrincipal AccountPrincipal principal,
			@Valid @RequestBody CreateBuyOrderRequest request) {
		return toResponse(marketApplicationService.createBuyOrder(
				principal.getAccountId(),
				request.itemDefinitionId(),
				request.quantity(),
				request.maxUnitPrice()));
	}

	@PostMapping("/{id}/fulfill")
	public MarketBuyOrderResponse fulfill(
			@AuthenticationPrincipal AccountPrincipal principal,
			@PathVariable("id") UUID orderId,
			@Valid @RequestBody FulfillBuyOrderRequest request) {
		return toResponse(marketApplicationService.fulfillBuyOrder(
				principal.getAccountId(),
				orderId,
				request.itemInstanceId(),
				request.quantity()));
	}

	@DeleteMapping("/{id}")
	public MarketBuyOrderResponse cancel(
			@AuthenticationPrincipal AccountPrincipal principal,
			@PathVariable("id") UUID orderId) {
		return toResponse(marketApplicationService.cancelBuyOrder(principal.getAccountId(), orderId));
	}

	static MarketBuyOrderResponse toResponse(MarketBuyOrderView view) {
		return new MarketBuyOrderResponse(
				view.id(),
				view.buyerCharacterId(),
				view.buyerName(),
				view.itemDefinitionId(),
				view.itemCode(),
				view.itemName(),
				view.itemType().name(),
				view.remainingQuantity(),
				view.originalQuantity(),
				view.maxUnitPrice(),
				view.reservedGold(),
				view.postingFeePaid(),
				view.status().name(),
				view.createdAt(),
				view.ownOrder());
	}
}
