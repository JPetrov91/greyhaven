# Sparring Yard

## Status

Implemented. This file is the product and architecture record for the live Sparring Yard.

Authoritative specs this work implements or refines:

- `docs/PHASE_3_SPEC.md` §6 Training Grounds
- `docs/game-design/LEVEL_1_10_PLAYER_JOURNEY.md` (practice before ranked Arena)
- `docs/tasks/phase-2/08-pvp-arena.md` (unranked live duels)
- Mockup: `docs/mockups/sparring-yard.png`

## Purpose

Give levels 1–10 a **safe practice place** next to City Square: unranked live duels against other recruits, and generated drill bots that use the real Combat engine.

Ranked Arena stays a separate building and a later recommendation. The yard must not become the best XP or gold farm.

---

# Business

## Player-facing offer

The Sparring Yard is a Greyhaven **SAFE** location (recommended levels 1–10), connected both ways to City Square.

| Activity | What the player does | What they get | What they do not get |
| --- | --- | --- | --- |
| Live duels | Challenge another character who is also in the yard, level ≤ 10 | Real Combat 2.0 / live duel resolution | Rating, Arena marks, honor, item loss |
| Training drills | Fight a generated bot of chosen level 1–10 | Practice timing, stamina, techniques | Silver, XP, loot, Arena marks |

Copy in the UI is explicit: drills teach timing, not loot. Defeat does not take gear. Ranked steel stays in the Arena (from level 11).

## Why this is not the Arena

Phase 3 called this “Training Grounds” and originally placed it on the Arena. The shipped product decision is a **separate location**:

- New players can walk there from the Square without entering the ranked building.
- Arena remains the ranked / defense / marks fantasy.
- Live duels are **location-gated** to the yard, not the Arena.
- Players above level 10 cannot start yard duels or drills (`SPARRING_LEVEL_REQUIRED`).

Journey beat “prove the build before the hideout” still holds. The place to do it is the yard, not a ranked Arena tab.

## Rules the player can trust

- Server is authoritative. The client only sends intent (`challenge`, `botLevel`).
- Bots never pretend to be player characters. Catalog names are fixed; combat ratings are rolled at fight start into an immutable session snapshot.
- Drill monster definition rows are stubs (1 HP / 0 rewards). Live stats come from `SparringBotGenerator` + `RandomProvider`.
- Drill reward plans are exactly-once zeros: `CombatRewardService` short-circuits `SPARRING_BOT_*` codes.
- Both fighters in a live duel must be in the yard and at or below level 10.
- Refresh-safe: `#sparring` is a Home activity, not a separate game view. Combat resume still uses the existing combat / duel queries.

## What is not in this release

- Posted duel requests / accept-decline queue (mockup chrome only).
- Daily practice limit, practice tokens, XP/silver from drills.
- Ranked marks, honor, or seasonal tracks.
- Activity-rail “recent sparring” as a dedicated feed (generic activity still applies).

---

# Architecture

## Backend module

New feature package `com.example.game.sparring` with the usual layers:

| Layer | Types |
| --- | --- |
| `api` | `SparringController`, `SparringBotResponse`, `SparringDrillRequest` |
| `application` | `SparringApplicationService`, `SparringErrors` |
| `domain` | `SparringBots`, `SparringBotCatalogEntry`, `SparringBotProfile`, `SparringBotGenerator` |

No new combat engine. Drills call `CombatApplicationService.startSparringDrill`. Live duels stay in `pvp` (`PvpDuelApplicationService`) with yard location checks in `PvpMatchSupport`.

### HTTP

| Method | Path | Intent |
| --- | --- | --- |
| `GET` | `/api/v1/sparring/bots` | Catalog of ten named bots (level, name, code) |
| `POST` | `/api/v1/sparring/drills` | Body `{ "botLevel": 1–10 }` → starts a PvE combat session |

World already exposes `CHALLENGE_DUEL` and `START_SPARRING_DRILL` on the location. Nearby characters use the existing world nearby API.

### Persistence

Flyway `V6__sparring_yard.sql`:

- Location `SPARRING_YARD` + connections to/from City Square.
- Ten `monster_definitions` stubs `SPARRING_BOT_L01` … `L10` (xp/gold 0).
- `combat_sessions.snap_enemy_max_health` so rolled drill HP can differ from the stub.

Do not edit `V6` after it has been applied.

### Domain catalog

Fixed names (must stay in sync with art codes):

| Level | Code | Name |
| --- | --- | --- |
| 1 | `SPARRING_BOT_L01` | Green Recruit |
| 2 | `SPARRING_BOT_L02` | Street Sparrer |
| 3 | `SPARRING_BOT_L03` | Watch Cadet |
| 4 | `SPARRING_BOT_L04` | Yard Regular |
| 5 | `SPARRING_BOT_L05` | Militia Drillman |
| 6 | `SPARRING_BOT_L06` | Veteran Sparrer |
| 7 | `SPARRING_BOT_L07` | Watch Corporal |
| 8 | `SPARRING_BOT_L08` | Yard Sergeant |
| 9 | `SPARRING_BOT_L09` | Drill Champion |
| 10 | `SPARRING_BOT_L10` | Watch Provost |

`SparringBots.MAX_PLAYER_LEVEL = 10`. Ranked Arena recommendation remains level 11+.

### Integrity reuse

- `CharacterCombatGuard` / unresolved encounter / pending outcome checks before a drill.
- Injectable `Clock` and `RandomProvider` on drill start.
- PvP duel snapshots unchanged; only the *where* and *who* gates changed.

## Frontend shell

Sparring is **not** a left-nav destination and **not** a full-page replacement like Arena (`#pvp`).

`#sparring` resolves to **Home** (`gameViewFromLocation`). `gameLink('sparring')` still writes the hash so the yard activity can be opened and scrolled.

| Home state | Hash | Mid-row (character / equipment / expeditions) | Yard fight panels |
| --- | --- | --- | --- |
| Default at any location | `` | Shown | Hidden |
| Default at the yard | `` | Shown | Hidden |
| Duels opened, player at yard | `#sparring` | Hidden | Shown |
| `#sparring` but not at the yard | `#sparring` | Shown | Hidden |

`GameLayout` computes `showYard` from the hash **and** current location actions (`CHALLENGE_DUEL` / `START_SPARRING_DRILL`). `LocationPanel` receives `showYard` and `onOpenSparring` (toggle hash ↔ Home).

Hero action row on the yard includes **Duels** (second tile after Travel). Selected tile uses global `location-hero-tile.is-selected`. The world (`#world`) page can open the same activity; it does not replace Home.

`AppShell` hash-focus includes `sparring` so `#sparring` scrolls to `id="sparring"`.

## UI composition (no yard-only theme)

Panels are built from the UI engine:

- `Panel`, `CompactDataRow`, `Field`, `TextInput` (`type="range"`), `Button`, `StatusBadge`, `EmptyState`
- Layout primitives `ui-split` and `ui-stack` in `layout.css`
- Range styling `ui-range` in `forms.css` (global control, not a location skin)
- Portrait list rows: `ui-row-has-portrait` in `rows.css`
- Tall figure well: `portrait-tall` in `components.css`

`SparringYardPanel` is embedded under the location hero. It does not wrap itself in a second “Sparring Yard” page chrome — the hero already names the place.

## Art

| Asset | Path | Role |
| --- | --- | --- |
| Location banner | `frontend/public/locations/sparring_yard.webp` | Home / world hero. `locationArtUrl('SPARRING_YARD')` — no longer aliases Arena art. |
| Weather | Overcast, 12°C, cloud icon | Distinct from Arena (Dusty / wind). |
| Bot mini | `frontend/public/sparring/mini/sparring_bot_lNN.webp` | Training bots list. |
| Bot full | `frontend/public/sparring/full/sparring_bot_lNN.webp` | Selected-bot preview and Combat fighter sprite. |

Mapping: `frontend/src/ui/sparringMedia.ts`. Combat reuses it: `monsterCombatArtUrl` returns the full plate when `code` starts with `SPARRING_BOT_`.

Combat sprites must be **RGBA cutouts** (transparent ground), same contract as `/combat/player.webp` and `/combat/street_thug.webp`. Scenic backgrounds read as a box on the arena backdrop. Mini plates stay square busts for the ledger row.

## Tests

- Domain: `SparringBotGeneratorTest`
- API: `SparringIntegrationTest`
- World / location actions: `LocationActionsTest`, `WorldLocationIntegrationTest`
- Frontend: `SparringYardPanel.test.tsx`, `LocationPanel` yard cases, `GameLayout` Home vs `#sparring`, `locationMedia`, `sparringMedia`, `combatMedia`, layout/forms/rows engine tests

---

# Product flow (as shipped)

1. Player travels to Sparring Yard (or is already there on Home).
2. Home shows the yard banner and the usual overview panels.
3. Player clicks **Duels**.
4. Overview panels hide. Live duels + training bots load on the same page.
5. Challenge a nearby recruit, or Fight / Start drill against a bot.
6. Existing Combat or PvP combat shell takes over for the match.
7. After the match, Home + `#sparring` (if still at the yard) returns them to the fight panels.

---

# Spec deltas

| Earlier spec | Shipped |
| --- | --- |
| Training Grounds as an Arena mode / tab | Separate `SPARRING_YARD` location |
| TG unlock emphasized at Level 8 | Location available 1–10; ranked still after 10 |
| Generated bots only | Bots **and** unranked live duels in one place |
| Arena PvE preview boards | Live drills on the Combat engine |
