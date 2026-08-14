package com.example.game.market.infrastructure;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface MarketBuyOrderFillRepository extends JpaRepository<MarketBuyOrderFillEntity, UUID> {
}
