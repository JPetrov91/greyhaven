package com.example.game.activity.infrastructure;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Limit;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ActivityEntryRepository extends JpaRepository<ActivityEntryEntity, UUID> {

	List<ActivityEntryEntity> findByCharacterIdOrderByCreatedAtDesc(UUID characterId, Limit limit);
}
