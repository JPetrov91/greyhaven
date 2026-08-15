package com.example.game.telemetry.application;

import java.util.List;
import java.util.Map;

public record BalanceDiagnosticsView(
		CatalogSnapshot catalog,
		XpProgressionDiagnostics xpProgression,
		List<CountRow> levelDistribution,
		List<AttributeMeansRow> attributeMeansByLevel,
		List<WeaponUsageRow> weaponUsage,
		List<CountRow> techniqueUsage,
		PveOutcomeDiagnostics pveOutcomes,
		List<CountRow> arenaRatingBuckets,
		List<ArenaWeaponWinRow> arenaWeaponWinRates,
		List<CountRow> arenaBuildWinRates,
		List<GoldFlowRow> goldCreated,
		List<GoldFlowRow> goldDestroyed,
		long goldCreatedTotal,
		long goldDestroyedTotal,
		long goldNet,
		List<ItemFlowRow> itemCreation,
		SalvageDiagnostics salvage,
		MarketVolumeDiagnostics marketVolume,
		List<CraftingOutputRow> craftingOutput
) {

	public record CatalogSnapshot(
			int[] cumulativeXpToReachLevel,
			double listingFeePercent,
			double saleFeePercent,
			long monsterCount,
			List<CountRow> monstersByTier
	) {
	}

	public record XpProgressionDiagnostics(
			List<GoldFlowRow> xpEarnedBySource,
			long levelsGained,
			Double meanHoursBetweenLevels
	) {
	}

	public record CountRow(String key, long count) {
	}

	public record AttributeMeansRow(
			int level,
			double strength,
			double agility,
			double endurance,
			double perception,
			long characters
	) {
	}

	public record WeaponUsageRow(String weaponFamily, long fights, long wins, double winRate) {
	}

	public record PveOutcomeDiagnostics(
			long wins,
			long losses,
			long retreats,
			double winRate,
			double retreatRate,
			Double meanRounds,
			Double meanDurationMs
	) {
	}

	public record ArenaWeaponWinRow(String weaponFamily, long matches, long wins, double winRate) {
	}

	public record GoldFlowRow(String reason, long amount) {
	}

	public record ItemFlowRow(String source, String rarity, long quantity) {
	}

	public record SalvageDiagnostics(long itemsSalvaged, List<CountRow> materials) {
	}

	public record MarketVolumeDiagnostics(long trades, long grossGold, long feesDestroyed) {
	}

	public record CraftingOutputRow(String profession, String rarity, long crafts, long quantity) {
	}
}
