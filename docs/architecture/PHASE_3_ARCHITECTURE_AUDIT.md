# Phase 3 Architecture Readiness Audit

**Date:** 2026-08-15  
**Scope:** Current Greyhaven modular monolith (post Task 01 database baseline)  
**Authoritative specs:** `docs/MVP_SPEC.md`, `docs/PHASE_2_SPEC.md`, `docs/PHASE_3_SPEC.md`, `docs/PHASE_3_EXECUTION.md`, `AGENTS.md`  
**Goal:** Phase 3 readiness, not architectural elegance. No rewrites recommended merely to look cleaner.

**Method:** Read specs; inspect backend feature modules (`api` / `application` / `domain` / `infrastructure`); inspect frontend `api/`, `components/`, `pages/`; verify Clock/RNG, transactions, locks, entity leakage, and combat/economy integrity in source.

---

## Verdict

The current architecture **is ready to start Phase 3 gameplay work**.

- Modular monolith packaging matches the spec.
- Domain engines are pure Java and testable.
- Controllers are thin. HTTP APIs do not expose JPA entities.
- Clock and `RandomProvider` are injected for gameplay time and RNG.
- Economy and combat use transactions, pessimistic locks, and exact-once reward plans.
- Arena combat already uses immutable snapshots.

**CRITICAL findings: none.**

Phase 3 completion criterion (“architecture audit has no unresolved critical findings”) is already met on integrity and server-authority grounds.

**IMPORTANT findings: 5.** These are targeted, local fixes that reduce risk for quests, combat refinement, Training Grounds, and heavier UI polling. They are listed at the end for human approval. Do not implement until approved.

Do **not** merge PvE and PvP engines, do **not** introduce hexagonal ports everywhere, and do **not** split `inventory`/`item` into new modules before Phase 3 content exists.

---

## 1. What is already sound

| Area | Status |
|------|--------|
| Feature packages | `account`, `character`, `world`, `combat`, `item`, `inventory`, `expedition`, `market`, `activity`, `chat`, plus Phase 2 `mastery`, `crafting`, `pvp`, `dungeon`, `telemetry`, `shared` |
| Layering | Consistent `api` → `application` → `domain`; `item` has no HTTP `api` (internal catalog) |
| Domain purity | No Spring / JPA / HTTP imports in `*.domain` |
| Server authority | Combat, loot, XP, gold, market, crafting, expeditions resolved on the server |
| Character hub | Other modules call `CharacterVitalsService`, location services, and guards — not `CharacterEntity` (except character’s own application layer) |
| Occupation inversion | `CharacterCombatGuard` / `CharacterOccupationExtension` / `CharacterTravelGuard` let combat, PvP, and dungeon plug in without character importing them |
| Exact-once rewards | Combat and expeditions pre-roll plans; `rewards_applied` / claimed status + row locks |
| Inventory-full combat | Split `TransactionTemplate`: persist turn, then claim rewards so a full bag does not reroll loot |
| Arena snapshots | Insert-only JSON snapshot; live defender gear changes do not affect the match (covered by integration test) |
| Clock | No `Instant.now()` / `System.currentTimeMillis()` in `src/main`; `ClockConfig` → `Clock.systemUTC()` |
| RNG | Gameplay uses `RandomProvider`; tests use scripted providers. No `Math.random()` in production gameplay |
| Flyway | Active path is `V1__phase3_baseline.sql`; Phase 1–2 scripts archived |

---

## 2. Evaluation by requested area

### 2.1 Modular monolith boundaries

**Healthy.** Sixteen feature packages with the expected layers.

**Coupling hubs (expected in a game monolith):** `item` (catalog), `character` (vitals/location), `combat` (engine reused by dungeon/PvP).

**Not a Phase 3 blocker:** `item` and `inventory` are one bounded context split across two packages (`EquipmentSlot` ↔ `ItemDefinitionData`). Keep them as-is unless a tiny cycle break is approved (see MEDIUM).

**Acceptable:** Application services using their own module’s JPA entities. That is the established transaction-script style.

### 2.2 Dependency direction

**Intended:** `api` → `application` → `domain`; other features depend on application views/ports, not foreign infrastructure.

| Pattern | Assessment |
|---------|------------|
| Controllers → `AccountPrincipal` in `account.infrastructure` | Layer skip; universal; MEDIUM |
| `inventory.application` → `item.infrastructure` entities/repos | Real bypass of `ItemCatalogService`; IMPORTANT |
| `dungeon.application` → `combat.infrastructure` encounter/monster repos | Bypass of combat application for persistence; MEDIUM |
| PvP/dungeon API DTOs composing `combat.api` types | Pragmatic reuse; MEDIUM |
| Domain → other domain (`combat` → `mastery.TechniqueEffectSpec`) | Fine for game rules |
| `character` ↛ `combat` | Correct one-way direction |

### 2.3 Oversized services

| Class | Lines | Role |
|-------|------:|------|
| `InventoryApplicationService` | 1176 | Grants, transfers, equip, potions, market snapshots, salvage sources |
| `CombatApplicationService` | 1106 | Turns, snapshots, loot plan/apply, vitals, dungeon events, previews |
| `MarketApplicationService` | 664 | Listings + buy orders |
| `MarketPanel.tsx` | 1740 | Merchants + player market + buy orders |

These are **maintainability risks** for Phase 3 edits, not integrity bugs. Proposed IMPORTANT fix is a **narrow extract** of combat rewards only, not a service rewrite.

### 2.4 Duplicated domain logic

| Logic | Status |
|-------|--------|
| Derived stats | Centralized in `CharacterStatCalculator` |
| XP / level | Centralized in `CharacterProgression` via `CharacterEntity.grantExperience` |
| Loot tables | `LootGenerator` / `UniqueLoot` |
| Strike math | Shared `CombatStrikeResolver` used by PvE `CombatEngine` and `PvpCombatEngine` |
| Round orchestration | Duplicated between `CombatEngine` (~447) and `PvpCombatEngine` (~384) — MEDIUM, do not merge for elegance |
| `masteryPassive` | Copied in `CombatApplicationService` and `PvpSnapshotFactory` — MEDIUM |
| `Phase1CombatEngine` | Frozen path for `CombatRulesVersion.PHASE_1` only; **new sessions are Combat 2** |

### 2.5 Domain logic inside controllers

**None found.** Controllers map views to response records. Example: `CombatController` delegates `submitAction` and maps `CombatView`.

### 2.6 Persistence entity leakage

**HTTP:** No `*Entity` types on controller method signatures or response DTOs.

**Internal:** Application services operate on entities. Acceptable.

**Cross-module:** Inventory and dungeon reach into another module’s repositories (see IMPORTANT / MEDIUM).

### 2.7 Transaction boundaries

Economy and combat writes use `@Transactional`. Cross-module helpers that mutate inventory/gold/activity use `Propagation.MANDATORY` so they cannot run outside a caller transaction.

Combat victory uses two transactions on purpose (turn commit, then reward claim). That is correct.

**Fragility (MEDIUM):** market `buy()` marks sold then transfers; safe only because it is one transaction.

### 2.8 Concurrency-sensitive operations

Pessimistic locks exist on characters, item instances, equipment, listings, buy orders, combat sessions, encounters, expeditions, crafting jobs, dungeon runs, PvP matches, professions, mastery.

Market buy concurrency is integration-tested. Merchant gold concurrent purchase is tested. Arena start locks attacker/defender in UUID order.

**Gaps (MEDIUM):** no DB constraint that listing reserved qty ≤ stack qty (application + character lock today); fewer tests for equip-vs-list races.

### 2.9 CombatEngine isolation

`CombatEngine`, `CombatStrikeResolver`, `StatusEffectEngine`, `EnemyAi`, `LootGenerator` are pure Java with injected `RandomProvider`. Unit tests exist.

`CombatApplicationService` is the orchestration bottleneck (IMPORTANT extract of rewards only).

PvP does **not** call `CombatEngine`. It uses `PvpCombatEngine` + shared strike resolver + snapshots. That is the correct player-vs-player path for Training Grounds. **Do not build a third engine. Do not merge PvE monster combat into PvP.**

### 2.10 Character progression isolation

Pure: `CharacterProgression`, `CharacterStatCalculator`, `ExperienceProgress`, `CharacterRecovery`.

Application split: attribute allocate/respec in `CharacterProgressionService`; XP/gold grants in `CharacterVitalsService`. Naming is slightly misleading (MEDIUM). Math is isolated.

`GET` character always locks and flushes recovery (IMPORTANT).

### 2.11 ItemDefinition / ItemInstance integrity

Schema: `fk_item_instances_definition`; stack unique index `uq_item_instances_owner_stackable_definition`; non-stackable quantity checks; instance row locks.

Instances cannot exist without a definition at the database level.

### 2.12 Inventory / Equipment interactions

Composite FK `fk_equipment_owned_item (item_instance_id, character_id)`; unique equipped instance; two-hand clears off-hand; equip blocked when listed; market/merchant/salvage blocked when equipped.

`CharacterCombatGuard` blocks equip during PvE combat and PvP occupation.

### 2.13 Market / Merchant / Crafting economy

Gold: `chk_characters_gold`, entity `spendGold`, vitals re-lock. Pricing in `MerchantPriceCalculator` / `MarketBalance`.

Crafting: rolls stored at job start; claim replays snapshot; `Clock` for job completion.

Merchant stock is static seed (no refresh RNG required).

Market listing expiration is **not implemented** (product placeholder “Time left”). Not a baseline defect.

### 2.14 Arena snapshot architecture

`PvpSnapshotFactory.capture` freezes attributes, derived stats, equipment, techniques, mastery passive, potion charges. Payload stored once on `pvp_match_snapshots`. Turns load snapshot + mutable HP/stamina/status on the match row.

Attacker occupation blocks live mutations. Defender may change live gear; combat ignores it.

**Training Grounds (Task 09):** snapshot + `PvpCombatEngine` path is reusable. Missing pieces are product work, not current defects: `TRAINING` match kind, bot snapshot factory, per-match RNG seed. Do not treat “bots don’t exist yet” as architecture failure.

### 2.15 Clock usage

Compliant in `src/main`. Expeditions, recovery, crafting, market timestamps, dungeon pause/resume, combat, PvP, chat rate limits, telemetry all use `Instant.now(clock)`.

### 2.16 RNG injection

Compliant for combat, loot, encounters, expeditions, item/affix generation, crafting, PvP.

No per-match seed stored. Live play does not need it. Training Grounds / replay (Task 09) should add `rng_seed` on the match row then — not now.

### 2.17 Frontend API / state separation

```
frontend/src/api/     fetch wrappers + types
frontend/src/pages/   auth / create character only
frontend/src/components/  TanStack Query + UI
frontend/src/ui/      presentational
```

`GameLayout` owns combat/encounter/expedition/PvP queries. Pages stay thin. Rewards, merchant prices, and combat outcomes come from the server.

**Gaps:** `PvpCombatPanel` does not render server `actionPreviews` (IMPORTANT). `CombatPanel.fallbackPreviews` hardcodes stamina defaults if previews are empty (MEDIUM). `MarketPanel` is oversized (MEDIUM).

### 2.18 Obvious Phase 1/2 technical debt

- `Phase1CombatEngine` retained for in-flight v1 sessions only.
- Item `legacy` flag / starter loadout.
- `ComingLater` / guild placeholder UI (intentional Phase 3 product work).
- No `TODO` / `FIXME` / mock gameplay services in `src/main`.
- `GameBalanceCatalog.get()` static reads from domain balance classes — testable enough today; MEDIUM if Phase 3 needs hot-reload balance.

---

## 3. Findings

Severity:

- **CRITICAL** — integrity, server-authority, or data-loss defect that must be fixed before Phase 3 content.
- **IMPORTANT** — will make Phase 3 work unsafe or expensive; small targeted fix.
- **MEDIUM** — real debt; fix when touching that area.
- **MINOR** — cleanup.

### CRITICAL

None.

---

### IMPORTANT

#### IMP-1 — Gold spend on respec bypasses `CharacterVitalsService`

**Why it matters:** Phase 3 will add more gold/XP sinks (quests, onboarding, economy). All mutations should go through one vitals API so locks, recovery sync, and telemetry stay consistent.

**Evidence:** `CharacterProgressionService.respec` calls `character.spendGold` on the entity. Telemetry is recorded separately. Other economy paths use `CharacterVitalsService.spendGold`.

**Minimal fix:** After the existing character row lock, call `characterVitalsService.spendGold(character.getId(), cost, GoldDestroyReason.RESPEC)` (or a variant that assumes the row is already locked) and delete the direct `spendGold` on the entity. Keep respec/un-equip in the same transaction.

**Do not:** Introduce a new “wallet microservice” or event bus.

#### IMP-2 — `GET /character` always pessimistic-locks and flushes recovery

**Why it matters:** Office Mode and Phase 3 shell will poll character state. Today `CharacterApplicationService.current` uses `findWithLockByAccountId` and `CharacterStateSyncService.sync` always `saveAndFlush`, updating `lastRecoveryAt`.

**Evidence:** `CharacterApplicationService.current` (transactional, lock); `CharacterStateSyncService.sync` always checkpoints recovery.

**Minimal fix:**

1. Load without lock for the read path.
2. If recovery or level catch-up would change HP/STA/level, re-load with lock and apply.
3. Skip `saveAndFlush` when nothing changed.

Keep lock+sync on allocate, respec, and all gold/XP mutations.

#### IMP-3 — Combat rewards mixed into the turn orchestrator

**Why it matters:** Task 08 (combat refinement) and Task 06 (quest combat objectives / exact-once rewards) will edit `CombatApplicationService` (1106 lines). Reward plan/apply is already a distinct, well-designed subsystem inside it.

**Evidence:** Same class: `persistTurn`, `createRewardPlan`, `claimVictoryRewards` / `applyRewardsExactlyOnce`, snapshot build, action previews.

**Minimal fix:** Move plan + exact-once apply to a package-private `CombatRewardService` in `combat.application`. Leave `CombatApplicationService` as the turn/session orchestrator. Do not invent Combat 3.0.

#### IMP-4 — Inventory mutates items via `item.infrastructure` instead of `ItemCatalogService`

**Why it matters:** `ItemCatalogService` exists specifically so other modules do not depend on item persistence. Inventory still loads `ItemDefinitionEntity` / modifier / affix / instance repositories. Quest item grants will keep growing this class (1176 lines).

**Evidence:** `InventoryApplicationService` has no `ItemCatalogService` import; definition reads go through item repos.

**Minimal fix:** Route **definition reads** through `ItemCatalogService` (already returns `ItemDefinitionView` / `toData()`). Leave **instance persistence** in inventory for now (inventory is the operational owner of stacks/equipment). Do not extract a full `ItemInstanceService` port unless Task 06 proves it is needed.

#### IMP-5 — Arena UI ignores server action previews

**Why it matters:** Server already computes hit chance, stamina cost, and disabled reasons (`PvpMatchSupport.actionPreviews`). `PvpCombatPanel` renders a hardcoded action list. Phase 3 combat polish and Training Grounds will look broken even though the server is authoritative (invalid clicks still 4xx).

**Evidence:** `frontend/src/components/PvpCombatPanel.tsx` `ACTIONS` constant; `match.actionPreviews` unused. Contrast `CombatPanel`, which uses `combat.actionPreviews`.

**Minimal fix:** Render buttons from `match.actionPreviews` the same way PvE does (disable + reason from server). Do not compute hit chance or stamina on the client.

---

### MEDIUM

| ID | Finding | When to fix |
|----|---------|-------------|
| M-1 | `PvpCombatEngine` duplicates round orchestration with `CombatEngine` | Do not merge. Training Grounds must reuse **PvP** engine + snapshots. Optionally extract a shared helper only if Task 08/09 touches both. |
| M-2 | `masteryPassive` duplicated in combat app vs `PvpSnapshotFactory` | Extract one helper when next editing snapshots. |
| M-3 | `DungeonApplicationService` uses `EncounterRepository` / `MonsterDefinitionRepository` | Add encounter query/create methods on `EncounterApplicationService` during dungeon refinement (Task 10). |
| M-4 | Listing reserved quantity enforced only in application code | Add a constraint/trigger if market bugs appear; otherwise leave. |
| M-5 | `market_listings.item_instance_id` `ON DELETE SET NULL` | Prefer `RESTRICT` if a later migration touches listings. |
| M-6 | Market `buy()` marks sold before transfer | Document invariant; reorder only if the method is rewritten. |
| M-7 | Market purchase gold spend often omits `GoldDestroyReason` | Add `MARKET_PURCHASE` when touching telemetry (Task 19). |
| M-8 | Merchant `goldRemaining` computed from pre-spend vitals | Re-read gold after mutation when touching merchant API. |
| M-9 | Controllers depend on `account.infrastructure.AccountPrincipal` | Move principal to `account.application` or `shared` if security is retouched. |
| M-10 | API DTOs import domain enums (`ItemRarity`, `CombatAction`) | Acceptable; map to strings only if clients need decoupling. |
| M-11 | `CharacterProgressionService` name vs XP living in vitals | Rename later if confusing; not required for Phase 3. |
| M-12 | `CombatPanel.fallbackPreviews` hardcoded stamina defaults | Remove fallback once previews are guaranteed on every combat payload. |
| M-13 | `MarketPanel.tsx` 1740 lines | Split tabs during UI productization (Task 14), not now. |
| M-14 | `inventory` ↔ `item` domain cycle on `EquipmentSlot` | Optional: move `EquipmentSlot` into `item.domain`. Tiny, but not required to start Phase 3. |
| M-15 | `Phase1CombatEngine` still on the submit path | Sunset when no v1 sessions remain; do not delete while old rows can exist. |
| M-16 | No per-match RNG seed | Add in Task 09 Training Grounds. |
| M-17 | `GET` character lock also used as recovery driver | Covered by IMP-2. |
| M-18 | Expedition start guard does not consult PvP/dungeon occupation | Location actions currently prevent arena expeditions; extend the guard when quests start expeditions from more places. |
| M-19 | Inventory embeds `MerchantPriceCalculator` | Fine until a second pricing path appears. |
| M-20 | Static `GameBalanceCatalog.get()` in domain | Inject catalog only if live balance tools need it. |

---

### MINOR

| ID | Finding |
|----|---------|
| N-1 | Duplicate listed-qty check in `equipForCharacter` |
| N-2 | Consume/destroy equipped items throw `itemNotOwned` |
| N-3 | Public equipped-item query skips missing definitions silently |
| N-4 | Combat attribute-points-gained recomputed from level delta |
| N-5 | PvP types live in `api/pvp.ts` vs shared `api/types.ts` |
| N-6 | Public inspect shows live gear during an arena match (combat still uses snapshot) |

---

## 4. Phase 3 constraints (do not “fix” these)

These look like architecture gaps but are **future tasks**:

| Topic | Correct action |
|-------|----------------|
| Quest/NPC module | Task 06 — add a module; reuse inventory/combat/progression; do not duplicate item/combat models |
| Training Grounds bots | Task 09 — `TRAINING` kind + bot snapshot factory + seeded RNG on **existing** PvP snapshot path |
| Clan module | Task 15 — new package; do not redesign chat/activity first |
| Combat 3.0 / Inventory 3.0 / Market 3.0 | Forbidden by Phase 3 spec |
| Second combat engine | Forbidden. PvP engine already exists; bots must use it, not `CombatEngine` (monster PvE) |
| Microservices, Kafka, CQRS, Redis | Forbidden |

---

## 5. Proposed IMPORTANT fixes (awaiting human approval)

Implement **only** items checked below, after approval. Each is a small, behavior-preserving change with tests.

| Approve? | ID | Change | Tests |
|----------|----|--------|-------|
| [x] | IMP-1 | Respec gold via `CharacterVitalsService` | Existing progression integration test; assert gold + telemetry |
| [x] | IMP-2 | Character read path: lock/flush only when recovery or catch-up mutates | Unit test on sync; integration GET does not require a write when already recovered |
| [x] | IMP-3 | Extract `CombatRewardService` (plan + exact-once apply) | Existing combat integration tests must stay green |
| [x] | IMP-4 | Inventory definition reads via `ItemCatalogService` | Existing inventory/equip tests |
| [x] | IMP-5 | `PvpCombatPanel` uses `actionPreviews` | Frontend PvP/combat panel tests |

**Not proposed for this pass:** splitting `InventoryApplicationService`, merging combat engines, DB listing triggers, moving `AccountPrincipal`, frontend `MarketPanel` split.

---

## 6. Recommended Phase 3 working rules

1. New systems (quests, clans, Training Grounds) **call existing application services**; they must not open a second item, combat, or gold path.
2. Gold/XP/item grants go through `CharacterVitalsService` and `InventoryApplicationService` grant methods.
3. Time: inject `Clock`. Random: inject `RandomProvider`. Training Grounds may add a seeded adapter later.
4. Exact-once: pre-persist reward plans; flip a row flag in the same transaction as the grant.
5. Do not start the next Phase 3 task until this audit’s approved IMPORTANT items are done or explicitly deferred.
