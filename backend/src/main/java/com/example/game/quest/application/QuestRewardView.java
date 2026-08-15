package com.example.game.quest.application;

import com.example.game.quest.domain.QuestRewardKind;

public record QuestRewardView(
		String kind,
		int amount,
		String itemCode,
		String unlockCode
) {

	public static QuestRewardView of(QuestRewardKind kind, int amount, String itemCode, String unlockCode) {
		return new QuestRewardView(kind.name(), amount, itemCode, unlockCode);
	}
}
