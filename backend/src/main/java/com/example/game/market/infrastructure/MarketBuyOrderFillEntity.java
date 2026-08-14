package com.example.game.market.infrastructure;

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
@Table(name = "market_buy_order_fills")
public class MarketBuyOrderFillEntity implements Persistable<UUID> {

	@Id
	private UUID id;

	@Column(name = "buy_order_id", nullable = false)
	private UUID buyOrderId;

	@Column(name = "seller_character_id", nullable = false)
	private UUID sellerCharacterId;

	@Column(name = "item_instance_id", nullable = false)
	private UUID itemInstanceId;

	@Column(nullable = false)
	private int quantity;

	@Column(name = "gross_gold", nullable = false)
	private int grossGold;

	@Column(name = "sale_fee", nullable = false)
	private int saleFee;

	@Column(name = "created_at", nullable = false)
	private Instant createdAt;

	@Transient
	private boolean unsaved;

	protected MarketBuyOrderFillEntity() {
	}

	public MarketBuyOrderFillEntity(
			UUID id,
			UUID buyOrderId,
			UUID sellerCharacterId,
			UUID itemInstanceId,
			int quantity,
			int grossGold,
			int saleFee,
			Instant createdAt) {
		this.id = id;
		this.buyOrderId = buyOrderId;
		this.sellerCharacterId = sellerCharacterId;
		this.itemInstanceId = itemInstanceId;
		this.quantity = quantity;
		this.grossGold = grossGold;
		this.saleFee = saleFee;
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
}
