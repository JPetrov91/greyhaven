package com.example.game.crafting.infrastructure;

import java.time.Instant;
import java.util.UUID;

import org.springframework.data.domain.Persistable;

import com.example.game.crafting.domain.CraftingJobStatus;
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
import jakarta.persistence.Version;

@Entity
@Table(name = "crafting_jobs")
public class CraftingJobEntity implements Persistable<UUID> {

	@Id
	private UUID id;

	@Column(name = "character_id", nullable = false)
	private UUID characterId;

	@Column(name = "recipe_id", nullable = false)
	private UUID recipeId;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 32)
	private Profession profession;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 32)
	private CraftingJobStatus status;

	@Column(name = "started_at", nullable = false)
	private Instant startedAt;

	@Column(name = "completes_at", nullable = false)
	private Instant completesAt;

	@Column(name = "claimed_at")
	private Instant claimedAt;

	@Column(name = "result_generated", nullable = false)
	private boolean resultGenerated;

	@Column(name = "output_item_definition_id", nullable = false)
	private UUID outputItemDefinitionId;

	@Column(name = "output_item_code", nullable = false, length = 64)
	private String outputItemCode;

	@Column(name = "output_quantity", nullable = false)
	private int outputQuantity;

	@Enumerated(EnumType.STRING)
	@Column(length = 32)
	private ItemRarity rarity;

	@Column(name = "rolled_weapon_damage")
	private Integer rolledWeaponDamage;

	@Column(name = "rolled_armor_value")
	private Integer rolledArmorValue;

	@Column(name = "rolled_affixes", columnDefinition = "TEXT")
	private String rolledAffixes;

	@Column(name = "profession_xp_planned", nullable = false)
	private int professionXpPlanned;

	@Version
	@Column(nullable = false)
	private long version;

	@Column(name = "created_at", nullable = false)
	private Instant createdAt;

	@Column(name = "updated_at", nullable = false)
	private Instant updatedAt;

	@Transient
	private boolean unsaved;

	protected CraftingJobEntity() {
	}

	public CraftingJobEntity(
			UUID id,
			UUID characterId,
			UUID recipeId,
			Profession profession,
			Instant startedAt,
			Instant completesAt,
			UUID outputItemDefinitionId,
			String outputItemCode,
			int outputQuantity,
			ItemRarity rarity,
			Integer rolledWeaponDamage,
			Integer rolledArmorValue,
			String rolledAffixes,
			int professionXpPlanned,
			Instant createdAt) {
		this.id = id;
		this.characterId = characterId;
		this.recipeId = recipeId;
		this.profession = profession;
		this.status = CraftingJobStatus.ACTIVE;
		this.startedAt = startedAt;
		this.completesAt = completesAt;
		this.claimedAt = null;
		this.resultGenerated = true;
		this.outputItemDefinitionId = outputItemDefinitionId;
		this.outputItemCode = outputItemCode;
		this.outputQuantity = outputQuantity;
		this.rarity = rarity;
		this.rolledWeaponDamage = rolledWeaponDamage;
		this.rolledArmorValue = rolledArmorValue;
		this.rolledAffixes = rolledAffixes;
		this.professionXpPlanned = professionXpPlanned;
		this.createdAt = createdAt;
		this.updatedAt = createdAt;
		this.unsaved = true;
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

	public UUID getCharacterId() {
		return characterId;
	}

	public UUID getRecipeId() {
		return recipeId;
	}

	public Profession getProfession() {
		return profession;
	}

	public CraftingJobStatus getStatus() {
		return status;
	}

	public Instant getStartedAt() {
		return startedAt;
	}

	public Instant getCompletesAt() {
		return completesAt;
	}

	public Instant getClaimedAt() {
		return claimedAt;
	}

	public boolean isResultGenerated() {
		return resultGenerated;
	}

	public UUID getOutputItemDefinitionId() {
		return outputItemDefinitionId;
	}

	public String getOutputItemCode() {
		return outputItemCode;
	}

	public int getOutputQuantity() {
		return outputQuantity;
	}

	public ItemRarity getRarity() {
		return rarity;
	}

	public Integer getRolledWeaponDamage() {
		return rolledWeaponDamage;
	}

	public Integer getRolledArmorValue() {
		return rolledArmorValue;
	}

	public String getRolledAffixes() {
		return rolledAffixes;
	}

	public int getProfessionXpPlanned() {
		return professionXpPlanned;
	}

	public boolean isDue(Instant now) {
		return status == CraftingJobStatus.ACTIVE && !completesAt.isAfter(now);
	}

	public void markCompleted(Instant now) {
		if (status != CraftingJobStatus.ACTIVE) {
			throw new IllegalStateException("only ACTIVE jobs can complete");
		}
		this.status = CraftingJobStatus.COMPLETED;
		this.updatedAt = now;
	}

	public void markClaimed(Instant now) {
		if (status != CraftingJobStatus.COMPLETED) {
			throw new IllegalStateException("only COMPLETED jobs can be claimed");
		}
		this.status = CraftingJobStatus.CLAIMED;
		this.claimedAt = now;
		this.updatedAt = now;
	}
}
