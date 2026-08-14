package com.example.game.market.infrastructure;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface MerchantStockRepository extends JpaRepository<MerchantStockEntity, UUID> {

	List<MerchantStockEntity> findByMerchantIdOrderBySortOrderAsc(UUID merchantId);

	List<MerchantStockEntity> findByMerchantIdInOrderByMerchantIdAscSortOrderAsc(Collection<UUID> merchantIds);

	Optional<MerchantStockEntity> findByMerchantIdAndItemDefinitionId(UUID merchantId, UUID itemDefinitionId);
}
