package com.example.game.quest.application;

import java.util.UUID;

import com.example.game.quest.domain.QuestProgressFact;

public interface QuestProgressSink {

	void notify(UUID characterId, QuestProgressFact fact);
}
