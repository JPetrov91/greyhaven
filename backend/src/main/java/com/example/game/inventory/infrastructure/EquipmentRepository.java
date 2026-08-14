package com.example.game.inventory.infrastructure;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.game.inventory.domain.EquipmentSlot;

import jakarta.persistence.LockModeType;

public interface EquipmentRepository extends JpaRepository<EquipmentEntity, UUID> {

	List<EquipmentEntity> findByCharacterId(UUID characterId);

	Optional<EquipmentEntity> findByCharacterIdAndSlot(UUID characterId, EquipmentSlot slot);

	Optional<EquipmentEntity> findByItemInstanceId(UUID itemInstanceId);

	boolean existsByItemInstanceId(UUID itemInstanceId);

	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("select e from EquipmentEntity e where e.characterId = :characterId")
	List<EquipmentEntity> findWithLockByCharacterId(@Param("characterId") UUID characterId);

	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("select e from EquipmentEntity e where e.characterId = :characterId and e.slot = :slot")
	Optional<EquipmentEntity> findWithLockByCharacterIdAndSlot(
			@Param("characterId") UUID characterId,
			@Param("slot") EquipmentSlot slot);

	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("select e from EquipmentEntity e where e.itemInstanceId = :itemInstanceId")
	Optional<EquipmentEntity> findWithLockByItemInstanceId(@Param("itemInstanceId") UUID itemInstanceId);
}
