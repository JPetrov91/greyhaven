package com.example.game.expedition.infrastructure;

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
@Table(name = "expedition_reward_items")
public class ExpeditionRewardItemEntity implements Persistable<UUID> {

	@Id
	private UUID id;

	@Column(name = "expedition_id", nullable = false)
	private UUID expeditionId;

	@Column(name = "item_definition_id", nullable = false)
	private UUID itemDefinitionId;

	@Column(nullable = false)
	private int quantity;

	@Transient
	private boolean unsaved;

	protected ExpeditionRewardItemEntity() {
	}

	public ExpeditionRewardItemEntity(UUID id, UUID expeditionId, UUID itemDefinitionId, int quantity) {
		this.id = id;
		this.expeditionId = expeditionId;
		this.itemDefinitionId = itemDefinitionId;
		this.quantity = quantity;
		this.unsaved = true;
	}

	@PostPersist
	@PostLoad
	void markStored() {
		this.unsaved = false;
	}

	@Override
	public UUID getId() {
		return id;
	}

	@Override
	public boolean isNew() {
		return unsaved;
	}

	public UUID getExpeditionId() {
		return expeditionId;
	}

	public UUID getItemDefinitionId() {
		return itemDefinitionId;
	}

	public int getQuantity() {
		return quantity;
	}
}
