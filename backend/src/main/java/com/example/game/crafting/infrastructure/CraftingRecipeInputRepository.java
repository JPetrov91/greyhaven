package com.example.game.crafting.infrastructure;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface CraftingRecipeInputRepository extends JpaRepository<CraftingRecipeInputEntity, UUID> {

	List<CraftingRecipeInputEntity> findByRecipeId(UUID recipeId);

	List<CraftingRecipeInputEntity> findByRecipeIdIn(Collection<UUID> recipeIds);
}
