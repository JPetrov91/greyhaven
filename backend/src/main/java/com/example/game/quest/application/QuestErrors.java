package com.example.game.quest.application;

import org.springframework.http.HttpStatus;

import com.example.game.shared.api.ApiException;

public final class QuestErrors {

	private QuestErrors() {
	}

	public static ApiException questNotFound() {
		return new ApiException("QUEST_NOT_FOUND", "That quest does not exist.", HttpStatus.NOT_FOUND);
	}

	public static ApiException questNotAvailable() {
		return new ApiException("QUEST_NOT_AVAILABLE", "This quest is no longer available.", HttpStatus.CONFLICT);
	}

	public static ApiException wrongBoardLocation() {
		return new ApiException("QUEST_WRONG_LOCATION", "You must be at the notice board.", HttpStatus.CONFLICT);
	}

	public static ApiException questAlreadyAccepted() {
		return new ApiException("QUEST_ALREADY_ACCEPTED", "You already accepted that quest.", HttpStatus.CONFLICT);
	}

	public static ApiException questNotReady() {
		return new ApiException("QUEST_NOT_READY", "That quest is not ready to turn in.", HttpStatus.CONFLICT);
	}

	public static ApiException wrongLocation() {
		return new ApiException("QUEST_WRONG_LOCATION", "You must speak with the right person here.", HttpStatus.CONFLICT);
	}

	public static ApiException trackLimit() {
		return new ApiException("QUEST_TRACK_LIMIT", "You can track at most 3 quests.", HttpStatus.CONFLICT);
	}

	public static ApiException questNotActive() {
		return new ApiException("QUEST_NOT_ACTIVE", "That quest is not on your log.", HttpStatus.CONFLICT);
	}

	public static ApiException npcNotFound() {
		return new ApiException("NPC_NOT_FOUND", "That person is not here.", HttpStatus.NOT_FOUND);
	}

	public static ApiException npcNotAtLocation() {
		return new ApiException("NPC_NOT_HERE", "That person is not at this location.", HttpStatus.CONFLICT);
	}
}
