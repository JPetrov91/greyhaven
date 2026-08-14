package com.example.game.market.infrastructure;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.game.market.domain.MarketBuyOrderStatus;

import jakarta.persistence.LockModeType;

public interface MarketBuyOrderRepository extends JpaRepository<MarketBuyOrderEntity, UUID> {

	@Query("""
			select o from MarketBuyOrderEntity o
			where o.status = :status
			  and (:buyerId is null or o.buyerCharacterId = :buyerId)
			  and (:itemDefinitionId is null or o.itemDefinitionId = :itemDefinitionId)
			""")
	Page<MarketBuyOrderEntity> search(
			@Param("status") MarketBuyOrderStatus status,
			@Param("buyerId") UUID buyerId,
			@Param("itemDefinitionId") UUID itemDefinitionId,
			Pageable pageable);

	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("select o from MarketBuyOrderEntity o where o.id = :id")
	java.util.Optional<MarketBuyOrderEntity> findWithLockById(@Param("id") UUID id);
}
