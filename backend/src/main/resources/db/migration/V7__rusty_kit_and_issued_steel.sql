-- Per-hit weapon ranges, rusty starter kit, shield soak, Issued Steel quest rework.

ALTER TABLE public.item_definitions
	ADD COLUMN weapon_damage_min integer,
	ADD COLUMN weapon_damage_max integer,
	ADD COLUMN block_soak_min integer,
	ADD COLUMN block_soak_max integer;

UPDATE public.item_definitions
SET weapon_damage_min = weapon_damage,
	weapon_damage_max = weapon_damage
WHERE type = 'WEAPON' AND weapon_damage IS NOT NULL;

ALTER TABLE public.item_definitions
	ADD CONSTRAINT chk_item_definitions_weapon_range CHECK (
		(weapon_damage_min IS NULL AND weapon_damage_max IS NULL)
		OR (weapon_damage_min IS NOT NULL AND weapon_damage_max IS NOT NULL
			AND weapon_damage_min >= 0 AND weapon_damage_max >= weapon_damage_min)
	);

ALTER TABLE public.item_definitions
	ADD CONSTRAINT chk_item_definitions_block_soak CHECK (
		(block_soak_min IS NULL AND block_soak_max IS NULL)
		OR (block_soak_min IS NOT NULL AND block_soak_max IS NOT NULL
			AND block_soak_min >= 0 AND block_soak_max >= block_soak_min)
	);

ALTER TABLE public.item_definitions
	ADD CONSTRAINT chk_item_definitions_weapon_has_range CHECK (
		((type)::text <> 'WEAPON')
		OR (weapon_damage_min IS NOT NULL AND weapon_damage_max IS NOT NULL)
	);

UPDATE public.item_definitions
SET weapon_damage_min = 4,
	weapon_damage_max = 8,
	weapon_damage = 6
WHERE code = 'RUSTY_SWORD';

INSERT INTO public.item_definitions (
	id, code, name, description, type, rarity, base_value, required_level,
	weapon_damage, armor_value, heal_amount, created_at, equipment_slot, two_handed,
	weapon_family, armor_category, required_strength, required_agility, required_endurance,
	required_perception, legacy, weapon_damage_min, weapon_damage_max, block_soak_min, block_soak_max
) VALUES
	('c0000000-0000-4000-8000-000000000027', 'RUSTY_AXE', 'Rusty Axe',
	 'A notched hatchet issued by the watch. Wide swings, honest rust.',
	 'WEAPON', 'COMMON', 5, 1, 7, NULL, NULL, '2026-01-01 00:00:00+00', 'MAIN_HAND', false,
	 'AXE', NULL, 0, 0, 0, 0, false, 4, 9, NULL, NULL),
	('c0000000-0000-4000-8000-000000000028', 'RUSTY_MACE', 'Rusty Mace',
	 'A weighted club with a tired head. High floor, little flourish.',
	 'WEAPON', 'COMMON', 5, 1, 7, NULL, NULL, '2026-01-01 00:00:00+00', 'MAIN_HAND', false,
	 'MACE', NULL, 0, 0, 0, 0, false, 5, 8, NULL, NULL),
	('c0000000-0000-4000-8000-000000000029', 'RUSTY_DAGGER', 'Rusty Dagger',
	 'A short blade for close work. Tight cuts. No shield.',
	 'WEAPON', 'COMMON', 5, 1, 4, NULL, NULL, '2026-01-01 00:00:00+00', 'MAIN_HAND', false,
	 'DAGGER', NULL, 0, 0, 0, 0, false, 3, 5, NULL, NULL),
	('c0000000-0000-4000-8000-00000000002a', 'RUSTY_SHIELD', 'Rusty Shield',
	 'A tired off-hand plate. It still covers, if you let it.',
	 'ARMOR', 'COMMON', 4, 1, NULL, 1, NULL, '2026-01-01 00:00:00+00', 'OFF_HAND', false,
	 NULL, 'LIGHT', 0, 0, 0, 0, false, NULL, NULL, 1, 2);

DELETE FROM public.merchant_stock
WHERE id = 'e0000000-0000-4000-8000-000000000001';

ALTER TABLE public.character_quest
	ADD COLUMN kit_family character varying(16),
	ADD COLUMN last_search_outcome character varying(16);

ALTER TABLE public.character_quest
	ADD CONSTRAINT chk_character_quest_kit_family CHECK (
		kit_family IS NULL OR kit_family IN ('SWORD', 'AXE', 'MACE', 'DAGGERS')
	);

ALTER TABLE public.character_quest
	ADD CONSTRAINT chk_character_quest_search_outcome CHECK (
		last_search_outcome IS NULL
		OR last_search_outcome IN ('NO_COMBAT', 'VICTORY', 'RETREAT')
	);

ALTER TABLE public.quest_objective_definition
	DROP CONSTRAINT chk_quest_objective_type;

ALTER TABLE public.quest_objective_definition
	ADD CONSTRAINT chk_quest_objective_type CHECK (((type)::text = ANY ((ARRAY[
		'KILL'::character varying,
		'COLLECT'::character varying,
		'VISIT_LOCATION'::character varying,
		'DEFEAT_ENEMY'::character varying,
		'COMPLETE_DUNGEON'::character varying,
		'CRAFT_ITEM'::character varying,
		'ACQUIRE_ITEM'::character varying,
		'TALK_TO_NPC'::character varying,
		'COMPLETE_EXPEDITION'::character varying,
		'WIN_ARENA_MATCH'::character varying,
		'SEARCH_LOCATION'::character varying
	])::text[])));

UPDATE public.locations
SET description = 'Greyhaven''s heart still pretends it is morning. Gates open. Bells ring. The watch is thinner than the crowd.'
WHERE code = 'CITY_SQUARE';

UPDATE public.locations
SET description = 'The Square''s noise dies in a lane. Wet stone, cheap ale, someone else''s blood already dry. The watch does not own this street after dark. They barely own it at noon.'
WHERE code = 'OLD_TOWN';

UPDATE public.npc_definitions
SET greeting = 'The watch has work, if you can follow an order.'
WHERE code = 'MILITIA_OFFICER';

UPDATE public.quest_definition
SET name = 'Issued Steel',
	description = 'The watch is thin. Bren will put rust in your hands and send you to Old Town. Come back.',
	offer_text = 'Greyhaven''s gates still open. Old Town does not care. Take rust from Bren, walk the alleys, report.',
	progress_text = 'Bren is waiting on a report. Old Town, then the Square.',
	complete_text = 'You came back. The watch noticed. The rust is yours.'
WHERE code = 'QST_MILITIA_NOTICE';

UPDATE public.quest_definition
SET offer_text = 'When the rust starts to embarrass you, come to the Market. I sell things that come back with you. That is not an order.',
	progress_text = 'I am still here. The stall is not hard to find.',
	complete_text = 'Good. Wear it. The timber road does not care what you meant to buy.',
	description = 'Edric Varn sells a better edge when the rust is no longer enough. Optional. You are already armed.'
WHERE code = 'QST_ARM_THE_WATCH';

UPDATE public.quest_objective_definition
SET type = 'TALK_TO_NPC',
	target_code = 'MILITIA_OFFICER',
	display_text = 'Speak with Watch-Sergeant Bren'
WHERE id = 'e0000000-0000-4000-8000-000000000111';

UPDATE public.quest_objective_definition
SET type = 'VISIT_LOCATION',
	target_code = 'OLD_TOWN',
	sort_order = 2,
	display_text = 'Reach Old Town'
WHERE id = 'e0000000-0000-4000-8000-000000000112';

INSERT INTO public.quest_objective_definition (
	id, quest_id, sort_order, type, target_code, required_amount, display_text, consume_on_turn_in
) VALUES (
	'e0000000-0000-4000-8000-000000000113',
	'e0000000-0000-4000-8000-000000000101',
	3,
	'SEARCH_LOCATION',
	'OLD_TOWN',
	1,
	'Search the alleys',
	false
);
