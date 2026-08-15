-- Persist hidden reward rolls when combat starts so retries can never reroll victory loot.

ALTER TABLE combat_sessions
    ADD COLUMN reward_plan_created BOOLEAN NOT NULL DEFAULT FALSE,
    ADD COLUMN planned_xp INT,
    ADD COLUMN planned_gold INT;

ALTER TABLE combat_sessions
    ADD CONSTRAINT chk_combat_sessions_reward_plan CHECK (
        (reward_plan_created = FALSE AND planned_xp IS NULL AND planned_gold IS NULL)
        OR (reward_plan_created = TRUE AND planned_xp IS NOT NULL AND planned_xp >= 0
            AND planned_gold IS NOT NULL AND planned_gold >= 0)
    );
