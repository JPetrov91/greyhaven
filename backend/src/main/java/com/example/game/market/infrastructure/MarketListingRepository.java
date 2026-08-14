package com.example.game.market.infrastructure;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.game.item.domain.ItemRarity;
import com.example.game.item.domain.ItemType;
import com.example.game.item.domain.WeaponFamily;
import com.example.game.market.domain.MarketListingStatus;

import jakarta.persistence.LockModeType;

public interface MarketListingRepository extends JpaRepository<MarketListingEntity, UUID> {

	@Query("""
			select l from MarketListingEntity l
			where l.status = :status
			  and (:sellerId is null or l.sellerCharacterId = :sellerId)
			  and (:itemType is null or l.itemType = :itemType)
			  and (:rarity is null or l.instanceRarity = :rarity)
			  and (:weaponFamily is null or l.weaponFamily = :weaponFamily)
			  and (:minLevel is null or l.requiredLevel >= :minLevel)
			  and (:maxLevel is null or l.requiredLevel <= :maxLevel)
			  and (:minPrice is null or l.price >= :minPrice)
			  and (:maxPrice is null or l.price <= :maxPrice)
			""")
	Page<MarketListingEntity> search(
			@Param("status") MarketListingStatus status,
			@Param("sellerId") UUID sellerId,
			@Param("itemType") ItemType itemType,
			@Param("rarity") ItemRarity rarity,
			@Param("weaponFamily") WeaponFamily weaponFamily,
			@Param("minLevel") Integer minLevel,
			@Param("maxLevel") Integer maxLevel,
			@Param("minPrice") Integer minPrice,
			@Param("maxPrice") Integer maxPrice,
			Pageable pageable);

	@Query("""
			select l from MarketListingEntity l
			where l.sellerCharacterId = :sellerId
			  and l.status in :statuses
			""")
	Page<MarketListingEntity> findHistory(
			@Param("sellerId") UUID sellerId,
			@Param("statuses") Collection<MarketListingStatus> statuses,
			Pageable pageable);

	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("select l from MarketListingEntity l where l.id = :id")
	Optional<MarketListingEntity> findWithLockById(@Param("id") UUID id);

	@Query("""
			select coalesce(sum(l.quantity), 0)
			from MarketListingEntity l
			where l.itemInstanceId = :itemInstanceId
			  and l.status = :status
			""")
	Long reservedQuantityByItemInstanceId(
			@Param("itemInstanceId") UUID itemInstanceId,
			@Param("status") MarketListingStatus status);

	@Query("""
			select new com.example.game.market.infrastructure.ItemInstanceReservation(
					l.itemInstanceId, sum(l.quantity))
			from MarketListingEntity l
			where l.itemInstanceId in :itemInstanceIds
			  and l.status = :status
			group by l.itemInstanceId
			""")
	List<ItemInstanceReservation> reservedQuantitiesByItemInstanceIds(
			@Param("itemInstanceIds") Collection<UUID> itemInstanceIds,
			@Param("status") MarketListingStatus status);
}
