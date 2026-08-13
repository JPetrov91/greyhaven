package com.example.game.world.infrastructure;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface LocationConnectionRepository extends JpaRepository<LocationConnectionEntity, UUID> {

	boolean existsByFromLocationIdAndToLocationId(UUID fromLocationId, UUID toLocationId);

	@Query("""
			select l from LocationEntity l
			join LocationConnectionEntity c on c.toLocationId = l.id
			where c.fromLocationId = :fromLocationId
			order by l.name
			""")
	List<LocationEntity> findDestinationsFrom(@Param("fromLocationId") UUID fromLocationId);
}
