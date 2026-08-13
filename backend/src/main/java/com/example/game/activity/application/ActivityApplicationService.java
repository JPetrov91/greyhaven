package com.example.game.activity.application;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Limit;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.example.game.activity.domain.ActivityType;
import com.example.game.activity.infrastructure.ActivityEntryEntity;
import com.example.game.activity.infrastructure.ActivityEntryRepository;
import com.example.game.character.application.CharacterVitalsService;
import com.example.game.character.application.CharacterVitalsView;

@Service
public class ActivityApplicationService {

	static final int DEFAULT_FEED_LIMIT = 50;

	private final ActivityEntryRepository activityEntryRepository;
	private final CharacterVitalsService characterVitalsService;
	private final Clock clock;

	public ActivityApplicationService(
			ActivityEntryRepository activityEntryRepository,
			CharacterVitalsService characterVitalsService,
			Clock clock) {
		this.activityEntryRepository = activityEntryRepository;
		this.characterVitalsService = characterVitalsService;
		this.clock = clock;
	}

	@Transactional(readOnly = true)
	public List<ActivityEntryView> listRecent(UUID accountId) {
		CharacterVitalsView vitals = characterVitalsService.vitalsOf(accountId);
		return activityEntryRepository
				.findByCharacterIdOrderByCreatedAtDesc(vitals.characterId(), Limit.of(DEFAULT_FEED_LIMIT))
				.stream()
				.map(ActivityApplicationService::toView)
				.toList();
	}

	/**
	 * Records a personal activity event inside the caller's transaction.
	 */
	@Transactional(propagation = Propagation.MANDATORY)
	public void record(UUID characterId, ActivityType type, String message) {
		if (characterId == null) {
			throw new IllegalArgumentException("characterId is required");
		}
		if (type == null) {
			throw new IllegalArgumentException("type is required");
		}
		if (message == null || message.isBlank()) {
			throw new IllegalArgumentException("message is required");
		}
		String trimmed = message.trim();
		if (trimmed.length() > 512) {
			trimmed = trimmed.substring(0, 512);
		}
		activityEntryRepository.saveAndFlush(new ActivityEntryEntity(
				UUID.randomUUID(),
				characterId,
				type,
				trimmed,
				Instant.now(clock)));
	}

	@Transactional(propagation = Propagation.MANDATORY)
	public void recordCombatVictory(UUID characterId, String monsterName) {
		record(characterId, ActivityType.COMBAT_VICTORY, "You defeated a " + monsterName + ".");
	}

	@Transactional(propagation = Propagation.MANDATORY)
	public void recordLevelUps(UUID characterId, int previousLevel, int newLevel) {
		if (newLevel <= previousLevel) {
			return;
		}
		for (int level = previousLevel + 1; level <= newLevel; level++) {
			record(characterId, ActivityType.LEVEL_UP, "You reached level " + level + ".");
		}
	}

	@Transactional(propagation = Propagation.MANDATORY)
	public void recordItemFound(UUID characterId, String itemName, int quantity) {
		if (quantity < 1) {
			throw new IllegalArgumentException("quantity must be positive");
		}
		String message = quantity == 1
				? "You found " + itemName + "."
				: "You found " + quantity + "x " + itemName + ".";
		record(characterId, ActivityType.ITEM_FOUND, message);
	}

	@Transactional(propagation = Propagation.MANDATORY)
	public void recordExpeditionCompleted(UUID characterId, String expeditionName) {
		record(characterId, ActivityType.EXPEDITION_COMPLETED, "Your " + expeditionName + " returned.");
	}

	@Transactional(propagation = Propagation.MANDATORY)
	public void recordExpeditionClaimed(UUID characterId, String expeditionName) {
		record(characterId, ActivityType.EXPEDITION_CLAIMED, "You claimed your " + expeditionName + " rewards.");
	}

	@Transactional(propagation = Propagation.MANDATORY)
	public void recordMarketSold(UUID characterId, int price) {
		record(characterId, ActivityType.MARKET_SOLD, "Your marketplace listing sold for " + price + " gold.");
	}

	@Transactional(propagation = Propagation.MANDATORY)
	public void recordMarketBought(UUID characterId, String itemName) {
		record(characterId, ActivityType.MARKET_BOUGHT, "You bought " + itemName + ".");
	}

	@Transactional(propagation = Propagation.MANDATORY)
	public void recordMarketCancelled(UUID characterId) {
		record(characterId, ActivityType.MARKET_CANCELLED, "You cancelled your marketplace listing.");
	}

	private static ActivityEntryView toView(ActivityEntryEntity entity) {
		return new ActivityEntryView(
				entity.getId(),
				entity.getType(),
				entity.getMessage(),
				entity.getCreatedAt(),
				entity.getReadAt());
	}
}
