-- Structured gameplay telemetry for Phase 2 balance diagnostics.
-- Payloads are JSON objects with coded identifiers only (no emails, names, chat, or tokens).

CREATE TABLE game_telemetry_events (
    id            UUID PRIMARY KEY,
    occurred_at   TIMESTAMPTZ NOT NULL,
    category      VARCHAR(32) NOT NULL,
    event_type    VARCHAR(64) NOT NULL,
    character_id  UUID,
    payload       JSONB NOT NULL,
    CONSTRAINT fk_game_telemetry_events_character
        FOREIGN KEY (character_id) REFERENCES characters (id),
    CONSTRAINT chk_game_telemetry_events_category
        CHECK (category IN ('PROGRESSION', 'COMBAT', 'PVP', 'ECONOMY', 'CRAFTING')),
    CONSTRAINT chk_game_telemetry_events_payload_object
        CHECK (jsonb_typeof(payload) = 'object')
);

CREATE INDEX idx_game_telemetry_events_category_type_time
    ON game_telemetry_events (category, event_type, occurred_at);

CREATE INDEX idx_game_telemetry_events_character_time
    ON game_telemetry_events (character_id, occurred_at);
