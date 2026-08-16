-- Notice Board metadata and City Square fixture quests.

ALTER TABLE public.quest_definition
	ADD COLUMN short_description text,
	ADD COLUMN quest_type character varying(32) NOT NULL DEFAULT 'SIDE',
	ADD COLUMN difficulty character varying(16) NOT NULL DEFAULT 'NORMAL',
	ADD COLUMN artwork_key character varying(64),
	ADD COLUMN board_location_code character varying(64),
	ADD COLUMN objective_location_code character varying(64),
	ADD COLUMN location_name character varying(128),
	ADD COLUMN region_name character varying(128),
	ADD COLUMN enabled boolean NOT NULL DEFAULT true;

ALTER TABLE public.quest_definition
	ADD CONSTRAINT chk_quest_definition_difficulty CHECK (((difficulty)::text = ANY ((ARRAY[
		'EASY'::character varying,
		'NORMAL'::character varying,
		'HARD'::character varying
	])::text[])));

ALTER TABLE public.quest_definition
	ADD CONSTRAINT fk_quest_definition_board_location
		FOREIGN KEY (board_location_code) REFERENCES public.locations(code);

ALTER TABLE public.quest_definition
	ADD CONSTRAINT fk_quest_definition_objective_location
		FOREIGN KEY (objective_location_code) REFERENCES public.locations(code);

UPDATE public.quest_definition
SET short_description = description,
	quest_type = category
WHERE short_description IS NULL;

INSERT INTO public.npc_definitions (
	id, code, name, title, description, greeting, portrait_code, location_code, merchant_code, interactions, sort_order
) VALUES (
	'e0000000-0000-4000-8000-000000000008',
	'CAPTAIN_VARRO',
	'Captain Varro',
	'Caravan captain',
	'A road captain who posts missing-convoy notices when the Old Road goes quiet.',
	'If you can read a notice, you can walk a road.',
	'militia-officer',
	'CITY_SQUARE',
	NULL,
	'TALK,QUEST,QUEST_TURN_IN',
	15
);

INSERT INTO public.quest_definition (
	id, code, name, description, category, recommended_level, min_level, start_npc_code, turn_in_npc_code,
	prerequisite_quest_code, next_quest_code, repeatable, sort_order, offer_text, progress_text, complete_text,
	short_description, quest_type, difficulty, artwork_key, board_location_code, objective_location_code,
	location_name, region_name, enabled
) VALUES
	(
		'e0000000-0000-4000-8000-000000000301',
		'QST_MISSING_CARAVAN',
		'The Missing Caravan',
		'A merchant caravan has vanished on the Old Road. Find what happened to Varro''s convoy and return with the truth.',
		'SIDE',
		4,
		4,
		'CAPTAIN_VARRO',
		'CAPTAIN_VARRO',
		NULL,
		NULL,
		false,
		30,
		'A merchant caravan has vanished on the Old Road. Find what happened to Varro''s convoy.',
		'The Old Road still has no word of the convoy.',
		'You brought back more than rumor. Take this and keep the road honest.',
		'A merchant caravan has vanished on the Old Road. Find what happened to Varro''s convoy.',
		'INVESTIGATION',
		'EASY',
		'NORTH_ROAD',
		'CITY_SQUARE',
		'NORTH_ROAD',
		'Old Road',
		'Blackstone Outskirts',
		true
	),
	(
		'e0000000-0000-4000-8000-000000000302',
		'QST_RAT_PROBLEM',
		'Rat Problem',
		'Rats have come up from the sewers again. Thin the nest and report back to the watch.',
		'SIDE',
		1,
		1,
		'MILITIA_OFFICER',
		'MILITIA_OFFICER',
		NULL,
		NULL,
		false,
		31,
		'The sewers are loud again. Kill a giant rat and come back.',
		'The nest is still scratching under the stones.',
		'Fewer teeth in the dark. That will do for tonight.',
		'Rats have come up from the sewers. Thin the nest before they reach the square.',
		'EXTERMINATION',
		'EASY',
		'SEWERS',
		'CITY_SQUARE',
		'SEWERS',
		'Sewers',
		'Greyhaven',
		true
	);

INSERT INTO public.quest_objective_definition (
	id, quest_id, sort_order, type, target_code, required_amount, display_text, consume_on_turn_in
) VALUES
	('e0000000-0000-4000-8000-000000000311', 'e0000000-0000-4000-8000-000000000301', 1, 'VISIT_LOCATION', 'NORTH_ROAD', 1, 'Search the Old Road', false),
	('e0000000-0000-4000-8000-000000000312', 'e0000000-0000-4000-8000-000000000301', 2, 'SEARCH_LOCATION', 'NORTH_ROAD', 1, 'Investigate the Abandoned Wagon', false),
	('e0000000-0000-4000-8000-000000000313', 'e0000000-0000-4000-8000-000000000301', 3, 'KILL', 'BANDIT', 1, 'Defeat Roadside Bandits', false),
	('e0000000-0000-4000-8000-000000000314', 'e0000000-0000-4000-8000-000000000301', 4, 'TALK_TO_NPC', 'CAPTAIN_VARRO', 1, 'Return to Captain Varro', false),
	('e0000000-0000-4000-8000-000000000321', 'e0000000-0000-4000-8000-000000000302', 1, 'VISIT_LOCATION', 'SEWERS', 1, 'Reach the Sewers', false),
	('e0000000-0000-4000-8000-000000000322', 'e0000000-0000-4000-8000-000000000302', 2, 'KILL', 'GIANT_RAT', 1, 'Defeat a Giant Rat', false);

INSERT INTO public.quest_reward_definition (id, quest_id, kind, amount, item_code, unlock_code, sort_order) VALUES
	('e0000000-0000-4000-8000-000000000331', 'e0000000-0000-4000-8000-000000000301', 'XP', 320, NULL, NULL, 1),
	('e0000000-0000-4000-8000-000000000332', 'e0000000-0000-4000-8000-000000000301', 'GOLD', 85, NULL, NULL, 2),
	('e0000000-0000-4000-8000-000000000341', 'e0000000-0000-4000-8000-000000000302', 'XP', 80, NULL, NULL, 1),
	('e0000000-0000-4000-8000-000000000342', 'e0000000-0000-4000-8000-000000000302', 'GOLD', 20, NULL, NULL, 2);
