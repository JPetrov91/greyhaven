package com.example.game.market.application;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.example.game.inventory.application.ItemReservationQuery;
import com.example.game.market.domain.MarketListingStatus;
import com.example.game.market.infrastructure.ItemInstanceReservation;
import com.example.game.market.infrastructure.MarketListingRepository;

@Component
public class MarketItemReservationQuery implements ItemReservationQuery {

	private final MarketListingRepository marketListingRepository;

	public MarketItemReservationQuery(MarketListingRepository marketListingRepository) {
		this.marketListingRepository = marketListingRepository;
	}

	@Override
	@Transactional(readOnly = true)
	public int reservedQuantity(UUID itemInstanceId) {
		if (itemInstanceId == null) {
			return 0;
		}
		Long reserved = marketListingRepository.reservedQuantityByItemInstanceId(
				itemInstanceId,
				MarketListingStatus.ACTIVE);
		return reserved == null ? 0 : reserved.intValue();
	}

	@Override
	@Transactional(readOnly = true)
	public Map<UUID, Integer> reservedQuantities(Collection<UUID> itemInstanceIds) {
		Map<UUID, Integer> reserved = new HashMap<>();
		if (itemInstanceIds == null || itemInstanceIds.isEmpty()) {
			return reserved;
		}
		for (ItemInstanceReservation row : marketListingRepository.reservedQuantitiesByItemInstanceIds(
				itemInstanceIds,
				MarketListingStatus.ACTIVE)) {
			if (row.reservedQuantity() != null) {
				reserved.put(row.itemInstanceId(), row.reservedQuantity().intValue());
			}
		}
		return reserved;
	}
}
