package com.example.game.market.application;

import java.util.List;
import java.util.UUID;

import com.example.game.market.domain.MerchantType;

public record MerchantView(
		UUID id,
		String code,
		String name,
		String title,
		String description,
		MerchantType merchantType,
		String portraitCode,
		List<MerchantStockItemView> stock
) {
}
