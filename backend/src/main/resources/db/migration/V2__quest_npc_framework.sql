-- Quest & NPC framework (Phase 3 Task 06).

ALTER TABLE public.activity_entries DROP CONSTRAINT chk_activity_entries_type;
ALTER TABLE public.activity_entries ADD CONSTRAINT chk_activity_entries_type CHECK (((type)::text = ANY ((ARRAY[
	'COMBAT_VICTORY'::character varying,
	'LEVEL_UP'::character varying,
	'ITEM_FOUND'::character varying,
	'EXPEDITION_COMPLETED'::character varying,
	'EXPEDITION_CLAIMED'::character varying,
	'MARKET_SOLD'::character varying,
	'MARKET_BOUGHT'::character varying,
	'MARKET_CANCELLED'::character varying,
	'MASTERY_UNLOCK'::character varying,
	'TECHNIQUE_UNLOCK'::character varying,
	'ARENA_VICTORY'::character varying,
	'ARENA_DEFEAT'::character varying,
	'DUEL_RESULT'::character varying,
	'CRAFTING_STARTED'::character varying,
	'CRAFTING_CLAIMED'::character varying,
	'PROFESSION_RANK_UP'::character varying,
	'ITEM_SALVAGED'::character varying,
	'MARKET_LISTING_FEE'::character varying,
	'MARKET_SALE'::character varying,
	'BUY_ORDER_CREATED'::character varying,
	'BUY_ORDER_FILLED'::character varying,
	'BUY_ORDER_CANCELLED'::character varying,
	'QUEST_ACCEPTED'::character varying,
	'QUEST_OBJECTIVE'::character varying,
	'QUEST_COMPLETED'::character varying
])::text[])));

CREATE TABLE public.npc_definitions (
	id uuid NOT NULL,
	code character varying(64) NOT NULL,
	name character varying(128) NOT NULL,
	title character varying(128) NOT NULL,
	description text NOT NULL,
	greeting text NOT NULL,
	portrait_code character varying(64) NOT NULL,
	location_code character varying(64) NOT NULL,
	merchant_code character varying(64),
	interactions character varying(256) NOT NULL,
	sort_order integer NOT NULL,
	CONSTRAINT npc_definitions_pkey PRIMARY KEY (id),
	CONSTRAINT uq_npc_definitions_code UNIQUE (code),
	CONSTRAINT fk_npc_definitions_location FOREIGN KEY (location_code) REFERENCES public.locations(code),
	CONSTRAINT fk_npc_definitions_merchant FOREIGN KEY (merchant_code) REFERENCES public.merchant_definitions(code)
);

CREATE TABLE public.quest_definition (
	id uuid NOT NULL,
	code character varying(64) NOT NULL,
	name character varying(128) NOT NULL,
	description text NOT NULL,
	category character varying(16) NOT NULL,
	recommended_level integer NOT NULL,
	min_level integer NOT NULL,
	start_npc_code character varying(64),
	turn_in_npc_code character varying(64),
	prerequisite_quest_code character varying(64),
	next_quest_code character varying(64),
	repeatable boolean NOT NULL,
	sort_order integer NOT NULL,
	offer_text text NOT NULL,
	progress_text text NOT NULL,
	complete_text text NOT NULL,
	CONSTRAINT quest_definition_pkey PRIMARY KEY (id),
	CONSTRAINT uq_quest_definition_code UNIQUE (code),
	CONSTRAINT chk_quest_definition_category CHECK (((category)::text = ANY ((ARRAY['MAIN'::character varying, 'SIDE'::character varying])::text[]))),
	CONSTRAINT chk_quest_definition_levels CHECK ((recommended_level >= 1 AND min_level >= 1)),
	CONSTRAINT fk_quest_definition_start_npc FOREIGN KEY (start_npc_code) REFERENCES public.npc_definitions(code),
	CONSTRAINT fk_quest_definition_turn_in_npc FOREIGN KEY (turn_in_npc_code) REFERENCES public.npc_definitions(code)
);

CREATE TABLE public.quest_objective_definition (
	id uuid NOT NULL,
	quest_id uuid NOT NULL,
	sort_order integer NOT NULL,
	type character varying(32) NOT NULL,
	target_code character varying(64) NOT NULL,
	required_amount integer NOT NULL,
	display_text character varying(256) NOT NULL,
	consume_on_turn_in boolean NOT NULL,
	CONSTRAINT quest_objective_definition_pkey PRIMARY KEY (id),
	CONSTRAINT uq_quest_objective_definition_quest_sort UNIQUE (quest_id, sort_order),
	CONSTRAINT chk_quest_objective_amount CHECK ((required_amount >= 1)),
	CONSTRAINT chk_quest_objective_type CHECK (((type)::text = ANY ((ARRAY[
		'KILL'::character varying,
		'COLLECT'::character varying,
		'VISIT_LOCATION'::character varying,
		'DEFEAT_ENEMY'::character varying,
		'COMPLETE_DUNGEON'::character varying,
		'CRAFT_ITEM'::character varying,
		'ACQUIRE_ITEM'::character varying,
		'TALK_TO_NPC'::character varying,
		'COMPLETE_EXPEDITION'::character varying,
		'WIN_ARENA_MATCH'::character varying
	])::text[]))),
	CONSTRAINT fk_quest_objective_definition_quest FOREIGN KEY (quest_id) REFERENCES public.quest_definition(id)
);

CREATE TABLE public.quest_reward_definition (
	id uuid NOT NULL,
	quest_id uuid NOT NULL,
	kind character varying(16) NOT NULL,
	amount integer NOT NULL,
	item_code character varying(64),
	unlock_code character varying(64),
	sort_order integer NOT NULL,
	CONSTRAINT quest_reward_definition_pkey PRIMARY KEY (id),
	CONSTRAINT uq_quest_reward_definition_quest_sort UNIQUE (quest_id, sort_order),
	CONSTRAINT chk_quest_reward_kind CHECK (((kind)::text = ANY ((ARRAY['XP'::character varying, 'GOLD'::character varying, 'ITEM'::character varying, 'UNLOCK'::character varying])::text[]))),
	CONSTRAINT chk_quest_reward_amount CHECK ((amount >= 0)),
	CONSTRAINT fk_quest_reward_definition_quest FOREIGN KEY (quest_id) REFERENCES public.quest_definition(id)
);

CREATE TABLE public.character_quest (
	id uuid NOT NULL,
	character_id uuid NOT NULL,
	quest_id uuid NOT NULL,
	status character varying(32) NOT NULL,
	accepted_at timestamp with time zone NOT NULL,
	ready_at timestamp with time zone,
	completed_at timestamp with time zone,
	rewards_applied boolean NOT NULL,
	CONSTRAINT character_quest_pkey PRIMARY KEY (id),
	CONSTRAINT uq_character_quest UNIQUE (character_id, quest_id),
	CONSTRAINT chk_character_quest_status CHECK (((status)::text = ANY ((ARRAY['ACTIVE'::character varying, 'READY_TO_TURN_IN'::character varying, 'COMPLETED'::character varying])::text[]))),
	CONSTRAINT fk_character_quest_character FOREIGN KEY (character_id) REFERENCES public.characters(id),
	CONSTRAINT fk_character_quest_definition FOREIGN KEY (quest_id) REFERENCES public.quest_definition(id)
);

CREATE TABLE public.character_quest_objective (
	id uuid NOT NULL,
	character_quest_id uuid NOT NULL,
	objective_id uuid NOT NULL,
	current_amount integer NOT NULL,
	completed boolean NOT NULL,
	CONSTRAINT character_quest_objective_pkey PRIMARY KEY (id),
	CONSTRAINT uq_character_quest_objective UNIQUE (character_quest_id, objective_id),
	CONSTRAINT chk_character_quest_objective_amount CHECK ((current_amount >= 0)),
	CONSTRAINT fk_character_quest_objective_quest FOREIGN KEY (character_quest_id) REFERENCES public.character_quest(id),
	CONSTRAINT fk_character_quest_objective_definition FOREIGN KEY (objective_id) REFERENCES public.quest_objective_definition(id)
);

CREATE TABLE public.character_quest_track (
	id uuid NOT NULL,
	character_id uuid NOT NULL,
	quest_id uuid NOT NULL,
	sort_order integer NOT NULL,
	CONSTRAINT character_quest_track_pkey PRIMARY KEY (id),
	CONSTRAINT uq_character_quest_track UNIQUE (character_id, quest_id),
	CONSTRAINT fk_character_quest_track_character FOREIGN KEY (character_id) REFERENCES public.characters(id),
	CONSTRAINT fk_character_quest_track_definition FOREIGN KEY (quest_id) REFERENCES public.quest_definition(id)
);

CREATE TABLE public.character_quest_progress_source (
	id uuid NOT NULL,
	character_id uuid NOT NULL,
	source_kind character varying(32) NOT NULL,
	source_id character varying(128) NOT NULL,
	CONSTRAINT character_quest_progress_source_pkey PRIMARY KEY (id),
	CONSTRAINT uq_character_quest_progress_source UNIQUE (character_id, source_kind, source_id),
	CONSTRAINT fk_character_quest_progress_source_character FOREIGN KEY (character_id) REFERENCES public.characters(id)
);

CREATE TABLE public.character_unlocks (
	id uuid NOT NULL,
	character_id uuid NOT NULL,
	unlock_code character varying(64) NOT NULL,
	granted_at timestamp with time zone NOT NULL,
	CONSTRAINT character_unlocks_pkey PRIMARY KEY (id),
	CONSTRAINT uq_character_unlocks UNIQUE (character_id, unlock_code),
	CONSTRAINT chk_character_unlocks_code CHECK (((unlock_code)::text = ANY ((ARRAY[
		'TRAINING_GROUNDS'::character varying,
		'LOST_CARAVAN_DUNGEON'::character varying,
		'RANKED_ARENA'::character varying
	])::text[]))),
	CONSTRAINT fk_character_unlocks_character FOREIGN KEY (character_id) REFERENCES public.characters(id)
);

CREATE INDEX idx_character_quest_character_status ON public.character_quest USING btree (character_id, status);
CREATE INDEX idx_character_quest_track_character ON public.character_quest_track USING btree (character_id, sort_order);
CREATE INDEX idx_npc_definitions_location ON public.npc_definitions USING btree (location_code);

INSERT INTO public.npc_definitions (id, code, name, title, description, greeting, portrait_code, location_code, merchant_code, interactions, sort_order) VALUES
	('e0000000-0000-4000-8000-000000000001', 'MILITIA_OFFICER', 'Watch-Sergeant Bren', 'Militia officer', 'A grey-cloaked watch sergeant who posts notices and takes reports in City Square.', 'The watch has work, if you can follow a notice.', 'militia-officer', 'CITY_SQUARE', NULL, 'TALK,QUEST,QUEST_TURN_IN', 10),
	('e0000000-0000-4000-8000-000000000002', 'EDRIC_VARN', 'Edric Varn', 'Weaponsmith', 'Greyhaven''s weaponsmith. He sells honest steel and has little patience for rust.', 'If you mean to leave the walls, buy something that will come back with you.', 'edric-varn', 'MARKET', 'WEAPONSMITH', 'TALK,SHOP,QUEST', 20),
	('e0000000-0000-4000-8000-000000000003', 'MARA_HELDEN', 'Mara Helden', 'Armorer', 'An armorer who fits leather and mail for people who expect to be hit.', 'Leather first. Plate when you have earned the weight.', 'mara-helden', 'MARKET', 'ARMORER', 'TALK,SHOP', 30),
	('e0000000-0000-4000-8000-000000000004', 'SISTER_CALIA', 'Sister Calia', 'Apothecary', 'The apothecary. Her flasks keep more militia on their feet than sermons do.', 'Drink when the bar turns red, not after.', 'sister-calia', 'MARKET', 'APOTHECARY', 'TALK,SHOP', 40),
	('e0000000-0000-4000-8000-000000000005', 'TOMAS_REED', 'Tomas Reed', 'General goods', 'A general merchant who sells what the road forgets.', 'Need a spare? I have one. Maybe two.', 'tomas-reed', 'MARKET', 'GENERAL', 'TALK,SHOP', 50),
	('e0000000-0000-4000-8000-000000000006', 'PATROL_SERGEANT', 'Patrol-Sergeant Ohlan', 'Forest patrol', 'Arranges Forest Patrols from the tavern when the timber road goes quiet.', 'If you cannot walk the woods tonight, send someone who can.', 'patrol-sergeant', 'TAVERN', NULL, 'TALK,QUEST', 60),
	('e0000000-0000-4000-8000-000000000007', 'DRILL_INSTRUCTOR', 'Drill-Master Vesk', 'Arena instructor', 'Runs the yard drills. Ranked steel can wait.', 'Show me you can stand. The ladder comes later.', 'drill-instructor', 'ARENA', NULL, 'TALK,QUEST', 70);

INSERT INTO public.quest_definition (id, code, name, description, category, recommended_level, min_level, start_npc_code, turn_in_npc_code, prerequisite_quest_code, next_quest_code, repeatable, sort_order, offer_text, progress_text, complete_text) VALUES
	('e0000000-0000-4000-8000-000000000101', 'QST_MILITIA_NOTICE', 'Militia Notice', 'Old Town is restless. The watch needs eyes — walk the alleys and put down a street thug, then report back.', 'MAIN', 1, 1, 'MILITIA_OFFICER', 'MILITIA_OFFICER', NULL, 'QST_ARM_THE_WATCH', false, 10,
		'Old Town is restless. Walk the alleys, deal with a street thug, and come back alive.',
		'The notice still stands. Old Town, then a thug, then my desk.',
		'You came back. That is more than some manage. Take this and arm yourself before you leave the walls.'),
	('e0000000-0000-4000-8000-000000000102', 'QST_ARM_THE_WATCH', 'Arm the Watch', 'Visit Edric Varn in the Market and hear what a real weapon costs.', 'MAIN', 2, 1, 'EDRIC_VARN', 'EDRIC_VARN', 'QST_MILITIA_NOTICE', NULL, false, 20,
		'Rust is a habit. Come to the Market and we will talk steel.',
		'I am still here. The stall is not hard to find.',
		'Good. Wear it. The timber road does not care what you meant to buy.');

INSERT INTO public.quest_objective_definition (id, quest_id, sort_order, type, target_code, required_amount, display_text, consume_on_turn_in) VALUES
	('e0000000-0000-4000-8000-000000000111', 'e0000000-0000-4000-8000-000000000101', 1, 'VISIT_LOCATION', 'OLD_TOWN', 1, 'Reach Old Town', false),
	('e0000000-0000-4000-8000-000000000112', 'e0000000-0000-4000-8000-000000000101', 2, 'KILL', 'STREET_THUG', 1, 'Defeat a Street Thug', false),
	('e0000000-0000-4000-8000-000000000121', 'e0000000-0000-4000-8000-000000000102', 1, 'VISIT_LOCATION', 'MARKET', 1, 'Reach the Market', false),
	('e0000000-0000-4000-8000-000000000122', 'e0000000-0000-4000-8000-000000000102', 2, 'TALK_TO_NPC', 'EDRIC_VARN', 1, 'Speak with Edric Varn', false);

INSERT INTO public.quest_reward_definition (id, quest_id, kind, amount, item_code, unlock_code, sort_order) VALUES
	('e0000000-0000-4000-8000-000000000131', 'e0000000-0000-4000-8000-000000000101', 'XP', 40, NULL, NULL, 1),
	('e0000000-0000-4000-8000-000000000132', 'e0000000-0000-4000-8000-000000000101', 'GOLD', 15, NULL, NULL, 2),
	('e0000000-0000-4000-8000-000000000133', 'e0000000-0000-4000-8000-000000000101', 'ITEM', 1, 'HEALING_POTION', NULL, 3);
