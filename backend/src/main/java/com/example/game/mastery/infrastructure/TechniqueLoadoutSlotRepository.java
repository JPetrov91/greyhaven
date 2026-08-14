package com.example.game.mastery.infrastructure;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;

public interface TechniqueLoadoutSlotRepository extends JpaRepository<TechniqueLoadoutSlotEntity, UUID> {

	List<TechniqueLoadoutSlotEntity> findByCharacterIdOrderBySlotIndexAsc(UUID characterId);

	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("""
			select s from TechniqueLoadoutSlotEntity s
			where s.characterId = :characterId
			order by s.slotIndex asc
			""")
	List<TechniqueLoadoutSlotEntity> findWithLockByCharacterIdOrderBySlotIndexAsc(
			@Param("characterId") UUID characterId);
}
