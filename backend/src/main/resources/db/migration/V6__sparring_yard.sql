-- Sparring Yard: low-level unranked PvP hub and drill-bot catalog.
-- Combat sessions store rolled enemy max HP so drills can differ from definition stubs.

ALTER TABLE public.combat_sessions
	ADD COLUMN snap_enemy_max_health integer DEFAULT 0 NOT NULL;

INSERT INTO public.locations (
	id, code, name, description, safety, region, created_at, recommended_level_min, recommended_level_max
) VALUES (
	'a0000000-0000-4000-8000-00000000000e',
	'SPARRING_YARD',
	'Sparring Yard',
	'Packed dirt and wooden posts beside the square. Recruits trade blows here before the Arena will take their names.',
	'SAFE',
	'Greyhaven',
	'2026-01-01 00:00:00+00',
	1,
	10
);

INSERT INTO public.location_connections (id, from_location_id, to_location_id) VALUES
	('b0000000-0000-4000-8000-00000000001d', 'a0000000-0000-4000-8000-000000000001', 'a0000000-0000-4000-8000-00000000000e'),
	('b0000000-0000-4000-8000-00000000001e', 'a0000000-0000-4000-8000-00000000000e', 'a0000000-0000-4000-8000-000000000001');

-- Stub combat ratings; live drill stats are rolled into the session snapshot at fight start.
INSERT INTO public.monster_definitions (
	id, code, name, level, max_health, damage_min, damage_max, xp_reward, gold_min, gold_max,
	created_at, armor, accuracy, dodge, critical_chance, max_stamina, ai_archetype, signature_status, monster_tier
) VALUES
	('d0000000-0000-4000-8000-000000000021', 'SPARRING_BOT_L01', 'Green Recruit', 1, 1, 1, 1, 0, 0, 0, '2026-01-01 00:00:00+00', 0, 70, 0, 0, 1, 'AGGRESSIVE', NULL, 'NORMAL'),
	('d0000000-0000-4000-8000-000000000022', 'SPARRING_BOT_L02', 'Street Sparrer', 2, 1, 1, 1, 0, 0, 0, '2026-01-01 00:00:00+00', 0, 70, 0, 0, 1, 'AGGRESSIVE', NULL, 'NORMAL'),
	('d0000000-0000-4000-8000-000000000023', 'SPARRING_BOT_L03', 'Watch Cadet', 3, 1, 1, 1, 0, 0, 0, '2026-01-01 00:00:00+00', 0, 70, 0, 0, 1, 'AGGRESSIVE', NULL, 'NORMAL'),
	('d0000000-0000-4000-8000-000000000024', 'SPARRING_BOT_L04', 'Yard Regular', 4, 1, 1, 1, 0, 0, 0, '2026-01-01 00:00:00+00', 0, 70, 0, 0, 1, 'DEFENSIVE', NULL, 'NORMAL'),
	('d0000000-0000-4000-8000-000000000025', 'SPARRING_BOT_L05', 'Militia Drillman', 5, 1, 1, 1, 0, 0, 0, '2026-01-01 00:00:00+00', 0, 70, 0, 0, 1, 'DEFENSIVE', NULL, 'NORMAL'),
	('d0000000-0000-4000-8000-000000000026', 'SPARRING_BOT_L06', 'Veteran Sparrer', 6, 1, 1, 1, 0, 0, 0, '2026-01-01 00:00:00+00', 0, 70, 0, 0, 1, 'DEFENSIVE', NULL, 'NORMAL'),
	('d0000000-0000-4000-8000-000000000027', 'SPARRING_BOT_L07', 'Watch Corporal', 7, 1, 1, 1, 0, 0, 0, '2026-01-01 00:00:00+00', 0, 70, 0, 0, 1, 'ARMORED', NULL, 'NORMAL'),
	('d0000000-0000-4000-8000-000000000028', 'SPARRING_BOT_L08', 'Yard Sergeant', 8, 1, 1, 1, 0, 0, 0, '2026-01-01 00:00:00+00', 0, 70, 0, 0, 1, 'ARMORED', NULL, 'NORMAL'),
	('d0000000-0000-4000-8000-000000000029', 'SPARRING_BOT_L09', 'Drill Champion', 9, 1, 1, 1, 0, 0, 0, '2026-01-01 00:00:00+00', 0, 70, 0, 0, 1, 'ARMORED', NULL, 'NORMAL'),
	('d0000000-0000-4000-8000-00000000002a', 'SPARRING_BOT_L10', 'Watch Provost', 10, 1, 1, 1, 0, 0, 0, '2026-01-01 00:00:00+00', 0, 70, 0, 0, 1, 'ARMORED', NULL, 'NORMAL');
