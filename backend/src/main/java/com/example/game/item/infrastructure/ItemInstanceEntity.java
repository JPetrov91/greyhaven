package com.example.game.item.infrastructure;

import java.time.Instant;
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
			Instant createdAt) {
		if (!stackable && quantity != 1) {
			throw new IllegalArgumentException("non-stackable items must have quantity 1");
		}
		this.id = id;
		this.itemDefinitionId = itemDefinitionId;
		this.ownerCharacterId = ownerCharacterId;
		this.quantity = quantity;
		this.stackable = stackable;
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
		this.quantity += amount;
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
}
