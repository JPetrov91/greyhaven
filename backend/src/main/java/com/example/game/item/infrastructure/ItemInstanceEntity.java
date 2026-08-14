package com.example.game.item.infrastructure;

import java.time.Instant;
import java.util.UUID;

import org.springframework.data.domain.Persistable;

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
@Table(name = "item_instances")
public class ItemInstanceEntity implements Persistable<UUID> {

	@Id
	private UUID id;

	@Column(name = "item_definition_id", nullable = false)
	private UUID itemDefinitionId;

	@Column(name = "owner_character_id", nullable = false)
	private UUID ownerCharacterId;

	@Column(nullable = false)
	private int quantity;

	@Column(nullable = false)
	private boolean stackable;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 32)
	private ItemRarity rarity;

	@Column(name = "rolled_weapon_damage")
	private Integer rolledWeaponDamage;

	@Column(name = "rolled_armor_value")
	private Integer rolledArmorValue;

	@Column(nullable = false)
	private boolean legacy;

	@Column(name = "created_at", nullable = false)
	private Instant createdAt;

	@Transient
	private boolean unsaved;

	protected ItemInstanceEntity() {
	}

	public ItemInstanceEntity(
			UUID id,
			UUID itemDefinitionId,
			UUID ownerCharacterId,
			int quantity,
			boolean stackable,
			ItemRarity rarity,
			Integer rolledWeaponDamage,
			Integer rolledArmorValue,
			boolean legacy,
			Instant createdAt) {
		if (!stackable && quantity != 1) {
			throw new IllegalArgumentException("non-stackable items must have quantity 1");
		}
		this.id = id;
		this.itemDefinitionId = itemDefinitionId;
		this.ownerCharacterId = ownerCharacterId;
		this.quantity = quantity;
		this.stackable = stackable;
		this.rarity = rarity;
		this.rolledWeaponDamage = rolledWeaponDamage;
		this.rolledArmorValue = rolledArmorValue;
		this.legacy = legacy;
		this.createdAt = createdAt;
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

	public UUID getItemDefinitionId() {
		return itemDefinitionId;
	}

	public UUID getOwnerCharacterId() {
		return ownerCharacterId;
	}

	public int getQuantity() {
		return quantity;
	}

	public boolean isStackable() {
		return stackable;
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

	public boolean isLegacy() {
		return legacy;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}

	public void increaseQuantity(int amount) {
		if (!stackable) {
			throw new IllegalStateException("cannot increase quantity on a non-stackable item");
		}
		if (amount < 1) {
			throw new IllegalArgumentException("amount must be positive");
		}
		this.quantity = Math.addExact(this.quantity, amount);
	}

	public void decreaseQuantity(int amount) {
		if (amount < 1) {
			throw new IllegalArgumentException("amount must be positive");
		}
		if (amount > quantity) {
			throw new IllegalArgumentException("cannot decrease below zero");
		}
		this.quantity -= amount;
	}

	public void transferTo(UUID newOwnerCharacterId) {
		if (newOwnerCharacterId == null) {
			throw new IllegalArgumentException("newOwnerCharacterId is required");
		}
		this.ownerCharacterId = newOwnerCharacterId;
	}
}
