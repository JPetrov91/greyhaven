package com.example.game.world.api;

import java.util.List;

public record QuestBoardResponse(String locationCode, List<QuestBoardEntryResponse> quests) {
}
