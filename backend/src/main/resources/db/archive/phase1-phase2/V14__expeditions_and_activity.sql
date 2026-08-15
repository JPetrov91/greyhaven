-- Asynchronous expeditions (timestamp-based completion) and persistent activity feed.

CREATE TABLE expeditions (
    id                   UUID PRIMARY KEY,
    character_id         UUID NOT NULL,
    expedition_type      VARCHAR(64) NOT NULL,
    strategy             VARCHAR(32) NOT NULL,
    status               VARCHAR(32) NOT NULL,
    started_at           TIMESTAMPTZ NOT NULL,
    completes_at         TIMESTAMPTZ NOT NULL,
    claimed_at           TIMESTAMPTZ,
    result_generated     BOOLEAN NOT NULL DEFAULT FALSE,
    planned_xp           INT,
    planned_gold         INT,
    planned_injury       INT,
    xp_awarded           INT,
    gold_awarded         INT,
    injury_applied       INT,
    version              BIGINT NOT NULL DEFAULT 0,
    created_at           TIMESTAMPTZ NOT NULL,
    updated_at           TIMESTAMPTZ NOT NULL,
    CONSTRAINT fk_expeditions_character
        FOREIGN KEY (character_id) REFERENCES characters (id),
    CONSTRAINT chk_expeditions_type
        CHECK (expedition_type IN ('FOREST_PATROL')),
    CONSTRAINT chk_expeditions_strategy
        CHECK (strategy IN ('CAUTIOUS', 'BALANCED', 'AGGRESSIVE')),
    CONSTRAINT chk_expeditions_status
        CHECK (status IN ('ACTIVE', 'COMPLETED', 'CLAIMED')),
    CONSTRAINT chk_expeditions_completion_window
        CHECK (completes_at > started_at),
    CONSTRAINT chk_expeditions_claimed_at
        CHECK (
            (status <> 'CLAIMED' AND claimed_at IS NULL)
            OR (status = 'CLAIMED' AND claimed_at IS NOT NULL)
        ),
    CONSTRAINT chk_expeditions_result_plan CHECK (
        (result_generated = FALSE
            AND planned_xp IS NULL
            AND planned_gold IS NULL
            AND planned_injury IS NULL)
        OR (result_generated = TRUE
            AND planned_xp IS NOT NULL AND planned_xp >= 0
            AND planned_gold IS NOT NULL AND planned_gold >= 0
            AND planned_injury IS NOT NULL AND planned_injury >= 0)
    ),
    CONSTRAINT chk_expeditions_awards CHECK (
        (status <> 'CLAIMED'
            AND xp_awarded IS NULL
            AND gold_awarded IS NULL
            AND injury_applied IS NULL)
        OR (status = 'CLAIMED'
            AND xp_awarded IS NOT NULL AND xp_awarded >= 0
            AND gold_awarded IS NOT NULL AND gold_awarded >= 0
            AND injury_applied IS NOT NULL AND injury_applied >= 0)
    )
);

CREATE UNIQUE INDEX uq_expeditions_one_active
    ON expeditions (character_id)
    WHERE status = 'ACTIVE';

CREATE INDEX idx_expeditions_character_status
    ON expeditions (character_id, status);

CREATE TABLE expedition_reward_items (
    id                  UUID PRIMARY KEY,
    expedition_id       UUID NOT NULL,
    item_definition_id  UUID NOT NULL,
    quantity            INT NOT NULL,
    CONSTRAINT fk_expedition_reward_items_expedition
        FOREIGN KEY (expedition_id) REFERENCES expeditions (id),
    CONSTRAINT fk_expedition_reward_items_item
        FOREIGN KEY (item_definition_id) REFERENCES item_definitions (id),
    CONSTRAINT chk_expedition_reward_items_quantity
        CHECK (quantity >= 1)
);

CREATE INDEX idx_expedition_reward_items_expedition
    ON expedition_reward_items (expedition_id);

CREATE TABLE activity_entries (
    id            UUID PRIMARY KEY,
    character_id  UUID NOT NULL,
    type          VARCHAR(64) NOT NULL,
    message       VARCHAR(512) NOT NULL,
    created_at    TIMESTAMPTZ NOT NULL,
    read_at       TIMESTAMPTZ,
    CONSTRAINT fk_activity_entries_character
        FOREIGN KEY (character_id) REFERENCES characters (id),
    CONSTRAINT chk_activity_entries_type CHECK (
        type IN (
            'COMBAT_VICTORY',
            'LEVEL_UP',
            'ITEM_FOUND',
            'EXPEDITION_COMPLETED',
            'EXPEDITION_CLAIMED'
        )
    ),
    CONSTRAINT chk_activity_entries_message
        CHECK (char_length(trim(message)) > 0)
);

CREATE INDEX idx_activity_entries_character_created
    ON activity_entries (character_id, created_at DESC);
