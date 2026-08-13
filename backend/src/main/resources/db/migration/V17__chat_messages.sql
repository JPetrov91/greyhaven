-- Global chat history. Rate limiting uses the latest row per character.

CREATE TABLE chat_messages (
    id            UUID PRIMARY KEY,
    character_id  UUID NOT NULL,
    body          VARCHAR(500) NOT NULL,
    created_at    TIMESTAMPTZ NOT NULL,
    CONSTRAINT fk_chat_messages_character
        FOREIGN KEY (character_id) REFERENCES characters (id),
    CONSTRAINT chk_chat_messages_body
        CHECK (char_length(trim(body)) > 0)
);

CREATE INDEX idx_chat_messages_created
    ON chat_messages (created_at DESC, id DESC);

CREATE INDEX idx_chat_messages_character_created
    ON chat_messages (character_id, created_at DESC);
