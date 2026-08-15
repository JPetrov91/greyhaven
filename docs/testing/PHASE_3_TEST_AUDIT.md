# Phase 3 Test Coverage Audit

**Date:** 2026-08-15  
**Scope:** Automated tests that protect gameplay and economy integrity before Phase 3 content work  
**Authoritative specs:** `docs/MVP_SPEC.md`, `docs/PHASE_3_SPEC.md`, `docs/PHASE_3_EXECUTION.md` (Task 03)  
**Method:** Risk-based review of existing unit and PostgreSQL Testcontainers integration tests. Coverage percentage was not a goal.

---

## Verdict

The Phase 1–2 suite is already a real safety net for most dangerous paths: exact-once combat/expedition/crafting claims, market and merchant gold races, one-character-per-account, session cookies, Combat 2.0, Arena settlement, and the Phase 3 Flyway baseline.

No requested area is **MISSING**.

The remaining holes were **PARTIAL** paths where a regression would duplicate XP, loot, gold, attribute points, or recovery. Those HIGH-RISK nets are implemented in this task (see §4).

Frontend Vitest files exercise UI contracts only. They are not treated as economy authority.

---

## Classification key

| Class | Meaning |
|-------|---------|
| **GOOD** | Dangerous regressions in this area would almost certainly fail an existing test |
| **PARTIAL** | Core rules are tested; at least one high-impact path was thin or unasserted |
| **WEAK** | Tests exist but would not catch the dangerous failure |
| **MISSING** | No meaningful automated protection |

---

## Risk matrix

| Area | Risk if it regresses | Class | Evidence | Residual gap |
|------|----------------------|-------|----------|--------------|
| Authentication | Account takeover, CSRF, session leakage | **GOOD** | `AuthAndCharacterIntegrationTest` (register/login/logout, CSRF, case-insensitive email); `SessionCookieIntegrationTest` (HttpOnly / SameSite); `RegistrationConcurrencyIntegrationTest` | None material |
| Character lifecycle | Duplicate characters, stolen names, wrong start state | **GOOD** | Auth IT: one character per account, unique names, starter gold/vitals/derived stats; inventory IT: starter loadout | None material |
| Progression | Wrong level, missing or extra attribute points | **GOOD** | `CharacterProgressionTest`, `ExperienceProgressTest`, `CharacterProgressionIntegrationTest` (XP table, catch-up, max level); combat IT allocates after a level-up | Live multi-level grant was thin (now covered) |
| XP and multi-level progression | Overflow XP skips levels or double-awards points | **PARTIAL** → **GOOD** | Domain already jumped 1→4 on 800 XP. Integration only had single-level combat and GET catch-up. Added mid-level overflow unit test + persisted multi-level combat settlement | None material |
| Attributes and respec | Free extra stats, gold not charged, illegal gear kept | **PARTIAL** → **GOOD** | Invalid allocation, free/paid respec, respec unequip (`ItemizationIntegrationTest`), combat blocks allocation. Respec-in-combat and concurrent allocation were untested (now covered) | None material |
| Health/stamina recovery | Double-heal on refresh or tab spam | **PARTIAL** → **GOOD** | `CharacterRecoveryTest` + sequential GET idempotence + combat pauses recovery. Concurrent GET was untested (now covered) | None material |
| Inventory | Duped items, capacity bypass, cross-account mutate | **GOOD** | `InventoryIntegrationTest`: ownership, capacity, stacks, unique stack constraint, potion consume, action flags | None material |
| Equipment | Invalid slots, stolen items, stats without gear | **GOOD** | Equip/unequip, requirements, DB owner integrity, starter derived stats, respec unequip | None material |
| Item generation | Non-deterministic loot, rarity reroll on refresh | **GOOD** | `ItemGeneratorTest`, `AffixGeneratorTest`, itemization IT (persist rolled state), crafting rarity persist | None material |
| CombatEngine | Wrong damage/hit/stamina; client-trustable outcomes | **GOOD** | `CombatEngineV2Test` (~64 cases), `Phase1CombatEngineTest`, `ArmorMitigationTest`, `EnemyAiTest` | Balance tweaks, not integrity |
| Combat persistence | Lost fight on refresh; occupation bypass | **GOOD** | Combat IT: current combat survives new session, encounter survives logout, statuses/techniques persist, move blocked, stale round rejected | None material |
| Combat settlement | Double XP/gold/loot; discarded unique items | **PARTIAL** → **GOOD** | Idempotent win, concurrent winning action, full-inventory abort, planned rolls do not reroll, dungeon unique trophy. Multi-level grant and `character_unique_drops` for open-world PvE were thin (now covered) | None material |
| Mastery | Extra mastery XP, wrong family | **GOOD** | `MasteryIntegrationTest` + `MasteryProgressionTest`: victory-only, family-scoped, idempotent, unarmed grants none | None material |
| Techniques | Illegal loadout, techniques in legacy combat | **GOOD** | Loadout validator + combat IT (bleed technique, stamina reject, legacy rejects techniques) | None material |
| Status effects | Stun/bleed persist incorrectly; refresh desync | **GOOD** | `StatusEffectEngineTest` + combat IT persist/reload for bleed and stun immunity | None material |
| Expeditions | Double claim, rerolled haul, play while blocked | **GOOD** | `ExpeditionIntegrationTest`: start/claim idempotent, concurrent start, concurrent claim, failed claim keeps plan, combat blocks start | None material |
| Player Market | Duped gold/items, fee bypass, overfill | **GOOD** | `MarketIntegrationTest` + purchase/buy-order concurrency ITs + fee/buy-order domain rules | None material |
| NPC merchants | Free items, gold below zero | **GOOD** | `MerchantIntegrationTest`: priced stock, location, concurrent gold-for-one-item, full inventory rollback | None material |
| Crafting | Double craft, salvage equipped/listed | **GOOD** | `CraftingIntegrationTest` + `CraftingDomainTest`: claim once, concurrent claim, salvage rules | None material |
| Arena | Rating overwrite, extra marks, live gear swap | **GOOD** | `PvpIntegrationTest`: snapshot, settle once, concurrent completion, delayed delta not overwrite, forfeit, daily limit | None material |
| Database migrations | Schema drift, seed loss, Hibernate mismatch | **GOOD** | `Phase3BaselineSchemaIntegrationTest` (single V1, seed counts, connections); `HealthAndBootstrapIntegrationTest`; app `ddl-auto=validate` on every IT | Isolated baseline test maps only `EquipmentEntity`; full model is validated at Spring startup |
| Concurrency-sensitive flows | Lost updates, double rewards | **PARTIAL** → **GOOD** | Registration, movement, market buy/cancel, buy-order fill, merchant gold, combat win, expedition start/claim, crafting claim, Arena settle. Recovery, attribute allocation, and concurrent expedition start were missing (now covered) | Cross-system gold (merchant + listing fee in one race) is still untested; both sinks already lock the character row |

---

## What “dangerous” means here

A test is HIGH-RISK when failure would:

- create gold, items, XP, mastery, or Arena marks twice;
- destroy or discard planned rewards;
- let two characters or two accounts share a unique constraint incorrectly;
- heal or recover more than elapsed time allows;
- allow mutations during an occupied combat;
- let Flyway/Hibernate disagree with the live schema.

UI copy, preview text, and cosmetic DTO fields are out of scope.

---

## Area notes

### Authentication — GOOD

Register, login, logout, unauthenticated 401, wrong password, CSRF, case-insensitive email/name, and cookie attributes are covered. Concurrent duplicate email/name/account-character creation hits the database unique indexes and returns 409, not 500.

### Character lifecycle — GOOD

Creation asserts starting attributes, gold 100, vitals, and derived combat stats. Second character on the same account is rejected. Starter equipment is asserted in inventory tests.

### Progression / XP — GOOD after this task

Domain math already covered single-level, multi-level from zero, max-level cap, and legacy catch-up. Integration covered GET catch-up and a 1→2 combat win. The dangerous hole was a **single grant that crosses several levels** persisting points and not being re-applied on refresh.

### Attributes and respec — GOOD after this task

Paid respec after level 10 destroys gold (`GOLD_DESTROYED` / `RESPEC`). Respec unequips illegal gear, including a leftover off-hand on a two-hander. Combat already blocked allocation; respec now has the same net.

### Recovery — GOOD after this task

Clock-driven domain tests plus sequential GET write-avoidance were already strong. Concurrent `current()` calls are the tab-spam case.

### Inventory / equipment / item generation — GOOD

Ownership, capacity, stack merge, equipment FK integrity, and deterministic generation are covered at both domain and PostgreSQL layers.

### Combat — GOOD after this task

Engine, persistence, occupation, and exact-once settlement were already the strongest part of the suite. Added:

- multi-level planned XP applied once;
- open-world `once_per_character` loot written to `character_unique_drops` and excluded from the next plan.

Dungeon unique trophy coverage remains; it now also asserts the unique-drop row.

### Mastery / techniques / status effects — GOOD

Victory mastery is idempotent and family-scoped. Techniques are rejected in legacy combat and persist bleed/stun across reload.

### Expeditions / market / merchants / crafting / Arena — GOOD

Each has an exact-once or single-winner concurrency test plus rejection of the obvious integrity cheats (equipped sale, own listing, full inventory, salvage of listed gear).

### Migrations — GOOD

`V1__phase3_baseline.sql` is the active baseline. `Phase3BaselineSchemaIntegrationTest` boots a fresh Postgres 18 container, applies Flyway, and checks table/seed counts. Every Spring IT starts with `hibernate.ddl-auto=validate`.

### Concurrency — GOOD after this task

Covered races: registration, character name, movement, market purchase/cancel, buy-order fill, merchant gold, combat win, expedition start/claim/complete, crafting claim, Arena settle/defense stacking, recovery, attribute allocation.

---

## Safety-net tests added (Task 03)

| Test | Risk closed |
|------|-------------|
| `CharacterProgressionTest.overflowFromMidLevelStillAwardsEveryCrossedThreshold` | Mid-level overflow skips a threshold |
| `CharacterIntegritySafetyNetIntegrationTest.concurrentRecoveryAppliesElapsedTimeOnce` | Double-heal under concurrent GET |
| `CharacterIntegritySafetyNetIntegrationTest.respecIsRejectedDuringActiveCombat` | Respec / rebuild during a fight |
| `CharacterIntegritySafetyNetIntegrationTest.concurrentAllocationSpendsUnspentPointsOnce` | Double-spend of attribute points |
| `CharacterIntegritySafetyNetIntegrationTest.databaseRejectsNegativeGold` | `chk_characters_gold` bypass |
| `CombatSettlementSafetyNetIntegrationTest.multiLevelCombatGrantPersistsPointsAndDoesNotDoubleOnRefresh` | Multi-level XP/points on settlement + refresh |
| `CombatSettlementSafetyNetIntegrationTest.uniqueLootIsRecordedOnceAndExcludedFromLaterPlans` | Duplicate unique PvE items |
| `DungeonIntegrationTest` unique-drop row assertion | Dungeon trophy vs `character_unique_drops` drift |
| `ExpeditionIntegrationTest.concurrentStartsCreateExactlyOneActiveExpedition` | Two overlapping starts creating two active patrols |

Intentionally **not** added: getter-only tests, DTO mapping tests, or extra CombatEngine cases that only raise line coverage.

---

## What Phase 3 should not treat as done

These are product/design tasks, not missing integrity tests:

- Level 1–10 journey timing and unlock order
- Quest/NPC framework
- Balance validation of XP/gold curves
- Clan / social systems

When those ship, add tests for **their** exact-once rewards and occupation rules. Do not rebuild Combat/Market/Inventory test suites from scratch.
