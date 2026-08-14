package com.example.game.crafting.infrastructure;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface CraftingRecipeRepository extends JpaRepository<CraftingRecipeEntity, UUID> {

	Optional<CraftingRecipeEntity> findByCode(String code);

	List<CraftingRecipeEntity> findAllByOrderByProfessionAscRequiredProfessionRankAscCodeAsc();
}
