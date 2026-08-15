-- Phase 3 database baseline.
-- Squashes incremental Flyway V1–V35 into a single fresh-schema migration.
-- Historical scripts are archived at classpath:db/archive/phase1-phase2 (not applied).
-- Schema and reference data match the post-V35 state. Local databases must be recreated.

--
-- Schema
--

--
--



SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

SET default_tablespace = '';

SET default_table_access_method = heap;

--
-- Name: accounts; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.accounts (
    id uuid NOT NULL,
    email character varying(255) NOT NULL,
    password_hash character varying(255) NOT NULL,
    created_at timestamp with time zone NOT NULL,
    updated_at timestamp with time zone NOT NULL
);


--
-- Name: activity_entries; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.activity_entries (
    id uuid NOT NULL,
    character_id uuid NOT NULL,
    type character varying(64) NOT NULL,
    message character varying(512) NOT NULL,
    created_at timestamp with time zone NOT NULL,
    read_at timestamp with time zone,
    CONSTRAINT chk_activity_entries_message CHECK ((char_length(TRIM(BOTH FROM message)) > 0)),
    CONSTRAINT chk_activity_entries_type CHECK (((type)::text = ANY ((ARRAY['COMBAT_VICTORY'::character varying, 'LEVEL_UP'::character varying, 'ITEM_FOUND'::character varying, 'EXPEDITION_COMPLETED'::character varying, 'EXPEDITION_CLAIMED'::character varying, 'MARKET_SOLD'::character varying, 'MARKET_BOUGHT'::character varying, 'MARKET_CANCELLED'::character varying, 'MASTERY_UNLOCK'::character varying, 'TECHNIQUE_UNLOCK'::character varying, 'ARENA_VICTORY'::character varying, 'ARENA_DEFEAT'::character varying, 'DUEL_RESULT'::character varying, 'CRAFTING_STARTED'::character varying, 'CRAFTING_CLAIMED'::character varying, 'PROFESSION_RANK_UP'::character varying, 'ITEM_SALVAGED'::character varying, 'MARKET_LISTING_FEE'::character varying, 'MARKET_SALE'::character varying, 'BUY_ORDER_CREATED'::character varying, 'BUY_ORDER_FILLED'::character varying, 'BUY_ORDER_CANCELLED'::character varying])::text[])))
);


--
-- Name: affix_definitions; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.affix_definitions (
    code character varying(64) NOT NULL,
    kind character varying(16) NOT NULL,
    display_name character varying(64) NOT NULL,
    stat character varying(32) NOT NULL,
    magnitude_min integer NOT NULL,
    magnitude_max integer NOT NULL,
    allowed_item_types character varying(128) DEFAULT ''::character varying NOT NULL,
    allowed_equipment_slots character varying(256) DEFAULT ''::character varying NOT NULL,
    allowed_weapon_families character varying(128) DEFAULT ''::character varying NOT NULL,
    allowed_armor_categories character varying(64) DEFAULT ''::character varying NOT NULL,
    CONSTRAINT chk_affix_definitions_kind CHECK (((kind)::text = ANY ((ARRAY['PREFIX'::character varying, 'SUFFIX'::character varying])::text[]))),
    CONSTRAINT chk_affix_definitions_magnitude CHECK (((magnitude_min >= 1) AND (magnitude_max >= magnitude_min))),
    CONSTRAINT chk_affix_definitions_stat CHECK (((stat)::text = ANY ((ARRAY['DAMAGE_PERCENT'::character varying, 'ACCURACY'::character varying, 'CRIT_CHANCE'::character varying, 'ARMOR'::character varying, 'STRENGTH'::character varying, 'AGILITY'::character varying, 'ENDURANCE'::character varying, 'PERCEPTION'::character varying, 'DODGE'::character varying, 'STAMINA_COST'::character varying])::text[])))
);


--
-- Name: arena_defense_profiles; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.arena_defense_profiles (
    character_id uuid NOT NULL,
    preferred_action character varying(32) NOT NULL,
    preferred_technique_code character varying(64),
    heal_when_hp_percent_below integer NOT NULL,
    defend_when_stamina_percent_below integer CONSTRAINT arena_defense_profiles_defend_when_stamina_percent_bel_not_null NOT NULL,
    finisher_when_enemy_hp_percent_below integer CONSTRAINT arena_defense_profiles_finisher_when_enemy_hp_percent__not_null NOT NULL,
    finisher_technique_code character varying(64),
    updated_at timestamp with time zone NOT NULL
);


--
-- Name: character_professions; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.character_professions (
    id uuid NOT NULL,
    character_id uuid NOT NULL,
    profession character varying(32) NOT NULL,
    xp integer NOT NULL,
    rank integer NOT NULL,
    updated_at timestamp with time zone NOT NULL,
    CONSTRAINT chk_character_professions_profession CHECK (((profession)::text = ANY ((ARRAY['BLACKSMITH'::character varying, 'ALCHEMIST'::character varying, 'HUNTER'::character varying])::text[]))),
    CONSTRAINT chk_character_professions_rank CHECK (((rank >= 1) AND (rank <= 10))),
    CONSTRAINT chk_character_professions_xp CHECK ((xp >= 0))
);


--
-- Name: character_techniques; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.character_techniques (
    id uuid NOT NULL,
    character_id uuid NOT NULL,
    technique_code character varying(64) NOT NULL,
    unlocked_at timestamp with time zone NOT NULL
);


--
-- Name: character_unique_drops; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.character_unique_drops (
    id uuid NOT NULL,
    character_id uuid NOT NULL,
    item_code character varying(64) NOT NULL,
    granted_at timestamp with time zone NOT NULL
);


--
-- Name: characters; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.characters (
    id uuid NOT NULL,
    account_id uuid NOT NULL,
    name character varying(64) NOT NULL,
    level integer NOT NULL,
    experience integer NOT NULL,
    strength integer NOT NULL,
    agility integer NOT NULL,
    endurance integer NOT NULL,
    perception integer NOT NULL,
    current_health integer NOT NULL,
    max_health integer NOT NULL,
    current_stamina integer NOT NULL,
    max_stamina integer NOT NULL,
    gold integer NOT NULL,
    current_location_id uuid NOT NULL,
    created_at timestamp with time zone NOT NULL,
    updated_at timestamp with time zone NOT NULL,
    unspent_attribute_points integer DEFAULT 0 NOT NULL,
    last_recovery_at timestamp with time zone NOT NULL,
    arena_rating integer DEFAULT 1000 NOT NULL,
    arena_marks integer DEFAULT 0 NOT NULL,
    CONSTRAINT chk_characters_agility CHECK ((agility >= 1)),
    CONSTRAINT chk_characters_current_health CHECK ((current_health >= 0)),
    CONSTRAINT chk_characters_current_stamina CHECK ((current_stamina >= 0)),
    CONSTRAINT chk_characters_endurance CHECK ((endurance >= 1)),
    CONSTRAINT chk_characters_experience CHECK ((experience >= 0)),
    CONSTRAINT chk_characters_gold CHECK ((gold >= 0)),
    CONSTRAINT chk_characters_level CHECK (((level >= 1) AND (level <= 30))),
    CONSTRAINT chk_characters_max_health CHECK ((max_health >= 1)),
    CONSTRAINT chk_characters_max_stamina CHECK ((max_stamina >= 1)),
    CONSTRAINT chk_characters_perception CHECK ((perception >= 1)),
    CONSTRAINT chk_characters_strength CHECK ((strength >= 1)),
    CONSTRAINT chk_characters_unspent_attribute_points CHECK ((unspent_attribute_points >= 0)),
    CONSTRAINT ck_characters_arena_marks_non_negative CHECK ((arena_marks >= 0)),
    CONSTRAINT ck_characters_arena_rating_non_negative CHECK ((arena_rating >= 0))
);


--
-- Name: chat_messages; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.chat_messages (
    id uuid NOT NULL,
    character_id uuid NOT NULL,
    body character varying(500) NOT NULL,
    created_at timestamp with time zone NOT NULL,
    CONSTRAINT chk_chat_messages_body CHECK ((char_length(TRIM(BOTH FROM body)) > 0))
);


--
-- Name: combat_events; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.combat_events (
    id uuid NOT NULL,
    session_id uuid NOT NULL,
    round_number integer NOT NULL,
    sequence_number integer NOT NULL,
    event_type character varying(64) NOT NULL,
    message text NOT NULL,
    created_at timestamp with time zone NOT NULL,
    CONSTRAINT chk_combat_events_round CHECK ((round_number >= 0)),
    CONSTRAINT chk_combat_events_sequence CHECK ((sequence_number >= 1))
);


--
-- Name: combat_reward_items; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.combat_reward_items (
    id uuid NOT NULL,
    session_id uuid NOT NULL,
    item_definition_id uuid NOT NULL,
    quantity integer NOT NULL,
    rarity character varying(32),
    rolled_weapon_damage integer,
    rolled_armor_value integer,
    rolled_affixes text DEFAULT ''::text NOT NULL,
    CONSTRAINT chk_combat_reward_items_quantity CHECK ((quantity >= 1)),
    CONSTRAINT chk_combat_reward_items_rarity CHECK (((rarity IS NULL) OR ((rarity)::text = ANY ((ARRAY['COMMON'::character varying, 'UNCOMMON'::character varying, 'RARE'::character varying, 'EPIC'::character varying])::text[]))))
);


--
-- Name: combat_sessions; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.combat_sessions (
    id uuid NOT NULL,
    encounter_id uuid NOT NULL,
    character_id uuid NOT NULL,
    monster_definition_id uuid NOT NULL,
    status character varying(32) NOT NULL,
    round_number integer NOT NULL,
    player_health integer NOT NULL,
    player_stamina integer NOT NULL,
    enemy_health integer NOT NULL,
    rewards_applied boolean DEFAULT false NOT NULL,
    xp_awarded integer,
    gold_awarded integer,
    version bigint DEFAULT 0 NOT NULL,
    created_at timestamp with time zone NOT NULL,
    updated_at timestamp with time zone NOT NULL,
    outcome_acknowledged boolean DEFAULT true NOT NULL,
    reward_plan_created boolean DEFAULT false NOT NULL,
    planned_xp integer,
    planned_gold integer,
    reward_previous_level integer,
    reward_new_level integer,
    rules_version integer DEFAULT 2 NOT NULL,
    enemy_stamina integer DEFAULT 0 NOT NULL,
    enemy_max_stamina integer DEFAULT 0 NOT NULL,
    snap_enemy_armor integer DEFAULT 0 NOT NULL,
    snap_enemy_accuracy integer DEFAULT 0 NOT NULL,
    snap_enemy_dodge integer DEFAULT 0 NOT NULL,
    snap_enemy_critical_chance integer DEFAULT 0 NOT NULL,
    snap_enemy_damage_min integer DEFAULT 0 NOT NULL,
    snap_enemy_damage_max integer DEFAULT 0 NOT NULL,
    snap_ai_archetype character varying(16),
    snap_signature_status character varying(32),
    weapon_family character varying(16),
    technique_codes character varying(256),
    stamina_cost_reduction integer DEFAULT 0 NOT NULL,
    last_enemy_missed boolean DEFAULT false NOT NULL,
    last_player_guarded boolean DEFAULT false NOT NULL,
    snap_monster_tier character varying(16),
    CONSTRAINT chk_combat_sessions_ai_archetype CHECK (((snap_ai_archetype IS NULL) OR ((snap_ai_archetype)::text = ANY ((ARRAY['AGGRESSIVE'::character varying, 'DEFENSIVE'::character varying, 'CONTROL'::character varying, 'ASSASSIN'::character varying, 'ARMORED'::character varying, 'BERSERKER'::character varying, 'SHIELDED'::character varying, 'MARKSMAN'::character varying])::text[])))),
    CONSTRAINT chk_combat_sessions_enemy_health CHECK ((enemy_health >= 0)),
    CONSTRAINT chk_combat_sessions_monster_tier CHECK (((snap_monster_tier IS NULL) OR ((snap_monster_tier)::text = ANY ((ARRAY['NORMAL'::character varying, 'ELITE'::character varying, 'MINI_BOSS'::character varying, 'BOSS'::character varying])::text[])))),
    CONSTRAINT chk_combat_sessions_player_health CHECK ((player_health >= 0)),
    CONSTRAINT chk_combat_sessions_player_stamina CHECK ((player_stamina >= 0)),
    CONSTRAINT chk_combat_sessions_reward_plan CHECK ((((reward_plan_created = false) AND (planned_xp IS NULL) AND (planned_gold IS NULL)) OR ((reward_plan_created = true) AND (planned_xp IS NOT NULL) AND (planned_xp >= 0) AND (planned_gold IS NOT NULL) AND (planned_gold >= 0)))),
    CONSTRAINT chk_combat_sessions_rewards CHECK ((((rewards_applied = false) AND (xp_awarded IS NULL) AND (gold_awarded IS NULL)) OR ((rewards_applied = true) AND (xp_awarded IS NOT NULL) AND (gold_awarded IS NOT NULL)))),
    CONSTRAINT chk_combat_sessions_round CHECK ((round_number >= 0)),
    CONSTRAINT chk_combat_sessions_rules_version CHECK ((rules_version = ANY (ARRAY[1, 2]))),
    CONSTRAINT chk_combat_sessions_status CHECK (((status)::text = ANY ((ARRAY['ACTIVE'::character varying, 'PLAYER_WON'::character varying, 'PLAYER_LOST'::character varying, 'PLAYER_ESCAPED'::character varying])::text[]))),
    CONSTRAINT chk_combat_sessions_weapon_family CHECK (((weapon_family IS NULL) OR ((weapon_family)::text = ANY ((ARRAY['SWORD'::character varying, 'AXE'::character varying, 'MACE'::character varying, 'DAGGER'::character varying, 'BOW'::character varying])::text[]))))
);


--
-- Name: combat_status_effects; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.combat_status_effects (
    id uuid NOT NULL,
    session_id uuid NOT NULL,
    target character varying(8) NOT NULL,
    status_type character varying(32) NOT NULL,
    stacks integer NOT NULL,
    remaining_rounds integer NOT NULL,
    CONSTRAINT chk_combat_status_rounds CHECK ((remaining_rounds >= 0)),
    CONSTRAINT chk_combat_status_stacks CHECK ((stacks >= 0)),
    CONSTRAINT chk_combat_status_target CHECK (((target)::text = ANY ((ARRAY['PLAYER'::character varying, 'ENEMY'::character varying])::text[]))),
    CONSTRAINT chk_combat_status_type CHECK (((status_type)::text = ANY ((ARRAY['BLEED'::character varying, 'POISON'::character varying, 'STUN'::character varying, 'ARMOR_BREAK'::character varying, 'OFF_BALANCE'::character varying, 'GUARDED'::character varying, 'STUN_IMMUNITY'::character varying])::text[])))
);


--
-- Name: combat_technique_definitions; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.combat_technique_definitions (
    code character varying(64) NOT NULL,
    display_name character varying(64) NOT NULL,
    description text NOT NULL,
    weapon_family character varying(16) NOT NULL,
    unlock_mastery_level integer NOT NULL,
    kind character varying(16) NOT NULL,
    effect_code character varying(64) NOT NULL,
    stamina_cost integer NOT NULL,
    accuracy_modifier integer NOT NULL,
    damage_percent_modifier integer NOT NULL,
    applies_status character varying(32),
    status_stacks integer NOT NULL,
    status_duration_rounds integer NOT NULL,
    tags character varying(128) NOT NULL,
    CONSTRAINT chk_technique_kind CHECK (((kind)::text = ANY ((ARRAY['ACTIVE'::character varying, 'PASSIVE'::character varying])::text[]))),
    CONSTRAINT chk_technique_passive_level CHECK (((((kind)::text = 'PASSIVE'::text) AND (unlock_mastery_level = 10)) OR (((kind)::text = 'ACTIVE'::text) AND (unlock_mastery_level = ANY (ARRAY[2, 4, 6, 8]))))),
    CONSTRAINT chk_technique_stamina_cost CHECK ((stamina_cost >= 0)),
    CONSTRAINT chk_technique_status_duration CHECK ((status_duration_rounds >= 0)),
    CONSTRAINT chk_technique_status_stacks CHECK ((status_stacks >= 0)),
    CONSTRAINT chk_technique_unlock_level CHECK ((unlock_mastery_level = ANY (ARRAY[2, 4, 6, 8, 10]))),
    CONSTRAINT chk_technique_weapon_family CHECK (((weapon_family)::text = ANY ((ARRAY['SWORD'::character varying, 'AXE'::character varying, 'MACE'::character varying, 'DAGGER'::character varying, 'BOW'::character varying])::text[])))
);


--
-- Name: crafting_jobs; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.crafting_jobs (
    id uuid NOT NULL,
    character_id uuid NOT NULL,
    recipe_id uuid NOT NULL,
    profession character varying(32) NOT NULL,
    status character varying(32) NOT NULL,
    started_at timestamp with time zone NOT NULL,
    completes_at timestamp with time zone NOT NULL,
    claimed_at timestamp with time zone,
    result_generated boolean NOT NULL,
    output_item_definition_id uuid NOT NULL,
    output_item_code character varying(64) NOT NULL,
    output_quantity integer NOT NULL,
    rarity character varying(32),
    rolled_weapon_damage integer,
    rolled_armor_value integer,
    rolled_affixes text,
    profession_xp_planned integer NOT NULL,
    version bigint NOT NULL,
    created_at timestamp with time zone NOT NULL,
    updated_at timestamp with time zone NOT NULL,
    CONSTRAINT chk_crafting_jobs_output_qty CHECK ((output_quantity >= 1)),
    CONSTRAINT chk_crafting_jobs_profession CHECK (((profession)::text = ANY ((ARRAY['BLACKSMITH'::character varying, 'ALCHEMIST'::character varying, 'HUNTER'::character varying])::text[]))),
    CONSTRAINT chk_crafting_jobs_status CHECK (((status)::text = ANY ((ARRAY['ACTIVE'::character varying, 'COMPLETED'::character varying, 'CLAIMED'::character varying])::text[]))),
    CONSTRAINT chk_crafting_jobs_xp CHECK ((profession_xp_planned >= 0))
);


--
-- Name: crafting_recipe_inputs; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.crafting_recipe_inputs (
    id uuid NOT NULL,
    recipe_id uuid NOT NULL,
    item_definition_id uuid NOT NULL,
    quantity integer NOT NULL,
    CONSTRAINT chk_crafting_recipe_inputs_qty CHECK ((quantity >= 1))
);


--
-- Name: crafting_recipes; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.crafting_recipes (
    id uuid NOT NULL,
    code character varying(64) NOT NULL,
    name character varying(128) NOT NULL,
    profession character varying(32) NOT NULL,
    required_profession_rank integer NOT NULL,
    required_character_level integer NOT NULL,
    gold_cost integer NOT NULL,
    duration_seconds integer NOT NULL,
    output_item_definition_id uuid NOT NULL,
    output_quantity integer NOT NULL,
    min_rarity character varying(32),
    max_rarity character varying(32),
    profession_xp integer NOT NULL,
    CONSTRAINT chk_crafting_recipes_duration CHECK ((duration_seconds >= 1)),
    CONSTRAINT chk_crafting_recipes_gold CHECK ((gold_cost >= 0)),
    CONSTRAINT chk_crafting_recipes_level CHECK ((required_character_level >= 1)),
    CONSTRAINT chk_crafting_recipes_output_qty CHECK ((output_quantity >= 1)),
    CONSTRAINT chk_crafting_recipes_profession CHECK (((profession)::text = ANY ((ARRAY['BLACKSMITH'::character varying, 'ALCHEMIST'::character varying, 'HUNTER'::character varying])::text[]))),
    CONSTRAINT chk_crafting_recipes_rank CHECK (((required_profession_rank >= 1) AND (required_profession_rank <= 10))),
    CONSTRAINT chk_crafting_recipes_rarity CHECK ((((min_rarity IS NULL) AND (max_rarity IS NULL)) OR (((min_rarity)::text = ANY ((ARRAY['COMMON'::character varying, 'UNCOMMON'::character varying, 'RARE'::character varying, 'EPIC'::character varying])::text[])) AND ((max_rarity)::text = ANY ((ARRAY['COMMON'::character varying, 'UNCOMMON'::character varying, 'RARE'::character varying, 'EPIC'::character varying])::text[]))))),
    CONSTRAINT chk_crafting_recipes_xp CHECK ((profession_xp >= 0))
);


--
-- Name: dungeon_definitions; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.dungeon_definitions (
    id uuid NOT NULL,
    code character varying(64) NOT NULL,
    name character varying(128) NOT NULL,
    entrance_location_id uuid NOT NULL
);


--
-- Name: dungeon_room_edges; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.dungeon_room_edges (
    id uuid NOT NULL,
    from_room_id uuid NOT NULL,
    to_room_id uuid NOT NULL,
    edge_code character varying(32) NOT NULL,
    skip_room_code character varying(64),
    CONSTRAINT chk_dungeon_edges_not_self CHECK ((from_room_id <> to_room_id))
);


--
-- Name: dungeon_rooms; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.dungeon_rooms (
    id uuid NOT NULL,
    dungeon_id uuid NOT NULL,
    code character varying(64) NOT NULL,
    name character varying(128) NOT NULL,
    description text NOT NULL,
    room_kind character varying(16) NOT NULL,
    monster_definition_id uuid,
    sort_order integer NOT NULL,
    CONSTRAINT chk_dungeon_rooms_kind CHECK (((room_kind)::text = ANY ((ARRAY['ENTRANCE'::character varying, 'COMBAT'::character varying, 'CHOICE'::character varying, 'OPTIONAL'::character varying, 'BOSS'::character varying])::text[]))),
    CONSTRAINT chk_dungeon_rooms_monster CHECK (((((room_kind)::text = ANY ((ARRAY['COMBAT'::character varying, 'OPTIONAL'::character varying, 'BOSS'::character varying])::text[])) AND (monster_definition_id IS NOT NULL)) OR (((room_kind)::text = ANY ((ARRAY['ENTRANCE'::character varying, 'CHOICE'::character varying])::text[])) AND (monster_definition_id IS NULL))))
);


--
-- Name: dungeon_run_rooms; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.dungeon_run_rooms (
    id uuid NOT NULL,
    run_id uuid NOT NULL,
    room_code character varying(64) NOT NULL,
    state character varying(16) NOT NULL,
    CONSTRAINT chk_dungeon_run_room_state CHECK (((state)::text = ANY ((ARRAY['LOCKED'::character varying, 'AVAILABLE'::character varying, 'CLEARED'::character varying, 'SKIPPED'::character varying])::text[])))
);


--
-- Name: dungeon_runs; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.dungeon_runs (
    id uuid NOT NULL,
    character_id uuid NOT NULL,
    dungeon_id uuid NOT NULL,
    status character varying(16) NOT NULL,
    paused boolean DEFAULT false NOT NULL,
    current_room_code character varying(64) NOT NULL,
    chosen_branch character varying(32),
    unique_reward_granted boolean DEFAULT false NOT NULL,
    created_at timestamp with time zone NOT NULL,
    updated_at timestamp with time zone NOT NULL,
    CONSTRAINT chk_dungeon_runs_status CHECK (((status)::text = ANY ((ARRAY['ACTIVE'::character varying, 'COMPLETED'::character varying, 'ABANDONED'::character varying])::text[])))
);


--
-- Name: encounters; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.encounters (
    id uuid NOT NULL,
    character_id uuid NOT NULL,
    location_id uuid NOT NULL,
    monster_definition_id uuid,
    status character varying(32) NOT NULL,
    created_at timestamp with time zone NOT NULL,
    updated_at timestamp with time zone NOT NULL,
    dungeon_run_id uuid,
    dungeon_room_code character varying(64),
    dungeon_optional boolean DEFAULT false NOT NULL,
    CONSTRAINT chk_encounters_monster_presence CHECK (((((status)::text = 'AVAILABLE'::text) AND (monster_definition_id IS NOT NULL)) OR ((status)::text = ANY ((ARRAY['COMBAT_STARTED'::character varying, 'RESOLVED'::character varying, 'EXPIRED'::character varying])::text[])))),
    CONSTRAINT chk_encounters_status CHECK (((status)::text = ANY ((ARRAY['AVAILABLE'::character varying, 'COMBAT_STARTED'::character varying, 'RESOLVED'::character varying, 'EXPIRED'::character varying])::text[])))
);


--
-- Name: equipment; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.equipment (
    id uuid NOT NULL,
    character_id uuid NOT NULL,
    slot character varying(16) NOT NULL,
    item_instance_id uuid NOT NULL,
    CONSTRAINT chk_equipment_slot CHECK (((slot)::text = ANY ((ARRAY['HEAD'::character varying, 'CHEST'::character varying, 'HANDS'::character varying, 'LEGS'::character varying, 'FEET'::character varying, 'MAIN_HAND'::character varying, 'OFF_HAND'::character varying, 'AMULET'::character varying, 'RING'::character varying])::text[])))
);


--
-- Name: expedition_reward_items; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.expedition_reward_items (
    id uuid NOT NULL,
    expedition_id uuid NOT NULL,
    item_definition_id uuid NOT NULL,
    quantity integer NOT NULL,
    rarity character varying(32),
    rolled_weapon_damage integer,
    rolled_armor_value integer,
    rolled_affixes text DEFAULT ''::text NOT NULL,
    CONSTRAINT chk_expedition_reward_items_quantity CHECK ((quantity >= 1)),
    CONSTRAINT chk_expedition_reward_items_rarity CHECK (((rarity IS NULL) OR ((rarity)::text = ANY ((ARRAY['COMMON'::character varying, 'UNCOMMON'::character varying, 'RARE'::character varying, 'EPIC'::character varying])::text[]))))
);


--
-- Name: expeditions; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.expeditions (
    id uuid NOT NULL,
    character_id uuid NOT NULL,
    expedition_type character varying(64) NOT NULL,
    strategy character varying(32) NOT NULL,
    status character varying(32) NOT NULL,
    started_at timestamp with time zone NOT NULL,
    completes_at timestamp with time zone NOT NULL,
    claimed_at timestamp with time zone,
    result_generated boolean DEFAULT false NOT NULL,
    planned_xp integer,
    planned_gold integer,
    planned_injury integer,
    xp_awarded integer,
    gold_awarded integer,
    injury_applied integer,
    version bigint DEFAULT 0 NOT NULL,
    created_at timestamp with time zone NOT NULL,
    updated_at timestamp with time zone NOT NULL,
    CONSTRAINT chk_expeditions_awards CHECK (((((status)::text <> 'CLAIMED'::text) AND (xp_awarded IS NULL) AND (gold_awarded IS NULL) AND (injury_applied IS NULL)) OR (((status)::text = 'CLAIMED'::text) AND (xp_awarded IS NOT NULL) AND (xp_awarded >= 0) AND (gold_awarded IS NOT NULL) AND (gold_awarded >= 0) AND (injury_applied IS NOT NULL) AND (injury_applied >= 0)))),
    CONSTRAINT chk_expeditions_claimed_at CHECK (((((status)::text <> 'CLAIMED'::text) AND (claimed_at IS NULL)) OR (((status)::text = 'CLAIMED'::text) AND (claimed_at IS NOT NULL)))),
    CONSTRAINT chk_expeditions_completion_window CHECK ((completes_at > started_at)),
    CONSTRAINT chk_expeditions_result_plan CHECK ((((result_generated = false) AND (planned_xp IS NULL) AND (planned_gold IS NULL) AND (planned_injury IS NULL)) OR ((result_generated = true) AND (planned_xp IS NOT NULL) AND (planned_xp >= 0) AND (planned_gold IS NOT NULL) AND (planned_gold >= 0) AND (planned_injury IS NOT NULL) AND (planned_injury >= 0)))),
    CONSTRAINT chk_expeditions_status CHECK (((status)::text = ANY ((ARRAY['ACTIVE'::character varying, 'COMPLETED'::character varying, 'CLAIMED'::character varying])::text[]))),
    CONSTRAINT chk_expeditions_strategy CHECK (((strategy)::text = ANY ((ARRAY['CAUTIOUS'::character varying, 'BALANCED'::character varying, 'AGGRESSIVE'::character varying])::text[]))),
    CONSTRAINT chk_expeditions_type CHECK (((expedition_type)::text = 'FOREST_PATROL'::text))
);


--
-- Name: game_telemetry_events; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.game_telemetry_events (
    id uuid NOT NULL,
    occurred_at timestamp with time zone NOT NULL,
    category character varying(32) NOT NULL,
    event_type character varying(64) NOT NULL,
    character_id uuid,
    payload jsonb NOT NULL,
    CONSTRAINT chk_game_telemetry_events_category CHECK (((category)::text = ANY ((ARRAY['PROGRESSION'::character varying, 'COMBAT'::character varying, 'PVP'::character varying, 'ECONOMY'::character varying, 'CRAFTING'::character varying])::text[]))),
    CONSTRAINT chk_game_telemetry_events_payload_object CHECK ((jsonb_typeof(payload) = 'object'::text))
);


--
-- Name: item_definition_modifiers; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.item_definition_modifiers (
    id uuid NOT NULL,
    item_definition_id uuid NOT NULL,
    stat character varying(32) NOT NULL,
    magnitude integer NOT NULL,
    CONSTRAINT chk_item_definition_modifiers_magnitude CHECK ((magnitude <> 0)),
    CONSTRAINT chk_item_definition_modifiers_stat CHECK (((stat)::text = ANY ((ARRAY['DAMAGE_PERCENT'::character varying, 'ACCURACY'::character varying, 'CRIT_CHANCE'::character varying, 'ARMOR'::character varying, 'STRENGTH'::character varying, 'AGILITY'::character varying, 'ENDURANCE'::character varying, 'PERCEPTION'::character varying, 'DODGE'::character varying, 'STAMINA_COST'::character varying])::text[])))
);


--
-- Name: item_definitions; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.item_definitions (
    id uuid NOT NULL,
    code character varying(64) NOT NULL,
    name character varying(128) NOT NULL,
    description text NOT NULL,
    type character varying(32) NOT NULL,
    rarity character varying(32) NOT NULL,
    base_value integer NOT NULL,
    required_level integer NOT NULL,
    weapon_damage integer,
    armor_value integer,
    heal_amount integer,
    created_at timestamp with time zone NOT NULL,
    equipment_slot character varying(16),
    two_handed boolean DEFAULT false NOT NULL,
    weapon_family character varying(32),
    armor_category character varying(32),
    required_strength integer DEFAULT 0 NOT NULL,
    required_agility integer DEFAULT 0 NOT NULL,
    required_endurance integer DEFAULT 0 NOT NULL,
    required_perception integer DEFAULT 0 NOT NULL,
    legacy boolean DEFAULT false NOT NULL,
    CONSTRAINT chk_item_definitions_accessory_stats CHECK (((((type)::text = 'ACCESSORY'::text) AND (weapon_damage IS NULL) AND (armor_value IS NULL) AND (heal_amount IS NULL)) OR ((type)::text <> 'ACCESSORY'::text))),
    CONSTRAINT chk_item_definitions_armor_category CHECK (((((type)::text = 'ARMOR'::text) AND ((armor_category)::text = ANY ((ARRAY['LIGHT'::character varying, 'MEDIUM'::character varying, 'HEAVY'::character varying])::text[]))) OR (((type)::text <> 'ARMOR'::text) AND (armor_category IS NULL)))),
    CONSTRAINT chk_item_definitions_armor_stats CHECK (((((type)::text = 'ARMOR'::text) AND (armor_value IS NOT NULL) AND (weapon_damage IS NULL) AND (heal_amount IS NULL)) OR ((type)::text <> 'ARMOR'::text))),
    CONSTRAINT chk_item_definitions_armor_value CHECK (((armor_value IS NULL) OR (armor_value >= 0))),
    CONSTRAINT chk_item_definitions_attr_requirements CHECK (((required_strength >= 0) AND (required_agility >= 0) AND (required_endurance >= 0) AND (required_perception >= 0))),
    CONSTRAINT chk_item_definitions_base_value CHECK ((base_value >= 0)),
    CONSTRAINT chk_item_definitions_consumable_stats CHECK (((((type)::text = 'CONSUMABLE'::text) AND (heal_amount IS NOT NULL) AND (weapon_damage IS NULL) AND (armor_value IS NULL)) OR ((type)::text <> 'CONSUMABLE'::text))),
    CONSTRAINT chk_item_definitions_equipment_slot CHECK (((equipment_slot IS NULL) OR ((equipment_slot)::text = ANY ((ARRAY['HEAD'::character varying, 'CHEST'::character varying, 'HANDS'::character varying, 'LEGS'::character varying, 'FEET'::character varying, 'MAIN_HAND'::character varying, 'OFF_HAND'::character varying, 'AMULET'::character varying, 'RING'::character varying])::text[])))),
    CONSTRAINT chk_item_definitions_equippable_slot CHECK (((((type)::text = ANY ((ARRAY['WEAPON'::character varying, 'ARMOR'::character varying, 'ACCESSORY'::character varying])::text[])) AND (equipment_slot IS NOT NULL)) OR (((type)::text = ANY ((ARRAY['CONSUMABLE'::character varying, 'MATERIAL'::character varying])::text[])) AND (equipment_slot IS NULL)))),
    CONSTRAINT chk_item_definitions_heal_amount CHECK (((heal_amount IS NULL) OR (heal_amount > 0))),
    CONSTRAINT chk_item_definitions_material_stats CHECK (((((type)::text = 'MATERIAL'::text) AND (weapon_damage IS NULL) AND (armor_value IS NULL) AND (heal_amount IS NULL)) OR ((type)::text <> 'MATERIAL'::text))),
    CONSTRAINT chk_item_definitions_rarity CHECK (((rarity)::text = ANY ((ARRAY['COMMON'::character varying, 'UNCOMMON'::character varying, 'RARE'::character varying, 'EPIC'::character varying])::text[]))),
    CONSTRAINT chk_item_definitions_required_level CHECK ((required_level >= 1)),
    CONSTRAINT chk_item_definitions_two_handed CHECK (((two_handed = false) OR ((two_handed = true) AND ((type)::text = 'WEAPON'::text) AND ((equipment_slot)::text = 'MAIN_HAND'::text)))),
    CONSTRAINT chk_item_definitions_type CHECK (((type)::text = ANY ((ARRAY['WEAPON'::character varying, 'ARMOR'::character varying, 'CONSUMABLE'::character varying, 'MATERIAL'::character varying, 'ACCESSORY'::character varying])::text[]))),
    CONSTRAINT chk_item_definitions_weapon_damage CHECK (((weapon_damage IS NULL) OR (weapon_damage >= 0))),
    CONSTRAINT chk_item_definitions_weapon_family CHECK (((((type)::text = 'WEAPON'::text) AND ((weapon_family)::text = ANY ((ARRAY['SWORD'::character varying, 'AXE'::character varying, 'MACE'::character varying, 'DAGGER'::character varying, 'BOW'::character varying])::text[]))) OR (((type)::text <> 'WEAPON'::text) AND (weapon_family IS NULL)))),
    CONSTRAINT chk_item_definitions_weapon_stats CHECK (((((type)::text = 'WEAPON'::text) AND (weapon_damage IS NOT NULL) AND (armor_value IS NULL) AND (heal_amount IS NULL)) OR ((type)::text <> 'WEAPON'::text)))
);


--
-- Name: item_instance_affixes; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.item_instance_affixes (
    id uuid NOT NULL,
    item_instance_id uuid NOT NULL,
    kind character varying(16) NOT NULL,
    ordinal integer NOT NULL,
    affix_code character varying(64) NOT NULL,
    rolled_magnitude integer NOT NULL,
    CONSTRAINT chk_item_instance_affixes_kind CHECK (((kind)::text = ANY ((ARRAY['PREFIX'::character varying, 'SUFFIX'::character varying])::text[]))),
    CONSTRAINT chk_item_instance_affixes_magnitude CHECK ((rolled_magnitude >= 0)),
    CONSTRAINT chk_item_instance_affixes_ordinal CHECK ((ordinal >= 0))
);


--
-- Name: item_instances; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.item_instances (
    id uuid NOT NULL,
    item_definition_id uuid NOT NULL,
    owner_character_id uuid NOT NULL,
    quantity integer NOT NULL,
    created_at timestamp with time zone NOT NULL,
    stackable boolean NOT NULL,
    legacy boolean DEFAULT false NOT NULL,
    rarity character varying(32) NOT NULL,
    rolled_weapon_damage integer,
    rolled_armor_value integer,
    CONSTRAINT chk_item_instances_nonstackable_quantity CHECK ((stackable OR (quantity = 1))),
    CONSTRAINT chk_item_instances_quantity CHECK ((quantity >= 1)),
    CONSTRAINT chk_item_instances_rarity CHECK (((rarity)::text = ANY ((ARRAY['COMMON'::character varying, 'UNCOMMON'::character varying, 'RARE'::character varying, 'EPIC'::character varying])::text[]))),
    CONSTRAINT chk_item_instances_rolled_armor CHECK (((rolled_armor_value IS NULL) OR (rolled_armor_value >= 0))),
    CONSTRAINT chk_item_instances_rolled_weapon CHECK (((rolled_weapon_damage IS NULL) OR (rolled_weapon_damage >= 0)))
);


--
-- Name: location_connections; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.location_connections (
    id uuid NOT NULL,
    from_location_id uuid NOT NULL,
    to_location_id uuid NOT NULL,
    CONSTRAINT chk_location_connections_not_self CHECK ((from_location_id <> to_location_id))
);


--
-- Name: location_encounter_weights; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.location_encounter_weights (
    id uuid NOT NULL,
    location_id uuid NOT NULL,
    monster_definition_id uuid,
    weight integer NOT NULL,
    CONSTRAINT chk_location_encounter_weight CHECK ((weight >= 1))
);


--
-- Name: locations; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.locations (
    id uuid NOT NULL,
    code character varying(64) NOT NULL,
    name character varying(128) NOT NULL,
    description text NOT NULL,
    safety character varying(32) NOT NULL,
    region character varying(64) NOT NULL,
    created_at timestamp with time zone NOT NULL,
    recommended_level_min integer,
    recommended_level_max integer,
    CONSTRAINT chk_locations_recommended_level CHECK ((((recommended_level_min IS NULL) AND (recommended_level_max IS NULL)) OR ((recommended_level_min IS NOT NULL) AND (recommended_level_max IS NOT NULL) AND (recommended_level_min >= 1) AND (recommended_level_max >= recommended_level_min)))),
    CONSTRAINT chk_locations_safety CHECK (((safety)::text = ANY ((ARRAY['SAFE'::character varying, 'DANGEROUS'::character varying])::text[])))
);


--
-- Name: market_buy_order_fills; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.market_buy_order_fills (
    id uuid NOT NULL,
    buy_order_id uuid NOT NULL,
    seller_character_id uuid NOT NULL,
    item_instance_id uuid NOT NULL,
    quantity integer NOT NULL,
    gross_gold integer NOT NULL,
    sale_fee integer NOT NULL,
    created_at timestamp with time zone NOT NULL,
    CONSTRAINT chk_market_buy_order_fills_fee CHECK ((sale_fee >= 0)),
    CONSTRAINT chk_market_buy_order_fills_gold CHECK ((gross_gold >= 1)),
    CONSTRAINT chk_market_buy_order_fills_qty CHECK ((quantity >= 1))
);


--
-- Name: market_buy_orders; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.market_buy_orders (
    id uuid NOT NULL,
    buyer_character_id uuid NOT NULL,
    item_definition_id uuid NOT NULL,
    remaining_quantity integer NOT NULL,
    original_quantity integer NOT NULL,
    max_unit_price integer NOT NULL,
    reserved_gold integer NOT NULL,
    status character varying(32) NOT NULL,
    version bigint NOT NULL,
    created_at timestamp with time zone NOT NULL,
    updated_at timestamp with time zone NOT NULL,
    filled_at timestamp with time zone,
    cancelled_at timestamp with time zone,
    posting_fee_paid integer DEFAULT 0 NOT NULL,
    CONSTRAINT chk_market_buy_orders_original CHECK ((original_quantity >= 1)),
    CONSTRAINT chk_market_buy_orders_posting_fee CHECK ((posting_fee_paid >= 0)),
    CONSTRAINT chk_market_buy_orders_price CHECK ((max_unit_price >= 1)),
    CONSTRAINT chk_market_buy_orders_remaining CHECK ((remaining_quantity >= 0)),
    CONSTRAINT chk_market_buy_orders_reserved CHECK ((reserved_gold >= 0)),
    CONSTRAINT chk_market_buy_orders_status CHECK (((status)::text = ANY ((ARRAY['ACTIVE'::character varying, 'FILLED'::character varying, 'CANCELLED'::character varying])::text[])))
);


--
-- Name: market_listings; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.market_listings (
    id uuid NOT NULL,
    seller_character_id uuid NOT NULL,
    buyer_character_id uuid,
    item_instance_id uuid,
    item_definition_id uuid NOT NULL,
    quantity integer NOT NULL,
    price integer NOT NULL,
    status character varying(32) NOT NULL,
    created_at timestamp with time zone NOT NULL,
    sold_at timestamp with time zone,
    cancelled_at timestamp with time zone,
    version bigint DEFAULT 0 NOT NULL,
    listing_fee_paid integer DEFAULT 0 NOT NULL,
    sale_fee_paid integer,
    instance_rarity character varying(32) NOT NULL,
    item_type character varying(32) NOT NULL,
    weapon_family character varying(32),
    required_level integer NOT NULL,
    CONSTRAINT chk_market_listings_active_item CHECK (((((status)::text = 'ACTIVE'::text) AND (item_instance_id IS NOT NULL)) OR ((status)::text <> 'ACTIVE'::text))),
    CONSTRAINT chk_market_listings_cancelled CHECK (((((status)::text = 'CANCELLED'::text) AND (cancelled_at IS NOT NULL) AND (buyer_character_id IS NULL) AND (sold_at IS NULL)) OR (((status)::text <> 'CANCELLED'::text) AND (cancelled_at IS NULL)))),
    CONSTRAINT chk_market_listings_instance_rarity CHECK (((instance_rarity)::text = ANY ((ARRAY['COMMON'::character varying, 'UNCOMMON'::character varying, 'RARE'::character varying, 'EPIC'::character varying])::text[]))),
    CONSTRAINT chk_market_listings_item_type CHECK (((item_type)::text = ANY ((ARRAY['WEAPON'::character varying, 'ARMOR'::character varying, 'CONSUMABLE'::character varying, 'MATERIAL'::character varying, 'ACCESSORY'::character varying])::text[]))),
    CONSTRAINT chk_market_listings_listing_fee CHECK ((listing_fee_paid >= 0)),
    CONSTRAINT chk_market_listings_not_self_buy CHECK (((buyer_character_id IS NULL) OR (buyer_character_id <> seller_character_id))),
    CONSTRAINT chk_market_listings_price CHECK ((price >= 1)),
    CONSTRAINT chk_market_listings_quantity CHECK ((quantity >= 1)),
    CONSTRAINT chk_market_listings_sale_fee CHECK (((sale_fee_paid IS NULL) OR (sale_fee_paid >= 0))),
    CONSTRAINT chk_market_listings_sold CHECK (((((status)::text = 'SOLD'::text) AND (buyer_character_id IS NOT NULL) AND (sold_at IS NOT NULL) AND (cancelled_at IS NULL)) OR (((status)::text <> 'SOLD'::text) AND (buyer_character_id IS NULL) AND (sold_at IS NULL)))),
    CONSTRAINT chk_market_listings_status CHECK (((status)::text = ANY ((ARRAY['ACTIVE'::character varying, 'SOLD'::character varying, 'CANCELLED'::character varying])::text[])))
);


--
-- Name: merchant_definitions; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.merchant_definitions (
    id uuid NOT NULL,
    code character varying(64) NOT NULL,
    name character varying(128) NOT NULL,
    title character varying(128) NOT NULL,
    description text NOT NULL,
    merchant_type character varying(32) NOT NULL,
    portrait_code character varying(64) NOT NULL,
    sort_order integer NOT NULL,
    CONSTRAINT chk_merchant_definitions_sort CHECK ((sort_order >= 0)),
    CONSTRAINT chk_merchant_definitions_type CHECK (((merchant_type)::text = ANY ((ARRAY['WEAPONSMITH'::character varying, 'ARMORER'::character varying, 'APOTHECARY'::character varying, 'GENERAL'::character varying])::text[])))
);


--
-- Name: merchant_stock; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.merchant_stock (
    id uuid NOT NULL,
    merchant_id uuid NOT NULL,
    item_definition_id uuid NOT NULL,
    availability_type character varying(32) NOT NULL,
    sort_order integer NOT NULL,
    CONSTRAINT chk_merchant_stock_availability CHECK (((availability_type)::text = 'UNLIMITED'::text)),
    CONSTRAINT chk_merchant_stock_sort CHECK ((sort_order >= 0))
);


--
-- Name: monster_definitions; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.monster_definitions (
    id uuid NOT NULL,
    code character varying(64) NOT NULL,
    name character varying(128) NOT NULL,
    level integer NOT NULL,
    max_health integer NOT NULL,
    damage_min integer NOT NULL,
    damage_max integer NOT NULL,
    xp_reward integer NOT NULL,
    gold_min integer NOT NULL,
    gold_max integer NOT NULL,
    created_at timestamp with time zone NOT NULL,
    armor integer DEFAULT 0 NOT NULL,
    accuracy integer DEFAULT 70 NOT NULL,
    dodge integer DEFAULT 0 NOT NULL,
    critical_chance integer DEFAULT 5 NOT NULL,
    max_stamina integer DEFAULT 40 NOT NULL,
    ai_archetype character varying(16) DEFAULT 'AGGRESSIVE'::character varying NOT NULL,
    signature_status character varying(32),
    monster_tier character varying(16) DEFAULT 'NORMAL'::character varying NOT NULL,
    CONSTRAINT chk_monster_ai_archetype CHECK (((ai_archetype)::text = ANY ((ARRAY['AGGRESSIVE'::character varying, 'DEFENSIVE'::character varying, 'CONTROL'::character varying, 'ASSASSIN'::character varying, 'ARMORED'::character varying, 'BERSERKER'::character varying, 'SHIELDED'::character varying, 'MARKSMAN'::character varying])::text[]))),
    CONSTRAINT chk_monster_armor CHECK ((armor >= 0)),
    CONSTRAINT chk_monster_definitions_damage CHECK (((damage_min >= 0) AND (damage_max >= damage_min))),
    CONSTRAINT chk_monster_definitions_gold CHECK (((gold_min >= 0) AND (gold_max >= gold_min))),
    CONSTRAINT chk_monster_definitions_level CHECK ((level >= 1)),
    CONSTRAINT chk_monster_definitions_max_health CHECK ((max_health >= 1)),
    CONSTRAINT chk_monster_definitions_xp CHECK ((xp_reward >= 0)),
    CONSTRAINT chk_monster_signature_status CHECK (((signature_status IS NULL) OR ((signature_status)::text = ANY ((ARRAY['BLEED'::character varying, 'POISON'::character varying, 'STUN'::character varying, 'ARMOR_BREAK'::character varying, 'OFF_BALANCE'::character varying, 'GUARDED'::character varying])::text[])))),
    CONSTRAINT chk_monster_stamina CHECK ((max_stamina >= 0)),
    CONSTRAINT chk_monster_tier CHECK (((monster_tier)::text = ANY ((ARRAY['NORMAL'::character varying, 'ELITE'::character varying, 'MINI_BOSS'::character varying, 'BOSS'::character varying])::text[])))
);


--
-- Name: monster_loot_entries; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.monster_loot_entries (
    id uuid NOT NULL,
    monster_definition_id uuid NOT NULL,
    item_definition_id uuid NOT NULL,
    drop_chance_percent integer NOT NULL,
    quantity_min integer NOT NULL,
    quantity_max integer NOT NULL,
    once_per_character boolean DEFAULT false NOT NULL,
    CONSTRAINT chk_monster_loot_chance CHECK (((drop_chance_percent >= 0) AND (drop_chance_percent <= 100))),
    CONSTRAINT chk_monster_loot_quantity CHECK (((quantity_min >= 1) AND (quantity_max >= quantity_min)))
);


--
-- Name: pvp_battle_history; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.pvp_battle_history (
    id uuid NOT NULL,
    match_id uuid NOT NULL,
    character_id uuid NOT NULL,
    opponent_id uuid NOT NULL,
    opponent_name character varying(64) NOT NULL,
    match_kind character varying(16) NOT NULL,
    result character varying(16) NOT NULL,
    rating_delta integer NOT NULL,
    marks_awarded integer NOT NULL,
    created_at timestamp with time zone NOT NULL
);


--
-- Name: pvp_match_events; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.pvp_match_events (
    id uuid NOT NULL,
    match_id uuid NOT NULL,
    round_number integer NOT NULL,
    sequence_number integer NOT NULL,
    event_type character varying(64) NOT NULL,
    message text NOT NULL,
    created_at timestamp with time zone NOT NULL
);


--
-- Name: pvp_match_snapshots; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.pvp_match_snapshots (
    match_id uuid NOT NULL,
    snapshot_version integer NOT NULL,
    payload text NOT NULL,
    created_at timestamp with time zone NOT NULL
);


--
-- Name: pvp_match_statuses; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.pvp_match_statuses (
    id uuid NOT NULL,
    match_id uuid NOT NULL,
    target character varying(8) NOT NULL,
    status_type character varying(32) NOT NULL,
    stacks integer NOT NULL,
    remaining_rounds integer NOT NULL
);


--
-- Name: pvp_matches; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.pvp_matches (
    id uuid NOT NULL,
    match_kind character varying(16) NOT NULL,
    status character varying(32) NOT NULL,
    attacker_id uuid NOT NULL,
    defender_id uuid NOT NULL,
    round_number integer NOT NULL,
    attacker_health integer NOT NULL,
    attacker_stamina integer NOT NULL,
    defender_health integer NOT NULL,
    defender_stamina integer NOT NULL,
    attacker_potion_charges integer NOT NULL,
    defender_potion_charges integer NOT NULL,
    last_defender_missed boolean DEFAULT false NOT NULL,
    last_attacker_guarded boolean DEFAULT false NOT NULL,
    pending_attacker_action character varying(32),
    pending_attacker_technique character varying(64),
    pending_defender_action character varying(32),
    pending_defender_technique character varying(64),
    action_deadline_at timestamp with time zone,
    expires_at timestamp with time zone,
    planned_attacker_rating_delta integer DEFAULT 0 NOT NULL,
    planned_defender_rating_delta integer DEFAULT 0 NOT NULL,
    planned_attacker_marks integer DEFAULT 0 NOT NULL,
    planned_defender_marks integer DEFAULT 0 NOT NULL,
    attacker_rating_at_start integer DEFAULT 1000 NOT NULL,
    defender_rating_at_start integer DEFAULT 1000 NOT NULL,
    rating_reward_multiplier numeric(6,3) DEFAULT 1 NOT NULL,
    settlement_applied boolean DEFAULT false NOT NULL,
    outcome_acknowledged boolean DEFAULT true NOT NULL,
    version integer DEFAULT 0 NOT NULL,
    created_at timestamp with time zone NOT NULL,
    updated_at timestamp with time zone NOT NULL,
    CONSTRAINT ck_pvp_matches_not_self CHECK ((attacker_id <> defender_id))
);


--
-- Name: salvage_outputs; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.salvage_outputs (
    id uuid NOT NULL,
    source_item_definition_id uuid NOT NULL,
    result_item_definition_id uuid NOT NULL,
    base_quantity integer NOT NULL,
    CONSTRAINT chk_salvage_outputs_qty CHECK ((base_quantity >= 1))
);


--
-- Name: schema_meta; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.schema_meta (
    id bigint NOT NULL,
    key character varying(100) NOT NULL,
    value character varying(255) NOT NULL,
    created_at timestamp with time zone DEFAULT now() NOT NULL
);


--
-- Name: schema_meta_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.schema_meta_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: schema_meta_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.schema_meta_id_seq OWNED BY public.schema_meta.id;


--
-- Name: technique_loadout_slots; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.technique_loadout_slots (
    id uuid NOT NULL,
    character_id uuid NOT NULL,
    slot_index integer NOT NULL,
    technique_code character varying(64),
    CONSTRAINT chk_technique_loadout_slot CHECK (((slot_index >= 0) AND (slot_index <= 3)))
);


--
-- Name: weapon_masteries; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.weapon_masteries (
    id uuid NOT NULL,
    character_id uuid NOT NULL,
    weapon_family character varying(16) NOT NULL,
    total_experience integer NOT NULL,
    level integer NOT NULL,
    updated_at timestamp with time zone NOT NULL,
    CONSTRAINT chk_weapon_masteries_family CHECK (((weapon_family)::text = ANY ((ARRAY['SWORD'::character varying, 'AXE'::character varying, 'MACE'::character varying, 'DAGGER'::character varying, 'BOW'::character varying])::text[]))),
    CONSTRAINT chk_weapon_masteries_level CHECK (((level >= 0) AND (level <= 10))),
    CONSTRAINT chk_weapon_masteries_xp CHECK ((total_experience >= 0))
);


--
-- Name: schema_meta id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.schema_meta ALTER COLUMN id SET DEFAULT nextval('public.schema_meta_id_seq'::regclass);


--
-- Name: accounts accounts_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.accounts
    ADD CONSTRAINT accounts_pkey PRIMARY KEY (id);


--
-- Name: activity_entries activity_entries_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.activity_entries
    ADD CONSTRAINT activity_entries_pkey PRIMARY KEY (id);


--
-- Name: affix_definitions affix_definitions_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.affix_definitions
    ADD CONSTRAINT affix_definitions_pkey PRIMARY KEY (code);


--
-- Name: arena_defense_profiles arena_defense_profiles_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.arena_defense_profiles
    ADD CONSTRAINT arena_defense_profiles_pkey PRIMARY KEY (character_id);


--
-- Name: character_professions character_professions_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.character_professions
    ADD CONSTRAINT character_professions_pkey PRIMARY KEY (id);


--
-- Name: character_techniques character_techniques_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.character_techniques
    ADD CONSTRAINT character_techniques_pkey PRIMARY KEY (id);


--
-- Name: character_unique_drops character_unique_drops_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.character_unique_drops
    ADD CONSTRAINT character_unique_drops_pkey PRIMARY KEY (id);


--
-- Name: characters characters_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.characters
    ADD CONSTRAINT characters_pkey PRIMARY KEY (id);


--
-- Name: chat_messages chat_messages_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.chat_messages
    ADD CONSTRAINT chat_messages_pkey PRIMARY KEY (id);


--
-- Name: combat_events combat_events_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.combat_events
    ADD CONSTRAINT combat_events_pkey PRIMARY KEY (id);


--
-- Name: combat_reward_items combat_reward_items_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.combat_reward_items
    ADD CONSTRAINT combat_reward_items_pkey PRIMARY KEY (id);


--
-- Name: combat_sessions combat_sessions_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.combat_sessions
    ADD CONSTRAINT combat_sessions_pkey PRIMARY KEY (id);


--
-- Name: combat_status_effects combat_status_effects_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.combat_status_effects
    ADD CONSTRAINT combat_status_effects_pkey PRIMARY KEY (id);


--
-- Name: combat_technique_definitions combat_technique_definitions_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.combat_technique_definitions
    ADD CONSTRAINT combat_technique_definitions_pkey PRIMARY KEY (code);


--
-- Name: crafting_jobs crafting_jobs_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.crafting_jobs
    ADD CONSTRAINT crafting_jobs_pkey PRIMARY KEY (id);


--
-- Name: crafting_recipe_inputs crafting_recipe_inputs_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.crafting_recipe_inputs
    ADD CONSTRAINT crafting_recipe_inputs_pkey PRIMARY KEY (id);


--
-- Name: crafting_recipes crafting_recipes_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.crafting_recipes
    ADD CONSTRAINT crafting_recipes_pkey PRIMARY KEY (id);


--
-- Name: dungeon_definitions dungeon_definitions_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.dungeon_definitions
    ADD CONSTRAINT dungeon_definitions_pkey PRIMARY KEY (id);


--
-- Name: dungeon_room_edges dungeon_room_edges_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.dungeon_room_edges
    ADD CONSTRAINT dungeon_room_edges_pkey PRIMARY KEY (id);


--
-- Name: dungeon_rooms dungeon_rooms_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.dungeon_rooms
    ADD CONSTRAINT dungeon_rooms_pkey PRIMARY KEY (id);


--
-- Name: dungeon_run_rooms dungeon_run_rooms_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.dungeon_run_rooms
    ADD CONSTRAINT dungeon_run_rooms_pkey PRIMARY KEY (id);


--
-- Name: dungeon_runs dungeon_runs_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.dungeon_runs
    ADD CONSTRAINT dungeon_runs_pkey PRIMARY KEY (id);


--
-- Name: encounters encounters_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.encounters
    ADD CONSTRAINT encounters_pkey PRIMARY KEY (id);


--
-- Name: equipment equipment_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.equipment
    ADD CONSTRAINT equipment_pkey PRIMARY KEY (id);


--
-- Name: expedition_reward_items expedition_reward_items_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.expedition_reward_items
    ADD CONSTRAINT expedition_reward_items_pkey PRIMARY KEY (id);


--
-- Name: expeditions expeditions_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.expeditions
    ADD CONSTRAINT expeditions_pkey PRIMARY KEY (id);


--
-- Name: game_telemetry_events game_telemetry_events_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.game_telemetry_events
    ADD CONSTRAINT game_telemetry_events_pkey PRIMARY KEY (id);


--
-- Name: item_definition_modifiers item_definition_modifiers_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.item_definition_modifiers
    ADD CONSTRAINT item_definition_modifiers_pkey PRIMARY KEY (id);


--
-- Name: item_definitions item_definitions_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.item_definitions
    ADD CONSTRAINT item_definitions_pkey PRIMARY KEY (id);


--
-- Name: item_instance_affixes item_instance_affixes_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.item_instance_affixes
    ADD CONSTRAINT item_instance_affixes_pkey PRIMARY KEY (id);


--
-- Name: item_instances item_instances_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.item_instances
    ADD CONSTRAINT item_instances_pkey PRIMARY KEY (id);


--
-- Name: location_connections location_connections_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.location_connections
    ADD CONSTRAINT location_connections_pkey PRIMARY KEY (id);


--
-- Name: location_encounter_weights location_encounter_weights_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.location_encounter_weights
    ADD CONSTRAINT location_encounter_weights_pkey PRIMARY KEY (id);


--
-- Name: locations locations_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.locations
    ADD CONSTRAINT locations_pkey PRIMARY KEY (id);


--
-- Name: market_buy_order_fills market_buy_order_fills_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.market_buy_order_fills
    ADD CONSTRAINT market_buy_order_fills_pkey PRIMARY KEY (id);


--
-- Name: market_buy_orders market_buy_orders_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.market_buy_orders
    ADD CONSTRAINT market_buy_orders_pkey PRIMARY KEY (id);


--
-- Name: market_listings market_listings_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.market_listings
    ADD CONSTRAINT market_listings_pkey PRIMARY KEY (id);


--
-- Name: merchant_definitions merchant_definitions_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.merchant_definitions
    ADD CONSTRAINT merchant_definitions_pkey PRIMARY KEY (id);


--
-- Name: merchant_stock merchant_stock_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.merchant_stock
    ADD CONSTRAINT merchant_stock_pkey PRIMARY KEY (id);


--
-- Name: monster_definitions monster_definitions_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.monster_definitions
    ADD CONSTRAINT monster_definitions_pkey PRIMARY KEY (id);


--
-- Name: monster_loot_entries monster_loot_entries_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.monster_loot_entries
    ADD CONSTRAINT monster_loot_entries_pkey PRIMARY KEY (id);


--
-- Name: pvp_battle_history pvp_battle_history_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.pvp_battle_history
    ADD CONSTRAINT pvp_battle_history_pkey PRIMARY KEY (id);


--
-- Name: pvp_match_events pvp_match_events_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.pvp_match_events
    ADD CONSTRAINT pvp_match_events_pkey PRIMARY KEY (id);


--
-- Name: pvp_match_snapshots pvp_match_snapshots_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.pvp_match_snapshots
    ADD CONSTRAINT pvp_match_snapshots_pkey PRIMARY KEY (match_id);


--
-- Name: pvp_match_statuses pvp_match_statuses_match_id_target_status_type_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.pvp_match_statuses
    ADD CONSTRAINT pvp_match_statuses_match_id_target_status_type_key UNIQUE (match_id, target, status_type);


--
-- Name: pvp_match_statuses pvp_match_statuses_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.pvp_match_statuses
    ADD CONSTRAINT pvp_match_statuses_pkey PRIMARY KEY (id);


--
-- Name: pvp_matches pvp_matches_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.pvp_matches
    ADD CONSTRAINT pvp_matches_pkey PRIMARY KEY (id);


--
-- Name: salvage_outputs salvage_outputs_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.salvage_outputs
    ADD CONSTRAINT salvage_outputs_pkey PRIMARY KEY (id);


--
-- Name: schema_meta schema_meta_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.schema_meta
    ADD CONSTRAINT schema_meta_pkey PRIMARY KEY (id);


--
-- Name: technique_loadout_slots technique_loadout_slots_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.technique_loadout_slots
    ADD CONSTRAINT technique_loadout_slots_pkey PRIMARY KEY (id);


--
-- Name: character_professions uq_character_professions; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.character_professions
    ADD CONSTRAINT uq_character_professions UNIQUE (character_id, profession);


--
-- Name: character_techniques uq_character_techniques_character_code; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.character_techniques
    ADD CONSTRAINT uq_character_techniques_character_code UNIQUE (character_id, technique_code);


--
-- Name: character_unique_drops uq_character_unique_drops; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.character_unique_drops
    ADD CONSTRAINT uq_character_unique_drops UNIQUE (character_id, item_code);


--
-- Name: characters uq_characters_account_id; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.characters
    ADD CONSTRAINT uq_characters_account_id UNIQUE (account_id);


--
-- Name: combat_events uq_combat_events_session_round_seq; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.combat_events
    ADD CONSTRAINT uq_combat_events_session_round_seq UNIQUE (session_id, round_number, sequence_number);


--
-- Name: combat_sessions uq_combat_sessions_encounter; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.combat_sessions
    ADD CONSTRAINT uq_combat_sessions_encounter UNIQUE (encounter_id);


--
-- Name: combat_status_effects uq_combat_status_session_target_type; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.combat_status_effects
    ADD CONSTRAINT uq_combat_status_session_target_type UNIQUE (session_id, target, status_type);


--
-- Name: crafting_recipe_inputs uq_crafting_recipe_inputs; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.crafting_recipe_inputs
    ADD CONSTRAINT uq_crafting_recipe_inputs UNIQUE (recipe_id, item_definition_id);


--
-- Name: crafting_recipes uq_crafting_recipes_code; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.crafting_recipes
    ADD CONSTRAINT uq_crafting_recipes_code UNIQUE (code);


--
-- Name: dungeon_definitions uq_dungeon_definitions_code; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.dungeon_definitions
    ADD CONSTRAINT uq_dungeon_definitions_code UNIQUE (code);


--
-- Name: dungeon_room_edges uq_dungeon_edges; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.dungeon_room_edges
    ADD CONSTRAINT uq_dungeon_edges UNIQUE (from_room_id, edge_code);


--
-- Name: dungeon_rooms uq_dungeon_rooms_code; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.dungeon_rooms
    ADD CONSTRAINT uq_dungeon_rooms_code UNIQUE (dungeon_id, code);


--
-- Name: dungeon_run_rooms uq_dungeon_run_rooms; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.dungeon_run_rooms
    ADD CONSTRAINT uq_dungeon_run_rooms UNIQUE (run_id, room_code);


--
-- Name: equipment uq_equipment_character_slot; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.equipment
    ADD CONSTRAINT uq_equipment_character_slot UNIQUE (character_id, slot);


--
-- Name: equipment uq_equipment_item_instance; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.equipment
    ADD CONSTRAINT uq_equipment_item_instance UNIQUE (item_instance_id);


--
-- Name: item_definition_modifiers uq_item_definition_modifiers_stat; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.item_definition_modifiers
    ADD CONSTRAINT uq_item_definition_modifiers_stat UNIQUE (item_definition_id, stat);


--
-- Name: item_definitions uq_item_definitions_code; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.item_definitions
    ADD CONSTRAINT uq_item_definitions_code UNIQUE (code);


--
-- Name: item_instance_affixes uq_item_instance_affixes_slot; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.item_instance_affixes
    ADD CONSTRAINT uq_item_instance_affixes_slot UNIQUE (item_instance_id, kind, ordinal);


--
-- Name: item_instances uq_item_instances_id_owner; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.item_instances
    ADD CONSTRAINT uq_item_instances_id_owner UNIQUE (id, owner_character_id);


--
-- Name: location_connections uq_location_connections_pair; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.location_connections
    ADD CONSTRAINT uq_location_connections_pair UNIQUE (from_location_id, to_location_id);


--
-- Name: locations uq_locations_code; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.locations
    ADD CONSTRAINT uq_locations_code UNIQUE (code);


--
-- Name: merchant_definitions uq_merchant_definitions_code; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.merchant_definitions
    ADD CONSTRAINT uq_merchant_definitions_code UNIQUE (code);


--
-- Name: merchant_stock uq_merchant_stock_item; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.merchant_stock
    ADD CONSTRAINT uq_merchant_stock_item UNIQUE (merchant_id, item_definition_id);


--
-- Name: monster_definitions uq_monster_definitions_code; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.monster_definitions
    ADD CONSTRAINT uq_monster_definitions_code UNIQUE (code);


--
-- Name: salvage_outputs uq_salvage_outputs; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.salvage_outputs
    ADD CONSTRAINT uq_salvage_outputs UNIQUE (source_item_definition_id, result_item_definition_id);


--
-- Name: schema_meta uq_schema_meta_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.schema_meta
    ADD CONSTRAINT uq_schema_meta_key UNIQUE (key);


--
-- Name: technique_loadout_slots uq_technique_loadout_character_slot; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.technique_loadout_slots
    ADD CONSTRAINT uq_technique_loadout_character_slot UNIQUE (character_id, slot_index);


--
-- Name: weapon_masteries uq_weapon_masteries_character_family; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.weapon_masteries
    ADD CONSTRAINT uq_weapon_masteries_character_family UNIQUE (character_id, weapon_family);


--
-- Name: weapon_masteries weapon_masteries_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.weapon_masteries
    ADD CONSTRAINT weapon_masteries_pkey PRIMARY KEY (id);


--
-- Name: idx_activity_entries_character_created; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_activity_entries_character_created ON public.activity_entries USING btree (character_id, created_at DESC);


--
-- Name: idx_character_professions_character; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_character_professions_character ON public.character_professions USING btree (character_id);


--
-- Name: idx_character_techniques_character; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_character_techniques_character ON public.character_techniques USING btree (character_id);


--
-- Name: idx_characters_current_location; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_characters_current_location ON public.characters USING btree (current_location_id);


--
-- Name: idx_chat_messages_character_created; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_chat_messages_character_created ON public.chat_messages USING btree (character_id, created_at DESC);


--
-- Name: idx_chat_messages_created; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_chat_messages_created ON public.chat_messages USING btree (created_at DESC, id DESC);


--
-- Name: idx_combat_events_session; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_combat_events_session ON public.combat_events USING btree (session_id, round_number, sequence_number);


--
-- Name: idx_combat_reward_items_session; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_combat_reward_items_session ON public.combat_reward_items USING btree (session_id);


--
-- Name: idx_combat_sessions_character; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_combat_sessions_character ON public.combat_sessions USING btree (character_id);


--
-- Name: idx_combat_status_session; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_combat_status_session ON public.combat_status_effects USING btree (session_id);


--
-- Name: idx_crafting_jobs_character; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_crafting_jobs_character ON public.crafting_jobs USING btree (character_id, created_at DESC);


--
-- Name: idx_crafting_recipe_inputs_recipe; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_crafting_recipe_inputs_recipe ON public.crafting_recipe_inputs USING btree (recipe_id);


--
-- Name: idx_dungeon_run_rooms_run; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_dungeon_run_rooms_run ON public.dungeon_run_rooms USING btree (run_id);


--
-- Name: idx_encounters_character; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_encounters_character ON public.encounters USING btree (character_id);


--
-- Name: idx_equipment_character; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_equipment_character ON public.equipment USING btree (character_id);


--
-- Name: idx_expedition_reward_items_expedition; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_expedition_reward_items_expedition ON public.expedition_reward_items USING btree (expedition_id);


--
-- Name: idx_expeditions_character_status; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_expeditions_character_status ON public.expeditions USING btree (character_id, status);


--
-- Name: idx_game_telemetry_events_category_type_time; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_game_telemetry_events_category_type_time ON public.game_telemetry_events USING btree (category, event_type, occurred_at);


--
-- Name: idx_game_telemetry_events_character_time; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_game_telemetry_events_character_time ON public.game_telemetry_events USING btree (character_id, occurred_at);


--
-- Name: idx_item_definition_modifiers_definition; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_item_definition_modifiers_definition ON public.item_definition_modifiers USING btree (item_definition_id);


--
-- Name: idx_item_instance_affixes_instance; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_item_instance_affixes_instance ON public.item_instance_affixes USING btree (item_instance_id);


--
-- Name: idx_item_instances_definition; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_item_instances_definition ON public.item_instances USING btree (item_definition_id);


--
-- Name: idx_item_instances_owner; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_item_instances_owner ON public.item_instances USING btree (owner_character_id);


--
-- Name: idx_location_connections_from; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_location_connections_from ON public.location_connections USING btree (from_location_id);


--
-- Name: idx_location_encounter_location; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_location_encounter_location ON public.location_encounter_weights USING btree (location_id);


--
-- Name: idx_market_buy_order_fills_order; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_market_buy_order_fills_order ON public.market_buy_order_fills USING btree (buy_order_id, created_at DESC);


--
-- Name: idx_market_buy_orders_active_item; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_market_buy_orders_active_item ON public.market_buy_orders USING btree (status, item_definition_id, created_at DESC);


--
-- Name: idx_market_buy_orders_buyer; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_market_buy_orders_buyer ON public.market_buy_orders USING btree (buyer_character_id, created_at DESC);


--
-- Name: idx_market_listings_browse; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_market_listings_browse ON public.market_listings USING btree (status, created_at DESC);


--
-- Name: idx_market_listings_filters; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_market_listings_filters ON public.market_listings USING btree (status, item_type, instance_rarity, weapon_family, required_level, price);


--
-- Name: idx_market_listings_seller_status; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_market_listings_seller_status ON public.market_listings USING btree (seller_character_id, status);


--
-- Name: idx_market_listings_status_created; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_market_listings_status_created ON public.market_listings USING btree (status, created_at DESC);


--
-- Name: idx_merchant_stock_merchant; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_merchant_stock_merchant ON public.merchant_stock USING btree (merchant_id, sort_order);


--
-- Name: idx_monster_loot_monster; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_monster_loot_monster ON public.monster_loot_entries USING btree (monster_definition_id);


--
-- Name: idx_pvp_history_character_created; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_pvp_history_character_created ON public.pvp_battle_history USING btree (character_id, created_at DESC, id DESC);


--
-- Name: idx_pvp_match_events_match; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_pvp_match_events_match ON public.pvp_match_events USING btree (match_id, round_number, sequence_number);


--
-- Name: idx_pvp_matches_arena_repeat; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_pvp_matches_arena_repeat ON public.pvp_matches USING btree (attacker_id, defender_id, created_at) WHERE ((match_kind)::text = 'ARENA'::text);


--
-- Name: idx_technique_loadout_character; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_technique_loadout_character ON public.technique_loadout_slots USING btree (character_id);


--
-- Name: idx_weapon_masteries_character; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_weapon_masteries_character ON public.weapon_masteries USING btree (character_id);


--
-- Name: uq_accounts_email_lower; Type: INDEX; Schema: public; Owner: -
--

CREATE UNIQUE INDEX uq_accounts_email_lower ON public.accounts USING btree (lower((email)::text));


--
-- Name: uq_characters_name_lower; Type: INDEX; Schema: public; Owner: -
--

CREATE UNIQUE INDEX uq_characters_name_lower ON public.characters USING btree (lower((name)::text));


--
-- Name: uq_combat_sessions_one_active_per_character; Type: INDEX; Schema: public; Owner: -
--

CREATE UNIQUE INDEX uq_combat_sessions_one_active_per_character ON public.combat_sessions USING btree (character_id) WHERE ((status)::text = 'ACTIVE'::text);


--
-- Name: uq_combat_sessions_one_unacked_outcome_per_character; Type: INDEX; Schema: public; Owner: -
--

CREATE UNIQUE INDEX uq_combat_sessions_one_unacked_outcome_per_character ON public.combat_sessions USING btree (character_id) WHERE (outcome_acknowledged = false);


--
-- Name: uq_crafting_jobs_one_open; Type: INDEX; Schema: public; Owner: -
--

CREATE UNIQUE INDEX uq_crafting_jobs_one_open ON public.crafting_jobs USING btree (character_id) WHERE ((status)::text = ANY ((ARRAY['ACTIVE'::character varying, 'COMPLETED'::character varying])::text[]));


--
-- Name: uq_dungeon_runs_one_active_per_character; Type: INDEX; Schema: public; Owner: -
--

CREATE UNIQUE INDEX uq_dungeon_runs_one_active_per_character ON public.dungeon_runs USING btree (character_id) WHERE ((status)::text = 'ACTIVE'::text);


--
-- Name: uq_encounters_one_unresolved_per_character; Type: INDEX; Schema: public; Owner: -
--

CREATE UNIQUE INDEX uq_encounters_one_unresolved_per_character ON public.encounters USING btree (character_id) WHERE ((status)::text = ANY ((ARRAY['AVAILABLE'::character varying, 'COMBAT_STARTED'::character varying])::text[]));


--
-- Name: uq_expeditions_one_active; Type: INDEX; Schema: public; Owner: -
--

CREATE UNIQUE INDEX uq_expeditions_one_active ON public.expeditions USING btree (character_id) WHERE ((status)::text = 'ACTIVE'::text);


--
-- Name: uq_item_instances_owner_stackable_definition; Type: INDEX; Schema: public; Owner: -
--

CREATE UNIQUE INDEX uq_item_instances_owner_stackable_definition ON public.item_instances USING btree (owner_character_id, item_definition_id) WHERE stackable;


--
-- Name: uq_pvp_history_match_character; Type: INDEX; Schema: public; Owner: -
--

CREATE UNIQUE INDEX uq_pvp_history_match_character ON public.pvp_battle_history USING btree (match_id, character_id);


--
-- Name: uq_pvp_one_active_arena_per_attacker; Type: INDEX; Schema: public; Owner: -
--

CREATE UNIQUE INDEX uq_pvp_one_active_arena_per_attacker ON public.pvp_matches USING btree (attacker_id) WHERE (((match_kind)::text = 'ARENA'::text) AND ((status)::text = 'ACTIVE'::text));


--
-- Name: uq_pvp_one_open_duel_attacker; Type: INDEX; Schema: public; Owner: -
--

CREATE UNIQUE INDEX uq_pvp_one_open_duel_attacker ON public.pvp_matches USING btree (attacker_id) WHERE (((match_kind)::text = 'DUEL'::text) AND ((status)::text = ANY ((ARRAY['PENDING'::character varying, 'ACTIVE'::character varying])::text[])));


--
-- Name: uq_pvp_one_open_duel_defender; Type: INDEX; Schema: public; Owner: -
--

CREATE UNIQUE INDEX uq_pvp_one_open_duel_defender ON public.pvp_matches USING btree (defender_id) WHERE (((match_kind)::text = 'DUEL'::text) AND ((status)::text = ANY ((ARRAY['PENDING'::character varying, 'ACTIVE'::character varying])::text[])));


--
-- Name: uq_pvp_one_unacked_arena_per_attacker; Type: INDEX; Schema: public; Owner: -
--

CREATE UNIQUE INDEX uq_pvp_one_unacked_arena_per_attacker ON public.pvp_matches USING btree (attacker_id) WHERE (((match_kind)::text = 'ARENA'::text) AND (outcome_acknowledged = false));


--
-- Name: uq_technique_loadout_character_technique; Type: INDEX; Schema: public; Owner: -
--

CREATE UNIQUE INDEX uq_technique_loadout_character_technique ON public.technique_loadout_slots USING btree (character_id, technique_code) WHERE (technique_code IS NOT NULL);


--
-- Name: arena_defense_profiles arena_defense_profiles_character_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.arena_defense_profiles
    ADD CONSTRAINT arena_defense_profiles_character_id_fkey FOREIGN KEY (character_id) REFERENCES public.characters(id);


--
-- Name: activity_entries fk_activity_entries_character; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.activity_entries
    ADD CONSTRAINT fk_activity_entries_character FOREIGN KEY (character_id) REFERENCES public.characters(id);


--
-- Name: character_professions fk_character_professions_character; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.character_professions
    ADD CONSTRAINT fk_character_professions_character FOREIGN KEY (character_id) REFERENCES public.characters(id);


--
-- Name: character_techniques fk_character_techniques_character; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.character_techniques
    ADD CONSTRAINT fk_character_techniques_character FOREIGN KEY (character_id) REFERENCES public.characters(id);


--
-- Name: character_techniques fk_character_techniques_definition; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.character_techniques
    ADD CONSTRAINT fk_character_techniques_definition FOREIGN KEY (technique_code) REFERENCES public.combat_technique_definitions(code);


--
-- Name: character_unique_drops fk_character_unique_drops_character; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.character_unique_drops
    ADD CONSTRAINT fk_character_unique_drops_character FOREIGN KEY (character_id) REFERENCES public.characters(id);


--
-- Name: characters fk_characters_account; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.characters
    ADD CONSTRAINT fk_characters_account FOREIGN KEY (account_id) REFERENCES public.accounts(id);


--
-- Name: characters fk_characters_current_location; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.characters
    ADD CONSTRAINT fk_characters_current_location FOREIGN KEY (current_location_id) REFERENCES public.locations(id);


--
-- Name: chat_messages fk_chat_messages_character; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.chat_messages
    ADD CONSTRAINT fk_chat_messages_character FOREIGN KEY (character_id) REFERENCES public.characters(id);


--
-- Name: combat_events fk_combat_events_session; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.combat_events
    ADD CONSTRAINT fk_combat_events_session FOREIGN KEY (session_id) REFERENCES public.combat_sessions(id);


--
-- Name: combat_reward_items fk_combat_reward_items_item; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.combat_reward_items
    ADD CONSTRAINT fk_combat_reward_items_item FOREIGN KEY (item_definition_id) REFERENCES public.item_definitions(id);


--
-- Name: combat_reward_items fk_combat_reward_items_session; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.combat_reward_items
    ADD CONSTRAINT fk_combat_reward_items_session FOREIGN KEY (session_id) REFERENCES public.combat_sessions(id);


--
-- Name: combat_sessions fk_combat_sessions_character; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.combat_sessions
    ADD CONSTRAINT fk_combat_sessions_character FOREIGN KEY (character_id) REFERENCES public.characters(id);


--
-- Name: combat_sessions fk_combat_sessions_encounter; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.combat_sessions
    ADD CONSTRAINT fk_combat_sessions_encounter FOREIGN KEY (encounter_id) REFERENCES public.encounters(id);


--
-- Name: combat_sessions fk_combat_sessions_monster; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.combat_sessions
    ADD CONSTRAINT fk_combat_sessions_monster FOREIGN KEY (monster_definition_id) REFERENCES public.monster_definitions(id);


--
-- Name: combat_status_effects fk_combat_status_session; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.combat_status_effects
    ADD CONSTRAINT fk_combat_status_session FOREIGN KEY (session_id) REFERENCES public.combat_sessions(id);


--
-- Name: crafting_jobs fk_crafting_jobs_character; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.crafting_jobs
    ADD CONSTRAINT fk_crafting_jobs_character FOREIGN KEY (character_id) REFERENCES public.characters(id);


--
-- Name: crafting_jobs fk_crafting_jobs_output; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.crafting_jobs
    ADD CONSTRAINT fk_crafting_jobs_output FOREIGN KEY (output_item_definition_id) REFERENCES public.item_definitions(id);


--
-- Name: crafting_jobs fk_crafting_jobs_recipe; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.crafting_jobs
    ADD CONSTRAINT fk_crafting_jobs_recipe FOREIGN KEY (recipe_id) REFERENCES public.crafting_recipes(id);


--
-- Name: crafting_recipe_inputs fk_crafting_recipe_inputs_item; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.crafting_recipe_inputs
    ADD CONSTRAINT fk_crafting_recipe_inputs_item FOREIGN KEY (item_definition_id) REFERENCES public.item_definitions(id);


--
-- Name: crafting_recipe_inputs fk_crafting_recipe_inputs_recipe; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.crafting_recipe_inputs
    ADD CONSTRAINT fk_crafting_recipe_inputs_recipe FOREIGN KEY (recipe_id) REFERENCES public.crafting_recipes(id);


--
-- Name: crafting_recipes fk_crafting_recipes_output; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.crafting_recipes
    ADD CONSTRAINT fk_crafting_recipes_output FOREIGN KEY (output_item_definition_id) REFERENCES public.item_definitions(id);


--
-- Name: dungeon_definitions fk_dungeon_definitions_entrance; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.dungeon_definitions
    ADD CONSTRAINT fk_dungeon_definitions_entrance FOREIGN KEY (entrance_location_id) REFERENCES public.locations(id);


--
-- Name: dungeon_room_edges fk_dungeon_edges_from; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.dungeon_room_edges
    ADD CONSTRAINT fk_dungeon_edges_from FOREIGN KEY (from_room_id) REFERENCES public.dungeon_rooms(id);


--
-- Name: dungeon_room_edges fk_dungeon_edges_to; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.dungeon_room_edges
    ADD CONSTRAINT fk_dungeon_edges_to FOREIGN KEY (to_room_id) REFERENCES public.dungeon_rooms(id);


--
-- Name: dungeon_rooms fk_dungeon_rooms_dungeon; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.dungeon_rooms
    ADD CONSTRAINT fk_dungeon_rooms_dungeon FOREIGN KEY (dungeon_id) REFERENCES public.dungeon_definitions(id);


--
-- Name: dungeon_rooms fk_dungeon_rooms_monster; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.dungeon_rooms
    ADD CONSTRAINT fk_dungeon_rooms_monster FOREIGN KEY (monster_definition_id) REFERENCES public.monster_definitions(id);


--
-- Name: dungeon_run_rooms fk_dungeon_run_rooms_run; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.dungeon_run_rooms
    ADD CONSTRAINT fk_dungeon_run_rooms_run FOREIGN KEY (run_id) REFERENCES public.dungeon_runs(id);


--
-- Name: dungeon_runs fk_dungeon_runs_character; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.dungeon_runs
    ADD CONSTRAINT fk_dungeon_runs_character FOREIGN KEY (character_id) REFERENCES public.characters(id);


--
-- Name: dungeon_runs fk_dungeon_runs_dungeon; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.dungeon_runs
    ADD CONSTRAINT fk_dungeon_runs_dungeon FOREIGN KEY (dungeon_id) REFERENCES public.dungeon_definitions(id);


--
-- Name: encounters fk_encounters_character; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.encounters
    ADD CONSTRAINT fk_encounters_character FOREIGN KEY (character_id) REFERENCES public.characters(id);


--
-- Name: encounters fk_encounters_dungeon_run; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.encounters
    ADD CONSTRAINT fk_encounters_dungeon_run FOREIGN KEY (dungeon_run_id) REFERENCES public.dungeon_runs(id);


--
-- Name: encounters fk_encounters_location; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.encounters
    ADD CONSTRAINT fk_encounters_location FOREIGN KEY (location_id) REFERENCES public.locations(id);


--
-- Name: encounters fk_encounters_monster; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.encounters
    ADD CONSTRAINT fk_encounters_monster FOREIGN KEY (monster_definition_id) REFERENCES public.monster_definitions(id);


--
-- Name: equipment fk_equipment_character; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.equipment
    ADD CONSTRAINT fk_equipment_character FOREIGN KEY (character_id) REFERENCES public.characters(id);


--
-- Name: equipment fk_equipment_owned_item; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.equipment
    ADD CONSTRAINT fk_equipment_owned_item FOREIGN KEY (item_instance_id, character_id) REFERENCES public.item_instances(id, owner_character_id);


--
-- Name: expedition_reward_items fk_expedition_reward_items_expedition; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.expedition_reward_items
    ADD CONSTRAINT fk_expedition_reward_items_expedition FOREIGN KEY (expedition_id) REFERENCES public.expeditions(id);


--
-- Name: expedition_reward_items fk_expedition_reward_items_item; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.expedition_reward_items
    ADD CONSTRAINT fk_expedition_reward_items_item FOREIGN KEY (item_definition_id) REFERENCES public.item_definitions(id);


--
-- Name: expeditions fk_expeditions_character; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.expeditions
    ADD CONSTRAINT fk_expeditions_character FOREIGN KEY (character_id) REFERENCES public.characters(id);


--
-- Name: game_telemetry_events fk_game_telemetry_events_character; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.game_telemetry_events
    ADD CONSTRAINT fk_game_telemetry_events_character FOREIGN KEY (character_id) REFERENCES public.characters(id);


--
-- Name: item_definition_modifiers fk_item_definition_modifiers_definition; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.item_definition_modifiers
    ADD CONSTRAINT fk_item_definition_modifiers_definition FOREIGN KEY (item_definition_id) REFERENCES public.item_definitions(id);


--
-- Name: item_instance_affixes fk_item_instance_affixes_affix; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.item_instance_affixes
    ADD CONSTRAINT fk_item_instance_affixes_affix FOREIGN KEY (affix_code) REFERENCES public.affix_definitions(code);


--
-- Name: item_instance_affixes fk_item_instance_affixes_instance; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.item_instance_affixes
    ADD CONSTRAINT fk_item_instance_affixes_instance FOREIGN KEY (item_instance_id) REFERENCES public.item_instances(id) ON DELETE CASCADE;


--
-- Name: item_instances fk_item_instances_definition; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.item_instances
    ADD CONSTRAINT fk_item_instances_definition FOREIGN KEY (item_definition_id) REFERENCES public.item_definitions(id);


--
-- Name: item_instances fk_item_instances_owner; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.item_instances
    ADD CONSTRAINT fk_item_instances_owner FOREIGN KEY (owner_character_id) REFERENCES public.characters(id);


--
-- Name: location_connections fk_location_connections_from; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.location_connections
    ADD CONSTRAINT fk_location_connections_from FOREIGN KEY (from_location_id) REFERENCES public.locations(id);


--
-- Name: location_connections fk_location_connections_to; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.location_connections
    ADD CONSTRAINT fk_location_connections_to FOREIGN KEY (to_location_id) REFERENCES public.locations(id);


--
-- Name: location_encounter_weights fk_location_encounter_location; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.location_encounter_weights
    ADD CONSTRAINT fk_location_encounter_location FOREIGN KEY (location_id) REFERENCES public.locations(id);


--
-- Name: location_encounter_weights fk_location_encounter_monster; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.location_encounter_weights
    ADD CONSTRAINT fk_location_encounter_monster FOREIGN KEY (monster_definition_id) REFERENCES public.monster_definitions(id);


--
-- Name: market_buy_order_fills fk_market_buy_order_fills_order; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.market_buy_order_fills
    ADD CONSTRAINT fk_market_buy_order_fills_order FOREIGN KEY (buy_order_id) REFERENCES public.market_buy_orders(id);


--
-- Name: market_buy_order_fills fk_market_buy_order_fills_seller; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.market_buy_order_fills
    ADD CONSTRAINT fk_market_buy_order_fills_seller FOREIGN KEY (seller_character_id) REFERENCES public.characters(id);


--
-- Name: market_buy_orders fk_market_buy_orders_buyer; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.market_buy_orders
    ADD CONSTRAINT fk_market_buy_orders_buyer FOREIGN KEY (buyer_character_id) REFERENCES public.characters(id);


--
-- Name: market_buy_orders fk_market_buy_orders_item; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.market_buy_orders
    ADD CONSTRAINT fk_market_buy_orders_item FOREIGN KEY (item_definition_id) REFERENCES public.item_definitions(id);


--
-- Name: market_listings fk_market_listings_buyer; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.market_listings
    ADD CONSTRAINT fk_market_listings_buyer FOREIGN KEY (buyer_character_id) REFERENCES public.characters(id);


--
-- Name: market_listings fk_market_listings_item_definition; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.market_listings
    ADD CONSTRAINT fk_market_listings_item_definition FOREIGN KEY (item_definition_id) REFERENCES public.item_definitions(id);


--
-- Name: market_listings fk_market_listings_item_instance; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.market_listings
    ADD CONSTRAINT fk_market_listings_item_instance FOREIGN KEY (item_instance_id) REFERENCES public.item_instances(id) ON DELETE SET NULL;


--
-- Name: market_listings fk_market_listings_seller; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.market_listings
    ADD CONSTRAINT fk_market_listings_seller FOREIGN KEY (seller_character_id) REFERENCES public.characters(id);


--
-- Name: merchant_stock fk_merchant_stock_item; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.merchant_stock
    ADD CONSTRAINT fk_merchant_stock_item FOREIGN KEY (item_definition_id) REFERENCES public.item_definitions(id);


--
-- Name: merchant_stock fk_merchant_stock_merchant; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.merchant_stock
    ADD CONSTRAINT fk_merchant_stock_merchant FOREIGN KEY (merchant_id) REFERENCES public.merchant_definitions(id);


--
-- Name: monster_loot_entries fk_monster_loot_item; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.monster_loot_entries
    ADD CONSTRAINT fk_monster_loot_item FOREIGN KEY (item_definition_id) REFERENCES public.item_definitions(id);


--
-- Name: monster_loot_entries fk_monster_loot_monster; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.monster_loot_entries
    ADD CONSTRAINT fk_monster_loot_monster FOREIGN KEY (monster_definition_id) REFERENCES public.monster_definitions(id);


--
-- Name: salvage_outputs fk_salvage_outputs_result; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.salvage_outputs
    ADD CONSTRAINT fk_salvage_outputs_result FOREIGN KEY (result_item_definition_id) REFERENCES public.item_definitions(id);


--
-- Name: salvage_outputs fk_salvage_outputs_source; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.salvage_outputs
    ADD CONSTRAINT fk_salvage_outputs_source FOREIGN KEY (source_item_definition_id) REFERENCES public.item_definitions(id);


--
-- Name: technique_loadout_slots fk_technique_loadout_character; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.technique_loadout_slots
    ADD CONSTRAINT fk_technique_loadout_character FOREIGN KEY (character_id) REFERENCES public.characters(id);


--
-- Name: technique_loadout_slots fk_technique_loadout_definition; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.technique_loadout_slots
    ADD CONSTRAINT fk_technique_loadout_definition FOREIGN KEY (technique_code) REFERENCES public.combat_technique_definitions(code);


--
-- Name: weapon_masteries fk_weapon_masteries_character; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.weapon_masteries
    ADD CONSTRAINT fk_weapon_masteries_character FOREIGN KEY (character_id) REFERENCES public.characters(id);


--
-- Name: pvp_battle_history pvp_battle_history_character_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.pvp_battle_history
    ADD CONSTRAINT pvp_battle_history_character_id_fkey FOREIGN KEY (character_id) REFERENCES public.characters(id);


--
-- Name: pvp_battle_history pvp_battle_history_match_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.pvp_battle_history
    ADD CONSTRAINT pvp_battle_history_match_id_fkey FOREIGN KEY (match_id) REFERENCES public.pvp_matches(id);


--
-- Name: pvp_battle_history pvp_battle_history_opponent_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.pvp_battle_history
    ADD CONSTRAINT pvp_battle_history_opponent_id_fkey FOREIGN KEY (opponent_id) REFERENCES public.characters(id);


--
-- Name: pvp_match_events pvp_match_events_match_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.pvp_match_events
    ADD CONSTRAINT pvp_match_events_match_id_fkey FOREIGN KEY (match_id) REFERENCES public.pvp_matches(id);


--
-- Name: pvp_match_snapshots pvp_match_snapshots_match_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.pvp_match_snapshots
    ADD CONSTRAINT pvp_match_snapshots_match_id_fkey FOREIGN KEY (match_id) REFERENCES public.pvp_matches(id);


--
-- Name: pvp_match_statuses pvp_match_statuses_match_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.pvp_match_statuses
    ADD CONSTRAINT pvp_match_statuses_match_id_fkey FOREIGN KEY (match_id) REFERENCES public.pvp_matches(id);


--
-- Name: pvp_matches pvp_matches_attacker_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.pvp_matches
    ADD CONSTRAINT pvp_matches_attacker_id_fkey FOREIGN KEY (attacker_id) REFERENCES public.characters(id);


--
-- Name: pvp_matches pvp_matches_defender_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.pvp_matches
    ADD CONSTRAINT pvp_matches_defender_id_fkey FOREIGN KEY (defender_id) REFERENCES public.characters(id);


--
--

--
-- Reference / seed data
--

--
--



SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

--
-- Data for Name: accounts; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- Data for Name: locations; Type: TABLE DATA; Schema: public; Owner: -
--

INSERT INTO public.locations (id, code, name, description, safety, region, created_at, recommended_level_min, recommended_level_max) VALUES ('a0000000-0000-4000-8000-000000000001', 'CITY_SQUARE', 'City Square', 'The bustling heart of Greyhaven. Merchants call out, travelers pass through, and every road in the region meets here.', 'SAFE', 'Greyhaven', '2026-01-01 00:00:00+00', NULL, NULL);
INSERT INTO public.locations (id, code, name, description, safety, region, created_at, recommended_level_min, recommended_level_max) VALUES ('a0000000-0000-4000-8000-000000000002', 'TAVERN', 'Tavern', 'A warm common room of oak and smoke. Adventurers rest here, swap rumors, and arrange expeditions into the wilds.', 'SAFE', 'Greyhaven', '2026-01-01 00:00:00+00', NULL, NULL);
INSERT INTO public.locations (id, code, name, description, safety, region, created_at, recommended_level_min, recommended_level_max) VALUES ('a0000000-0000-4000-8000-000000000003', 'MARKET', 'Market', 'Stalls crowd the square with goods and bargains. Players trade surplus loot and rare finds here.', 'SAFE', 'Greyhaven', '2026-01-01 00:00:00+00', NULL, NULL);
INSERT INTO public.locations (id, code, name, description, safety, region, created_at, recommended_level_min, recommended_level_max) VALUES ('a0000000-0000-4000-8000-000000000004', 'OLD_TOWN', 'Old Town', 'Narrow alleys and crumbling stone. Trouble lurks behind shuttered windows — encounters are common here.', 'DANGEROUS', 'Greyhaven', '2026-01-01 00:00:00+00', 1, 5);
INSERT INTO public.locations (id, code, name, description, safety, region, created_at, recommended_level_min, recommended_level_max) VALUES ('a0000000-0000-4000-8000-000000000005', 'FOREST', 'Forest', 'Dense woods press close to the road. Wolves and bandits hunt among the trees; forest patrols depart from here.', 'DANGEROUS', 'Greyhaven', '2026-01-01 00:00:00+00', 3, 8);
INSERT INTO public.locations (id, code, name, description, safety, region, created_at, recommended_level_min, recommended_level_max) VALUES ('a0000000-0000-4000-8000-000000000006', 'NORTH_ROAD', 'North Road', 'A lonely stretch of packed earth heading north from the city. Travelers are few, and danger is not.', 'DANGEROUS', 'Greyhaven', '2026-01-01 00:00:00+00', 3, 10);
INSERT INTO public.locations (id, code, name, description, safety, region, created_at, recommended_level_min, recommended_level_max) VALUES ('a0000000-0000-4000-8000-000000000007', 'ARENA', 'Arena', 'Sand, stone tiers, and a gate that will one day open for duels. For now the pit is quiet — a place to gather, not to fight.', 'SAFE', 'Greyhaven', '2026-01-01 00:00:00+00', NULL, NULL);
INSERT INTO public.locations (id, code, name, description, safety, region, created_at, recommended_level_min, recommended_level_max) VALUES ('a0000000-0000-4000-8000-000000000008', 'CRAFTSMEN_WARD', 'Craftsmen Ward', 'Hammers ring on anvils and quench-steam hangs in the lanes. The forges of Greyhaven wait for those who would take up a trade.', 'SAFE', 'Greyhaven', '2026-01-01 00:00:00+00', NULL, NULL);
INSERT INTO public.locations (id, code, name, description, safety, region, created_at, recommended_level_min, recommended_level_max) VALUES ('a0000000-0000-4000-8000-000000000009', 'HARBOUR', 'Harbour', 'Slick stones, tar, and shouting from the wharves. Smugglers and dock brawlers test anyone who lingers after dusk.', 'DANGEROUS', 'Greyhaven', '2026-01-01 00:00:00+00', 4, 9);
INSERT INTO public.locations (id, code, name, description, safety, region, created_at, recommended_level_min, recommended_level_max) VALUES ('a0000000-0000-4000-8000-00000000000a', 'SEWERS', 'Sewers', 'A brick throat under Old Town. The air bites, and things that should not hunt still do.', 'DANGEROUS', 'Greyhaven', '2026-01-01 00:00:00+00', 5, 10);
INSERT INTO public.locations (id, code, name, description, safety, region, created_at, recommended_level_min, recommended_level_max) VALUES ('a0000000-0000-4000-8000-00000000000b', 'OLD_MINE', 'Old Mine', 'Collapsed galleries and a rusted cage-winch. Something still works the dark with pick and fist.', 'DANGEROUS', 'Greyhaven', '2026-01-01 00:00:00+00', 8, 15);
INSERT INTO public.locations (id, code, name, description, safety, region, created_at, recommended_level_min, recommended_level_max) VALUES ('a0000000-0000-4000-8000-00000000000c', 'BANDIT_CAMP', 'Bandit Camp', 'Palings, stolen banners, and cookfires. The road''s predators keep their own order here.', 'DANGEROUS', 'Greyhaven', '2026-01-01 00:00:00+00', 12, 20);
INSERT INTO public.locations (id, code, name, description, safety, region, created_at, recommended_level_min, recommended_level_max) VALUES ('a0000000-0000-4000-8000-00000000000d', 'ANCIENT_RUINS', 'Ancient Ruins', 'Broken colonnades above a sealed keep. The stones remember a warden who never left.', 'DANGEROUS', 'Greyhaven', '2026-01-01 00:00:00+00', 18, 30);


--
-- Data for Name: characters; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- Data for Name: activity_entries; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- Data for Name: affix_definitions; Type: TABLE DATA; Schema: public; Owner: -
--

INSERT INTO public.affix_definitions (code, kind, display_name, stat, magnitude_min, magnitude_max, allowed_item_types, allowed_equipment_slots, allowed_weapon_families, allowed_armor_categories) VALUES ('SHARP', 'PREFIX', 'Sharp', 'DAMAGE_PERCENT', 4, 8, 'WEAPON', '', '', '');
INSERT INTO public.affix_definitions (code, kind, display_name, stat, magnitude_min, magnitude_max, allowed_item_types, allowed_equipment_slots, allowed_weapon_families, allowed_armor_categories) VALUES ('BALANCED', 'PREFIX', 'Balanced', 'ACCURACY', 3, 6, 'WEAPON', '', '', '');
INSERT INTO public.affix_definitions (code, kind, display_name, stat, magnitude_min, magnitude_max, allowed_item_types, allowed_equipment_slots, allowed_weapon_families, allowed_armor_categories) VALUES ('VICIOUS', 'PREFIX', 'Vicious', 'CRIT_CHANCE', 2, 4, 'WEAPON', '', '', '');
INSERT INTO public.affix_definitions (code, kind, display_name, stat, magnitude_min, magnitude_max, allowed_item_types, allowed_equipment_slots, allowed_weapon_families, allowed_armor_categories) VALUES ('QUICK', 'PREFIX', 'Quick', 'STAMINA_COST', 1, 3, 'WEAPON', '', '', '');
INSERT INTO public.affix_definitions (code, kind, display_name, stat, magnitude_min, magnitude_max, allowed_item_types, allowed_equipment_slots, allowed_weapon_families, allowed_armor_categories) VALUES ('REINFORCED', 'PREFIX', 'Reinforced', 'ARMOR', 2, 5, 'ARMOR', '', '', '');
INSERT INTO public.affix_definitions (code, kind, display_name, stat, magnitude_min, magnitude_max, allowed_item_types, allowed_equipment_slots, allowed_weapon_families, allowed_armor_categories) VALUES ('FORTIFIED', 'PREFIX', 'Fortified', 'ARMOR', 3, 6, 'ARMOR', '', '', '');
INSERT INTO public.affix_definitions (code, kind, display_name, stat, magnitude_min, magnitude_max, allowed_item_types, allowed_equipment_slots, allowed_weapon_families, allowed_armor_categories) VALUES ('HARDENED', 'PREFIX', 'Hardened', 'ARMOR', 1, 3, 'ARMOR', '', '', '');
INSERT INTO public.affix_definitions (code, kind, display_name, stat, magnitude_min, magnitude_max, allowed_item_types, allowed_equipment_slots, allowed_weapon_families, allowed_armor_categories) VALUES ('NIMBLE', 'PREFIX', 'Nimble', 'DODGE', 2, 4, 'ARMOR', '', '', 'LIGHT');
INSERT INTO public.affix_definitions (code, kind, display_name, stat, magnitude_min, magnitude_max, allowed_item_types, allowed_equipment_slots, allowed_weapon_families, allowed_armor_categories) VALUES ('GUARDED', 'PREFIX', 'Guarded', 'ARMOR', 1, 3, 'ARMOR', 'OFF_HAND', '', '');
INSERT INTO public.affix_definitions (code, kind, display_name, stat, magnitude_min, magnitude_max, allowed_item_types, allowed_equipment_slots, allowed_weapon_families, allowed_armor_categories) VALUES ('GLIMMERING', 'PREFIX', 'Glimmering', 'CRIT_CHANCE', 1, 3, 'ACCESSORY', '', '', '');
INSERT INTO public.affix_definitions (code, kind, display_name, stat, magnitude_min, magnitude_max, allowed_item_types, allowed_equipment_slots, allowed_weapon_families, allowed_armor_categories) VALUES ('WARDING', 'PREFIX', 'Warding', 'ARMOR', 1, 2, 'ACCESSORY', '', '', '');
INSERT INTO public.affix_definitions (code, kind, display_name, stat, magnitude_min, magnitude_max, allowed_item_types, allowed_equipment_slots, allowed_weapon_families, allowed_armor_categories) VALUES ('SWIFT', 'PREFIX', 'Swift', 'DODGE', 1, 3, 'ACCESSORY', '', '', '');
INSERT INTO public.affix_definitions (code, kind, display_name, stat, magnitude_min, magnitude_max, allowed_item_types, allowed_equipment_slots, allowed_weapon_families, allowed_armor_categories) VALUES ('OF_STRENGTH', 'SUFFIX', 'of Strength', 'STRENGTH', 1, 3, 'WEAPON,ARMOR,ACCESSORY', '', '', '');
INSERT INTO public.affix_definitions (code, kind, display_name, stat, magnitude_min, magnitude_max, allowed_item_types, allowed_equipment_slots, allowed_weapon_families, allowed_armor_categories) VALUES ('OF_THE_FOX', 'SUFFIX', 'of the Fox', 'AGILITY', 1, 3, 'WEAPON,ARMOR,ACCESSORY', '', '', '');
INSERT INTO public.affix_definitions (code, kind, display_name, stat, magnitude_min, magnitude_max, allowed_item_types, allowed_equipment_slots, allowed_weapon_families, allowed_armor_categories) VALUES ('OF_VITALITY', 'SUFFIX', 'of Vitality', 'ENDURANCE', 1, 3, 'WEAPON,ARMOR,ACCESSORY', '', '', '');
INSERT INTO public.affix_definitions (code, kind, display_name, stat, magnitude_min, magnitude_max, allowed_item_types, allowed_equipment_slots, allowed_weapon_families, allowed_armor_categories) VALUES ('OF_PRECISION', 'SUFFIX', 'of Precision', 'PERCEPTION', 1, 3, 'WEAPON,ARMOR,ACCESSORY', '', '', '');


--
-- Data for Name: arena_defense_profiles; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- Data for Name: character_professions; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- Data for Name: combat_technique_definitions; Type: TABLE DATA; Schema: public; Owner: -
--

INSERT INTO public.combat_technique_definitions (code, display_name, description, weapon_family, unlock_mastery_level, kind, effect_code, stamina_cost, accuracy_modifier, damage_percent_modifier, applies_status, status_stacks, status_duration_rounds, tags) VALUES ('SWORD_RIPOSTE', 'Riposte', 'A precise counter after an opening. Contract for Combat 2.0.', 'SWORD', 2, 'ACTIVE', 'RIPOSTE', 8, 8, 0, NULL, 0, 0, 'COUNTER');
INSERT INTO public.combat_technique_definitions (code, display_name, description, weapon_family, unlock_mastery_level, kind, effect_code, stamina_cost, accuracy_modifier, damage_percent_modifier, applies_status, status_stacks, status_duration_rounds, tags) VALUES ('SWORD_DEEP_CUT', 'Deep Cut', 'A heavier slash intended to open a bleeding wound.', 'SWORD', 4, 'ACTIVE', 'DEEP_CUT', 12, 0, 15, 'BLEED', 1, 3, '');
INSERT INTO public.combat_technique_definitions (code, display_name, description, weapon_family, unlock_mastery_level, kind, effect_code, stamina_cost, accuracy_modifier, damage_percent_modifier, applies_status, status_stacks, status_duration_rounds, tags) VALUES ('SWORD_GUARD_BREAK', 'Guard Break', 'Forces the opponent off balance by hammering their guard.', 'SWORD', 6, 'ACTIVE', 'GUARD_BREAK', 14, -4, 10, 'OFF_BALANCE', 1, 1, '');
INSERT INTO public.combat_technique_definitions (code, display_name, description, weapon_family, unlock_mastery_level, kind, effect_code, stamina_cost, accuracy_modifier, damage_percent_modifier, applies_status, status_stacks, status_duration_rounds, tags) VALUES ('SWORD_DUELISTS_TEMPO', 'Duelist''s Tempo', 'An advanced sequence that rewards measured swordplay.', 'SWORD', 8, 'ACTIVE', 'DUELISTS_TEMPO', 16, 6, 20, NULL, 0, 0, 'ADVANCED');
INSERT INTO public.combat_technique_definitions (code, display_name, description, weapon_family, unlock_mastery_level, kind, effect_code, stamina_cost, accuracy_modifier, damage_percent_modifier, applies_status, status_stacks, status_duration_rounds, tags) VALUES ('SWORD_MASTERY', 'Sword Mastery', 'Passive familiarity with blades. Resolved in Combat 2.0.', 'SWORD', 10, 'PASSIVE', 'SWORD_MASTERY', 0, 4, 5, NULL, 0, 0, 'MASTERY_PASSIVE');
INSERT INTO public.combat_technique_definitions (code, display_name, description, weapon_family, unlock_mastery_level, kind, effect_code, stamina_cost, accuracy_modifier, damage_percent_modifier, applies_status, status_stacks, status_duration_rounds, tags) VALUES ('AXE_RENDING_CHOP', 'Rending Chop', 'A brutal chop meant to tear through flesh.', 'AXE', 2, 'ACTIVE', 'RENDING_CHOP', 10, -2, 18, 'BLEED', 1, 2, '');
INSERT INTO public.combat_technique_definitions (code, display_name, description, weapon_family, unlock_mastery_level, kind, effect_code, stamina_cost, accuracy_modifier, damage_percent_modifier, applies_status, status_stacks, status_duration_rounds, tags) VALUES ('AXE_CLEAVE', 'Cleave', 'A wide swing. Contract reserved for Combat 2.0.', 'AXE', 4, 'ACTIVE', 'CLEAVE', 14, -6, 22, NULL, 0, 0, 'CLEAVE');
INSERT INTO public.combat_technique_definitions (code, display_name, description, weapon_family, unlock_mastery_level, kind, effect_code, stamina_cost, accuracy_modifier, damage_percent_modifier, applies_status, status_stacks, status_duration_rounds, tags) VALUES ('AXE_SHATTER_ARMOR', 'Shatter Armor', 'Splits protection and leaves the target easier to wound.', 'AXE', 6, 'ACTIVE', 'SHATTER_ARMOR', 14, -4, 12, 'ARMOR_BREAK', 1, 3, '');
INSERT INTO public.combat_technique_definitions (code, display_name, description, weapon_family, unlock_mastery_level, kind, effect_code, stamina_cost, accuracy_modifier, damage_percent_modifier, applies_status, status_stacks, status_duration_rounds, tags) VALUES ('AXE_EXECUTIONER', 'Executioner', 'An advanced finishing blow against wounded foes.', 'AXE', 8, 'ACTIVE', 'EXECUTIONER', 18, 0, 28, NULL, 0, 0, 'ADVANCED');
INSERT INTO public.combat_technique_definitions (code, display_name, description, weapon_family, unlock_mastery_level, kind, effect_code, stamina_cost, accuracy_modifier, damage_percent_modifier, applies_status, status_stacks, status_duration_rounds, tags) VALUES ('AXE_MASTERY', 'Axe Mastery', 'Passive familiarity with axes. Resolved in Combat 2.0.', 'AXE', 10, 'PASSIVE', 'AXE_MASTERY', 0, 0, 8, NULL, 0, 0, 'MASTERY_PASSIVE');
INSERT INTO public.combat_technique_definitions (code, display_name, description, weapon_family, unlock_mastery_level, kind, effect_code, stamina_cost, accuracy_modifier, damage_percent_modifier, applies_status, status_stacks, status_duration_rounds, tags) VALUES ('MACE_CRUSHING_BLOW', 'Crushing Blow', 'A heavy strike that trades finesse for impact.', 'MACE', 2, 'ACTIVE', 'CRUSHING_BLOW', 10, -4, 20, NULL, 0, 0, '');
INSERT INTO public.combat_technique_definitions (code, display_name, description, weapon_family, unlock_mastery_level, kind, effect_code, stamina_cost, accuracy_modifier, damage_percent_modifier, applies_status, status_stacks, status_duration_rounds, tags) VALUES ('MACE_CONCUSSIVE_STRIKE', 'Concussive Strike', 'A stunning impact. Anti-chain rules belong to Combat 2.0.', 'MACE', 4, 'ACTIVE', 'CONCUSSIVE_STRIKE', 16, -2, 8, 'STUN', 1, 1, '');
INSERT INTO public.combat_technique_definitions (code, display_name, description, weapon_family, unlock_mastery_level, kind, effect_code, stamina_cost, accuracy_modifier, damage_percent_modifier, applies_status, status_stacks, status_duration_rounds, tags) VALUES ('MACE_BREAK_GUARD', 'Break Guard', 'Smashes through a defensive stance.', 'MACE', 6, 'ACTIVE', 'BREAK_GUARD', 14, -4, 10, 'ARMOR_BREAK', 1, 2, '');
INSERT INTO public.combat_technique_definitions (code, display_name, description, weapon_family, unlock_mastery_level, kind, effect_code, stamina_cost, accuracy_modifier, damage_percent_modifier, applies_status, status_stacks, status_duration_rounds, tags) VALUES ('MACE_OVERWHELM', 'Overwhelm', 'An advanced press that keeps the opponent reeling.', 'MACE', 8, 'ACTIVE', 'OVERWHELM', 18, 0, 24, 'OFF_BALANCE', 1, 1, 'ADVANCED');
INSERT INTO public.combat_technique_definitions (code, display_name, description, weapon_family, unlock_mastery_level, kind, effect_code, stamina_cost, accuracy_modifier, damage_percent_modifier, applies_status, status_stacks, status_duration_rounds, tags) VALUES ('MACE_MASTERY', 'Mace Mastery', 'Passive familiarity with blunt weapons. Resolved in Combat 2.0.', 'MACE', 10, 'PASSIVE', 'MACE_MASTERY', 0, 0, 8, NULL, 0, 0, 'MASTERY_PASSIVE');
INSERT INTO public.combat_technique_definitions (code, display_name, description, weapon_family, unlock_mastery_level, kind, effect_code, stamina_cost, accuracy_modifier, damage_percent_modifier, applies_status, status_stacks, status_duration_rounds, tags) VALUES ('DAGGER_FEINT', 'Feint', 'A deceptive cut that leaves the target off balance.', 'DAGGER', 2, 'ACTIVE', 'FEINT', 6, 6, 0, 'OFF_BALANCE', 1, 1, '');
INSERT INTO public.combat_technique_definitions (code, display_name, description, weapon_family, unlock_mastery_level, kind, effect_code, stamina_cost, accuracy_modifier, damage_percent_modifier, applies_status, status_stacks, status_duration_rounds, tags) VALUES ('DAGGER_POISONED_STRIKE', 'Poisoned Strike', 'Delivers lingering toxin. Combat 2.0 owns the tick.', 'DAGGER', 4, 'ACTIVE', 'POISONED_STRIKE', 10, 2, 5, 'POISON', 1, 4, '');
INSERT INTO public.combat_technique_definitions (code, display_name, description, weapon_family, unlock_mastery_level, kind, effect_code, stamina_cost, accuracy_modifier, damage_percent_modifier, applies_status, status_stacks, status_duration_rounds, tags) VALUES ('DAGGER_EVASIVE_CUT', 'Evasive Cut', 'A light attack that favors positioning over power.', 'DAGGER', 6, 'ACTIVE', 'EVASIVE_CUT', 8, 8, 8, NULL, 0, 0, '');
INSERT INTO public.combat_technique_definitions (code, display_name, description, weapon_family, unlock_mastery_level, kind, effect_code, stamina_cost, accuracy_modifier, damage_percent_modifier, applies_status, status_stacks, status_duration_rounds, tags) VALUES ('DAGGER_FINISHER', 'Finisher', 'An advanced strike against a compromised foe.', 'DAGGER', 8, 'ACTIVE', 'FINISHER', 14, 4, 30, NULL, 0, 0, 'ADVANCED');
INSERT INTO public.combat_technique_definitions (code, display_name, description, weapon_family, unlock_mastery_level, kind, effect_code, stamina_cost, accuracy_modifier, damage_percent_modifier, applies_status, status_stacks, status_duration_rounds, tags) VALUES ('DAGGER_MASTERY', 'Dagger Mastery', 'Passive familiarity with daggers. Resolved in Combat 2.0.', 'DAGGER', 10, 'PASSIVE', 'DAGGER_MASTERY', 0, 6, 4, NULL, 0, 0, 'MASTERY_PASSIVE');
INSERT INTO public.combat_technique_definitions (code, display_name, description, weapon_family, unlock_mastery_level, kind, effect_code, stamina_cost, accuracy_modifier, damage_percent_modifier, applies_status, status_stacks, status_duration_rounds, tags) VALUES ('BOW_AIMED_SHOT', 'Aimed Shot', 'A careful shot that favors accuracy.', 'BOW', 2, 'ACTIVE', 'AIMED_SHOT', 10, 12, 5, NULL, 0, 0, '');
INSERT INTO public.combat_technique_definitions (code, display_name, description, weapon_family, unlock_mastery_level, kind, effect_code, stamina_cost, accuracy_modifier, damage_percent_modifier, applies_status, status_stacks, status_duration_rounds, tags) VALUES ('BOW_CRIPPLING_SHOT', 'Crippling Shot', 'A shot meant to hobble movement.', 'BOW', 4, 'ACTIVE', 'CRIPPLING_SHOT', 12, 4, 8, 'OFF_BALANCE', 1, 2, '');
INSERT INTO public.combat_technique_definitions (code, display_name, description, weapon_family, unlock_mastery_level, kind, effect_code, stamina_cost, accuracy_modifier, damage_percent_modifier, applies_status, status_stacks, status_duration_rounds, tags) VALUES ('BOW_PIERCING_SHOT', 'Piercing Shot', 'Ignores part of a target''s protection. Combat 2.0 resolves it.', 'BOW', 6, 'ACTIVE', 'PIERCING_SHOT', 14, 2, 16, 'ARMOR_BREAK', 1, 2, '');
INSERT INTO public.combat_technique_definitions (code, display_name, description, weapon_family, unlock_mastery_level, kind, effect_code, stamina_cost, accuracy_modifier, damage_percent_modifier, applies_status, status_stacks, status_duration_rounds, tags) VALUES ('BOW_RAPID_SHOT', 'Rapid Shot', 'An advanced flurry that spends stamina for tempo.', 'BOW', 8, 'ACTIVE', 'RAPID_SHOT', 16, -2, 12, NULL, 0, 0, 'ADVANCED');
INSERT INTO public.combat_technique_definitions (code, display_name, description, weapon_family, unlock_mastery_level, kind, effect_code, stamina_cost, accuracy_modifier, damage_percent_modifier, applies_status, status_stacks, status_duration_rounds, tags) VALUES ('BOW_MASTERY', 'Bow Mastery', 'Passive familiarity with bows. Resolved in Combat 2.0.', 'BOW', 10, 'PASSIVE', 'BOW_MASTERY', 0, 6, 4, NULL, 0, 0, 'MASTERY_PASSIVE');


--
-- Data for Name: character_techniques; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- Data for Name: character_unique_drops; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- Data for Name: chat_messages; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- Data for Name: dungeon_definitions; Type: TABLE DATA; Schema: public; Owner: -
--

INSERT INTO public.dungeon_definitions (id, code, name, entrance_location_id) VALUES ('90000000-0000-4000-8000-000000000001', 'RUINED_KEEP', 'Ruined Keep', 'a0000000-0000-4000-8000-00000000000d');


--
-- Data for Name: dungeon_runs; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- Data for Name: monster_definitions; Type: TABLE DATA; Schema: public; Owner: -
--

INSERT INTO public.monster_definitions (id, code, name, level, max_health, damage_min, damage_max, xp_reward, gold_min, gold_max, created_at, armor, accuracy, dodge, critical_chance, max_stamina, ai_archetype, signature_status, monster_tier) VALUES ('d0000000-0000-4000-8000-000000000001', 'STREET_THUG', 'Street Thug', 1, 70, 5, 8, 20, 4, 10, '2026-01-01 00:00:00+00', 4, 72, 4, 5, 40, 'AGGRESSIVE', NULL, 'NORMAL');
INSERT INTO public.monster_definitions (id, code, name, level, max_health, damage_min, damage_max, xp_reward, gold_min, gold_max, created_at, armor, accuracy, dodge, critical_chance, max_stamina, ai_archetype, signature_status, monster_tier) VALUES ('d0000000-0000-4000-8000-000000000002', 'GIANT_RAT', 'Giant Rat', 1, 50, 3, 6, 15, 2, 6, '2026-01-01 00:00:00+00', 2, 70, 12, 8, 35, 'ASSASSIN', NULL, 'NORMAL');
INSERT INTO public.monster_definitions (id, code, name, level, max_health, damage_min, damage_max, xp_reward, gold_min, gold_max, created_at, armor, accuracy, dodge, critical_chance, max_stamina, ai_archetype, signature_status, monster_tier) VALUES ('d0000000-0000-4000-8000-000000000003', 'FOREST_WOLF', 'Forest Wolf', 2, 100, 7, 11, 30, 6, 14, '2026-01-01 00:00:00+00', 3, 74, 8, 6, 45, 'BERSERKER', 'BLEED', 'NORMAL');
INSERT INTO public.monster_definitions (id, code, name, level, max_health, damage_min, damage_max, xp_reward, gold_min, gold_max, created_at, armor, accuracy, dodge, critical_chance, max_stamina, ai_archetype, signature_status, monster_tier) VALUES ('d0000000-0000-4000-8000-000000000004', 'BANDIT', 'Bandit', 3, 130, 10, 15, 45, 10, 22, '2026-01-01 00:00:00+00', 8, 76, 6, 5, 50, 'DEFENSIVE', NULL, 'NORMAL');
INSERT INTO public.monster_definitions (id, code, name, level, max_health, damage_min, damage_max, xp_reward, gold_min, gold_max, created_at, armor, accuracy, dodge, critical_chance, max_stamina, ai_archetype, signature_status, monster_tier) VALUES ('d0000000-0000-4000-8000-000000000005', 'BANDIT_VETERAN', 'Bandit Veteran', 5, 220, 15, 22, 80, 18, 35, '2026-01-01 00:00:00+00', 16, 80, 4, 5, 55, 'ARMORED', 'ARMOR_BREAK', 'ELITE');
INSERT INTO public.monster_definitions (id, code, name, level, max_health, damage_min, damage_max, xp_reward, gold_min, gold_max, created_at, armor, accuracy, dodge, critical_chance, max_stamina, ai_archetype, signature_status, monster_tier) VALUES ('d0000000-0000-4000-8000-000000000006', 'DOCK_BRAWLER', 'Dock Brawler', 4, 120, 8, 12, 48, 8, 18, '2026-01-01 00:00:00+00', 10, 74, 6, 5, 55, 'DEFENSIVE', NULL, 'NORMAL');
INSERT INTO public.monster_definitions (id, code, name, level, max_health, damage_min, damage_max, xp_reward, gold_min, gold_max, created_at, armor, accuracy, dodge, critical_chance, max_stamina, ai_archetype, signature_status, monster_tier) VALUES ('d0000000-0000-4000-8000-00000000000a', 'MINE_CRAWLER', 'Mine Crawler', 9, 140, 10, 15, 180, 12, 26, '2026-01-01 00:00:00+00', 6, 78, 16, 7, 48, 'ASSASSIN', 'BLEED', 'NORMAL');
INSERT INTO public.monster_definitions (id, code, name, level, max_health, damage_min, damage_max, xp_reward, gold_min, gold_max, created_at, armor, accuracy, dodge, critical_chance, max_stamina, ai_archetype, signature_status, monster_tier) VALUES ('d0000000-0000-4000-8000-00000000000d', 'CAMP_CUTTHROAT', 'Camp Cutthroat', 14, 130, 12, 18, 280, 18, 36, '2026-01-01 00:00:00+00', 5, 84, 18, 14, 48, 'AGGRESSIVE', NULL, 'NORMAL');
INSERT INTO public.monster_definitions (id, code, name, level, max_health, damage_min, damage_max, xp_reward, gold_min, gold_max, created_at, armor, accuracy, dodge, critical_chance, max_stamina, ai_archetype, signature_status, monster_tier) VALUES ('d0000000-0000-4000-8000-00000000000e', 'SHIELDED_RAIDER', 'Shielded Raider', 15, 210, 11, 16, 320, 20, 38, '2026-01-01 00:00:00+00', 18, 76, 5, 5, 58, 'SHIELDED', 'BLEED', 'ELITE');
INSERT INTO public.monster_definitions (id, code, name, level, max_health, damage_min, damage_max, xp_reward, gold_min, gold_max, created_at, armor, accuracy, dodge, critical_chance, max_stamina, ai_archetype, signature_status, monster_tier) VALUES ('d0000000-0000-4000-8000-000000000011', 'RUIN_GUARDIAN', 'Ruin Guardian', 22, 280, 14, 19, 700, 30, 55, '2026-01-01 00:00:00+00', 14, 78, 8, 5, 64, 'BERSERKER', 'BLEED', 'ELITE');
INSERT INTO public.monster_definitions (id, code, name, level, max_health, damage_min, damage_max, xp_reward, gold_min, gold_max, created_at, armor, accuracy, dodge, critical_chance, max_stamina, ai_archetype, signature_status, monster_tier) VALUES ('d0000000-0000-4000-8000-000000000012', 'WARDEN_OF_THE_KEEP', 'Warden of the Keep', 24, 360, 16, 22, 1800, 80, 120, '2026-01-01 00:00:00+00', 22, 82, 6, 6, 70, 'MARKSMAN', 'OFF_BALANCE', 'BOSS');
INSERT INTO public.monster_definitions (id, code, name, level, max_health, damage_min, damage_max, xp_reward, gold_min, gold_max, created_at, armor, accuracy, dodge, critical_chance, max_stamina, ai_archetype, signature_status, monster_tier) VALUES ('d0000000-0000-4000-8000-000000000008', 'PLAGUE_RAT', 'Plague Rat', 6, 80, 6, 10, 85, 6, 14, '2026-01-01 00:00:00+00', 3, 76, 16, 10, 40, 'ASSASSIN', 'POISON', 'NORMAL');
INSERT INTO public.monster_definitions (id, code, name, level, max_health, damage_min, damage_max, xp_reward, gold_min, gold_max, created_at, armor, accuracy, dodge, critical_chance, max_stamina, ai_archetype, signature_status, monster_tier) VALUES ('d0000000-0000-4000-8000-000000000009', 'SEWER_WATCHMAN', 'Sewer Watchman', 7, 150, 8, 12, 95, 10, 20, '2026-01-01 00:00:00+00', 12, 74, 4, 4, 50, 'SHIELDED', NULL, 'NORMAL');
INSERT INTO public.monster_definitions (id, code, name, level, max_health, damage_min, damage_max, xp_reward, gold_min, gold_max, created_at, armor, accuracy, dodge, critical_chance, max_stamina, ai_archetype, signature_status, monster_tier) VALUES ('d0000000-0000-4000-8000-00000000000b', 'CAVE_BRUTE', 'Cave Brute', 11, 200, 12, 16, 240, 16, 32, '2026-01-01 00:00:00+00', 28, 70, 2, 4, 50, 'ARMORED', NULL, 'NORMAL');
INSERT INTO public.monster_definitions (id, code, name, level, max_health, damage_min, damage_max, xp_reward, gold_min, gold_max, created_at, armor, accuracy, dodge, critical_chance, max_stamina, ai_archetype, signature_status, monster_tier) VALUES ('d0000000-0000-4000-8000-00000000000c', 'PIT_OVERSEER', 'Pit Overseer', 13, 240, 13, 18, 400, 22, 40, '2026-01-01 00:00:00+00', 14, 80, 6, 6, 60, 'CONTROL', 'STUN', 'MINI_BOSS');
INSERT INTO public.monster_definitions (id, code, name, level, max_health, damage_min, damage_max, xp_reward, gold_min, gold_max, created_at, armor, accuracy, dodge, critical_chance, max_stamina, ai_archetype, signature_status, monster_tier) VALUES ('d0000000-0000-4000-8000-00000000000f', 'BANDIT_LIEUTENANT', 'Bandit Lieutenant', 16, 260, 16, 22, 480, 28, 48, '2026-01-01 00:00:00+00', 10, 80, 8, 8, 62, 'BERSERKER', 'BLEED', 'MINI_BOSS');
INSERT INTO public.monster_definitions (id, code, name, level, max_health, damage_min, damage_max, xp_reward, gold_min, gold_max, created_at, armor, accuracy, dodge, critical_chance, max_stamina, ai_archetype, signature_status, monster_tier) VALUES ('d0000000-0000-4000-8000-000000000010', 'RUIN_STALKER', 'Ruin Stalker', 20, 170, 14, 20, 520, 24, 44, '2026-01-01 00:00:00+00', 6, 86, 20, 12, 52, 'ASSASSIN', 'OFF_BALANCE', 'NORMAL');
INSERT INTO public.monster_definitions (id, code, name, level, max_health, damage_min, damage_max, xp_reward, gold_min, gold_max, created_at, armor, accuracy, dodge, critical_chance, max_stamina, ai_archetype, signature_status, monster_tier) VALUES ('d0000000-0000-4000-8000-000000000007', 'SMUGGLER', 'Smuggler', 6, 95, 9, 13, 70, 12, 24, '2026-01-01 00:00:00+00', 4, 92, 8, 8, 45, 'MARKSMAN', 'OFF_BALANCE', 'NORMAL');


--
-- Data for Name: encounters; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- Data for Name: combat_sessions; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- Data for Name: combat_events; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- Data for Name: item_definitions; Type: TABLE DATA; Schema: public; Owner: -
--

INSERT INTO public.item_definitions (id, code, name, description, type, rarity, base_value, required_level, weapon_damage, armor_value, heal_amount, created_at, equipment_slot, two_handed, weapon_family, armor_category, required_strength, required_agility, required_endurance, required_perception, legacy) VALUES ('c0000000-0000-4000-8000-000000000019', 'MILITIA_SHORTSWORD', 'Militia Shortsword', 'A serviceable short blade issued to Greyhaven wall watch. Better steel than rust, still honest work.', 'WEAPON', 'COMMON', 8, 1, 7, NULL, NULL, '2026-01-01 00:00:00+00', 'MAIN_HAND', false, 'SWORD', NULL, 0, 0, 0, 0, false);
INSERT INTO public.item_definitions (id, code, name, description, type, rarity, base_value, required_level, weapon_damage, armor_value, heal_amount, created_at, equipment_slot, two_handed, weapon_family, armor_category, required_strength, required_agility, required_endurance, required_perception, legacy) VALUES ('c0000000-0000-4000-8000-00000000001a', 'ARMING_SWORD', 'Arming Sword', 'A straight one-handed sword for travellers who have outgrown a notched starter.', 'WEAPON', 'COMMON', 12, 1, 8, NULL, NULL, '2026-01-01 00:00:00+00', 'MAIN_HAND', false, 'SWORD', NULL, 0, 0, 0, 0, false);
INSERT INTO public.item_definitions (id, code, name, description, type, rarity, base_value, required_level, weapon_damage, armor_value, heal_amount, created_at, equipment_slot, two_handed, weapon_family, armor_category, required_strength, required_agility, required_endurance, required_perception, legacy) VALUES ('c0000000-0000-4000-8000-00000000001b', 'VENOM_GLAND', 'Venom Gland', 'A swollen sac of sewer toxin. Alchemists will want this.', 'MATERIAL', 'UNCOMMON', 14, 1, NULL, NULL, NULL, '2026-01-01 00:00:00+00', NULL, false, NULL, NULL, 0, 0, 0, 0, false);
INSERT INTO public.item_definitions (id, code, name, description, type, rarity, base_value, required_level, weapon_damage, armor_value, heal_amount, created_at, equipment_slot, two_handed, weapon_family, armor_category, required_strength, required_agility, required_endurance, required_perception, legacy) VALUES ('c0000000-0000-4000-8000-00000000001c', 'IRON_ORE', 'Iron Ore', 'Rough ore pried from the old galleries.', 'MATERIAL', 'COMMON', 8, 1, NULL, NULL, NULL, '2026-01-01 00:00:00+00', NULL, false, NULL, NULL, 0, 0, 0, 0, false);
INSERT INTO public.item_definitions (id, code, name, description, type, rarity, base_value, required_level, weapon_damage, armor_value, heal_amount, created_at, equipment_slot, two_handed, weapon_family, armor_category, required_strength, required_agility, required_endurance, required_perception, legacy) VALUES ('c0000000-0000-4000-8000-00000000001d', 'BANDIT_TOKEN', 'Bandit Token', 'A notched iron coin elites use to mark their own.', 'MATERIAL', 'UNCOMMON', 20, 1, NULL, NULL, NULL, '2026-01-01 00:00:00+00', NULL, false, NULL, NULL, 0, 0, 0, 0, false);
INSERT INTO public.item_definitions (id, code, name, description, type, rarity, base_value, required_level, weapon_damage, armor_value, heal_amount, created_at, equipment_slot, two_handed, weapon_family, armor_category, required_strength, required_agility, required_endurance, required_perception, legacy) VALUES ('c0000000-0000-4000-8000-000000000006', 'HEALING_POTION', 'Healing Potion', 'A bitter red tonic that knits minor wounds.', 'CONSUMABLE', 'COMMON', 10, 1, NULL, NULL, 40, '2026-01-01 00:00:00+00', NULL, false, NULL, NULL, 0, 0, 0, 0, true);
INSERT INTO public.item_definitions (id, code, name, description, type, rarity, base_value, required_level, weapon_damage, armor_value, heal_amount, created_at, equipment_slot, two_handed, weapon_family, armor_category, required_strength, required_agility, required_endurance, required_perception, legacy) VALUES ('c0000000-0000-4000-8000-000000000007', 'WOLF_PELT', 'Wolf Pelt', 'Thick fur from a forest wolf. Valuable to traders.', 'MATERIAL', 'COMMON', 6, 1, NULL, NULL, NULL, '2026-01-01 00:00:00+00', NULL, false, NULL, NULL, 0, 0, 0, 0, true);
INSERT INTO public.item_definitions (id, code, name, description, type, rarity, base_value, required_level, weapon_damage, armor_value, heal_amount, created_at, equipment_slot, two_handed, weapon_family, armor_category, required_strength, required_agility, required_endurance, required_perception, legacy) VALUES ('c0000000-0000-4000-8000-00000000001e', 'RUIN_FRAGMENT', 'Ruin Fragment', 'A shard of warded stone from the keep below the ruins.', 'MATERIAL', 'RARE', 35, 1, NULL, NULL, NULL, '2026-01-01 00:00:00+00', NULL, false, NULL, NULL, 0, 0, 0, 0, false);
INSERT INTO public.item_definitions (id, code, name, description, type, rarity, base_value, required_level, weapon_damage, armor_value, heal_amount, created_at, equipment_slot, two_handed, weapon_family, armor_category, required_strength, required_agility, required_endurance, required_perception, legacy) VALUES ('c0000000-0000-4000-8000-00000000001f', 'WARDENS_SIGNET', 'Warden''s Signet', 'A heavy signet taken from the keep''s last warden. It still remembers the gate.', 'ACCESSORY', 'EPIC', 180, 18, NULL, NULL, NULL, '2026-01-01 00:00:00+00', 'AMULET', false, NULL, NULL, 0, 0, 0, 0, false);
INSERT INTO public.item_definitions (id, code, name, description, type, rarity, base_value, required_level, weapon_damage, armor_value, heal_amount, created_at, equipment_slot, two_handed, weapon_family, armor_category, required_strength, required_agility, required_endurance, required_perception, legacy) VALUES ('c0000000-0000-4000-8000-000000000020', 'IRON_INGOT', 'Iron Ingot', 'Smelted iron ready for the forge.', 'MATERIAL', 'COMMON', 12, 1, NULL, NULL, NULL, '2026-01-01 00:00:00+00', NULL, false, NULL, NULL, 0, 0, 0, 0, false);
INSERT INTO public.item_definitions (id, code, name, description, type, rarity, base_value, required_level, weapon_damage, armor_value, heal_amount, created_at, equipment_slot, two_handed, weapon_family, armor_category, required_strength, required_agility, required_endurance, required_perception, legacy) VALUES ('c0000000-0000-4000-8000-000000000021', 'CURED_LEATHER', 'Cured Leather', 'Hide worked until it holds a stitch.', 'MATERIAL', 'COMMON', 10, 1, NULL, NULL, NULL, '2026-01-01 00:00:00+00', NULL, false, NULL, NULL, 0, 0, 0, 0, false);
INSERT INTO public.item_definitions (id, code, name, description, type, rarity, base_value, required_level, weapon_damage, armor_value, heal_amount, created_at, equipment_slot, two_handed, weapon_family, armor_category, required_strength, required_agility, required_endurance, required_perception, legacy) VALUES ('c0000000-0000-4000-8000-000000000022', 'LEATHER_STRIPS', 'Leather Strips', 'Cut bindings for light gear.', 'MATERIAL', 'COMMON', 4, 1, NULL, NULL, NULL, '2026-01-01 00:00:00+00', NULL, false, NULL, NULL, 0, 0, 0, 0, false);
INSERT INTO public.item_definitions (id, code, name, description, type, rarity, base_value, required_level, weapon_damage, armor_value, heal_amount, created_at, equipment_slot, two_handed, weapon_family, armor_category, required_strength, required_agility, required_endurance, required_perception, legacy) VALUES ('c0000000-0000-4000-8000-000000000001', 'RUSTY_SWORD', 'Rusty Sword', 'A notched starter blade. Better than empty hands.', 'WEAPON', 'COMMON', 5, 1, 6, NULL, NULL, '2026-01-01 00:00:00+00', 'MAIN_HAND', false, 'SWORD', NULL, 0, 0, 0, 0, true);
INSERT INTO public.item_definitions (id, code, name, description, type, rarity, base_value, required_level, weapon_damage, armor_value, heal_amount, created_at, equipment_slot, two_handed, weapon_family, armor_category, required_strength, required_agility, required_endurance, required_perception, legacy) VALUES ('c0000000-0000-4000-8000-000000000003', 'IRON_SWORD', 'Iron Sword', 'A sturdy iron blade favored by city guards.', 'WEAPON', 'UNCOMMON', 25, 2, 10, NULL, NULL, '2026-01-01 00:00:00+00', 'MAIN_HAND', false, 'SWORD', NULL, 0, 0, 0, 0, true);
INSERT INTO public.item_definitions (id, code, name, description, type, rarity, base_value, required_level, weapon_damage, armor_value, heal_amount, created_at, equipment_slot, two_handed, weapon_family, armor_category, required_strength, required_agility, required_endurance, required_perception, legacy) VALUES ('c0000000-0000-4000-8000-000000000005', 'OLD_DAGGER', 'Old Dagger', 'A light blade that still finds soft spots.', 'WEAPON', 'COMMON', 8, 1, 4, NULL, NULL, '2026-01-01 00:00:00+00', 'MAIN_HAND', false, 'DAGGER', NULL, 0, 0, 0, 0, true);
INSERT INTO public.item_definitions (id, code, name, description, type, rarity, base_value, required_level, weapon_damage, armor_value, heal_amount, created_at, equipment_slot, two_handed, weapon_family, armor_category, required_strength, required_agility, required_endurance, required_perception, legacy) VALUES ('c0000000-0000-4000-8000-000000000002', 'WORN_LEATHER_ARMOR', 'Worn Leather Armor', 'Scuffed leather that still turns a glancing blow.', 'ARMOR', 'COMMON', 5, 1, NULL, 3, NULL, '2026-01-01 00:00:00+00', 'CHEST', false, NULL, 'LIGHT', 0, 0, 0, 0, true);
INSERT INTO public.item_definitions (id, code, name, description, type, rarity, base_value, required_level, weapon_damage, armor_value, heal_amount, created_at, equipment_slot, two_handed, weapon_family, armor_category, required_strength, required_agility, required_endurance, required_perception, legacy) VALUES ('c0000000-0000-4000-8000-000000000004', 'LEATHER_ARMOR', 'Leather Armor', 'Well-kept leather with reinforced stitching.', 'ARMOR', 'UNCOMMON', 25, 2, NULL, 6, NULL, '2026-01-01 00:00:00+00', 'CHEST', false, NULL, 'LIGHT', 0, 0, 0, 0, true);
INSERT INTO public.item_definitions (id, code, name, description, type, rarity, base_value, required_level, weapon_damage, armor_value, heal_amount, created_at, equipment_slot, two_handed, weapon_family, armor_category, required_strength, required_agility, required_endurance, required_perception, legacy) VALUES ('c0000000-0000-4000-8000-000000000008', 'HUNTING_BOW', 'Hunting Bow', 'A two-handed bow used by Greyhaven scouts.', 'WEAPON', 'COMMON', 18, 1, 8, NULL, NULL, '2026-01-01 00:00:00+00', 'MAIN_HAND', true, 'BOW', NULL, 0, 0, 0, 0, false);
INSERT INTO public.item_definitions (id, code, name, description, type, rarity, base_value, required_level, weapon_damage, armor_value, heal_amount, created_at, equipment_slot, two_handed, weapon_family, armor_category, required_strength, required_agility, required_endurance, required_perception, legacy) VALUES ('c0000000-0000-4000-8000-000000000009', 'IRON_AXE', 'Iron Axe', 'A heavy axe that trades grace for force.', 'WEAPON', 'UNCOMMON', 28, 2, 13, NULL, NULL, '2026-01-01 00:00:00+00', 'MAIN_HAND', false, 'AXE', NULL, 0, 0, 0, 0, false);
INSERT INTO public.item_definitions (id, code, name, description, type, rarity, base_value, required_level, weapon_damage, armor_value, heal_amount, created_at, equipment_slot, two_handed, weapon_family, armor_category, required_strength, required_agility, required_endurance, required_perception, legacy) VALUES ('c0000000-0000-4000-8000-00000000000a', 'IRON_MACE', 'Iron Mace', 'A blunt iron head made to defeat armor.', 'WEAPON', 'UNCOMMON', 26, 2, 11, NULL, NULL, '2026-01-01 00:00:00+00', 'MAIN_HAND', false, 'MACE', NULL, 0, 0, 0, 0, false);
INSERT INTO public.item_definitions (id, code, name, description, type, rarity, base_value, required_level, weapon_damage, armor_value, heal_amount, created_at, equipment_slot, two_handed, weapon_family, armor_category, required_strength, required_agility, required_endurance, required_perception, legacy) VALUES ('c0000000-0000-4000-8000-00000000000b', 'WOODEN_BUCKLER', 'Wooden Buckler', 'A light off-hand shield.', 'ARMOR', 'COMMON', 12, 1, NULL, 2, NULL, '2026-01-01 00:00:00+00', 'OFF_HAND', false, NULL, 'LIGHT', 0, 0, 0, 0, false);
INSERT INTO public.item_definitions (id, code, name, description, type, rarity, base_value, required_level, weapon_damage, armor_value, heal_amount, created_at, equipment_slot, two_handed, weapon_family, armor_category, required_strength, required_agility, required_endurance, required_perception, legacy) VALUES ('c0000000-0000-4000-8000-00000000000c', 'LEATHER_CAP', 'Leather Cap', 'A simple leather cap.', 'ARMOR', 'COMMON', 8, 1, NULL, 1, NULL, '2026-01-01 00:00:00+00', 'HEAD', false, NULL, 'LIGHT', 0, 0, 0, 0, false);
INSERT INTO public.item_definitions (id, code, name, description, type, rarity, base_value, required_level, weapon_damage, armor_value, heal_amount, created_at, equipment_slot, two_handed, weapon_family, armor_category, required_strength, required_agility, required_endurance, required_perception, legacy) VALUES ('c0000000-0000-4000-8000-00000000000d', 'IRON_HELM', 'Iron Helm', 'A heavy helm that demands strength.', 'ARMOR', 'UNCOMMON', 40, 1, NULL, 4, NULL, '2026-01-01 00:00:00+00', 'HEAD', false, NULL, 'HEAVY', 8, 0, 6, 0, false);
INSERT INTO public.item_definitions (id, code, name, description, type, rarity, base_value, required_level, weapon_damage, armor_value, heal_amount, created_at, equipment_slot, two_handed, weapon_family, armor_category, required_strength, required_agility, required_endurance, required_perception, legacy) VALUES ('c0000000-0000-4000-8000-00000000000e', 'LEATHER_GLOVES', 'Leather Gloves', 'Supple gloves that keep hands free.', 'ARMOR', 'COMMON', 7, 1, NULL, 1, NULL, '2026-01-01 00:00:00+00', 'HANDS', false, NULL, 'LIGHT', 0, 0, 0, 0, false);
INSERT INTO public.item_definitions (id, code, name, description, type, rarity, base_value, required_level, weapon_damage, armor_value, heal_amount, created_at, equipment_slot, two_handed, weapon_family, armor_category, required_strength, required_agility, required_endurance, required_perception, legacy) VALUES ('c0000000-0000-4000-8000-00000000000f', 'LEATHER_LEGGINGS', 'Leather Leggings', 'Light protection for the legs.', 'ARMOR', 'COMMON', 10, 1, NULL, 2, NULL, '2026-01-01 00:00:00+00', 'LEGS', false, NULL, 'LIGHT', 0, 0, 0, 0, false);
INSERT INTO public.item_definitions (id, code, name, description, type, rarity, base_value, required_level, weapon_damage, armor_value, heal_amount, created_at, equipment_slot, two_handed, weapon_family, armor_category, required_strength, required_agility, required_endurance, required_perception, legacy) VALUES ('c0000000-0000-4000-8000-000000000010', 'LEATHER_BOOTS', 'Leather Boots', 'Quiet boots for city streets.', 'ARMOR', 'COMMON', 8, 1, NULL, 1, NULL, '2026-01-01 00:00:00+00', 'FEET', false, NULL, 'LIGHT', 0, 0, 0, 0, false);
INSERT INTO public.item_definitions (id, code, name, description, type, rarity, base_value, required_level, weapon_damage, armor_value, heal_amount, created_at, equipment_slot, two_handed, weapon_family, armor_category, required_strength, required_agility, required_endurance, required_perception, legacy) VALUES ('c0000000-0000-4000-8000-000000000011', 'MAIL_HAUBERK', 'Mail Hauberk', 'Balanced medium armor for Greyhaven guards.', 'ARMOR', 'UNCOMMON', 45, 4, NULL, 8, NULL, '2026-01-01 00:00:00+00', 'CHEST', false, NULL, 'MEDIUM', 6, 0, 0, 0, false);
INSERT INTO public.item_definitions (id, code, name, description, type, rarity, base_value, required_level, weapon_damage, armor_value, heal_amount, created_at, equipment_slot, two_handed, weapon_family, armor_category, required_strength, required_agility, required_endurance, required_perception, legacy) VALUES ('c0000000-0000-4000-8000-000000000012', 'IRON_PLATE', 'Iron Plate', 'Heavy chest armor with demanding requirements.', 'ARMOR', 'RARE', 90, 8, NULL, 12, NULL, '2026-01-01 00:00:00+00', 'CHEST', false, NULL, 'HEAVY', 14, 0, 10, 0, false);
INSERT INTO public.item_definitions (id, code, name, description, type, rarity, base_value, required_level, weapon_damage, armor_value, heal_amount, created_at, equipment_slot, two_handed, weapon_family, armor_category, required_strength, required_agility, required_endurance, required_perception, legacy) VALUES ('c0000000-0000-4000-8000-000000000013', 'COPPER_AMULET', 'Copper Amulet', 'A modest charm worn at the throat.', 'ACCESSORY', 'COMMON', 15, 1, NULL, NULL, NULL, '2026-01-01 00:00:00+00', 'AMULET', false, NULL, NULL, 0, 0, 0, 0, false);
INSERT INTO public.item_definitions (id, code, name, description, type, rarity, base_value, required_level, weapon_damage, armor_value, heal_amount, created_at, equipment_slot, two_handed, weapon_family, armor_category, required_strength, required_agility, required_endurance, required_perception, legacy) VALUES ('c0000000-0000-4000-8000-000000000014', 'COPPER_RING', 'Copper Ring', 'A simple copper band.', 'ACCESSORY', 'COMMON', 12, 1, NULL, NULL, NULL, '2026-01-01 00:00:00+00', 'RING', false, NULL, NULL, 0, 0, 0, 0, false);
INSERT INTO public.item_definitions (id, code, name, description, type, rarity, base_value, required_level, weapon_damage, armor_value, heal_amount, created_at, equipment_slot, two_handed, weapon_family, armor_category, required_strength, required_agility, required_endurance, required_perception, legacy) VALUES ('c0000000-0000-4000-8000-000000000015', 'WOODSMAN_AXE', 'Woodsman Axe', 'A hatchet honest enough for Greyhaven''s gate guards.', 'WEAPON', 'COMMON', 10, 1, 8, NULL, NULL, '2026-01-01 00:00:00+00', 'MAIN_HAND', false, 'AXE', NULL, 0, 0, 0, 0, false);
INSERT INTO public.item_definitions (id, code, name, description, type, rarity, base_value, required_level, weapon_damage, armor_value, heal_amount, created_at, equipment_slot, two_handed, weapon_family, armor_category, required_strength, required_agility, required_endurance, required_perception, legacy) VALUES ('c0000000-0000-4000-8000-000000000016', 'KNOBBED_CLUB', 'Knobbed Club', 'A weighted stick that still counts as a mace.', 'WEAPON', 'COMMON', 9, 1, 7, NULL, NULL, '2026-01-01 00:00:00+00', 'MAIN_HAND', false, 'MACE', NULL, 0, 0, 0, 0, false);
INSERT INTO public.item_definitions (id, code, name, description, type, rarity, base_value, required_level, weapon_damage, armor_value, heal_amount, created_at, equipment_slot, two_handed, weapon_family, armor_category, required_strength, required_agility, required_endurance, required_perception, legacy) VALUES ('c0000000-0000-4000-8000-000000000017', 'PADDED_JACK', 'Padded Jack', 'Quilted medium armor for travellers who cannot wait on mail.', 'ARMOR', 'COMMON', 12, 1, NULL, 5, NULL, '2026-01-01 00:00:00+00', 'CHEST', false, NULL, 'MEDIUM', 2, 0, 0, 0, false);
INSERT INTO public.item_definitions (id, code, name, description, type, rarity, base_value, required_level, weapon_damage, armor_value, heal_amount, created_at, equipment_slot, two_handed, weapon_family, armor_category, required_strength, required_agility, required_endurance, required_perception, legacy) VALUES ('c0000000-0000-4000-8000-000000000018', 'SPLINT_VEST', 'Splint Vest', 'Crude heavy plates on a leather backing. Better than hope.', 'ARMOR', 'COMMON', 14, 1, NULL, 6, NULL, '2026-01-01 00:00:00+00', 'CHEST', false, NULL, 'HEAVY', 4, 0, 2, 0, false);
INSERT INTO public.item_definitions (id, code, name, description, type, rarity, base_value, required_level, weapon_damage, armor_value, heal_amount, created_at, equipment_slot, two_handed, weapon_family, armor_category, required_strength, required_agility, required_endurance, required_perception, legacy) VALUES ('c0000000-0000-4000-8000-000000000023', 'WEAPON_COMPONENTS', 'Weapon Components', 'Salvaged fittings and blades from broken arms.', 'MATERIAL', 'COMMON', 6, 1, NULL, NULL, NULL, '2026-01-01 00:00:00+00', NULL, false, NULL, NULL, 0, 0, 0, 0, false);
INSERT INTO public.item_definitions (id, code, name, description, type, rarity, base_value, required_level, weapon_damage, armor_value, heal_amount, created_at, equipment_slot, two_handed, weapon_family, armor_category, required_strength, required_agility, required_endurance, required_perception, legacy) VALUES ('c0000000-0000-4000-8000-000000000024', 'ARMOR_SCRAPS', 'Armor Scraps', 'Torn plates and hide left after salvage.', 'MATERIAL', 'COMMON', 5, 1, NULL, NULL, NULL, '2026-01-01 00:00:00+00', NULL, false, NULL, NULL, 0, 0, 0, 0, false);
INSERT INTO public.item_definitions (id, code, name, description, type, rarity, base_value, required_level, weapon_damage, armor_value, heal_amount, created_at, equipment_slot, two_handed, weapon_family, armor_category, required_strength, required_agility, required_endurance, required_perception, legacy) VALUES ('c0000000-0000-4000-8000-000000000025', 'RIVER_HERB', 'River Herb', 'A bitter green used in Greyhaven draughts.', 'MATERIAL', 'COMMON', 3, 1, NULL, NULL, NULL, '2026-01-01 00:00:00+00', NULL, false, NULL, NULL, 0, 0, 0, 0, false);
INSERT INTO public.item_definitions (id, code, name, description, type, rarity, base_value, required_level, weapon_damage, armor_value, heal_amount, created_at, equipment_slot, two_handed, weapon_family, armor_category, required_strength, required_agility, required_endurance, required_perception, legacy) VALUES ('c0000000-0000-4000-8000-000000000026', 'GREATER_HEALING_POTION', 'Greater Healing Potion', 'A stronger draught that knits deeper wounds.', 'CONSUMABLE', 'UNCOMMON', 22, 1, NULL, NULL, 80, '2026-01-01 00:00:00+00', NULL, false, NULL, NULL, 0, 0, 0, 0, false);


--
-- Data for Name: combat_reward_items; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- Data for Name: combat_status_effects; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- Data for Name: crafting_recipes; Type: TABLE DATA; Schema: public; Owner: -
--

INSERT INTO public.crafting_recipes (id, code, name, profession, required_profession_rank, required_character_level, gold_cost, duration_seconds, output_item_definition_id, output_quantity, min_rarity, max_rarity, profession_xp) VALUES ('f0000000-0000-4000-8000-000000000001', 'SMELT_IRON_INGOT', 'Smelt Iron Ingot', 'BLACKSMITH', 1, 1, 4, 60, 'c0000000-0000-4000-8000-000000000020', 1, NULL, NULL, 12);
INSERT INTO public.crafting_recipes (id, code, name, profession, required_profession_rank, required_character_level, gold_cost, duration_seconds, output_item_definition_id, output_quantity, min_rarity, max_rarity, profession_xp) VALUES ('f0000000-0000-4000-8000-000000000002', 'FORGE_IRON_SWORD', 'Forge Iron Sword', 'BLACKSMITH', 2, 1, 16, 180, 'c0000000-0000-4000-8000-000000000003', 1, 'COMMON', 'RARE', 20);
INSERT INTO public.crafting_recipes (id, code, name, profession, required_profession_rank, required_character_level, gold_cost, duration_seconds, output_item_definition_id, output_quantity, min_rarity, max_rarity, profession_xp) VALUES ('f0000000-0000-4000-8000-000000000003', 'FORGE_IRON_AXE', 'Forge Iron Axe', 'BLACKSMITH', 3, 1, 16, 180, 'c0000000-0000-4000-8000-000000000009', 1, 'COMMON', 'RARE', 22);
INSERT INTO public.crafting_recipes (id, code, name, profession, required_profession_rank, required_character_level, gold_cost, duration_seconds, output_item_definition_id, output_quantity, min_rarity, max_rarity, profession_xp) VALUES ('f0000000-0000-4000-8000-000000000004', 'FORGE_IRON_HELM', 'Forge Iron Helm', 'BLACKSMITH', 4, 1, 20, 210, 'c0000000-0000-4000-8000-00000000000d', 1, 'COMMON', 'RARE', 24);
INSERT INTO public.crafting_recipes (id, code, name, profession, required_profession_rank, required_character_level, gold_cost, duration_seconds, output_item_definition_id, output_quantity, min_rarity, max_rarity, profession_xp) VALUES ('f0000000-0000-4000-8000-000000000005', 'FORGE_IRON_PLATE', 'Forge Iron Plate', 'BLACKSMITH', 7, 8, 36, 300, 'c0000000-0000-4000-8000-000000000012', 1, 'COMMON', 'EPIC', 40);
INSERT INTO public.crafting_recipes (id, code, name, profession, required_profession_rank, required_character_level, gold_cost, duration_seconds, output_item_definition_id, output_quantity, min_rarity, max_rarity, profession_xp) VALUES ('f0000000-0000-4000-8000-000000000006', 'BREW_HEALING_POTION', 'Brew Healing Potion', 'ALCHEMIST', 1, 1, 8, 90, 'c0000000-0000-4000-8000-000000000006', 1, NULL, NULL, 12);
INSERT INTO public.crafting_recipes (id, code, name, profession, required_profession_rank, required_character_level, gold_cost, duration_seconds, output_item_definition_id, output_quantity, min_rarity, max_rarity, profession_xp) VALUES ('f0000000-0000-4000-8000-000000000007', 'BREW_GREATER_HEALING_POTION', 'Brew Greater Healing Potion', 'ALCHEMIST', 5, 5, 24, 150, 'c0000000-0000-4000-8000-000000000026', 1, NULL, NULL, 28);
INSERT INTO public.crafting_recipes (id, code, name, profession, required_profession_rank, required_character_level, gold_cost, duration_seconds, output_item_definition_id, output_quantity, min_rarity, max_rarity, profession_xp) VALUES ('f0000000-0000-4000-8000-000000000008', 'CURE_LEATHER', 'Cure Leather', 'HUNTER', 1, 1, 4, 60, 'c0000000-0000-4000-8000-000000000021', 1, NULL, NULL, 12);
INSERT INTO public.crafting_recipes (id, code, name, profession, required_profession_rank, required_character_level, gold_cost, duration_seconds, output_item_definition_id, output_quantity, min_rarity, max_rarity, profession_xp) VALUES ('f0000000-0000-4000-8000-000000000009', 'CUT_LEATHER_STRIPS', 'Cut Leather Strips', 'HUNTER', 1, 1, 2, 45, 'c0000000-0000-4000-8000-000000000022', 2, NULL, NULL, 10);
INSERT INTO public.crafting_recipes (id, code, name, profession, required_profession_rank, required_character_level, gold_cost, duration_seconds, output_item_definition_id, output_quantity, min_rarity, max_rarity, profession_xp) VALUES ('f0000000-0000-4000-8000-00000000000a', 'CRAFT_LEATHER_CAP', 'Craft Leather Cap', 'HUNTER', 2, 1, 12, 150, 'c0000000-0000-4000-8000-00000000000c', 1, 'COMMON', 'UNCOMMON', 18);
INSERT INTO public.crafting_recipes (id, code, name, profession, required_profession_rank, required_character_level, gold_cost, duration_seconds, output_item_definition_id, output_quantity, min_rarity, max_rarity, profession_xp) VALUES ('f0000000-0000-4000-8000-00000000000b', 'CRAFT_LEATHER_GLOVES', 'Craft Leather Gloves', 'HUNTER', 3, 1, 12, 150, 'c0000000-0000-4000-8000-00000000000e', 1, 'COMMON', 'UNCOMMON', 20);
INSERT INTO public.crafting_recipes (id, code, name, profession, required_profession_rank, required_character_level, gold_cost, duration_seconds, output_item_definition_id, output_quantity, min_rarity, max_rarity, profession_xp) VALUES ('f0000000-0000-4000-8000-00000000000c', 'CRAFT_LEATHER_ARMOR', 'Craft Leather Armor', 'HUNTER', 4, 1, 20, 210, 'c0000000-0000-4000-8000-000000000004', 1, 'COMMON', 'RARE', 26);


--
-- Data for Name: crafting_jobs; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- Data for Name: crafting_recipe_inputs; Type: TABLE DATA; Schema: public; Owner: -
--

INSERT INTO public.crafting_recipe_inputs (id, recipe_id, item_definition_id, quantity) VALUES ('f1000000-0000-4000-8000-000000000001', 'f0000000-0000-4000-8000-000000000001', 'c0000000-0000-4000-8000-00000000001c', 3);
INSERT INTO public.crafting_recipe_inputs (id, recipe_id, item_definition_id, quantity) VALUES ('f1000000-0000-4000-8000-000000000002', 'f0000000-0000-4000-8000-000000000002', 'c0000000-0000-4000-8000-000000000020', 4);
INSERT INTO public.crafting_recipe_inputs (id, recipe_id, item_definition_id, quantity) VALUES ('f1000000-0000-4000-8000-000000000003', 'f0000000-0000-4000-8000-000000000003', 'c0000000-0000-4000-8000-000000000020', 4);
INSERT INTO public.crafting_recipe_inputs (id, recipe_id, item_definition_id, quantity) VALUES ('f1000000-0000-4000-8000-000000000004', 'f0000000-0000-4000-8000-000000000004', 'c0000000-0000-4000-8000-000000000020', 3);
INSERT INTO public.crafting_recipe_inputs (id, recipe_id, item_definition_id, quantity) VALUES ('f1000000-0000-4000-8000-000000000005', 'f0000000-0000-4000-8000-000000000005', 'c0000000-0000-4000-8000-000000000020', 6);
INSERT INTO public.crafting_recipe_inputs (id, recipe_id, item_definition_id, quantity) VALUES ('f1000000-0000-4000-8000-000000000006', 'f0000000-0000-4000-8000-000000000006', 'c0000000-0000-4000-8000-000000000025', 2);
INSERT INTO public.crafting_recipe_inputs (id, recipe_id, item_definition_id, quantity) VALUES ('f1000000-0000-4000-8000-000000000007', 'f0000000-0000-4000-8000-000000000007', 'c0000000-0000-4000-8000-000000000025', 4);
INSERT INTO public.crafting_recipe_inputs (id, recipe_id, item_definition_id, quantity) VALUES ('f1000000-0000-4000-8000-000000000008', 'f0000000-0000-4000-8000-000000000007', 'c0000000-0000-4000-8000-000000000006', 1);
INSERT INTO public.crafting_recipe_inputs (id, recipe_id, item_definition_id, quantity) VALUES ('f1000000-0000-4000-8000-000000000009', 'f0000000-0000-4000-8000-000000000008', 'c0000000-0000-4000-8000-000000000007', 2);
INSERT INTO public.crafting_recipe_inputs (id, recipe_id, item_definition_id, quantity) VALUES ('f1000000-0000-4000-8000-00000000000a', 'f0000000-0000-4000-8000-000000000009', 'c0000000-0000-4000-8000-000000000021', 1);
INSERT INTO public.crafting_recipe_inputs (id, recipe_id, item_definition_id, quantity) VALUES ('f1000000-0000-4000-8000-00000000000b', 'f0000000-0000-4000-8000-00000000000a', 'c0000000-0000-4000-8000-000000000021', 2);
INSERT INTO public.crafting_recipe_inputs (id, recipe_id, item_definition_id, quantity) VALUES ('f1000000-0000-4000-8000-00000000000c', 'f0000000-0000-4000-8000-00000000000a', 'c0000000-0000-4000-8000-000000000022', 1);
INSERT INTO public.crafting_recipe_inputs (id, recipe_id, item_definition_id, quantity) VALUES ('f1000000-0000-4000-8000-00000000000d', 'f0000000-0000-4000-8000-00000000000b', 'c0000000-0000-4000-8000-000000000021', 1);
INSERT INTO public.crafting_recipe_inputs (id, recipe_id, item_definition_id, quantity) VALUES ('f1000000-0000-4000-8000-00000000000e', 'f0000000-0000-4000-8000-00000000000b', 'c0000000-0000-4000-8000-000000000022', 2);
INSERT INTO public.crafting_recipe_inputs (id, recipe_id, item_definition_id, quantity) VALUES ('f1000000-0000-4000-8000-00000000000f', 'f0000000-0000-4000-8000-00000000000c', 'c0000000-0000-4000-8000-000000000021', 3);
INSERT INTO public.crafting_recipe_inputs (id, recipe_id, item_definition_id, quantity) VALUES ('f1000000-0000-4000-8000-000000000010', 'f0000000-0000-4000-8000-00000000000c', 'c0000000-0000-4000-8000-000000000022', 2);


--
-- Data for Name: dungeon_rooms; Type: TABLE DATA; Schema: public; Owner: -
--

INSERT INTO public.dungeon_rooms (id, dungeon_id, code, name, description, room_kind, monster_definition_id, sort_order) VALUES ('91000000-0000-4000-8000-000000000001', '90000000-0000-4000-8000-000000000001', 'ENTRANCE', 'Keep Gate', 'A cracked portcullis hangs over the threshold.', 'ENTRANCE', NULL, 1);
INSERT INTO public.dungeon_rooms (id, dungeon_id, code, name, description, room_kind, monster_definition_id, sort_order) VALUES ('91000000-0000-4000-8000-000000000002', '90000000-0000-4000-8000-000000000001', 'GUARD_ROOM', 'Guard Room', 'The first hall still has a brute on watch.', 'COMBAT', 'd0000000-0000-4000-8000-00000000000b', 2);
INSERT INTO public.dungeon_rooms (id, dungeon_id, code, name, description, room_kind, monster_definition_id, sort_order) VALUES ('91000000-0000-4000-8000-000000000003', '90000000-0000-4000-8000-000000000001', 'COURTYARD', 'Courtyard', 'Two doors: the armory, or the prison stair.', 'CHOICE', NULL, 3);
INSERT INTO public.dungeon_rooms (id, dungeon_id, code, name, description, room_kind, monster_definition_id, sort_order) VALUES ('91000000-0000-4000-8000-000000000004', '90000000-0000-4000-8000-000000000001', 'ARMORY', 'Armory', 'Shields still hang. So does their owner.', 'COMBAT', 'd0000000-0000-4000-8000-00000000000e', 4);
INSERT INTO public.dungeon_rooms (id, dungeon_id, code, name, description, room_kind, monster_definition_id, sort_order) VALUES ('91000000-0000-4000-8000-000000000005', '90000000-0000-4000-8000-000000000001', 'PRISON', 'Prison', 'Rusted cages and a cutthroat who never left.', 'COMBAT', 'd0000000-0000-4000-8000-00000000000d', 5);
INSERT INTO public.dungeon_rooms (id, dungeon_id, code, name, description, room_kind, monster_definition_id, sort_order) VALUES ('91000000-0000-4000-8000-000000000006', '90000000-0000-4000-8000-000000000001', 'COMMAND_HALL', 'Command Hall', 'An overseer still drills the dark.', 'COMBAT', 'd0000000-0000-4000-8000-00000000000c', 6);
INSERT INTO public.dungeon_rooms (id, dungeon_id, code, name, description, room_kind, monster_definition_id, sort_order) VALUES ('91000000-0000-4000-8000-000000000007', '90000000-0000-4000-8000-000000000001', 'CRYPT', 'Crypt', 'An optional descent. A guardian waits among the urns.', 'OPTIONAL', 'd0000000-0000-4000-8000-000000000011', 7);
INSERT INTO public.dungeon_rooms (id, dungeon_id, code, name, description, room_kind, monster_definition_id, sort_order) VALUES ('91000000-0000-4000-8000-000000000008', '90000000-0000-4000-8000-000000000001', 'THRONE', 'Warden''s Seat', 'The keep''s last command has not been relieved.', 'BOSS', 'd0000000-0000-4000-8000-000000000012', 8);


--
-- Data for Name: dungeon_room_edges; Type: TABLE DATA; Schema: public; Owner: -
--

INSERT INTO public.dungeon_room_edges (id, from_room_id, to_room_id, edge_code, skip_room_code) VALUES ('92000000-0000-4000-8000-000000000001', '91000000-0000-4000-8000-000000000001', '91000000-0000-4000-8000-000000000002', 'CONTINUE', NULL);
INSERT INTO public.dungeon_room_edges (id, from_room_id, to_room_id, edge_code, skip_room_code) VALUES ('92000000-0000-4000-8000-000000000002', '91000000-0000-4000-8000-000000000002', '91000000-0000-4000-8000-000000000003', 'CONTINUE', NULL);
INSERT INTO public.dungeon_room_edges (id, from_room_id, to_room_id, edge_code, skip_room_code) VALUES ('92000000-0000-4000-8000-000000000005', '91000000-0000-4000-8000-000000000004', '91000000-0000-4000-8000-000000000006', 'CONTINUE', NULL);
INSERT INTO public.dungeon_room_edges (id, from_room_id, to_room_id, edge_code, skip_room_code) VALUES ('92000000-0000-4000-8000-000000000006', '91000000-0000-4000-8000-000000000005', '91000000-0000-4000-8000-000000000006', 'CONTINUE', NULL);
INSERT INTO public.dungeon_room_edges (id, from_room_id, to_room_id, edge_code, skip_room_code) VALUES ('92000000-0000-4000-8000-000000000007', '91000000-0000-4000-8000-000000000006', '91000000-0000-4000-8000-000000000007', 'OPTIONAL', NULL);
INSERT INTO public.dungeon_room_edges (id, from_room_id, to_room_id, edge_code, skip_room_code) VALUES ('92000000-0000-4000-8000-000000000009', '91000000-0000-4000-8000-000000000007', '91000000-0000-4000-8000-000000000008', 'CONTINUE', NULL);
INSERT INTO public.dungeon_room_edges (id, from_room_id, to_room_id, edge_code, skip_room_code) VALUES ('92000000-0000-4000-8000-000000000003', '91000000-0000-4000-8000-000000000003', '91000000-0000-4000-8000-000000000004', 'ARMORY', 'PRISON');
INSERT INTO public.dungeon_room_edges (id, from_room_id, to_room_id, edge_code, skip_room_code) VALUES ('92000000-0000-4000-8000-000000000004', '91000000-0000-4000-8000-000000000003', '91000000-0000-4000-8000-000000000005', 'PRISON', 'ARMORY');
INSERT INTO public.dungeon_room_edges (id, from_room_id, to_room_id, edge_code, skip_room_code) VALUES ('92000000-0000-4000-8000-000000000008', '91000000-0000-4000-8000-000000000006', '91000000-0000-4000-8000-000000000008', 'CONTINUE', 'CRYPT');


--
-- Data for Name: dungeon_run_rooms; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- Data for Name: item_instances; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- Data for Name: equipment; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- Data for Name: expeditions; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- Data for Name: expedition_reward_items; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- Data for Name: game_telemetry_events; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- Data for Name: item_definition_modifiers; Type: TABLE DATA; Schema: public; Owner: -
--

INSERT INTO public.item_definition_modifiers (id, item_definition_id, stat, magnitude) VALUES ('16618a71-d362-41e5-82a1-602277538f74', 'c0000000-0000-4000-8000-000000000019', 'CRIT_CHANCE', 1);
INSERT INTO public.item_definition_modifiers (id, item_definition_id, stat, magnitude) VALUES ('8c2759d3-197a-4828-9be0-f04918edbbeb', 'c0000000-0000-4000-8000-000000000019', 'ACCURACY', 4);
INSERT INTO public.item_definition_modifiers (id, item_definition_id, stat, magnitude) VALUES ('81db20c0-0d6c-4f68-a2db-427228cdbde3', 'c0000000-0000-4000-8000-00000000001a', 'CRIT_CHANCE', 1);
INSERT INTO public.item_definition_modifiers (id, item_definition_id, stat, magnitude) VALUES ('e243f6ab-f496-4fd2-8913-675617040e6d', 'c0000000-0000-4000-8000-00000000001a', 'ACCURACY', 4);
INSERT INTO public.item_definition_modifiers (id, item_definition_id, stat, magnitude) VALUES ('84c67555-ec43-4fd9-9abb-89a56a5a4366', 'c0000000-0000-4000-8000-000000000001', 'CRIT_CHANCE', 1);
INSERT INTO public.item_definition_modifiers (id, item_definition_id, stat, magnitude) VALUES ('479b2a25-2338-42d0-a334-a56a1b8f36df', 'c0000000-0000-4000-8000-000000000001', 'ACCURACY', 4);
INSERT INTO public.item_definition_modifiers (id, item_definition_id, stat, magnitude) VALUES ('7f967d30-aede-4a02-b3d9-b3ec5eb2bcb9', 'c0000000-0000-4000-8000-000000000003', 'CRIT_CHANCE', 1);
INSERT INTO public.item_definition_modifiers (id, item_definition_id, stat, magnitude) VALUES ('b3bc5d17-aa8f-4a04-ad8b-74be9789aa5d', 'c0000000-0000-4000-8000-000000000003', 'ACCURACY', 4);
INSERT INTO public.item_definition_modifiers (id, item_definition_id, stat, magnitude) VALUES ('ae64ebe5-0d79-47f5-aa3c-ac5fe0756734', 'c0000000-0000-4000-8000-000000000005', 'STAMINA_COST', 1);
INSERT INTO public.item_definition_modifiers (id, item_definition_id, stat, magnitude) VALUES ('ded8b07a-a4aa-46d9-b7c3-35594dd8841a', 'c0000000-0000-4000-8000-000000000005', 'DODGE', 2);
INSERT INTO public.item_definition_modifiers (id, item_definition_id, stat, magnitude) VALUES ('e5076698-acb5-4c34-9c8e-23b77688246b', 'c0000000-0000-4000-8000-000000000005', 'CRIT_CHANCE', 4);
INSERT INTO public.item_definition_modifiers (id, item_definition_id, stat, magnitude) VALUES ('20ea774d-e600-4ef9-8647-e2272ae074d1', 'c0000000-0000-4000-8000-000000000008', 'CRIT_CHANCE', 2);
INSERT INTO public.item_definition_modifiers (id, item_definition_id, stat, magnitude) VALUES ('1f1a60d5-3011-4282-94a4-e6cacce67510', 'c0000000-0000-4000-8000-000000000008', 'ACCURACY', 6);
INSERT INTO public.item_definition_modifiers (id, item_definition_id, stat, magnitude) VALUES ('c1984557-f5ff-4370-a4a9-2967ec7503c2', 'c0000000-0000-4000-8000-000000000009', 'CRIT_CHANCE', 3);
INSERT INTO public.item_definition_modifiers (id, item_definition_id, stat, magnitude) VALUES ('bb10f07e-b3a1-46b9-99c4-f40d92dad7bb', 'c0000000-0000-4000-8000-00000000000a', 'ACCURACY', 2);
INSERT INTO public.item_definition_modifiers (id, item_definition_id, stat, magnitude) VALUES ('f75b6b85-4397-490c-aa37-8de70a1f515e', 'c0000000-0000-4000-8000-00000000000a', 'ARMOR', 1);
INSERT INTO public.item_definition_modifiers (id, item_definition_id, stat, magnitude) VALUES ('6c94cde5-33a5-4518-82b7-8c27e57426a9', 'c0000000-0000-4000-8000-00000000000b', 'DODGE', 1);
INSERT INTO public.item_definition_modifiers (id, item_definition_id, stat, magnitude) VALUES ('7d57dbd4-4592-418d-863d-663668c3e810', 'c0000000-0000-4000-8000-00000000000c', 'PERCEPTION', 1);
INSERT INTO public.item_definition_modifiers (id, item_definition_id, stat, magnitude) VALUES ('afbcb9f5-0aa1-4359-ae82-b69005f7344e', 'c0000000-0000-4000-8000-00000000000d', 'PERCEPTION', 1);
INSERT INTO public.item_definition_modifiers (id, item_definition_id, stat, magnitude) VALUES ('20579cfe-79d1-41bc-abc8-72663addc782', 'c0000000-0000-4000-8000-00000000000e', 'ACCURACY', 2);
INSERT INTO public.item_definition_modifiers (id, item_definition_id, stat, magnitude) VALUES ('13e3b156-cd18-4e5b-a03c-5c72181c8b83', 'c0000000-0000-4000-8000-00000000000f', 'AGILITY', 1);
INSERT INTO public.item_definition_modifiers (id, item_definition_id, stat, magnitude) VALUES ('81939433-5b4f-4e65-b42f-2c88bad8fab4', 'c0000000-0000-4000-8000-000000000010', 'DODGE', 1);
INSERT INTO public.item_definition_modifiers (id, item_definition_id, stat, magnitude) VALUES ('c9c2481b-504c-482b-9c90-9c0bb479f43a', 'c0000000-0000-4000-8000-000000000013', 'CRIT_CHANCE', 2);
INSERT INTO public.item_definition_modifiers (id, item_definition_id, stat, magnitude) VALUES ('a3ca26be-561f-44c2-a072-636cc6ddfe84', 'c0000000-0000-4000-8000-000000000014', 'ACCURACY', 2);
INSERT INTO public.item_definition_modifiers (id, item_definition_id, stat, magnitude) VALUES ('a9e4679f-e5de-4e79-83cf-61b7aa2b725a', 'c0000000-0000-4000-8000-000000000015', 'CRIT_CHANCE', 3);
INSERT INTO public.item_definition_modifiers (id, item_definition_id, stat, magnitude) VALUES ('a5ab595f-ddc9-44b8-9b5b-a683ce321e56', 'c0000000-0000-4000-8000-000000000016', 'ACCURACY', 2);
INSERT INTO public.item_definition_modifiers (id, item_definition_id, stat, magnitude) VALUES ('c87b3e5e-b652-4e85-be36-68d923e63eee', 'c0000000-0000-4000-8000-000000000016', 'ARMOR', 1);
INSERT INTO public.item_definition_modifiers (id, item_definition_id, stat, magnitude) VALUES ('881416f8-3304-40a8-ba9d-da5f94f7f2ed', 'c0000000-0000-4000-8000-00000000001f', 'ENDURANCE', 2);
INSERT INTO public.item_definition_modifiers (id, item_definition_id, stat, magnitude) VALUES ('1eea62cd-21e7-4828-8aed-f79ee66c52d2', 'c0000000-0000-4000-8000-00000000001f', 'ARMOR', 3);


--
-- Data for Name: item_instance_affixes; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- Data for Name: location_connections; Type: TABLE DATA; Schema: public; Owner: -
--

INSERT INTO public.location_connections (id, from_location_id, to_location_id) VALUES ('b0000000-0000-4000-8000-000000000001', 'a0000000-0000-4000-8000-000000000001', 'a0000000-0000-4000-8000-000000000005');
INSERT INTO public.location_connections (id, from_location_id, to_location_id) VALUES ('b0000000-0000-4000-8000-000000000002', 'a0000000-0000-4000-8000-000000000005', 'a0000000-0000-4000-8000-000000000001');
INSERT INTO public.location_connections (id, from_location_id, to_location_id) VALUES ('b0000000-0000-4000-8000-000000000003', 'a0000000-0000-4000-8000-000000000001', 'a0000000-0000-4000-8000-000000000004');
INSERT INTO public.location_connections (id, from_location_id, to_location_id) VALUES ('b0000000-0000-4000-8000-000000000004', 'a0000000-0000-4000-8000-000000000004', 'a0000000-0000-4000-8000-000000000001');
INSERT INTO public.location_connections (id, from_location_id, to_location_id) VALUES ('b0000000-0000-4000-8000-000000000005', 'a0000000-0000-4000-8000-000000000001', 'a0000000-0000-4000-8000-000000000003');
INSERT INTO public.location_connections (id, from_location_id, to_location_id) VALUES ('b0000000-0000-4000-8000-000000000006', 'a0000000-0000-4000-8000-000000000003', 'a0000000-0000-4000-8000-000000000001');
INSERT INTO public.location_connections (id, from_location_id, to_location_id) VALUES ('b0000000-0000-4000-8000-000000000007', 'a0000000-0000-4000-8000-000000000001', 'a0000000-0000-4000-8000-000000000006');
INSERT INTO public.location_connections (id, from_location_id, to_location_id) VALUES ('b0000000-0000-4000-8000-000000000008', 'a0000000-0000-4000-8000-000000000006', 'a0000000-0000-4000-8000-000000000001');
INSERT INTO public.location_connections (id, from_location_id, to_location_id) VALUES ('b0000000-0000-4000-8000-000000000009', 'a0000000-0000-4000-8000-000000000003', 'a0000000-0000-4000-8000-000000000002');
INSERT INTO public.location_connections (id, from_location_id, to_location_id) VALUES ('b0000000-0000-4000-8000-00000000000a', 'a0000000-0000-4000-8000-000000000002', 'a0000000-0000-4000-8000-000000000003');
INSERT INTO public.location_connections (id, from_location_id, to_location_id) VALUES ('b0000000-0000-4000-8000-00000000000b', 'a0000000-0000-4000-8000-000000000001', 'a0000000-0000-4000-8000-000000000007');
INSERT INTO public.location_connections (id, from_location_id, to_location_id) VALUES ('b0000000-0000-4000-8000-00000000000c', 'a0000000-0000-4000-8000-000000000007', 'a0000000-0000-4000-8000-000000000001');
INSERT INTO public.location_connections (id, from_location_id, to_location_id) VALUES ('b0000000-0000-4000-8000-00000000000d', 'a0000000-0000-4000-8000-000000000001', 'a0000000-0000-4000-8000-000000000008');
INSERT INTO public.location_connections (id, from_location_id, to_location_id) VALUES ('b0000000-0000-4000-8000-00000000000e', 'a0000000-0000-4000-8000-000000000008', 'a0000000-0000-4000-8000-000000000001');
INSERT INTO public.location_connections (id, from_location_id, to_location_id) VALUES ('b0000000-0000-4000-8000-00000000000f', 'a0000000-0000-4000-8000-000000000001', 'a0000000-0000-4000-8000-000000000009');
INSERT INTO public.location_connections (id, from_location_id, to_location_id) VALUES ('b0000000-0000-4000-8000-000000000010', 'a0000000-0000-4000-8000-000000000009', 'a0000000-0000-4000-8000-000000000001');
INSERT INTO public.location_connections (id, from_location_id, to_location_id) VALUES ('b0000000-0000-4000-8000-000000000011', 'a0000000-0000-4000-8000-000000000008', 'a0000000-0000-4000-8000-000000000004');
INSERT INTO public.location_connections (id, from_location_id, to_location_id) VALUES ('b0000000-0000-4000-8000-000000000012', 'a0000000-0000-4000-8000-000000000004', 'a0000000-0000-4000-8000-000000000008');
INSERT INTO public.location_connections (id, from_location_id, to_location_id) VALUES ('b0000000-0000-4000-8000-000000000013', 'a0000000-0000-4000-8000-000000000004', 'a0000000-0000-4000-8000-00000000000a');
INSERT INTO public.location_connections (id, from_location_id, to_location_id) VALUES ('b0000000-0000-4000-8000-000000000014', 'a0000000-0000-4000-8000-00000000000a', 'a0000000-0000-4000-8000-000000000004');
INSERT INTO public.location_connections (id, from_location_id, to_location_id) VALUES ('b0000000-0000-4000-8000-000000000015', 'a0000000-0000-4000-8000-000000000005', 'a0000000-0000-4000-8000-00000000000b');
INSERT INTO public.location_connections (id, from_location_id, to_location_id) VALUES ('b0000000-0000-4000-8000-000000000016', 'a0000000-0000-4000-8000-00000000000b', 'a0000000-0000-4000-8000-000000000005');
INSERT INTO public.location_connections (id, from_location_id, to_location_id) VALUES ('b0000000-0000-4000-8000-000000000017', 'a0000000-0000-4000-8000-000000000006', 'a0000000-0000-4000-8000-00000000000c');
INSERT INTO public.location_connections (id, from_location_id, to_location_id) VALUES ('b0000000-0000-4000-8000-000000000018', 'a0000000-0000-4000-8000-00000000000c', 'a0000000-0000-4000-8000-000000000006');
INSERT INTO public.location_connections (id, from_location_id, to_location_id) VALUES ('b0000000-0000-4000-8000-00000000001b', 'a0000000-0000-4000-8000-00000000000c', 'a0000000-0000-4000-8000-00000000000d');
INSERT INTO public.location_connections (id, from_location_id, to_location_id) VALUES ('b0000000-0000-4000-8000-00000000001c', 'a0000000-0000-4000-8000-00000000000d', 'a0000000-0000-4000-8000-00000000000c');


--
-- Data for Name: location_encounter_weights; Type: TABLE DATA; Schema: public; Owner: -
--

INSERT INTO public.location_encounter_weights (id, location_id, monster_definition_id, weight) VALUES ('f0000000-0000-4000-8000-000000000001', 'a0000000-0000-4000-8000-000000000004', 'd0000000-0000-4000-8000-000000000001', 55);
INSERT INTO public.location_encounter_weights (id, location_id, monster_definition_id, weight) VALUES ('f0000000-0000-4000-8000-000000000002', 'a0000000-0000-4000-8000-000000000004', 'd0000000-0000-4000-8000-000000000002', 35);
INSERT INTO public.location_encounter_weights (id, location_id, monster_definition_id, weight) VALUES ('f0000000-0000-4000-8000-000000000003', 'a0000000-0000-4000-8000-000000000004', NULL, 10);
INSERT INTO public.location_encounter_weights (id, location_id, monster_definition_id, weight) VALUES ('f0000000-0000-4000-8000-000000000004', 'a0000000-0000-4000-8000-000000000005', 'd0000000-0000-4000-8000-000000000003', 70);
INSERT INTO public.location_encounter_weights (id, location_id, monster_definition_id, weight) VALUES ('f0000000-0000-4000-8000-000000000005', 'a0000000-0000-4000-8000-000000000005', 'd0000000-0000-4000-8000-000000000004', 20);
INSERT INTO public.location_encounter_weights (id, location_id, monster_definition_id, weight) VALUES ('f0000000-0000-4000-8000-000000000006', 'a0000000-0000-4000-8000-000000000005', NULL, 10);
INSERT INTO public.location_encounter_weights (id, location_id, monster_definition_id, weight) VALUES ('f0000000-0000-4000-8000-000000000007', 'a0000000-0000-4000-8000-000000000006', 'd0000000-0000-4000-8000-000000000004', 40);
INSERT INTO public.location_encounter_weights (id, location_id, monster_definition_id, weight) VALUES ('f0000000-0000-4000-8000-000000000008', 'a0000000-0000-4000-8000-000000000006', 'd0000000-0000-4000-8000-000000000001', 30);
INSERT INTO public.location_encounter_weights (id, location_id, monster_definition_id, weight) VALUES ('f0000000-0000-4000-8000-000000000009', 'a0000000-0000-4000-8000-000000000006', 'd0000000-0000-4000-8000-000000000005', 15);
INSERT INTO public.location_encounter_weights (id, location_id, monster_definition_id, weight) VALUES ('f0000000-0000-4000-8000-00000000000a', 'a0000000-0000-4000-8000-000000000006', 'd0000000-0000-4000-8000-000000000002', 10);
INSERT INTO public.location_encounter_weights (id, location_id, monster_definition_id, weight) VALUES ('f0000000-0000-4000-8000-00000000000b', 'a0000000-0000-4000-8000-000000000006', NULL, 5);
INSERT INTO public.location_encounter_weights (id, location_id, monster_definition_id, weight) VALUES ('f0000000-0000-4000-8000-00000000000c', 'a0000000-0000-4000-8000-000000000009', 'd0000000-0000-4000-8000-000000000006', 50);
INSERT INTO public.location_encounter_weights (id, location_id, monster_definition_id, weight) VALUES ('f0000000-0000-4000-8000-00000000000d', 'a0000000-0000-4000-8000-000000000009', 'd0000000-0000-4000-8000-000000000007', 35);
INSERT INTO public.location_encounter_weights (id, location_id, monster_definition_id, weight) VALUES ('f0000000-0000-4000-8000-00000000000e', 'a0000000-0000-4000-8000-000000000009', NULL, 15);
INSERT INTO public.location_encounter_weights (id, location_id, monster_definition_id, weight) VALUES ('f0000000-0000-4000-8000-000000000012', 'a0000000-0000-4000-8000-00000000000a', NULL, 15);
INSERT INTO public.location_encounter_weights (id, location_id, monster_definition_id, weight) VALUES ('f0000000-0000-4000-8000-000000000013', 'a0000000-0000-4000-8000-00000000000b', 'd0000000-0000-4000-8000-00000000000a', 40);
INSERT INTO public.location_encounter_weights (id, location_id, monster_definition_id, weight) VALUES ('f0000000-0000-4000-8000-000000000014', 'a0000000-0000-4000-8000-00000000000b', 'd0000000-0000-4000-8000-00000000000b', 35);
INSERT INTO public.location_encounter_weights (id, location_id, monster_definition_id, weight) VALUES ('f0000000-0000-4000-8000-000000000015', 'a0000000-0000-4000-8000-00000000000b', 'd0000000-0000-4000-8000-00000000000c', 10);
INSERT INTO public.location_encounter_weights (id, location_id, monster_definition_id, weight) VALUES ('f0000000-0000-4000-8000-000000000016', 'a0000000-0000-4000-8000-00000000000b', NULL, 15);
INSERT INTO public.location_encounter_weights (id, location_id, monster_definition_id, weight) VALUES ('f0000000-0000-4000-8000-00000000001b', 'a0000000-0000-4000-8000-00000000000c', NULL, 15);
INSERT INTO public.location_encounter_weights (id, location_id, monster_definition_id, weight) VALUES ('f0000000-0000-4000-8000-00000000001f', 'a0000000-0000-4000-8000-00000000000d', NULL, 20);
INSERT INTO public.location_encounter_weights (id, location_id, monster_definition_id, weight) VALUES ('f0000000-0000-4000-8000-00000000000f', 'a0000000-0000-4000-8000-00000000000a', 'd0000000-0000-4000-8000-000000000008', 50);
INSERT INTO public.location_encounter_weights (id, location_id, monster_definition_id, weight) VALUES ('f0000000-0000-4000-8000-000000000010', 'a0000000-0000-4000-8000-00000000000a', 'd0000000-0000-4000-8000-000000000009', 35);
INSERT INTO public.location_encounter_weights (id, location_id, monster_definition_id, weight) VALUES ('f0000000-0000-4000-8000-000000000017', 'a0000000-0000-4000-8000-00000000000c', 'd0000000-0000-4000-8000-00000000000d', 40);
INSERT INTO public.location_encounter_weights (id, location_id, monster_definition_id, weight) VALUES ('f0000000-0000-4000-8000-000000000018', 'a0000000-0000-4000-8000-00000000000c', 'd0000000-0000-4000-8000-00000000000e', 32);
INSERT INTO public.location_encounter_weights (id, location_id, monster_definition_id, weight) VALUES ('f0000000-0000-4000-8000-000000000019', 'a0000000-0000-4000-8000-00000000000c', 'd0000000-0000-4000-8000-00000000000f', 13);
INSERT INTO public.location_encounter_weights (id, location_id, monster_definition_id, weight) VALUES ('f0000000-0000-4000-8000-00000000001c', 'a0000000-0000-4000-8000-00000000000d', 'd0000000-0000-4000-8000-000000000010', 55);
INSERT INTO public.location_encounter_weights (id, location_id, monster_definition_id, weight) VALUES ('f0000000-0000-4000-8000-00000000001d', 'a0000000-0000-4000-8000-00000000000d', 'd0000000-0000-4000-8000-000000000011', 25);


--
-- Data for Name: market_buy_orders; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- Data for Name: market_buy_order_fills; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- Data for Name: market_listings; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- Data for Name: merchant_definitions; Type: TABLE DATA; Schema: public; Owner: -
--

INSERT INTO public.merchant_definitions (id, code, name, title, description, merchant_type, portrait_code, sort_order) VALUES ('d0000000-0000-4000-8000-000000000001', 'WEAPONSMITH', 'Edric Varn', 'Greyhaven Weaponsmith', 'Edric keeps a modest rack of honest steel for travellers who cannot wait on another adventurer''s listing.', 'WEAPONSMITH', 'edric-varn', 0);
INSERT INTO public.merchant_definitions (id, code, name, title, description, merchant_type, portrait_code, sort_order) VALUES ('d0000000-0000-4000-8000-000000000002', 'ARMORER', 'Mara Helden', 'Greyhaven Armorer', 'Mara sells sturdy everyday protection and a simple shield. Fancy plate is someone else''s problem.', 'ARMORER', 'mara-helden', 1);
INSERT INTO public.merchant_definitions (id, code, name, title, description, merchant_type, portrait_code, sort_order) VALUES ('d0000000-0000-4000-8000-000000000003', 'APOTHECARY', 'Sister Calia', 'Greyhaven Apothecary', 'Calia restocks bitter healing draughts for those who return from the forest in one piece.', 'APOTHECARY', 'sister-calia', 2);
INSERT INTO public.merchant_definitions (id, code, name, title, description, merchant_type, portrait_code, sort_order) VALUES ('d0000000-0000-4000-8000-000000000004', 'GENERAL', 'Tomas Reed', 'Greyhaven Provisioner', 'Tomas deals in modest charms and odds that keep a newcomer equipped without emptying the player market.', 'GENERAL', 'tomas-reed', 3);


--
-- Data for Name: merchant_stock; Type: TABLE DATA; Schema: public; Owner: -
--

INSERT INTO public.merchant_stock (id, merchant_id, item_definition_id, availability_type, sort_order) VALUES ('e0000000-0000-4000-8000-000000000001', 'd0000000-0000-4000-8000-000000000001', 'c0000000-0000-4000-8000-000000000001', 'UNLIMITED', 0);
INSERT INTO public.merchant_stock (id, merchant_id, item_definition_id, availability_type, sort_order) VALUES ('e0000000-0000-4000-8000-000000000002', 'd0000000-0000-4000-8000-000000000001', 'c0000000-0000-4000-8000-000000000005', 'UNLIMITED', 1);
INSERT INTO public.merchant_stock (id, merchant_id, item_definition_id, availability_type, sort_order) VALUES ('e0000000-0000-4000-8000-000000000003', 'd0000000-0000-4000-8000-000000000001', 'c0000000-0000-4000-8000-000000000008', 'UNLIMITED', 2);
INSERT INTO public.merchant_stock (id, merchant_id, item_definition_id, availability_type, sort_order) VALUES ('e0000000-0000-4000-8000-000000000004', 'd0000000-0000-4000-8000-000000000002', 'c0000000-0000-4000-8000-000000000002', 'UNLIMITED', 0);
INSERT INTO public.merchant_stock (id, merchant_id, item_definition_id, availability_type, sort_order) VALUES ('e0000000-0000-4000-8000-000000000005', 'd0000000-0000-4000-8000-000000000002', 'c0000000-0000-4000-8000-00000000000b', 'UNLIMITED', 1);
INSERT INTO public.merchant_stock (id, merchant_id, item_definition_id, availability_type, sort_order) VALUES ('e0000000-0000-4000-8000-000000000006', 'd0000000-0000-4000-8000-000000000002', 'c0000000-0000-4000-8000-00000000000c', 'UNLIMITED', 2);
INSERT INTO public.merchant_stock (id, merchant_id, item_definition_id, availability_type, sort_order) VALUES ('e0000000-0000-4000-8000-000000000007', 'd0000000-0000-4000-8000-000000000002', 'c0000000-0000-4000-8000-00000000000e', 'UNLIMITED', 3);
INSERT INTO public.merchant_stock (id, merchant_id, item_definition_id, availability_type, sort_order) VALUES ('e0000000-0000-4000-8000-000000000008', 'd0000000-0000-4000-8000-000000000002', 'c0000000-0000-4000-8000-00000000000f', 'UNLIMITED', 4);
INSERT INTO public.merchant_stock (id, merchant_id, item_definition_id, availability_type, sort_order) VALUES ('e0000000-0000-4000-8000-000000000009', 'd0000000-0000-4000-8000-000000000002', 'c0000000-0000-4000-8000-000000000010', 'UNLIMITED', 5);
INSERT INTO public.merchant_stock (id, merchant_id, item_definition_id, availability_type, sort_order) VALUES ('e0000000-0000-4000-8000-00000000000a', 'd0000000-0000-4000-8000-000000000003', 'c0000000-0000-4000-8000-000000000006', 'UNLIMITED', 0);
INSERT INTO public.merchant_stock (id, merchant_id, item_definition_id, availability_type, sort_order) VALUES ('e0000000-0000-4000-8000-00000000000b', 'd0000000-0000-4000-8000-000000000004', 'c0000000-0000-4000-8000-000000000013', 'UNLIMITED', 0);
INSERT INTO public.merchant_stock (id, merchant_id, item_definition_id, availability_type, sort_order) VALUES ('e0000000-0000-4000-8000-00000000000c', 'd0000000-0000-4000-8000-000000000004', 'c0000000-0000-4000-8000-000000000014', 'UNLIMITED', 1);
INSERT INTO public.merchant_stock (id, merchant_id, item_definition_id, availability_type, sort_order) VALUES ('e0000000-0000-4000-8000-00000000000d', 'd0000000-0000-4000-8000-000000000004', 'c0000000-0000-4000-8000-000000000006', 'UNLIMITED', 2);
INSERT INTO public.merchant_stock (id, merchant_id, item_definition_id, availability_type, sort_order) VALUES ('e0000000-0000-4000-8000-00000000000e', 'd0000000-0000-4000-8000-000000000001', 'c0000000-0000-4000-8000-000000000015', 'UNLIMITED', 3);
INSERT INTO public.merchant_stock (id, merchant_id, item_definition_id, availability_type, sort_order) VALUES ('e0000000-0000-4000-8000-00000000000f', 'd0000000-0000-4000-8000-000000000001', 'c0000000-0000-4000-8000-000000000016', 'UNLIMITED', 4);
INSERT INTO public.merchant_stock (id, merchant_id, item_definition_id, availability_type, sort_order) VALUES ('e0000000-0000-4000-8000-000000000010', 'd0000000-0000-4000-8000-000000000002', 'c0000000-0000-4000-8000-000000000017', 'UNLIMITED', 6);
INSERT INTO public.merchant_stock (id, merchant_id, item_definition_id, availability_type, sort_order) VALUES ('e0000000-0000-4000-8000-000000000011', 'd0000000-0000-4000-8000-000000000002', 'c0000000-0000-4000-8000-000000000018', 'UNLIMITED', 7);
INSERT INTO public.merchant_stock (id, merchant_id, item_definition_id, availability_type, sort_order) VALUES ('e0000000-0000-4000-8000-000000000012', 'd0000000-0000-4000-8000-000000000001', 'c0000000-0000-4000-8000-000000000019', 'UNLIMITED', 5);
INSERT INTO public.merchant_stock (id, merchant_id, item_definition_id, availability_type, sort_order) VALUES ('e0000000-0000-4000-8000-000000000013', 'd0000000-0000-4000-8000-000000000001', 'c0000000-0000-4000-8000-00000000001a', 'UNLIMITED', 6);


--
-- Data for Name: monster_loot_entries; Type: TABLE DATA; Schema: public; Owner: -
--

INSERT INTO public.monster_loot_entries (id, monster_definition_id, item_definition_id, drop_chance_percent, quantity_min, quantity_max, once_per_character) VALUES ('e0000000-0000-4000-8000-000000000001', 'd0000000-0000-4000-8000-000000000001', 'c0000000-0000-4000-8000-000000000006', 25, 1, 1, false);
INSERT INTO public.monster_loot_entries (id, monster_definition_id, item_definition_id, drop_chance_percent, quantity_min, quantity_max, once_per_character) VALUES ('e0000000-0000-4000-8000-000000000002', 'd0000000-0000-4000-8000-000000000001', 'c0000000-0000-4000-8000-000000000005', 10, 1, 1, false);
INSERT INTO public.monster_loot_entries (id, monster_definition_id, item_definition_id, drop_chance_percent, quantity_min, quantity_max, once_per_character) VALUES ('e0000000-0000-4000-8000-000000000003', 'd0000000-0000-4000-8000-000000000002', 'c0000000-0000-4000-8000-000000000006', 15, 1, 1, false);
INSERT INTO public.monster_loot_entries (id, monster_definition_id, item_definition_id, drop_chance_percent, quantity_min, quantity_max, once_per_character) VALUES ('e0000000-0000-4000-8000-000000000004', 'd0000000-0000-4000-8000-000000000003', 'c0000000-0000-4000-8000-000000000007', 70, 1, 1, false);
INSERT INTO public.monster_loot_entries (id, monster_definition_id, item_definition_id, drop_chance_percent, quantity_min, quantity_max, once_per_character) VALUES ('e0000000-0000-4000-8000-000000000005', 'd0000000-0000-4000-8000-000000000003', 'c0000000-0000-4000-8000-000000000006', 30, 1, 1, false);
INSERT INTO public.monster_loot_entries (id, monster_definition_id, item_definition_id, drop_chance_percent, quantity_min, quantity_max, once_per_character) VALUES ('e0000000-0000-4000-8000-000000000007', 'd0000000-0000-4000-8000-000000000004', 'c0000000-0000-4000-8000-000000000006', 35, 1, 2, false);
INSERT INTO public.monster_loot_entries (id, monster_definition_id, item_definition_id, drop_chance_percent, quantity_min, quantity_max, once_per_character) VALUES ('e0000000-0000-4000-8000-000000000008', 'd0000000-0000-4000-8000-000000000004', 'c0000000-0000-4000-8000-000000000004', 8, 1, 1, false);
INSERT INTO public.monster_loot_entries (id, monster_definition_id, item_definition_id, drop_chance_percent, quantity_min, quantity_max, once_per_character) VALUES ('e0000000-0000-4000-8000-00000000000a', 'd0000000-0000-4000-8000-000000000005', 'c0000000-0000-4000-8000-000000000004', 20, 1, 1, false);
INSERT INTO public.monster_loot_entries (id, monster_definition_id, item_definition_id, drop_chance_percent, quantity_min, quantity_max, once_per_character) VALUES ('e0000000-0000-4000-8000-00000000000b', 'd0000000-0000-4000-8000-000000000005', 'c0000000-0000-4000-8000-000000000006', 40, 1, 2, false);
INSERT INTO public.monster_loot_entries (id, monster_definition_id, item_definition_id, drop_chance_percent, quantity_min, quantity_max, once_per_character) VALUES ('e0000000-0000-4000-8000-000000000006', 'd0000000-0000-4000-8000-000000000004', 'c0000000-0000-4000-8000-000000000009', 12, 1, 1, false);
INSERT INTO public.monster_loot_entries (id, monster_definition_id, item_definition_id, drop_chance_percent, quantity_min, quantity_max, once_per_character) VALUES ('e0000000-0000-4000-8000-000000000009', 'd0000000-0000-4000-8000-000000000005', 'c0000000-0000-4000-8000-000000000009', 25, 1, 1, false);
INSERT INTO public.monster_loot_entries (id, monster_definition_id, item_definition_id, drop_chance_percent, quantity_min, quantity_max, once_per_character) VALUES ('e0000000-0000-4000-8000-00000000000c', 'd0000000-0000-4000-8000-000000000005', 'c0000000-0000-4000-8000-00000000001d', 100, 1, 1, false);
INSERT INTO public.monster_loot_entries (id, monster_definition_id, item_definition_id, drop_chance_percent, quantity_min, quantity_max, once_per_character) VALUES ('e0000000-0000-4000-8000-00000000000d', 'd0000000-0000-4000-8000-000000000006', 'c0000000-0000-4000-8000-000000000006', 30, 1, 1, false);
INSERT INTO public.monster_loot_entries (id, monster_definition_id, item_definition_id, drop_chance_percent, quantity_min, quantity_max, once_per_character) VALUES ('e0000000-0000-4000-8000-00000000000e', 'd0000000-0000-4000-8000-000000000007', 'c0000000-0000-4000-8000-000000000008', 12, 1, 1, false);
INSERT INTO public.monster_loot_entries (id, monster_definition_id, item_definition_id, drop_chance_percent, quantity_min, quantity_max, once_per_character) VALUES ('e0000000-0000-4000-8000-00000000000f', 'd0000000-0000-4000-8000-000000000007', 'c0000000-0000-4000-8000-000000000006', 25, 1, 1, false);
INSERT INTO public.monster_loot_entries (id, monster_definition_id, item_definition_id, drop_chance_percent, quantity_min, quantity_max, once_per_character) VALUES ('e0000000-0000-4000-8000-000000000010', 'd0000000-0000-4000-8000-000000000008', 'c0000000-0000-4000-8000-00000000001b', 70, 1, 1, false);
INSERT INTO public.monster_loot_entries (id, monster_definition_id, item_definition_id, drop_chance_percent, quantity_min, quantity_max, once_per_character) VALUES ('e0000000-0000-4000-8000-000000000011', 'd0000000-0000-4000-8000-000000000008', 'c0000000-0000-4000-8000-000000000006', 35, 1, 1, false);
INSERT INTO public.monster_loot_entries (id, monster_definition_id, item_definition_id, drop_chance_percent, quantity_min, quantity_max, once_per_character) VALUES ('e0000000-0000-4000-8000-000000000012', 'd0000000-0000-4000-8000-000000000009', 'c0000000-0000-4000-8000-00000000000b', 20, 1, 1, false);
INSERT INTO public.monster_loot_entries (id, monster_definition_id, item_definition_id, drop_chance_percent, quantity_min, quantity_max, once_per_character) VALUES ('e0000000-0000-4000-8000-000000000013', 'd0000000-0000-4000-8000-000000000009', 'c0000000-0000-4000-8000-000000000006', 30, 1, 1, false);
INSERT INTO public.monster_loot_entries (id, monster_definition_id, item_definition_id, drop_chance_percent, quantity_min, quantity_max, once_per_character) VALUES ('e0000000-0000-4000-8000-000000000014', 'd0000000-0000-4000-8000-00000000000a', 'c0000000-0000-4000-8000-00000000001c', 55, 1, 2, false);
INSERT INTO public.monster_loot_entries (id, monster_definition_id, item_definition_id, drop_chance_percent, quantity_min, quantity_max, once_per_character) VALUES ('e0000000-0000-4000-8000-000000000015', 'd0000000-0000-4000-8000-00000000000a', 'c0000000-0000-4000-8000-000000000006', 30, 1, 1, false);
INSERT INTO public.monster_loot_entries (id, monster_definition_id, item_definition_id, drop_chance_percent, quantity_min, quantity_max, once_per_character) VALUES ('e0000000-0000-4000-8000-000000000016', 'd0000000-0000-4000-8000-00000000000b', 'c0000000-0000-4000-8000-00000000001c', 80, 1, 2, false);
INSERT INTO public.monster_loot_entries (id, monster_definition_id, item_definition_id, drop_chance_percent, quantity_min, quantity_max, once_per_character) VALUES ('e0000000-0000-4000-8000-000000000017', 'd0000000-0000-4000-8000-00000000000b', 'c0000000-0000-4000-8000-00000000000a', 10, 1, 1, false);
INSERT INTO public.monster_loot_entries (id, monster_definition_id, item_definition_id, drop_chance_percent, quantity_min, quantity_max, once_per_character) VALUES ('e0000000-0000-4000-8000-000000000018', 'd0000000-0000-4000-8000-00000000000c', 'c0000000-0000-4000-8000-00000000001c', 100, 1, 2, false);
INSERT INTO public.monster_loot_entries (id, monster_definition_id, item_definition_id, drop_chance_percent, quantity_min, quantity_max, once_per_character) VALUES ('e0000000-0000-4000-8000-000000000019', 'd0000000-0000-4000-8000-00000000000c', 'c0000000-0000-4000-8000-00000000000a', 25, 1, 1, false);
INSERT INTO public.monster_loot_entries (id, monster_definition_id, item_definition_id, drop_chance_percent, quantity_min, quantity_max, once_per_character) VALUES ('e0000000-0000-4000-8000-00000000001a', 'd0000000-0000-4000-8000-00000000000d', 'c0000000-0000-4000-8000-00000000001b', 40, 1, 1, false);
INSERT INTO public.monster_loot_entries (id, monster_definition_id, item_definition_id, drop_chance_percent, quantity_min, quantity_max, once_per_character) VALUES ('e0000000-0000-4000-8000-00000000001b', 'd0000000-0000-4000-8000-00000000000d', 'c0000000-0000-4000-8000-000000000005', 15, 1, 1, false);
INSERT INTO public.monster_loot_entries (id, monster_definition_id, item_definition_id, drop_chance_percent, quantity_min, quantity_max, once_per_character) VALUES ('e0000000-0000-4000-8000-00000000001c', 'd0000000-0000-4000-8000-00000000000e', 'c0000000-0000-4000-8000-00000000001d', 100, 1, 1, false);
INSERT INTO public.monster_loot_entries (id, monster_definition_id, item_definition_id, drop_chance_percent, quantity_min, quantity_max, once_per_character) VALUES ('e0000000-0000-4000-8000-00000000001d', 'd0000000-0000-4000-8000-00000000000e', 'c0000000-0000-4000-8000-00000000000b', 30, 1, 1, false);
INSERT INTO public.monster_loot_entries (id, monster_definition_id, item_definition_id, drop_chance_percent, quantity_min, quantity_max, once_per_character) VALUES ('e0000000-0000-4000-8000-00000000001e', 'd0000000-0000-4000-8000-00000000000f', 'c0000000-0000-4000-8000-00000000001d', 100, 1, 1, false);
INSERT INTO public.monster_loot_entries (id, monster_definition_id, item_definition_id, drop_chance_percent, quantity_min, quantity_max, once_per_character) VALUES ('e0000000-0000-4000-8000-00000000001f', 'd0000000-0000-4000-8000-00000000000f', 'c0000000-0000-4000-8000-000000000009', 20, 1, 1, false);
INSERT INTO public.monster_loot_entries (id, monster_definition_id, item_definition_id, drop_chance_percent, quantity_min, quantity_max, once_per_character) VALUES ('e0000000-0000-4000-8000-000000000020', 'd0000000-0000-4000-8000-000000000010', 'c0000000-0000-4000-8000-00000000001e', 40, 1, 1, false);
INSERT INTO public.monster_loot_entries (id, monster_definition_id, item_definition_id, drop_chance_percent, quantity_min, quantity_max, once_per_character) VALUES ('e0000000-0000-4000-8000-000000000021', 'd0000000-0000-4000-8000-000000000011', 'c0000000-0000-4000-8000-00000000001e', 100, 1, 1, false);
INSERT INTO public.monster_loot_entries (id, monster_definition_id, item_definition_id, drop_chance_percent, quantity_min, quantity_max, once_per_character) VALUES ('e0000000-0000-4000-8000-000000000022', 'd0000000-0000-4000-8000-000000000011', 'c0000000-0000-4000-8000-000000000012', 12, 1, 1, false);
INSERT INTO public.monster_loot_entries (id, monster_definition_id, item_definition_id, drop_chance_percent, quantity_min, quantity_max, once_per_character) VALUES ('e0000000-0000-4000-8000-000000000024', 'd0000000-0000-4000-8000-000000000012', 'c0000000-0000-4000-8000-00000000001e', 100, 1, 2, false);
INSERT INTO public.monster_loot_entries (id, monster_definition_id, item_definition_id, drop_chance_percent, quantity_min, quantity_max, once_per_character) VALUES ('e0000000-0000-4000-8000-000000000025', 'd0000000-0000-4000-8000-000000000012', 'c0000000-0000-4000-8000-000000000006', 60, 1, 2, false);
INSERT INTO public.monster_loot_entries (id, monster_definition_id, item_definition_id, drop_chance_percent, quantity_min, quantity_max, once_per_character) VALUES ('e0000000-0000-4000-8000-000000000023', 'd0000000-0000-4000-8000-000000000012', 'c0000000-0000-4000-8000-00000000001f', 100, 1, 1, true);


--
-- Data for Name: pvp_matches; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- Data for Name: pvp_battle_history; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- Data for Name: pvp_match_events; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- Data for Name: pvp_match_snapshots; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- Data for Name: pvp_match_statuses; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- Data for Name: salvage_outputs; Type: TABLE DATA; Schema: public; Owner: -
--

INSERT INTO public.salvage_outputs (id, source_item_definition_id, result_item_definition_id, base_quantity) VALUES ('530504ac-7da9-40df-b886-b105aea99357', 'c0000000-0000-4000-8000-000000000019', 'c0000000-0000-4000-8000-000000000023', 1);
INSERT INTO public.salvage_outputs (id, source_item_definition_id, result_item_definition_id, base_quantity) VALUES ('12ff8835-cc21-4fe6-8671-14f361bd3123', 'c0000000-0000-4000-8000-00000000001a', 'c0000000-0000-4000-8000-000000000023', 1);
INSERT INTO public.salvage_outputs (id, source_item_definition_id, result_item_definition_id, base_quantity) VALUES ('da090727-7b8d-4f8a-962d-5b640c33c0fb', 'c0000000-0000-4000-8000-000000000001', 'c0000000-0000-4000-8000-000000000023', 1);
INSERT INTO public.salvage_outputs (id, source_item_definition_id, result_item_definition_id, base_quantity) VALUES ('cd65fbfc-59d3-4733-b42c-413264bf9e87', 'c0000000-0000-4000-8000-000000000003', 'c0000000-0000-4000-8000-000000000023', 1);
INSERT INTO public.salvage_outputs (id, source_item_definition_id, result_item_definition_id, base_quantity) VALUES ('f15d3c07-138e-4894-b747-9025505a59fd', 'c0000000-0000-4000-8000-000000000005', 'c0000000-0000-4000-8000-000000000023', 1);
INSERT INTO public.salvage_outputs (id, source_item_definition_id, result_item_definition_id, base_quantity) VALUES ('5d027353-08f2-470c-968d-eeb0296f187a', 'c0000000-0000-4000-8000-000000000008', 'c0000000-0000-4000-8000-000000000023', 1);
INSERT INTO public.salvage_outputs (id, source_item_definition_id, result_item_definition_id, base_quantity) VALUES ('8f529e3e-06b6-405e-a116-e505091ca341', 'c0000000-0000-4000-8000-000000000009', 'c0000000-0000-4000-8000-000000000023', 1);
INSERT INTO public.salvage_outputs (id, source_item_definition_id, result_item_definition_id, base_quantity) VALUES ('067fa745-f996-4d51-a5c0-95786c4d954c', 'c0000000-0000-4000-8000-00000000000a', 'c0000000-0000-4000-8000-000000000023', 1);
INSERT INTO public.salvage_outputs (id, source_item_definition_id, result_item_definition_id, base_quantity) VALUES ('6e7004bc-4a97-4b22-bfc2-1551713d53ae', 'c0000000-0000-4000-8000-000000000015', 'c0000000-0000-4000-8000-000000000023', 1);
INSERT INTO public.salvage_outputs (id, source_item_definition_id, result_item_definition_id, base_quantity) VALUES ('2791d13e-bec3-4fd5-ba7a-e1c0f4984a71', 'c0000000-0000-4000-8000-000000000016', 'c0000000-0000-4000-8000-000000000023', 1);
INSERT INTO public.salvage_outputs (id, source_item_definition_id, result_item_definition_id, base_quantity) VALUES ('f11ed456-f440-4685-bd11-39923f341a63', 'c0000000-0000-4000-8000-00000000001f', 'c0000000-0000-4000-8000-000000000024', 1);
INSERT INTO public.salvage_outputs (id, source_item_definition_id, result_item_definition_id, base_quantity) VALUES ('e30948e7-d6b9-4b89-b3df-36018a96e49a', 'c0000000-0000-4000-8000-000000000002', 'c0000000-0000-4000-8000-000000000024', 1);
INSERT INTO public.salvage_outputs (id, source_item_definition_id, result_item_definition_id, base_quantity) VALUES ('62ce0435-0d8c-4151-8779-c92aa7b59358', 'c0000000-0000-4000-8000-000000000004', 'c0000000-0000-4000-8000-000000000024', 1);
INSERT INTO public.salvage_outputs (id, source_item_definition_id, result_item_definition_id, base_quantity) VALUES ('c5568331-4697-47d0-9518-8a1f2694ae3a', 'c0000000-0000-4000-8000-00000000000b', 'c0000000-0000-4000-8000-000000000024', 1);
INSERT INTO public.salvage_outputs (id, source_item_definition_id, result_item_definition_id, base_quantity) VALUES ('555b98a2-431a-414b-86f5-039fd7c4d9e6', 'c0000000-0000-4000-8000-00000000000c', 'c0000000-0000-4000-8000-000000000024', 1);
INSERT INTO public.salvage_outputs (id, source_item_definition_id, result_item_definition_id, base_quantity) VALUES ('7fd298a9-7f4d-43f0-b8fb-eda4ad95bd93', 'c0000000-0000-4000-8000-00000000000d', 'c0000000-0000-4000-8000-000000000024', 1);
INSERT INTO public.salvage_outputs (id, source_item_definition_id, result_item_definition_id, base_quantity) VALUES ('6096e128-1ab8-4ced-b001-7ef5e9c32c41', 'c0000000-0000-4000-8000-00000000000e', 'c0000000-0000-4000-8000-000000000024', 1);
INSERT INTO public.salvage_outputs (id, source_item_definition_id, result_item_definition_id, base_quantity) VALUES ('b0d77699-8b08-481a-b7b5-db5d6e793db7', 'c0000000-0000-4000-8000-00000000000f', 'c0000000-0000-4000-8000-000000000024', 1);
INSERT INTO public.salvage_outputs (id, source_item_definition_id, result_item_definition_id, base_quantity) VALUES ('b7fef540-466a-41a5-8b52-379d5a93384f', 'c0000000-0000-4000-8000-000000000010', 'c0000000-0000-4000-8000-000000000024', 1);
INSERT INTO public.salvage_outputs (id, source_item_definition_id, result_item_definition_id, base_quantity) VALUES ('61b9513f-8af4-4a14-974a-3a9ff86e21ec', 'c0000000-0000-4000-8000-000000000011', 'c0000000-0000-4000-8000-000000000024', 1);
INSERT INTO public.salvage_outputs (id, source_item_definition_id, result_item_definition_id, base_quantity) VALUES ('93a62af9-7028-45fd-825c-0c13270daac9', 'c0000000-0000-4000-8000-000000000012', 'c0000000-0000-4000-8000-000000000024', 1);
INSERT INTO public.salvage_outputs (id, source_item_definition_id, result_item_definition_id, base_quantity) VALUES ('ea9f4b62-d840-4b68-8590-32db3cba6e1d', 'c0000000-0000-4000-8000-000000000013', 'c0000000-0000-4000-8000-000000000024', 1);
INSERT INTO public.salvage_outputs (id, source_item_definition_id, result_item_definition_id, base_quantity) VALUES ('8c0f5b39-913e-47ea-b736-8f9a41fe51d3', 'c0000000-0000-4000-8000-000000000014', 'c0000000-0000-4000-8000-000000000024', 1);
INSERT INTO public.salvage_outputs (id, source_item_definition_id, result_item_definition_id, base_quantity) VALUES ('53309044-9896-4617-88d5-15f98c3450c6', 'c0000000-0000-4000-8000-000000000017', 'c0000000-0000-4000-8000-000000000024', 1);
INSERT INTO public.salvage_outputs (id, source_item_definition_id, result_item_definition_id, base_quantity) VALUES ('ab8319eb-ef88-4d00-b1d9-e78e43a73360', 'c0000000-0000-4000-8000-000000000018', 'c0000000-0000-4000-8000-000000000024', 1);
INSERT INTO public.salvage_outputs (id, source_item_definition_id, result_item_definition_id, base_quantity) VALUES ('89c841d9-3c10-4867-b263-d319bd65f975', 'c0000000-0000-4000-8000-000000000019', 'c0000000-0000-4000-8000-00000000001c', 1);
INSERT INTO public.salvage_outputs (id, source_item_definition_id, result_item_definition_id, base_quantity) VALUES ('3387c42a-d2c5-4cf9-8c60-081ee423bb2b', 'c0000000-0000-4000-8000-00000000001a', 'c0000000-0000-4000-8000-00000000001c', 1);
INSERT INTO public.salvage_outputs (id, source_item_definition_id, result_item_definition_id, base_quantity) VALUES ('71d677ee-e2a9-4058-9fc5-0a1b99b330c5', 'c0000000-0000-4000-8000-000000000001', 'c0000000-0000-4000-8000-00000000001c', 1);
INSERT INTO public.salvage_outputs (id, source_item_definition_id, result_item_definition_id, base_quantity) VALUES ('cb5fd37a-c2fe-48a0-a014-6f88b45327b7', 'c0000000-0000-4000-8000-000000000003', 'c0000000-0000-4000-8000-00000000001c', 1);
INSERT INTO public.salvage_outputs (id, source_item_definition_id, result_item_definition_id, base_quantity) VALUES ('21d83d43-7f11-4e7c-9e1f-e17c2bd5e789', 'c0000000-0000-4000-8000-000000000005', 'c0000000-0000-4000-8000-00000000001c', 1);
INSERT INTO public.salvage_outputs (id, source_item_definition_id, result_item_definition_id, base_quantity) VALUES ('aa909d9b-39b7-4be7-9254-e9f2ea6cbdde', 'c0000000-0000-4000-8000-000000000008', 'c0000000-0000-4000-8000-00000000001c', 1);
INSERT INTO public.salvage_outputs (id, source_item_definition_id, result_item_definition_id, base_quantity) VALUES ('917f0805-386a-4896-a278-456c51adcfdd', 'c0000000-0000-4000-8000-000000000009', 'c0000000-0000-4000-8000-00000000001c', 1);
INSERT INTO public.salvage_outputs (id, source_item_definition_id, result_item_definition_id, base_quantity) VALUES ('4994c0fa-f760-4f0f-a275-0a720780d507', 'c0000000-0000-4000-8000-00000000000a', 'c0000000-0000-4000-8000-00000000001c', 1);
INSERT INTO public.salvage_outputs (id, source_item_definition_id, result_item_definition_id, base_quantity) VALUES ('c7cf3453-c1d8-4158-ad81-85fc2520b29a', 'c0000000-0000-4000-8000-000000000015', 'c0000000-0000-4000-8000-00000000001c', 1);
INSERT INTO public.salvage_outputs (id, source_item_definition_id, result_item_definition_id, base_quantity) VALUES ('c7f6e61c-ff48-4701-a21e-7983f1c603e3', 'c0000000-0000-4000-8000-000000000016', 'c0000000-0000-4000-8000-00000000001c', 1);
INSERT INTO public.salvage_outputs (id, source_item_definition_id, result_item_definition_id, base_quantity) VALUES ('03b57965-cc60-49db-8a57-2be930e3c816', 'c0000000-0000-4000-8000-00000000001f', 'c0000000-0000-4000-8000-000000000022', 1);
INSERT INTO public.salvage_outputs (id, source_item_definition_id, result_item_definition_id, base_quantity) VALUES ('7bb70749-2b75-464a-9551-d0f8d5d5fb79', 'c0000000-0000-4000-8000-000000000002', 'c0000000-0000-4000-8000-000000000022', 1);
INSERT INTO public.salvage_outputs (id, source_item_definition_id, result_item_definition_id, base_quantity) VALUES ('09bce385-7288-4a7a-a6e3-901ac9b04a46', 'c0000000-0000-4000-8000-000000000004', 'c0000000-0000-4000-8000-000000000022', 1);
INSERT INTO public.salvage_outputs (id, source_item_definition_id, result_item_definition_id, base_quantity) VALUES ('a5ccf2b8-7c6a-4a29-9d2f-ebaead356512', 'c0000000-0000-4000-8000-00000000000b', 'c0000000-0000-4000-8000-000000000022', 1);
INSERT INTO public.salvage_outputs (id, source_item_definition_id, result_item_definition_id, base_quantity) VALUES ('034e14cd-aa0d-4c50-a0f5-0a26753bfbf9', 'c0000000-0000-4000-8000-00000000000c', 'c0000000-0000-4000-8000-000000000022', 1);
INSERT INTO public.salvage_outputs (id, source_item_definition_id, result_item_definition_id, base_quantity) VALUES ('7efc2204-1d91-452b-a49b-da2d8e1542b2', 'c0000000-0000-4000-8000-00000000000d', 'c0000000-0000-4000-8000-000000000022', 1);
INSERT INTO public.salvage_outputs (id, source_item_definition_id, result_item_definition_id, base_quantity) VALUES ('651cb0b1-14bf-4df9-8afc-42be2717dc6e', 'c0000000-0000-4000-8000-00000000000e', 'c0000000-0000-4000-8000-000000000022', 1);
INSERT INTO public.salvage_outputs (id, source_item_definition_id, result_item_definition_id, base_quantity) VALUES ('ecd7d3a0-7242-4d62-abfc-ac3668bfd335', 'c0000000-0000-4000-8000-00000000000f', 'c0000000-0000-4000-8000-000000000022', 1);
INSERT INTO public.salvage_outputs (id, source_item_definition_id, result_item_definition_id, base_quantity) VALUES ('70873b03-9e44-4dfc-86ae-ecddafac80f5', 'c0000000-0000-4000-8000-000000000010', 'c0000000-0000-4000-8000-000000000022', 1);
INSERT INTO public.salvage_outputs (id, source_item_definition_id, result_item_definition_id, base_quantity) VALUES ('f726bfc1-72ac-4e05-8578-c61e010f2957', 'c0000000-0000-4000-8000-000000000011', 'c0000000-0000-4000-8000-000000000022', 1);
INSERT INTO public.salvage_outputs (id, source_item_definition_id, result_item_definition_id, base_quantity) VALUES ('42912629-f074-42a1-a79f-0aabdfcfe016', 'c0000000-0000-4000-8000-000000000012', 'c0000000-0000-4000-8000-000000000022', 1);
INSERT INTO public.salvage_outputs (id, source_item_definition_id, result_item_definition_id, base_quantity) VALUES ('ed26d4f4-e8fd-498f-a5e7-4e3be9be2fb5', 'c0000000-0000-4000-8000-000000000013', 'c0000000-0000-4000-8000-000000000022', 1);
INSERT INTO public.salvage_outputs (id, source_item_definition_id, result_item_definition_id, base_quantity) VALUES ('972fd5a6-faff-4edf-85d6-c18b66ab66a9', 'c0000000-0000-4000-8000-000000000014', 'c0000000-0000-4000-8000-000000000022', 1);
INSERT INTO public.salvage_outputs (id, source_item_definition_id, result_item_definition_id, base_quantity) VALUES ('a00c4741-8880-4b71-b6b3-a22cdf45c5c4', 'c0000000-0000-4000-8000-000000000017', 'c0000000-0000-4000-8000-000000000022', 1);
INSERT INTO public.salvage_outputs (id, source_item_definition_id, result_item_definition_id, base_quantity) VALUES ('fe92c3e7-8a23-4c6d-bbd5-e28b413a36fa', 'c0000000-0000-4000-8000-000000000018', 'c0000000-0000-4000-8000-000000000022', 1);


--
-- Data for Name: schema_meta; Type: TABLE DATA; Schema: public; Owner: -
--

INSERT INTO public.schema_meta (id, key, value, created_at) VALUES (1, 'bootstrap_version', 'phase3', '2026-01-01 00:00:00+00');


--
-- Data for Name: technique_loadout_slots; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- Data for Name: weapon_masteries; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- Name: schema_meta_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.schema_meta_id_seq', 1, true);


--
--
