-- Unspent attribute points awarded on level-up (Task 5).

ALTER TABLE characters
    ADD COLUMN unspent_attribute_points INT NOT NULL DEFAULT 0;

ALTER TABLE characters
    ADD CONSTRAINT chk_characters_unspent_attribute_points
        CHECK (unspent_attribute_points >= 0);
