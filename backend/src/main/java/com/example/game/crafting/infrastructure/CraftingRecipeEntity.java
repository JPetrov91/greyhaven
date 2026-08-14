package com.example.game.crafting.infrastructure;

import java.util.UUID;

import org.springframework.data.domain.Persistable;

import com.example.game.crafting.domain.Profession;
import com.example.game.item.domain.ItemRarity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.PostLoad;
import jakarta.persistence.PostPersist;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

@Entity
@Table(name = "crafting_recipes")
public class CraftingRecipeEntity implements Persistable<UUID> {

	@Id
	private UUID id;

	@Column(nullable = false, unique = true, length = 64)
	private String code;

	@Column(nullable = false, length = 128)
	private String name;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 32)
	private Profession profession;

	@Column(name = "required_profession_rank", nullable = false)
	private int requiredProfessionRank;

	@Column(name = "required_character_level", nullable = false)
	private int requiredCharacterLevel;

	@Column(name = "gold_cost", nullable = false)
	private int goldCost;

	@Column(name = "duration_seconds", nullable = false)
	private int durationSeconds;

	@Column(name = "output_item_definition_id", nullable = false)
	private UUID outputItemDefinitionId;

	@Column(name = "output_quantity", nullable = false)
	private int outputQuantity;

	@Enumerated(EnumType.STRING)
	@Column(name = "min_rarity", length = 32)
	private ItemRarity minRarity;

	@Enumerated(EnumType.STRING)
	@Column(name = "max_rarity", length = 32)
	private ItemRarity maxRarity;

	@Column(name = "profession_xp", nullable = false)
	private int professionXp;

	@Transient
	private boolean unsaved;

	protected CraftingRecipeEntity() {
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

	public String getCode() {
		return code;
	}

	public String getName() {
		return name;
	}

	public Profession getProfession() {
		return profession;
	}

	public int getRequiredProfessionRank() {
		return requiredProfessionRank;
	}

	public int getRequiredCharacterLevel() {
		return requiredCharacterLevel;
	}

	public int getGoldCost() {
		return goldCost;
	}

	public int getDurationSeconds() {
		return durationSeconds;
	}

	public UUID getOutputItemDefinitionId() {
		return outputItemDefinitionId;
	}

	public int getOutputQuantity() {
		return outputQuantity;
	}

	public ItemRarity getMinRarity() {
		return minRarity;
	}

	public ItemRarity getMaxRarity() {
		return maxRarity;
	}

	public int getProfessionXp() {
		return professionXp;
	}
}
