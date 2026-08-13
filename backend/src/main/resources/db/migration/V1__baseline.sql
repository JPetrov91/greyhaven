-- Baseline schema for Greyhaven.
-- Domain tables (accounts, characters, world, combat, inventory, etc.) are introduced in later tasks.

CREATE TABLE schema_meta (
    id          BIGSERIAL PRIMARY KEY,
    key         VARCHAR(100) NOT NULL,
    value       VARCHAR(255) NOT NULL,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_schema_meta_key UNIQUE (key)
);

INSERT INTO schema_meta (key, value)
VALUES ('bootstrap_version', '1');
