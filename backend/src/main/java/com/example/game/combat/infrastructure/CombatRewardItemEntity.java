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
@Table(name = "combat_reward_items")
public class CombatRewardItemEntity implements Persistable<UUID> {

	@Id
	private UUID id;

	@Column(name = "session_id", nullable = false)
	private UUID sessionId;

	@Column(name = "item_definition_id", nullable = false)
	private UUID itemDefinitionId;

	@Column(nullable = false)
	private int quantity;

	@Transient
	private boolean unsaved;

	protected CombatRewardItemEntity() {
	}

	public CombatRewardItemEntity(
			UUID id,
			UUID sessionId,
			UUID itemDefinitionId,
			int quantity) {
		this.id = id;
		this.sessionId = sessionId;
		this.itemDefinitionId = itemDefinitionId;
		this.quantity = quantity;
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

	public UUID getSessionId() {
		return sessionId;
	}

	public UUID getItemDefinitionId() {
		return itemDefinitionId;
	}

	public int getQuantity() {
		return quantity;
	}
}
