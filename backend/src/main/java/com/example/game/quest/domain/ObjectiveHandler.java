package com.example.game.quest.domain;

public interface ObjectiveHandler {

	boolean supports(QuestObjectiveType type);

	boolean apply(QuestObjectiveSpec spec, ObjectiveProgress progress, QuestProgressFact fact, ItemQuantitySource items);
}
