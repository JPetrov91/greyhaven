package com.example.game.quest.application;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.game.quest.infrastructure.QuestDefinitionEntity;
import com.example.game.quest.infrastructure.QuestDefinitionRepository;
import com.example.game.quest.infrastructure.QuestObjectiveDefinitionEntity;
import com.example.game.quest.infrastructure.QuestObjectiveDefinitionRepository;
import com.example.game.quest.infrastructure.QuestRewardDefinitionEntity;
import com.example.game.quest.infrastructure.QuestRewardDefinitionRepository;

@Service
public class QuestCatalog {

	private final QuestDefinitionRepository questDefinitionRepository;
	private final QuestObjectiveDefinitionRepository objectiveDefinitionRepository;
	private final QuestRewardDefinitionRepository rewardDefinitionRepository;

	public QuestCatalog(
			QuestDefinitionRepository questDefinitionRepository,
			QuestObjectiveDefinitionRepository objectiveDefinitionRepository,
			QuestRewardDefinitionRepository rewardDefinitionRepository) {
		this.questDefinitionRepository = questDefinitionRepository;
		this.objectiveDefinitionRepository = objectiveDefinitionRepository;
		this.rewardDefinitionRepository = rewardDefinitionRepository;
	}

	@Transactional(readOnly = true)
	public List<QuestDefinitionEntity> allQuests() {
		return questDefinitionRepository.findAllByOrderBySortOrderAsc();
	}

	@Transactional(readOnly = true)
	public QuestDefinitionEntity requireByCode(String code) {
		return questDefinitionRepository.findByCode(code).orElseThrow(QuestErrors::questNotFound);
	}

	@Transactional(readOnly = true)
	public QuestDefinitionEntity requireById(UUID id) {
		return questDefinitionRepository.findById(id).orElseThrow(QuestErrors::questNotFound);
	}

	@Transactional(readOnly = true)
	public Map<UUID, List<QuestObjectiveDefinitionEntity>> objectivesByQuestId(List<UUID> questIds) {
		return objectiveDefinitionRepository.findByQuestIdInOrderBySortOrderAsc(questIds).stream()
				.collect(Collectors.groupingBy(QuestObjectiveDefinitionEntity::getQuestId));
	}

	@Transactional(readOnly = true)
	public List<QuestObjectiveDefinitionEntity> objectivesOf(UUID questId) {
		return objectiveDefinitionRepository.findByQuestIdOrderBySortOrderAsc(questId);
	}

	@Transactional(readOnly = true)
	public Map<UUID, QuestObjectiveDefinitionEntity> objectivesById(List<UUID> questIds) {
		return objectiveDefinitionRepository.findByQuestIdInOrderBySortOrderAsc(questIds).stream()
				.collect(Collectors.toMap(QuestObjectiveDefinitionEntity::getId, Function.identity()));
	}

	@Transactional(readOnly = true)
	public List<QuestRewardDefinitionEntity> rewardsOf(UUID questId) {
		return rewardDefinitionRepository.findByQuestIdOrderBySortOrderAsc(questId);
	}

	@Transactional(readOnly = true)
	public Map<UUID, List<QuestRewardDefinitionEntity>> rewardsByQuestId(List<UUID> questIds) {
		return rewardDefinitionRepository.findByQuestIdInOrderBySortOrderAsc(questIds).stream()
				.collect(Collectors.groupingBy(QuestRewardDefinitionEntity::getQuestId));
	}
}
