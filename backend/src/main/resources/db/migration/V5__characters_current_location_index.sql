-- Nearby-character lookups filter on current_location_id on every location screen load,
-- and the characters.current_location_id foreign key has no supporting index yet.

CREATE INDEX idx_characters_current_location ON characters (current_location_id);
