-- Yard counterpart of Arena Drill-Master Vesk. One NPC row per location_code.
INSERT INTO public.npc_definitions (
	id, code, name, title, description, greeting, portrait_code, location_code, merchant_code, interactions, sort_order
) VALUES (
	'e0000000-0000-4000-8000-000000000009',
	'YARD_INSTRUCTOR',
	'Drill-Master Vesk',
	'Yard instructor',
	'Runs the packed-dirt drills beside the square. Ranked steel can wait.',
	'Show me you can stand. The ladder comes later.',
	'drill-instructor',
	'SPARRING_YARD',
	NULL,
	'TALK,QUEST',
	70
);
