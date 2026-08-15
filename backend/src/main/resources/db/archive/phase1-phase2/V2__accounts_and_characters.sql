-- Account authentication and player characters (Task 2).
-- current_location_id is nullable until Task 3 introduces locations.

CREATE TABLE accounts (
    id              UUID PRIMARY KEY,
    email           VARCHAR(255) NOT NULL,
    password_hash   VARCHAR(255) NOT NULL,
    created_at      TIMESTAMPTZ NOT NULL,
    updated_at      TIMESTAMPTZ NOT NULL,
    CONSTRAINT uq_accounts_email UNIQUE (email)
);

CREATE TABLE characters (
    id                  UUID PRIMARY KEY,
    account_id          UUID NOT NULL,
    name                VARCHAR(64) NOT NULL,
    level               INT NOT NULL,
    experience          INT NOT NULL,
    strength            INT NOT NULL,
    agility             INT NOT NULL,
    endurance           INT NOT NULL,
    perception          INT NOT NULL,
    current_health      INT NOT NULL,
    max_health          INT NOT NULL,
    current_stamina     INT NOT NULL,
    max_stamina         INT NOT NULL,
    gold                INT NOT NULL,
    current_location_id UUID NULL,
    created_at          TIMESTAMPTZ NOT NULL,
    updated_at          TIMESTAMPTZ NOT NULL,
    CONSTRAINT uq_characters_account_id UNIQUE (account_id),
    CONSTRAINT fk_characters_account FOREIGN KEY (account_id) REFERENCES accounts (id),
    CONSTRAINT chk_characters_level CHECK (level >= 1 AND level <= 10),
    CONSTRAINT chk_characters_experience CHECK (experience >= 0),
    CONSTRAINT chk_characters_strength CHECK (strength >= 1),
    CONSTRAINT chk_characters_agility CHECK (agility >= 1),
    CONSTRAINT chk_characters_endurance CHECK (endurance >= 1),
    CONSTRAINT chk_characters_perception CHECK (perception >= 1),
    CONSTRAINT chk_characters_current_health CHECK (current_health >= 0),
    CONSTRAINT chk_characters_max_health CHECK (max_health >= 1),
    CONSTRAINT chk_characters_current_stamina CHECK (current_stamina >= 0),
    CONSTRAINT chk_characters_max_stamina CHECK (max_stamina >= 1),
    CONSTRAINT chk_characters_gold CHECK (gold >= 0)
);

CREATE UNIQUE INDEX uq_characters_name_lower ON characters (LOWER(name));
