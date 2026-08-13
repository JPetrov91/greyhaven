TASK 3 — Itemization and Equipment 2.0
Goal

Create the full Phase 2 item/equipment foundation.

Implement equipment slots:

HEAD
CHEST
HANDS
LEGS
FEET
MAIN_HAND
OFF_HAND
AMULET
RING

Implement:

weapon family
armor category
two-handed rules
level requirements
attribute requirements
rarity
base stat rolls
prefixes
suffixes
random affix generation

Create:

ItemGenerator
AffixGenerator
EquipmentValidator
ItemStatCalculator

Migrate legacy items safely.

Implement item comparison API data.

Update inventory/equipment frontend.

Tests

Cover:

slot compatibility
two-handed weapon logic
requirements
respec causing invalid equipment
rarity generation
affix compatibility
deterministic item generation
legacy items
item ownership
transaction safety

Do not implement mastery or crafting yet.