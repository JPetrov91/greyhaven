package com.example.game.item.infrastructure;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ItemInstanceAffixRepository extends JpaRepository<ItemInstanceAffixEntity, UUID> {

	List<ItemInstanceAffixEntity> findByItemInstanceIdIn(Collection<UUID> itemInstanceIds);
}
