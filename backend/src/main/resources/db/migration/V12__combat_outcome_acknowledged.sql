-- Terminal combat outcomes stay resumable until the player acknowledges the result screen.
-- ACTIVE sessions keep outcome_acknowledged = TRUE; it flips to FALSE only when combat ends.

ALTER TABLE combat_sessions
    ADD COLUMN outcome_acknowledged BOOLEAN NOT NULL DEFAULT TRUE;

CREATE UNIQUE INDEX uq_combat_sessions_one_unacked_outcome_per_character
    ON combat_sessions (character_id)
    WHERE outcome_acknowledged = FALSE;
