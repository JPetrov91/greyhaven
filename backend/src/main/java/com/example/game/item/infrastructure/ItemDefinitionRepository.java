package com.example.game.item.infrastructure;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.game.item.domain.ItemType;

public interface ItemDefinitionRepository extends JpaRepository<ItemDefinitionEntity, UUID> {

	Optional<ItemDefinitionEntity> findByCode(String code);

	List<ItemDefinitionEntity> findByCodeIn(Collection<String> codes);

	boolean existsByCode(String code);

	@Query("select d.id from ItemDefinitionEntity d where d.type = :type")
	List<UUID> findIdsByType(@Param("type") ItemType type);
}
