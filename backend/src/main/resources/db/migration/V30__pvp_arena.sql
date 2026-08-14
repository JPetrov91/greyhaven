ALTER TABLE characters
	ADD COLUMN arena_rating INTEGER NOT NULL DEFAULT 1000,
	ADD COLUMN arena_marks INTEGER NOT NULL DEFAULT 0;

ALTER TABLE characters
	ADD CONSTRAINT ck_characters_arena_marks_non_negative CHECK (arena_marks >= 0),
	ADD CONSTRAINT ck_characters_arena_rating_non_negative CHECK (arena_rating >= 0);

CREATE TABLE arena_defense_profiles (
	character_id UUID PRIMARY KEY REFERENCES characters (id),
	preferred_action VARCHAR(32) NOT NULL,
	preferred_technique_code VARCHAR(64),
	heal_when_hp_percent_below INTEGER NOT NULL,
	defend_when_stamina_percent_below INTEGER NOT NULL,
	finisher_when_enemy_hp_percent_below INTEGER NOT NULL,
	finisher_technique_code VARCHAR(64),
	updated_at TIMESTAMPTZ NOT NULL
);

CREATE TABLE pvp_matches (
	id UUID PRIMARY KEY,
	match_kind VARCHAR(16) NOT NULL,
	status VARCHAR(32) NOT NULL,
	attacker_id UUID NOT NULL REFERENCES characters (id),
	defender_id UUID NOT NULL REFERENCES characters (id),
	round_number INTEGER NOT NULL,
	attacker_health INTEGER NOT NULL,
	attacker_stamina INTEGER NOT NULL,
	defender_health INTEGER NOT NULL,
	defender_stamina INTEGER NOT NULL,
	attacker_potion_charges INTEGER NOT NULL,
	defender_potion_charges INTEGER NOT NULL,
	last_defender_missed BOOLEAN NOT NULL DEFAULT FALSE,
	last_attacker_guarded BOOLEAN NOT NULL DEFAULT FALSE,
	pending_attacker_action VARCHAR(32),
	pending_attacker_technique VARCHAR(64),
	pending_defender_action VARCHAR(32),
	pending_defender_technique VARCHAR(64),
	action_deadline_at TIMESTAMPTZ,
	expires_at TIMESTAMPTZ,
	planned_attacker_rating_delta INTEGER NOT NULL DEFAULT 0,
	planned_defender_rating_delta INTEGER NOT NULL DEFAULT 0,
	planned_attacker_marks INTEGER NOT NULL DEFAULT 0,
	planned_defender_marks INTEGER NOT NULL DEFAULT 0,
	attacker_rating_at_start INTEGER NOT NULL DEFAULT 1000,
	defender_rating_at_start INTEGER NOT NULL DEFAULT 1000,
	rating_reward_multiplier NUMERIC(6, 3) NOT NULL DEFAULT 1,
	settlement_applied BOOLEAN NOT NULL DEFAULT FALSE,
	outcome_acknowledged BOOLEAN NOT NULL DEFAULT TRUE,
	version INTEGER NOT NULL DEFAULT 0,
	created_at TIMESTAMPTZ NOT NULL,
	updated_at TIMESTAMPTZ NOT NULL,
	CONSTRAINT ck_pvp_matches_not_self CHECK (attacker_id <> defender_id)
);

CREATE UNIQUE INDEX uq_pvp_one_active_arena_per_attacker
	ON pvp_matches (attacker_id)
	WHERE match_kind = 'ARENA' AND status = 'ACTIVE';

CREATE UNIQUE INDEX uq_pvp_one_unacked_arena_per_attacker
	ON pvp_matches (attacker_id)
	WHERE match_kind = 'ARENA' AND outcome_acknowledged = FALSE;

CREATE UNIQUE INDEX uq_pvp_one_open_duel_attacker
	ON pvp_matches (attacker_id)
	WHERE match_kind = 'DUEL' AND status IN ('PENDING', 'ACTIVE');

CREATE UNIQUE INDEX uq_pvp_one_open_duel_defender
	ON pvp_matches (defender_id)
	WHERE match_kind = 'DUEL' AND status IN ('PENDING', 'ACTIVE');

CREATE INDEX idx_pvp_matches_arena_repeat
	ON pvp_matches (attacker_id, defender_id, created_at)
	WHERE match_kind = 'ARENA';

CREATE TABLE pvp_match_snapshots (
	match_id UUID PRIMARY KEY REFERENCES pvp_matches (id),
	snapshot_version INTEGER NOT NULL,
	payload TEXT NOT NULL,
	created_at TIMESTAMPTZ NOT NULL
);

CREATE TABLE pvp_match_events (
	id UUID PRIMARY KEY,
	match_id UUID NOT NULL REFERENCES pvp_matches (id),
	round_number INTEGER NOT NULL,
	sequence_number INTEGER NOT NULL,
	event_type VARCHAR(64) NOT NULL,
	message TEXT NOT NULL,
	created_at TIMESTAMPTZ NOT NULL
);

CREATE INDEX idx_pvp_match_events_match ON pvp_match_events (match_id, round_number, sequence_number);

CREATE TABLE pvp_match_statuses (
	id UUID PRIMARY KEY,
	match_id UUID NOT NULL REFERENCES pvp_matches (id),
	target VARCHAR(8) NOT NULL,
	status_type VARCHAR(32) NOT NULL,
	stacks INTEGER NOT NULL,
	remaining_rounds INTEGER NOT NULL,
	UNIQUE (match_id, target, status_type)
);

CREATE TABLE pvp_battle_history (
	id UUID PRIMARY KEY,
	match_id UUID NOT NULL REFERENCES pvp_matches (id),
	character_id UUID NOT NULL REFERENCES characters (id),
	opponent_id UUID NOT NULL REFERENCES characters (id),
	opponent_name VARCHAR(64) NOT NULL,
	match_kind VARCHAR(16) NOT NULL,
	result VARCHAR(16) NOT NULL,
	rating_delta INTEGER NOT NULL,
	marks_awarded INTEGER NOT NULL,
	created_at TIMESTAMPTZ NOT NULL
);

CREATE UNIQUE INDEX uq_pvp_history_match_character
	ON pvp_battle_history (match_id, character_id);

CREATE INDEX idx_pvp_history_character_created
	ON pvp_battle_history (character_id, created_at DESC, id DESC);
