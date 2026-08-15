-- Quest chain codes must point at real quest definitions.

ALTER TABLE public.quest_definition
	ADD CONSTRAINT fk_quest_definition_prerequisite
	FOREIGN KEY (prerequisite_quest_code) REFERENCES public.quest_definition(code);

ALTER TABLE public.quest_definition
	ADD CONSTRAINT fk_quest_definition_next
	FOREIGN KEY (next_quest_code) REFERENCES public.quest_definition(code);
