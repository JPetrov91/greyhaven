package com.example.game.chat.infrastructure;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Limit;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatMessageRepository extends JpaRepository<ChatMessageEntity, UUID> {

	List<ChatMessageEntity> findAllByOrderByCreatedAtDescIdDesc(Limit limit);

	Optional<ChatMessageEntity> findTopByCharacterIdOrderByCreatedAtDesc(UUID characterId);
}
