package com.example.game.item.infrastructure;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;

public interface ItemInstanceRepository extends JpaRepository<ItemInstanceEntity, UUID> {

	List<ItemInstanceEntity> findByOwnerCharacterIdOrderByCreatedAtAscIdAsc(UUID ownerCharacterId);

	long countByOwnerCharacterId(UUID ownerCharacterId);

	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("select i from ItemInstanceEntity i where i.id = :id")
	Optional<ItemInstanceEntity> findWithLockById(@Param("id") UUID id);

	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("""
			select i from ItemInstanceEntity i
			where i.ownerCharacterId = :ownerCharacterId
			  and i.itemDefinitionId = :itemDefinitionId
			""")
	Optional<ItemInstanceEntity> findWithLockByOwnerCharacterIdAndItemDefinitionId(
			@Param("ownerCharacterId") UUID ownerCharacterId,
			@Param("itemDefinitionId") UUID itemDefinitionId);
}
