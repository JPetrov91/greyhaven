package com.example.game.market.infrastructure;

import java.util.UUID;

import org.springframework.data.domain.Persistable;

import com.example.game.market.domain.MerchantAvailabilityType;

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
@Table(name = "merchant_stock")
public class MerchantStockEntity implements Persistable<UUID> {

	@Id
	private UUID id;

	@Column(name = "merchant_id", nullable = false)
	private UUID merchantId;

	@Column(name = "item_definition_id", nullable = false)
	private UUID itemDefinitionId;

	@Enumerated(EnumType.STRING)
	@Column(name = "availability_type", nullable = false, length = 32)
	private MerchantAvailabilityType availabilityType;

	@Column(name = "sort_order", nullable = false)
	private int sortOrder;

	@Transient
	private boolean unsaved;

	protected MerchantStockEntity() {
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

	public UUID getMerchantId() {
		return merchantId;
	}

	public UUID getItemDefinitionId() {
		return itemDefinitionId;
	}

	public MerchantAvailabilityType getAvailabilityType() {
		return availabilityType;
	}

	public int getSortOrder() {
		return sortOrder;
	}
}
