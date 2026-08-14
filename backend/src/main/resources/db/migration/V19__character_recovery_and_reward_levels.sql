-- Phase 2 Task 2: lazy HP/stamina recovery baseline and combat reward level-up snapshot.

ALTER TABLE characters
    ADD COLUMN last_recovery_at TIMESTAMPTZ;

UPDATE characters
SET last_recovery_at = updated_at
WHERE last_recovery_at IS NULL;

ALTER TABLE characters
    ALTER COLUMN last_recovery_at SET NOT NULL;

ALTER TABLE combat_sessions
    ADD COLUMN reward_previous_level INT,
    ADD COLUMN reward_new_level INT;
