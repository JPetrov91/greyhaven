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
import com.example.game.item.domain.ItemType;
import com.example.game.market.application.MarketApplicationService;
import com.example.game.market.application.MarketListingPage;
import com.example.game.market.application.MarketListingView;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/market/listings")
public class MarketController {

	private final MarketApplicationService marketApplicationService;

	public MarketController(MarketApplicationService marketApplicationService) {
		this.marketApplicationService = marketApplicationService;
	}

	@GetMapping
	public MarketListingsResponse list(
			@AuthenticationPrincipal AccountPrincipal principal,
			@RequestParam(name = "itemType", required = false) ItemType itemType,
			@RequestParam(name = "mine", defaultValue = "false") boolean mine) {
		MarketListingPage page = marketApplicationService.listActive(principal.getAccountId(), itemType, mine);
		return new MarketListingsResponse(
				page.listings().stream().map(MarketController::toResponse).toList(),
				page.truncated());
	}

	@PostMapping
	public MarketListingResponse create(
			@AuthenticationPrincipal AccountPrincipal principal,
			@Valid @RequestBody CreateMarketListingRequest request) {
		return toResponse(marketApplicationService.create(
				principal.getAccountId(),
				request.itemInstanceId(),
				request.quantity(),
				request.price()));
	}

	@PostMapping("/{id}/buy")
	public MarketListingResponse buy(
			@AuthenticationPrincipal AccountPrincipal principal,
			@PathVariable("id") UUID listingId) {
		return toResponse(marketApplicationService.buy(principal.getAccountId(), listingId));
	}

	@DeleteMapping("/{id}")
	public MarketListingResponse cancel(
			@AuthenticationPrincipal AccountPrincipal principal,
			@PathVariable("id") UUID listingId) {
		return toResponse(marketApplicationService.cancel(principal.getAccountId(), listingId));
	}

	static MarketListingResponse toResponse(MarketListingView view) {
		return new MarketListingResponse(
				view.id(),
				view.sellerCharacterId(),
				view.sellerName(),
				view.itemInstanceId(),
				view.itemCode(),
				view.itemName(),
				view.itemType().name(),
				view.rarity().name(),
				view.quantity(),
				view.price(),
				view.status().name(),
				view.createdAt(),
				view.soldAt(),
				view.ownListing());
	}
}
