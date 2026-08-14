package com.example.game.market.infrastructure;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface MerchantDefinitionRepository extends JpaRepository<MerchantDefinitionEntity, UUID> {

	List<MerchantDefinitionEntity> findAllByOrderBySortOrderAsc();

	Optional<MerchantDefinitionEntity> findByCode(String code);
}
