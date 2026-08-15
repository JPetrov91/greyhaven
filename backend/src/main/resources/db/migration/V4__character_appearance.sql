ALTER TABLE public.characters
	ADD COLUMN gender character varying(8) DEFAULT 'MALE' NOT NULL,
	ADD COLUMN avatar_code character varying(64) DEFAULT 'male_unyielding' NOT NULL;

ALTER TABLE public.characters
	ADD CONSTRAINT chk_characters_gender CHECK ((gender IN ('MALE', 'FEMALE')));

ALTER TABLE public.characters
	ADD CONSTRAINT chk_characters_avatar_code CHECK ((avatar_code IN (
		'male_unyielding',
		'male_iron_vow',
		'male_ashen_wolf',
		'male_pale_heir',
		'male_oathbound',
		'female_veiled',
		'female_nightbloom',
		'female_silver_thorn',
		'female_ember_queen',
		'female_hollow_saint'
	)));
