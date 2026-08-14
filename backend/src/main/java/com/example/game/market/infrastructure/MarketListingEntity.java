package com.example.game.market.infrastructure;

import java.time.Instant;
import java.util.UUID;

import org.springframework.data.domain.Persistable;

import com.example.game.item.domain.ItemRarity;
import com.example.game.item.domain.ItemType;
import com.example.game.item.domain.WeaponFamily;
import com.example.game.market.domain.MarketListingStatus;

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
@Table(name = "market_listings")
public class MarketListingEntity implements Persistable<UUID> {

	@Id
	private UUID id;

	@Column(name = "seller_character_id", nullable = false)
	private UUID sellerCharacterId;

	@Column(name = "buyer_character_id")
	private UUID buyerCharacterId;

	@Column(name = "item_instance_id")
	private UUID itemInstanceId;

	@Column(name = "item_definition_id", nullable = false)
	private UUID itemDefinitionId;

	@Column(nullable = false)
	private int quantity;

	@Column(nullable = false)
	private int price;

	@Column(name = "listing_fee_paid", nullable = false)
	private int listingFeePaid;

	@Column(name = "sale_fee_paid")
	private Integer saleFeePaid;

	@Enumerated(EnumType.STRING)
	@Column(name = "instance_rarity", nullable = false, length = 32)
	private ItemRarity instanceRarity;

	@Enumerated(EnumType.STRING)
	@Column(name = "item_type", nullable = false, length = 32)
	private ItemType itemType;

	@Enumerated(EnumType.STRING)
	@Column(name = "weapon_family", length = 32)
	private WeaponFamily weaponFamily;

	@Column(name = "required_level", nullable = false)
	private int requiredLevel;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 32)
	private MarketListingStatus status;

	@Column(name = "created_at", nullable = false)
	private Instant createdAt;

	@Column(name = "sold_at")
	private Instant soldAt;

	@Column(name = "cancelled_at")
	private Instant cancelledAt;

	@Version
	@Column(nullable = false)
	private long version;

	@Transient
	private boolean unsaved;

	protected MarketListingEntity() {
	}

	public MarketListingEntity(
			UUID id,
			UUID sellerCharacterId,
			UUID itemInstanceId,
			UUID itemDefinitionId,
			int quantity,
			int price,
			int listingFeePaid,
			ItemRarity instanceRarity,
			ItemType itemType,
			WeaponFamily weaponFamily,
			int requiredLevel,
			Instant createdAt) {
		this.id = id;
		this.sellerCharacterId = sellerCharacterId;
		this.buyerCharacterId = null;
		this.itemInstanceId = itemInstanceId;
		this.itemDefinitionId = itemDefinitionId;
		this.quantity = quantity;
		this.price = price;
		this.listingFeePaid = listingFeePaid;
		this.saleFeePaid = null;
		this.instanceRarity = instanceRarity;
		this.itemType = itemType;
		this.weaponFamily = weaponFamily;
		this.requiredLevel = requiredLevel;
		this.status = MarketListingStatus.ACTIVE;
		this.createdAt = createdAt;
		this.soldAt = null;
		this.cancelledAt = null;
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

	public UUID getSellerCharacterId() {
		return sellerCharacterId;
	}

	public UUID getBuyerCharacterId() {
		return buyerCharacterId;
	}

	public UUID getItemInstanceId() {
		return itemInstanceId;
	}

	public UUID getItemDefinitionId() {
		return itemDefinitionId;
	}

	public int getQuantity() {
		return quantity;
	}

	public int getPrice() {
		return price;
	}

	public int getListingFeePaid() {
		return listingFeePaid;
	}

	public Integer getSaleFeePaid() {
		return saleFeePaid;
	}

	public ItemRarity getInstanceRarity() {
		return instanceRarity;
	}

	public ItemType getItemType() {
		return itemType;
	}

	public WeaponFamily getWeaponFamily() {
		return weaponFamily;
	}

	public int getRequiredLevel() {
		return requiredLevel;
	}

	public MarketListingStatus getStatus() {
		return status;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}

	public Instant getSoldAt() {
		return soldAt;
	}

	public Instant getCancelledAt() {
		return cancelledAt;
	}

	public void markSold(UUID buyerCharacterId, Instant soldAt, int saleFeePaid) {
		if (status != MarketListingStatus.ACTIVE) {
			throw new IllegalStateException("only ACTIVE listings can be sold");
		}
		if (buyerCharacterId == null) {
			throw new IllegalArgumentException("buyerCharacterId is required");
		}
		this.status = MarketListingStatus.SOLD;
		this.buyerCharacterId = buyerCharacterId;
		this.soldAt = soldAt;
		this.saleFeePaid = saleFeePaid;
		this.cancelledAt = null;
	}

	public void markCancelled(Instant cancelledAt) {
		if (status != MarketListingStatus.ACTIVE) {
			throw new IllegalStateException("only ACTIVE listings can be cancelled");
		}
		this.status = MarketListingStatus.CANCELLED;
		this.cancelledAt = cancelledAt;
		this.buyerCharacterId = null;
		this.soldAt = null;
	}
}
