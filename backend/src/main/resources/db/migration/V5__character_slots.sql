ALTER TABLE public.characters
	DROP CONSTRAINT uq_characters_account_id;

ALTER TABLE public.characters
	ADD COLUMN slot_index smallint;

UPDATE public.characters
	SET slot_index = 0
	WHERE slot_index IS NULL;

ALTER TABLE public.characters
	ALTER COLUMN slot_index SET NOT NULL;

ALTER TABLE public.characters
	ADD CONSTRAINT chk_characters_slot_index CHECK ((slot_index >= 0) AND (slot_index <= 2));

ALTER TABLE public.characters
	ADD CONSTRAINT uq_characters_account_slot UNIQUE (account_id, slot_index);

ALTER TABLE public.accounts
	ADD COLUMN active_character_id uuid;

UPDATE public.accounts AS account
	SET active_character_id = character.id
	FROM public.characters AS character
	WHERE character.account_id = account.id;

ALTER TABLE public.accounts
	ADD CONSTRAINT fk_accounts_active_character
	FOREIGN KEY (active_character_id) REFERENCES public.characters(id) ON DELETE SET NULL;
