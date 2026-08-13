package com.example.game.chat.application;

import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.data.domain.Limit;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import com.example.game.character.application.CharacterIdentityService;
import com.example.game.character.application.CharacterVitalsService;
import com.example.game.character.application.CharacterVitalsView;
import com.example.game.chat.domain.ChatRules;
import com.example.game.chat.infrastructure.ChatMessageEntity;
import com.example.game.chat.infrastructure.ChatMessageRepository;

@Service
public class ChatApplicationService {

	private final ChatMessageRepository chatMessageRepository;
	private final CharacterVitalsService characterVitalsService;
	private final CharacterIdentityService characterIdentityService;
	private final ChatMessagePublisher chatMessagePublisher;
	private final Clock clock;

	public ChatApplicationService(
			ChatMessageRepository chatMessageRepository,
			CharacterVitalsService characterVitalsService,
			CharacterIdentityService characterIdentityService,
			ChatMessagePublisher chatMessagePublisher,
			Clock clock) {
		this.chatMessageRepository = chatMessageRepository;
		this.characterVitalsService = characterVitalsService;
		this.characterIdentityService = characterIdentityService;
		this.chatMessagePublisher = chatMessagePublisher;
		this.clock = clock;
	}

	@Transactional(readOnly = true)
	public List<ChatMessageView> listRecent(UUID accountId) {
		requireCharacter(accountId);
		return loadRecentViews();
	}

	@Transactional(readOnly = true)
	public List<ChatMessageView> listAfter(UUID accountId, UUID afterMessageId) {
		requireCharacter(accountId);
		if (afterMessageId == null) {
			return List.of();
		}
		List<ChatMessageView> recent = loadRecentViews();
		int cursor = -1;
		for (int index = 0; index < recent.size(); index++) {
			if (recent.get(index).id().equals(afterMessageId)) {
				cursor = index;
				break;
			}
		}
		if (cursor >= 0) {
			return List.copyOf(recent.subList(cursor + 1, recent.size()));
		}
		return recent;
	}

	@Transactional(readOnly = true)
	public void assertCanListen(UUID accountId) {
		requireCharacter(accountId);
	}

	@Transactional
	public ChatMessageView post(UUID accountId, String rawBody) {
		CharacterVitalsView vitals = characterVitalsService.lockVitalsOf(accountId);
		String body = ChatRules.normalizeBody(rawBody);
		if (!ChatRules.isAcceptableBody(body)) {
			throw ChatErrors.invalidMessage();
		}
		Instant now = Instant.now(clock);
		chatMessageRepository.findTopByCharacterIdOrderByCreatedAtDesc(vitals.characterId())
				.filter(previous -> ChatRules.isRateLimited(previous.getCreatedAt(), now))
				.ifPresent(previous -> {
					throw ChatErrors.rateLimited();
				});

		String characterName = characterIdentityService.requireName(vitals.characterId());
		ChatMessageEntity saved = chatMessageRepository.saveAndFlush(new ChatMessageEntity(
				UUID.randomUUID(),
				vitals.characterId(),
				body,
				now));
		ChatMessageView view = new ChatMessageView(
				saved.getId(),
				saved.getCharacterId(),
				characterName,
				saved.getBody(),
				saved.getCreatedAt());
		publishAfterCommit(view);
		return view;
	}

	private CharacterVitalsView requireCharacter(UUID accountId) {
		return characterVitalsService.vitalsOf(accountId);
	}

	private List<ChatMessageView> loadRecentViews() {
		List<ChatMessageEntity> newestFirst = chatMessageRepository
				.findAllByOrderByCreatedAtDescIdDesc(Limit.of(ChatRules.HISTORY_LIMIT));
		List<ChatMessageEntity> chronological = new ArrayList<>(newestFirst);
		Collections.reverse(chronological);
		return toViews(chronological);
	}

	private List<ChatMessageView> toViews(List<ChatMessageEntity> entities) {
		if (entities.isEmpty()) {
			return List.of();
		}
		Map<UUID, String> names = characterIdentityService.namesOf(
				entities.stream().map(ChatMessageEntity::getCharacterId).distinct().toList());
		return entities.stream()
				.map(entity -> new ChatMessageView(
						entity.getId(),
						entity.getCharacterId(),
						names.getOrDefault(entity.getCharacterId(), "Unknown"),
						entity.getBody(),
						entity.getCreatedAt()))
				.toList();
	}

	private void publishAfterCommit(ChatMessageView view) {
		if (!TransactionSynchronizationManager.isSynchronizationActive()) {
			chatMessagePublisher.publish(view);
			return;
		}
		TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
			@Override
			public void afterCommit() {
				chatMessagePublisher.publish(view);
			}
		});
	}
}
