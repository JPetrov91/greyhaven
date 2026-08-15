TASK 1 — Phase 2 Foundation and Migration Preparation
Goal

Prepare the existing Phase 1 repository for Phase 2 without changing core gameplay yet.

Required Work

Inspect:

AGENTS.md
docs/MVP_SPEC.md
current codebase
current Flyway migrations
current entities
current APIs

Create:

docs/PHASE_2_SPEC.md

if not already present.

Establish or improve centralized balance infrastructure.

Prepare model changes necessary for future:

levels
attributes
equipment slots
item rarity
item affixes
mastery
techniques

Do not implement the full systems yet.

Create Flyway migration strategy for legacy data.

Document conversion of:

WEAPON → MAIN_HAND
ARMOR → CHEST

Determine how existing XP representation maps to the Phase 2 total-XP model.

No player progression may be silently lost.

Verification
existing Phase 1 tests pass;
migrations work from clean database;
migrations work from Phase 1 schema;
existing characters still load;
existing inventory still loads;
existing combat/expedition/market functionality remains operational.

Do not proceed automatically.