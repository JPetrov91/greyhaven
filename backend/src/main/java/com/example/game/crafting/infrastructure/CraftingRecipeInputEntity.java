package com.example.game.crafting.infrastructure;

import java.util.UUID;

import org.springframework.data.domain.Persistable;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PostLoad;
import jakarta.persistence.PostPersist;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

@Entity
@Table(name = "crafting_recipe_inputs")
public class CraftingRecipeInputEntity implements Persistable<UUID> {

	@Id
	private UUID id;

	@Column(name = "recipe_id", nullable = false)
	private UUID recipeId;

	@Column(name = "item_definition_id", nullable = false)
	private UUID itemDefinitionId;

	@Column(nullable = false)
	private int quantity;

	@Transient
	private boolean unsaved;

	protected CraftingRecipeInputEntity() {
	}

	@Override
	public UUID getId() {
		return id;
	}

	@Override
	public boolean isNew() {
		return unsaved;
	}

	@PostPersist
	@PostLoad
	void markStored() {
		this.unsaved = false;
	}

	public UUID getRecipeId() {
		return recipeId;
	}

	public UUID getItemDefinitionId() {
		return itemDefinitionId;
	}

	public int getQuantity() {
		return quantity;
	}
}
