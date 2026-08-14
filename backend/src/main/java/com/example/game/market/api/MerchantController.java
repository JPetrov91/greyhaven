package com.example.game.market.api;

import java.util.UUID;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.game.account.infrastructure.AccountPrincipal;
import com.example.game.market.application.MerchantApplicationService;
import com.example.game.market.application.MerchantPurchaseView;
import com.example.game.market.application.MerchantSaleView;
import com.example.game.market.application.MerchantStockItemView;
import com.example.game.market.application.MerchantView;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/market")
public class MerchantController {

	private final MerchantApplicationService merchantApplicationService;

	public MerchantController(MerchantApplicationService merchantApplicationService) {
		this.merchantApplicationService = merchantApplicationService;
	}

	@GetMapping("/merchants")
	public MerchantListResponse list(@AuthenticationPrincipal AccountPrincipal principal) {
		return new MerchantListResponse(
				merchantApplicationService.listMerchants().stream().map(MerchantController::toResponse).toList());
	}

	@GetMapping("/merchants/{merchantId}")
	public MerchantResponse get(
			@AuthenticationPrincipal AccountPrincipal principal,
			@PathVariable("merchantId") UUID merchantId) {
		return toResponse(merchantApplicationService.getMerchant(merchantId));
	}

	@PostMapping("/merchants/{merchantId}/purchases")
	public MerchantPurchaseResponse purchase(
			@AuthenticationPrincipal AccountPrincipal principal,
			@PathVariable("merchantId") UUID merchantId,
			@Valid @RequestBody MerchantPurchaseRequest request) {
		MerchantPurchaseView view = merchantApplicationService.purchase(
				principal.getAccountId(),
				merchantId,
				request.itemDefinitionId(),
				request.quantity());
		return new MerchantPurchaseResponse(
				view.merchantId(),
				view.itemDefinitionId(),
				view.itemCode(),
				view.itemName(),
				view.quantity(),
				view.pricePaid(),
				view.goldRemaining());
	}

	@PostMapping("/merchant-sales")
	public MerchantSaleResponse sell(
			@AuthenticationPrincipal AccountPrincipal principal,
			@Valid @RequestBody MerchantSaleRequest request) {
		MerchantSaleView view = merchantApplicationService.sell(
				principal.getAccountId(),
				request.itemInstanceId(),
				request.quantity());
		return new MerchantSaleResponse(
				view.itemInstanceId(),
				view.itemCode(),
				view.itemName(),
				view.quantity(),
				view.goldAwarded(),
				view.goldRemaining());
	}

	static MerchantResponse toResponse(MerchantView view) {
		return new MerchantResponse(
				view.id(),
				view.code(),
				view.name(),
				view.title(),
				view.description(),
				view.merchantType().name(),
				view.portraitCode(),
				view.stock().stream().map(MerchantController::toStockResponse).toList());
	}

	private static MerchantStockItemResponse toStockResponse(MerchantStockItemView item) {
		return new MerchantStockItemResponse(
				item.itemDefinitionId(),
				item.itemCode(),
				item.itemName(),
				item.description(),
				item.itemType().name(),
				item.rarity().name(),
				item.sellPrice(),
				item.availabilityType().name(),
				item.requiredLevel(),
				item.weaponDamage(),
				item.armorValue(),
				item.healAmount(),
				item.twoHanded(),
				item.equipmentSlot() == null ? null : item.equipmentSlot().name(),
				item.weaponFamily() == null ? null : item.weaponFamily().name(),
				item.armorCategory() == null ? null : item.armorCategory().name(),
				item.requiredStrength(),
				item.requiredAgility(),
				item.requiredEndurance(),
				item.requiredPerception());
	}
}
