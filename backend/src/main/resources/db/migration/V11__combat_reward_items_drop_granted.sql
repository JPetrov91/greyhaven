-- Combat rewards are all-or-nothing: a full inventory now aborts the round instead of
-- recording loot that was rolled but never given to the player.

ALTER TABLE combat_reward_items
    DROP COLUMN granted;
