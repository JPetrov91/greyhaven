package com.example.game.market.api;

import java.util.UUID;

import org.springframework.data.domain.Sort;
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
import com.example.game.inventory.application.ItemAffixView;
import com.example.game.item.domain.ItemRarity;
import com.example.game.item.domain.ItemType;
import com.example.game.item.domain.WeaponFamily;
import com.example.game.market.application.MarketApplicationService;
import com.example.game.market.application.MarketFeesView;
import com.example.game.market.application.MarketListingPage;
import com.example.game.market.application.MarketListingView;
import com.example.game.market.domain.MarketListingSort;

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
			@RequestParam(name = "rarity", required = false) ItemRarity rarity,
			@RequestParam(name = "weaponFamily", required = false) WeaponFamily weaponFamily,
			@RequestParam(name = "minLevel", required = false) Integer minLevel,
			@RequestParam(name = "maxLevel", required = false) Integer maxLevel,
			@RequestParam(name = "minPrice", required = false) Integer minPrice,
			@RequestParam(name = "maxPrice", required = false) Integer maxPrice,
			@RequestParam(name = "sort", required = false) MarketListingSort sort,
			@RequestParam(name = "direction", required = false) Sort.Direction direction,
			@RequestParam(name = "page", defaultValue = "0") int page,
			@RequestParam(name = "size", defaultValue = "20") int size,
			@RequestParam(name = "mine", defaultValue = "false") boolean mine) {
		MarketListingPage listingPage = marketApplicationService.listActive(
				principal.getAccountId(),
				mine,
				itemType,
				rarity,
				weaponFamily,
				minLevel,
				maxLevel,
				minPrice,
				maxPrice,
				sort == null ? MarketListingSort.CREATED_AT : sort,
				direction,
				page,
				size);
		return toPage(listingPage);
	}

	@GetMapping("/history")
	public MarketListingsResponse history(
			@AuthenticationPrincipal AccountPrincipal principal,
			@RequestParam(name = "page", defaultValue = "0") int page,
			@RequestParam(name = "size", defaultValue = "20") int size) {
		return toPage(marketApplicationService.history(principal.getAccountId(), page, size));
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

	private MarketListingsResponse toPage(MarketListingPage listingPage) {
		MarketFeesView fees = marketApplicationService.fees();
		return new MarketListingsResponse(
				listingPage.listings().stream().map(MarketController::toResponse).toList(),
				listingPage.truncated(),
				listingPage.page(),
				listingPage.size(),
				listingPage.total(),
				fees.listingFeePercent(),
				fees.saleFeePercent());
	}

	static MarketListingResponse toResponse(MarketListingView view) {
		return new MarketListingResponse(
				view.id(),
				view.sellerCharacterId(),
				view.sellerName(),
				view.itemInstanceId(),
				view.itemDefinitionId(),
				view.itemCode(),
				view.itemName(),
				view.displayName(),
				view.itemType().name(),
				view.rarity().name(),
				view.weaponFamily() == null ? null : view.weaponFamily().name(),
				view.requiredLevel(),
				view.quantity(),
				view.price(),
				view.listingFeePaid(),
				view.saleFeePaid(),
				view.status().name(),
				view.createdAt(),
				view.soldAt(),
				view.ownListing(),
				view.affixes().stream().map(MarketController::toAffix).toList());
	}

	private static ItemAffixApiResponse toAffix(ItemAffixView affix) {
		return new ItemAffixApiResponse(
				affix.code(),
				affix.kind().name(),
				affix.displayName(),
				affix.stat().name(),
				affix.magnitude());
	}
}
