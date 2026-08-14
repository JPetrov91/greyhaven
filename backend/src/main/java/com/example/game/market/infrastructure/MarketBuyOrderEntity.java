package com.example.game.market.infrastructure;

import java.time.Instant;
import java.util.UUID;

import org.springframework.data.domain.Persistable;

import com.example.game.market.domain.MarketBuyOrderStatus;

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
@Table(name = "market_buy_orders")
public class MarketBuyOrderEntity implements Persistable<UUID> {

	@Id
	private UUID id;

	@Column(name = "buyer_character_id", nullable = false)
	private UUID buyerCharacterId;

	@Column(name = "item_definition_id", nullable = false)
	private UUID itemDefinitionId;

	@Column(name = "remaining_quantity", nullable = false)
	private int remainingQuantity;

	@Column(name = "original_quantity", nullable = false)
	private int originalQuantity;

	@Column(name = "max_unit_price", nullable = false)
	private int maxUnitPrice;

	@Column(name = "reserved_gold", nullable = false)
	private int reservedGold;

	@Column(name = "posting_fee_paid", nullable = false)
	private int postingFeePaid;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 32)
	private MarketBuyOrderStatus status;

	@Version
	@Column(nullable = false)
	private long version;

	@Column(name = "created_at", nullable = false)
	private Instant createdAt;

	@Column(name = "updated_at", nullable = false)
	private Instant updatedAt;

	@Column(name = "filled_at")
	private Instant filledAt;

	@Column(name = "cancelled_at")
	private Instant cancelledAt;

	@Transient
	private boolean unsaved;

	protected MarketBuyOrderEntity() {
	}

	public MarketBuyOrderEntity(
			UUID id,
			UUID buyerCharacterId,
			UUID itemDefinitionId,
			int quantity,
			int maxUnitPrice,
			int reservedGold,
			int postingFeePaid,
			Instant createdAt) {
		this.id = id;
		this.buyerCharacterId = buyerCharacterId;
		this.itemDefinitionId = itemDefinitionId;
		this.remainingQuantity = quantity;
		this.originalQuantity = quantity;
		this.maxUnitPrice = maxUnitPrice;
		this.reservedGold = reservedGold;
		this.postingFeePaid = postingFeePaid;
		this.status = MarketBuyOrderStatus.ACTIVE;
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

	public UUID getBuyerCharacterId() {
		return buyerCharacterId;
	}

	public UUID getItemDefinitionId() {
		return itemDefinitionId;
	}

	public int getRemainingQuantity() {
		return remainingQuantity;
	}

	public int getOriginalQuantity() {
		return originalQuantity;
	}

	public int getMaxUnitPrice() {
		return maxUnitPrice;
	}

	public int getReservedGold() {
		return reservedGold;
	}

	public int getPostingFeePaid() {
		return postingFeePaid;
	}

	public MarketBuyOrderStatus getStatus() {
		return status;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}

	public Instant getUpdatedAt() {
		return updatedAt;
	}

	public Instant getFilledAt() {
		return filledAt;
	}

	public Instant getCancelledAt() {
		return cancelledAt;
	}

	public void applyFill(int remainingQuantity, int reservedGold, boolean completed, Instant now) {
		if (status != MarketBuyOrderStatus.ACTIVE) {
			throw new IllegalStateException("only ACTIVE buy orders can be filled");
		}
		this.remainingQuantity = remainingQuantity;
		this.reservedGold = reservedGold;
		this.updatedAt = now;
		if (completed) {
			this.status = MarketBuyOrderStatus.FILLED;
			this.filledAt = now;
		}
	}

	public int cancel(Instant now) {
		if (status != MarketBuyOrderStatus.ACTIVE) {
			throw new IllegalStateException("only ACTIVE buy orders can be cancelled");
		}
		int refund = reservedGold;
		this.status = MarketBuyOrderStatus.CANCELLED;
		this.cancelledAt = now;
		this.updatedAt = now;
		this.reservedGold = 0;
		return refund;
	}
}
