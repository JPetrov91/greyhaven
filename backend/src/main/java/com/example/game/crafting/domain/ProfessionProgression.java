package com.example.game.crafting.domain;

/**
 * Pure profession XP / rank math. Does not touch persistence.
 */
public final class ProfessionProgression {

	private ProfessionProgression() {
	}

	public record ProgressionResult(int rank, int experience, int ranksGained) {
	}

	public static ProgressionResult applyExperience(int currentRank, int currentExperience, int xpGain) {
		if (xpGain < 0) {
			throw new IllegalArgumentException("xpGain must be non-negative");
		}
		if (currentRank < CraftingBalance.STARTING_RANK || currentRank > CraftingBalance.MAX_RANK) {
			throw new IllegalArgumentException("currentRank out of range");
		}
		if (currentExperience < 0) {
			throw new IllegalArgumentException("currentExperience must be non-negative");
		}

		int rank = currentRank;
		int experience = currentExperience + xpGain;
		int ranksGained = 0;

		while (rank < CraftingBalance.MAX_RANK
				&& experience >= CraftingBalance.cumulativeXpForRank(rank + 1)) {
			rank++;
			ranksGained++;
		}

		if (rank >= CraftingBalance.MAX_RANK) {
			experience = Math.min(experience, CraftingBalance.cumulativeXpForRank(CraftingBalance.MAX_RANK));
		}

		return new ProgressionResult(rank, experience, ranksGained);
	}
}
