package com.example.game.quest.infrastructure;

import java.util.UUID;

import org.springframework.data.domain.Persistable;

import com.example.game.quest.domain.QuestCategory;
import com.example.game.quest.domain.QuestDifficulty;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.PostLoad;
import jakarta.persistence.PostPersist;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

@Entity
@Table(name = "quest_definition")
public class QuestDefinitionEntity implements Persistable<UUID> {

	@Id
	private UUID id;

	@Column(nullable = false, unique = true, length = 64)
	private String code;

	@Column(nullable = false, length = 128)
	private String name;

	@Column(nullable = false, columnDefinition = "TEXT")
	private String description;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 16)
	private QuestCategory category;

	@Column(name = "recommended_level", nullable = false)
	private int recommendedLevel;

	@Column(name = "min_level", nullable = false)
	private int minLevel;

	@Column(name = "start_npc_code", length = 64)
	private String startNpcCode;

	@Column(name = "turn_in_npc_code", length = 64)
	private String turnInNpcCode;

	@Column(name = "prerequisite_quest_code", length = 64)
	private String prerequisiteQuestCode;

	@Column(name = "next_quest_code", length = 64)
	private String nextQuestCode;

	@Column(nullable = false)
	private boolean repeatable;

	@Column(name = "sort_order", nullable = false)
	private int sortOrder;

	@Column(name = "offer_text", nullable = false, columnDefinition = "TEXT")
	private String offerText;

	@Column(name = "progress_text", nullable = false, columnDefinition = "TEXT")
	private String progressText;

	@Column(name = "complete_text", nullable = false, columnDefinition = "TEXT")
	private String completeText;

	@Column(name = "short_description", columnDefinition = "TEXT")
	private String shortDescription;

	@Column(name = "quest_type", nullable = false, length = 32)
	private String questType;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 16)
	private QuestDifficulty difficulty;

	@Column(name = "artwork_key", length = 64)
	private String artworkKey;

	@Column(name = "board_location_code", length = 64)
	private String boardLocationCode;

	@Column(name = "objective_location_code", length = 64)
	private String objectiveLocationCode;

	@Column(name = "location_name", length = 128)
	private String locationName;

	@Column(name = "region_name", length = 128)
	private String regionName;

	@Column(nullable = false)
	private boolean enabled;

	@Transient
	private boolean unsaved;

	protected QuestDefinitionEntity() {
	}

	public QuestDefinitionEntity(
			UUID id,
			String code,
			String name,
			String description,
			QuestCategory category,
			int recommendedLevel,
			int minLevel,
			String startNpcCode,
			String turnInNpcCode,
			String prerequisiteQuestCode,
			String nextQuestCode,
			boolean repeatable,
			int sortOrder,
			String offerText,
			String progressText,
			String completeText) {
		this.id = id;
		this.code = code;
		this.name = name;
		this.description = description;
		this.category = category;
		this.recommendedLevel = recommendedLevel;
		this.minLevel = minLevel;
		this.startNpcCode = startNpcCode;
		this.turnInNpcCode = turnInNpcCode;
		this.prerequisiteQuestCode = prerequisiteQuestCode;
		this.nextQuestCode = nextQuestCode;
		this.repeatable = repeatable;
		this.sortOrder = sortOrder;
		this.offerText = offerText;
		this.progressText = progressText;
		this.completeText = completeText;
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

	public String getCode() {
		return code;
	}

	public String getName() {
		return name;
	}

	public String getDescription() {
		return description;
	}

	public QuestCategory getCategory() {
		return category;
	}

	public int getRecommendedLevel() {
		return recommendedLevel;
	}

	public int getMinLevel() {
		return minLevel;
	}

	public String getStartNpcCode() {
		return startNpcCode;
	}

	public String getTurnInNpcCode() {
		return turnInNpcCode;
	}

	public String getPrerequisiteQuestCode() {
		return prerequisiteQuestCode;
	}

	public String getNextQuestCode() {
		return nextQuestCode;
	}

	public boolean isRepeatable() {
		return repeatable;
	}

	public int getSortOrder() {
		return sortOrder;
	}

	public String getOfferText() {
		return offerText;
	}

	public String getProgressText() {
		return progressText;
	}

	public String getCompleteText() {
		return completeText;
	}

	public String getShortDescription() {
		return shortDescription;
	}

	public String getQuestType() {
		return questType;
	}

	public QuestDifficulty getDifficulty() {
		return difficulty;
	}

	public String getArtworkKey() {
		return artworkKey;
	}

	public String getBoardLocationCode() {
		return boardLocationCode;
	}

	public String getObjectiveLocationCode() {
		return objectiveLocationCode;
	}

	public String getLocationName() {
		return locationName;
	}

	public String getRegionName() {
		return regionName;
	}

	public boolean isEnabled() {
		return enabled;
	}
}
