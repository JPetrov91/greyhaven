package com.example.game.crafting.infrastructure;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.game.crafting.domain.Profession;

import jakarta.persistence.LockModeType;

public interface CharacterProfessionRepository extends JpaRepository<CharacterProfessionEntity, UUID> {

	List<CharacterProfessionEntity> findByCharacterIdOrderByProfessionAsc(UUID characterId);

	Optional<CharacterProfessionEntity> findByCharacterIdAndProfession(UUID characterId, Profession profession);

	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("""
			select p from CharacterProfessionEntity p
			where p.characterId = :characterId and p.profession = :profession
			""")
	Optional<CharacterProfessionEntity> findWithLockByCharacterIdAndProfession(
			@Param("characterId") UUID characterId,
			@Param("profession") Profession profession);
}
