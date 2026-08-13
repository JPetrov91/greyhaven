package com.example.game.market.infrastructure;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Limit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.game.market.domain.MarketListingStatus;

import jakarta.persistence.LockModeType;

public interface MarketListingRepository extends JpaRepository<MarketListingEntity, UUID> {

	List<MarketListingEntity> findByStatusOrderByCreatedAtDesc(MarketListingStatus status, Limit limit);

	/**
	 * Item type is a property of the item definition, which belongs to another module, so the
	 * filter arrives as the set of definition ids of that type rather than as a cross-module join.
	 */
	List<MarketListingEntity> findByStatusAndItemDefinitionIdInOrderByCreatedAtDesc(
			MarketListingStatus status,
			Collection<UUID> itemDefinitionIds,
			Limit limit);

	List<MarketListingEntity> findBySellerCharacterIdAndStatusOrderByCreatedAtDesc(
			UUID sellerCharacterId,
			MarketListingStatus status,
			Limit limit);

	List<MarketListingEntity> findBySellerCharacterIdAndStatusAndItemDefinitionIdInOrderByCreatedAtDesc(
			UUID sellerCharacterId,
			MarketListingStatus status,
			Collection<UUID> itemDefinitionIds,
			Limit limit);

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

	/**
	 * Reserved quantities for a whole inventory in one query. Instances without a listing are
	 * absent from the result.
	 */
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
