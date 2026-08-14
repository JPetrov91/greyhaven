package com.example.game.combat.infrastructure;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CharacterUniqueDropRepository extends JpaRepository<CharacterUniqueDropEntity, UUID> {

	boolean existsByCharacterIdAndItemCode(UUID characterId, String itemCode);

	@Query("select d.itemCode from CharacterUniqueDropEntity d where d.characterId = :characterId")
	List<String> findItemCodesByCharacterId(@Param("characterId") UUID characterId);
}
