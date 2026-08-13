# Phase 2 foundation — legacy data mapping

Operational notes for Flyway `V18__phase2_foundation.sql`. Product rules live in `docs/PHASE_2_SPEC.md` §7–§9. This file does not change Phase 1 gameplay by itself.

Do not edit already-applied Phase 1 migrations (`V1`–`V17`).

## Equipment slots

| Phase 1 slot | Phase 2 slot |
| ------------ | ------------ |
| `WEAPON`     | `MAIN_HAND`  |
| `ARMOR`      | `CHEST`      |

`ItemType` values `WEAPON` and `ARMOR` stay as catalog categories (loot, market filters). They are not renamed.

After V18 the `equipment.slot` check allows all nine Phase 2 names. Task 1 still only writes `MAIN_HAND` and `CHEST`. Inventory JSON keeps `weaponItemId` / `armorItemId` as aliases for those two slots.

## Item instances

Existing rows keep their IDs, quantities, owners, and definition stats (`weapon_damage`, `armor_value`, `heal_amount`).

`item_instances.legacy` and `item_definitions.legacy` are backfilled to `TRUE` for rows that already exist at V18. New rows default to `FALSE` so Task 3 can generate affixes without another backfill. Treat `legacy = TRUE` items as valid zero-affix items until Task 3.

## Experience and level (identity mapping)

`characters.experience` is already total lifetime XP, not XP-into-the-current-level. Phase 2 uses the same representation. V18 does **not** rewrite `level` or `experience`.

Task 1 keeps the Phase 1 curve and application cap (`MAX_LEVEL = 10`). The database level check is widened to `1..30` so Task 2 can raise the cap later.

### Task 2 catch-up (do not run in Task 1)

The Phase 2 cumulative table is lower than Phase 1 from level 3 upward. Examples:

- Phase 1 level 10 with 8500 XP already exceeds Phase 2’s 7230 for level 11.
- Phase 1 level 5 with 1600 XP is still level 5 on the Phase 1 curve (level 6 at 2500) but would be level 6 on Phase 2 (level 6 at 1510).

Task 2 must preserve stored `experience`, then apply an **explicit** catch-up that awards pending levels and attribute points. Do not silently recompute level on login in Task 1.

## Out of scope for Task 1

- Character Progression 2.0 (level 30 curve, respec, xp-to-next API)
- Affixes, instance rarity rolls, item generators
- Playable nine-slot UI, two-handed off-hand lock
- Mastery, techniques, Combat 2.0, PvP, crafting
