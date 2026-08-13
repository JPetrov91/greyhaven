package com.example.game.combat.infrastructure;

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
@Table(name = "monster_loot_entries")
public class MonsterLootEntryEntity implements Persistable<UUID> {

	@Id
	private UUID id;

	@Column(name = "monster_definition_id", nullable = false)
	private UUID monsterDefinitionId;

	@Column(name = "item_definition_id", nullable = false)
	private UUID itemDefinitionId;

	@Column(name = "drop_chance_percent", nullable = false)
	private int dropChancePercent;

	@Column(name = "quantity_min", nullable = false)
	private int quantityMin;

	@Column(name = "quantity_max", nullable = false)
	private int quantityMax;

	@Transient
	private boolean unsaved;

	protected MonsterLootEntryEntity() {
	}

	public MonsterLootEntryEntity(
			UUID id,
			UUID monsterDefinitionId,
			UUID itemDefinitionId,
			int dropChancePercent,
			int quantityMin,
			int quantityMax) {
		this.id = id;
		this.monsterDefinitionId = monsterDefinitionId;
		this.itemDefinitionId = itemDefinitionId;
		this.dropChancePercent = dropChancePercent;
		this.quantityMin = quantityMin;
		this.quantityMax = quantityMax;
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

	public UUID getMonsterDefinitionId() {
		return monsterDefinitionId;
	}

	public UUID getItemDefinitionId() {
		return itemDefinitionId;
	}

	public int getDropChancePercent() {
		return dropChancePercent;
	}

	public int getQuantityMin() {
		return quantityMin;
	}

	public int getQuantityMax() {
		return quantityMax;
	}
}
