package com.example.game.telemetry.application;

import java.util.List;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.game.shared.balance.GameBalance;
import com.example.game.shared.balance.GameBalanceCatalog;
import com.example.game.telemetry.application.BalanceDiagnosticsView.ArenaWeaponWinRow;
import com.example.game.telemetry.application.BalanceDiagnosticsView.AttributeMeansRow;
import com.example.game.telemetry.application.BalanceDiagnosticsView.CatalogSnapshot;
import com.example.game.telemetry.application.BalanceDiagnosticsView.CountRow;
import com.example.game.telemetry.application.BalanceDiagnosticsView.CraftingOutputRow;
import com.example.game.telemetry.application.BalanceDiagnosticsView.GoldFlowRow;
import com.example.game.telemetry.application.BalanceDiagnosticsView.ItemFlowRow;
import com.example.game.telemetry.application.BalanceDiagnosticsView.MarketVolumeDiagnostics;
import com.example.game.telemetry.application.BalanceDiagnosticsView.PveOutcomeDiagnostics;
import com.example.game.telemetry.application.BalanceDiagnosticsView.SalvageDiagnostics;
import com.example.game.telemetry.application.BalanceDiagnosticsView.WeaponUsageRow;
import com.example.game.telemetry.application.BalanceDiagnosticsView.XpProgressionDiagnostics;

@Service
public class BalanceDiagnosticsService {

	private final JdbcTemplate jdbcTemplate;

	public BalanceDiagnosticsService(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	@Transactional(readOnly = true)
	public BalanceDiagnosticsView snapshot() {
		GameBalance balance = GameBalanceCatalog.get();
		List<GoldFlowRow> created = goldFlows("GOLD_CREATED");
		List<GoldFlowRow> destroyed = goldFlows("GOLD_DESTROYED");
		long createdTotal = created.stream().mapToLong(GoldFlowRow::amount).sum();
		long destroyedTotal = destroyed.stream().mapToLong(GoldFlowRow::amount).sum();
		return new BalanceDiagnosticsView(
				new CatalogSnapshot(
						balance.progression().cumulativeXpToReachLevel(),
						balance.market().listingFeePercent(),
						balance.market().saleFeePercent(),
						nullToZero(jdbcTemplate.queryForObject("select count(*) from monster_definitions", Long.class)),
						countRows("select monster_tier, count(*) from monster_definitions group by monster_tier order by monster_tier")),
				new XpProgressionDiagnostics(
						goldFlowsFrom("XP_EARNED", "source"),
						nullToZero(jdbcTemplate.queryForObject(
								"select count(*) from game_telemetry_events where event_type = 'LEVEL_UP'",
								Long.class)),
						jdbcTemplate.query(
								"""
										select avg(extract(epoch from (occurred_at - prev_at)) / 3600.0)
										from (
										  select occurred_at,
										         lag(occurred_at) over (partition by character_id order by occurred_at) as prev_at
										  from game_telemetry_events
										  where event_type = 'LEVEL_UP'
										) timed
										where prev_at is not null
										""",
								rs -> rs.next() ? (Double) rs.getObject(1) : null)),
				countRows("select level::text, count(*) from characters group by level order by level"),
				attributeMeans(),
				weaponUsage(),
				countRows("""
						select key, sum(value::bigint)
						from game_telemetry_events,
						     jsonb_each_text(payload -> 'techniqueUses')
						where event_type = 'COMBAT_ENDED'
						group by key
						order by sum(value::bigint) desc, key
						"""),
				pveOutcomes(),
				countRows("""
						select
						  (width_bucket(arena_rating, 0, 2500, 10) * 250)::text || '-' || ((width_bucket(arena_rating, 0, 2500, 10) * 250) + 249)::text,
						  count(*)
						from characters
						group by 1
						order by min(arena_rating)
						"""),
				arenaWeaponWins(),
				countRows("""
						select payload->>'attackerBuild', count(*)
						from game_telemetry_events
						where event_type = 'PVP_MATCH_SETTLED'
						  and payload->>'mode' = 'ARENA'
						  and (payload->>'attackerWon')::boolean
						group by 1
						order by count(*) desc
						"""),
				created,
				destroyed,
				createdTotal,
				destroyedTotal,
				createdTotal - destroyedTotal,
				itemCreation(),
				salvage(),
				marketVolume(),
				craftingOutput());
	}

	private List<GoldFlowRow> goldFlows(String eventType) {
		return goldFlowsFrom(eventType, "reason");
	}

	private List<GoldFlowRow> goldFlowsFrom(String eventType, String field) {
		return jdbcTemplate.query(
				"""
						select payload->>? as reason, coalesce(sum((payload->>'amount')::bigint), 0)
						from game_telemetry_events
						where event_type = ?
						group by 1
						order by 2 desc, 1
						""",
				(rs, rowNum) -> new GoldFlowRow(rs.getString(1), rs.getLong(2)),
				field,
				eventType);
	}

	private List<CountRow> countRows(String sql) {
		return jdbcTemplate.query(sql, (rs, rowNum) -> new CountRow(
				rs.getString(1) == null ? "NONE" : rs.getString(1),
				rs.getLong(2)));
	}

	private List<AttributeMeansRow> attributeMeans() {
		return jdbcTemplate.query(
				"""
						select level,
						       avg(strength),
						       avg(agility),
						       avg(endurance),
						       avg(perception),
						       count(*)
						from characters
						group by level
						order by level
						""",
				(rs, rowNum) -> new AttributeMeansRow(
						rs.getInt(1),
						rs.getDouble(2),
						rs.getDouble(3),
						rs.getDouble(4),
						rs.getDouble(5),
						rs.getLong(6)));
	}

	private List<WeaponUsageRow> weaponUsage() {
		return jdbcTemplate.query(
				"""
						select coalesce(payload->>'weaponFamily', 'NONE') as family,
						       count(*),
						       count(*) filter (where payload->>'outcome' = 'WIN'),
						       case when count(*) = 0 then 0
						            else count(*) filter (where payload->>'outcome' = 'WIN')::float / count(*)
						       end
						from game_telemetry_events
						where event_type = 'COMBAT_ENDED'
						group by 1
						order by 2 desc, 1
						""",
				(rs, rowNum) -> new WeaponUsageRow(rs.getString(1), rs.getLong(2), rs.getLong(3), rs.getDouble(4)));
	}

	private PveOutcomeDiagnostics pveOutcomes() {
		return jdbcTemplate.query(
				"""
						select
						  count(*) filter (where payload->>'outcome' = 'WIN'),
						  count(*) filter (where payload->>'outcome' = 'LOSS'),
						  count(*) filter (where payload->>'outcome' = 'RETREAT'),
						  count(*),
						  avg((payload->>'roundCount')::float),
						  avg((payload->>'durationMs')::float)
						from game_telemetry_events
						where event_type = 'COMBAT_ENDED'
						""",
				rs -> {
					if (!rs.next()) {
						return new PveOutcomeDiagnostics(0, 0, 0, 0, 0, null, null);
					}
					long wins = rs.getLong(1);
					long losses = rs.getLong(2);
					long retreats = rs.getLong(3);
					long total = rs.getLong(4);
					return new PveOutcomeDiagnostics(
							wins,
							losses,
							retreats,
							total == 0 ? 0 : (double) wins / total,
							total == 0 ? 0 : (double) retreats / total,
							(Double) rs.getObject(5),
							(Double) rs.getObject(6));
				});
	}

	private List<ArenaWeaponWinRow> arenaWeaponWins() {
		return jdbcTemplate.query(
				"""
						select family, count(*), count(*) filter (where won),
						       case when count(*) = 0 then 0
						            else count(*) filter (where won)::float / count(*)
						       end
						from (
						  select payload->>'attackerWeaponFamily' as family,
						         (payload->>'attackerWon')::boolean as won
						  from game_telemetry_events
						  where event_type = 'PVP_MATCH_SETTLED' and payload->>'mode' = 'ARENA'
						  union all
						  select payload->>'defenderWeaponFamily' as family,
						         not (payload->>'attackerWon')::boolean as won
						  from game_telemetry_events
						  where event_type = 'PVP_MATCH_SETTLED' and payload->>'mode' = 'ARENA'
						) sides
						group by family
						order by count(*) desc, family
						""",
				(rs, rowNum) -> new ArenaWeaponWinRow(rs.getString(1), rs.getLong(2), rs.getLong(3), rs.getDouble(4)));
	}

	private List<ItemFlowRow> itemCreation() {
		return jdbcTemplate.query(
				"""
						select payload->>'source', payload->>'rarity', coalesce(sum((payload->>'quantity')::bigint), 0)
						from game_telemetry_events
						where event_type = 'ITEM_CREATED'
						group by 1, 2
						order by 1, 2
						""",
				(rs, rowNum) -> new ItemFlowRow(rs.getString(1), rs.getString(2), rs.getLong(3)));
	}

	private SalvageDiagnostics salvage() {
		long items = nullToZero(jdbcTemplate.queryForObject(
				"select count(*) from game_telemetry_events where event_type = 'ITEM_SALVAGED'",
				Long.class));
		List<CountRow> materials = countRows("""
				select key, sum(value::bigint)
				from game_telemetry_events,
				     jsonb_each_text(payload -> 'materials')
				where event_type = 'ITEM_SALVAGED'
				group by key
				order by sum(value::bigint) desc, key
				""");
		return new SalvageDiagnostics(items, materials);
	}

	private MarketVolumeDiagnostics marketVolume() {
		return jdbcTemplate.query(
				"""
						select count(*),
						       coalesce(sum((payload->>'gross')::bigint), 0),
						       coalesce(sum((payload->>'feeDestroyed')::bigint), 0)
						from game_telemetry_events
						where event_type = 'MARKET_TRADE'
						""",
				rs -> {
					if (!rs.next()) {
						return new MarketVolumeDiagnostics(0, 0, 0);
					}
					return new MarketVolumeDiagnostics(rs.getLong(1), rs.getLong(2), rs.getLong(3));
				});
	}

	private List<CraftingOutputRow> craftingOutput() {
		return jdbcTemplate.query(
				"""
						select payload->>'profession',
						       payload->>'rarity',
						       count(*),
						       coalesce(sum((payload->>'quantity')::bigint), 0)
						from game_telemetry_events
						where event_type = 'CRAFTING_CLAIMED'
						group by 1, 2
						order by 1, 2
						""",
				(rs, rowNum) -> new CraftingOutputRow(
						rs.getString(1),
						rs.getString(2),
						rs.getLong(3),
						rs.getLong(4)));
	}

	private static long nullToZero(Long value) {
		return value == null ? 0 : value;
	}
}
