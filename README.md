# Greyhaven

Browser-based persistent RPG MVP.

## Prerequisites

- Java 25 (`JAVA_HOME` must point to JDK 25; Maven Enforcer rejects other versions)
- Node.js 20.17+
- Docker Desktop (PostgreSQL via Compose)
- Maven Wrapper is included (`backend/mvnw`)

> Note: if host port `5432` is already used by another local Postgres, this project maps Compose Postgres to host port `5434`.

## Project layout

```text
backend/     Spring Boot modular monolith
frontend/    React + TypeScript + Vite SPA
docs/        Product and task specifications
docker-compose.yml
```

## Quick start

Starts PostgreSQL, the backend and the frontend, waits for each to become healthy, and
pins the Vite `/api` proxy to the backend port it actually started. Each run restarts
backend and frontend on the configured ports so you pick up the latest compiled code.

`scripts/dev-start.sh` is a Bash script. On Windows, Git Bash is the recommended way to
run it; you can also call `bash` from Command Prompt / PowerShell. Ensure `JAVA_HOME`
points at JDK 25 before starting.

### Git Bash (Windows, recommended)

1. Install [Git for Windows](https://git-scm.com/download/win) if needed.
2. Open **Git Bash** (Start menu → “Git Bash”).
3. Go to the repository root, for example:

```bash
cd /c/Projects/greyhaven
```

4. Confirm JDK 25 is visible (adjust the path if your install differs):

```bash
export JAVA_HOME="/c/Users/$USERNAME/.jdks/jdk-25.0.4+7"
export PATH="$JAVA_HOME/bin:$PATH"
java -version
```

5. Start the stack:

```bash
scripts/dev-start.sh
```

Optional port overrides:

```bash
BACKEND_PORT=8081 FRONTEND_PORT=5174 scripts/dev-start.sh
```

### macOS / Linux

From the repository root:

```bash
scripts/dev-start.sh
```

Optional port overrides:

```bash
BACKEND_PORT=8081 FRONTEND_PORT=5174 scripts/dev-start.sh
```

### Windows Command Prompt (cmd)

Git for Windows must be installed, and `bash` must be on `PATH` (typical with
"Git Bash" / Git for Windows). From the repository root:

```bat
bash scripts/dev-start.sh
```

If `bash` is not on `PATH`, use the full path:

```bat
"C:\Program Files\Git\bin\bash.exe" scripts/dev-start.sh
```

Optional port overrides:

```bat
set BACKEND_PORT=8081
set FRONTEND_PORT=5174
bash scripts/dev-start.sh
```

### Windows PowerShell

Same requirement: Git for Windows / `bash` available. From the repository root:

```powershell
bash scripts/dev-start.sh
```

Or with the full path:

```powershell
& "C:\Program Files\Git\bin\bash.exe" scripts/dev-start.sh
```

Optional port overrides:

```powershell
$env:BACKEND_PORT = "8081"
$env:FRONTEND_PORT = "5174"
bash scripts/dev-start.sh
```

Then open `http://localhost:5173`. Ctrl+C stops backend and frontend; PostgreSQL keeps
running. Backend and frontend output goes to `logs/backend.log` and `logs/frontend.log`.

The steps below run the same stack manually.

## Start PostgreSQL

```bash
docker compose up -d
```

Database defaults (local profile):

- host: `localhost:5434` (host port `5434` maps to container `5432`)
- database: `greyhaven`
- user: `greyhaven`
- password: `greyhaven`

## Backend

```bash
cd backend
./mvnw spring-boot:run -Dspring-boot.run.profiles=local
```

On Windows (Git Bash / PowerShell):

```bash
cd backend
./mvnw.cmd spring-boot:run -Dspring-boot.run.profiles=local
```

Useful endpoints:

- Health: `http://localhost:8080/actuator/health`
- Bootstrap probe: `http://localhost:8080/api/v1/bootstrap`

Run tests:

```bash
cd backend
./mvnw test
```

### Selenium (Task 2–5 browser automation)

Requires Docker (Testcontainers PostgreSQL), Node.js deps in `frontend/`, and Google Chrome.

```bash
cd frontend
npm install

cd ../backend
./mvnw verify -Pselenium
```

These tests start Spring Boot on a random port, launch Vite against that API, and drive Chrome headless through:

- Task 2: registration, login, logout, character creation, and conflict/redirect cases
- Task 3: location display, valid/invalid movement, refresh persistence, and nearby characters
- Task 4: inventory / equipment
- Task 5: encounter search, fight actions, and combat resolution UI
- Task 6: expeditions and activity feed (API coverage in backend integration tests)
- Task 7: marketplace (API coverage in backend integration tests)
- Task 8: global chat, office mode, and layout polish (API + frontend unit tests)
- Phase 2 Task 9: professions, timestamp crafting, salvage, marketplace fees, and buy orders

### Combat API (Task 5)

Authenticated session + CSRF required:

- `POST /api/v1/encounters/search`
- `GET /api/v1/encounters/current` (resume an unresolved Fight/Ignore prompt)
- `POST /api/v1/encounters/{id}/fight`
- `POST /api/v1/encounters/{id}/ignore`
- `GET /api/v1/combat/current` (ACTIVE fight, or unacknowledged result/reward screen)
- `POST /api/v1/combat/{id}/actions` body `{ "action": "HEAVY_ATTACK", "expectedRoundNumber": 3 }`
- `POST /api/v1/combat/{id}/acknowledge` (dismiss result/reward screen after combat ends)
- `POST /api/v1/character/attributes` body attribute deltas

Playable loop: travel to a dangerous location → search → fight → loot/XP → equip → allocate attribute points.

Combat rules worth knowing:

- Victory rewards are rolled and persisted when combat starts. If the loot does not fit, the
  killing round is rejected with `INVENTORY_FULL` and the same reward plan is retained.
  Inventory and character-build changes are unavailable during active combat.
- Defeat costs the fight and its rewards; the character is restored to 50% of max HP/stamina.

### Expeditions & Activity (Task 6)

Authenticated session + CSRF required:

- `POST /api/v1/expeditions` body `{ "strategy": "BALANCED" }` (CAUTIOUS / BALANCED / AGGRESSIVE)
- `GET /api/v1/expeditions/current` (ACTIVE or unclaimed COMPLETED; 204 when none)
- `POST /api/v1/expeditions/{id}/claim`
- `GET /api/v1/activity`

Forest Patrol lasts 20 minutes by server timestamps (no sleeping workers). The outcome is rolled
and persisted when the patrol starts, and stays hidden until the server clock passes `completesAt`,
so repeated inspect or claim requests can never reroll rewards — including a claim that fails
because the inventory is full. An active patrol does not block travel, encounters or any other
action; only one patrol may run at a time. Activity feed records combat victories, level-ups, item
finds, and expedition completed/claimed events.

### Marketplace (Task 7)

Authenticated session + CSRF required:

- `GET /api/v1/market/listings?itemType=&mine=`
- `POST /api/v1/market/listings` body `{ "itemInstanceId", "quantity", "price" }`
- `POST /api/v1/market/listings/{id}/buy`
- `DELETE /api/v1/market/listings/{id}`

Purchases are transactional and concurrency-safe. Listings can be created only at the Market,
and equipped items cannot be sold.

### Crafting, professions & Economy 2.0 (Phase 2 Task 9)

Authenticated session + CSRF required:

- `GET /api/v1/crafting/professions`
- `GET /api/v1/crafting/recipes`
- `GET /api/v1/crafting/jobs/current` (ACTIVE or unclaimed COMPLETED; 204 when none)
- `POST /api/v1/crafting/jobs` body `{ "recipeCode": "SMELT_IRON_INGOT" }`
- `POST /api/v1/crafting/jobs/{id}/claim`
- `POST /api/v1/items/{itemId}/salvage`
- `GET /api/v1/market/listings` query: `itemType`, `rarity`, `weaponFamily`, `minLevel`, `maxLevel`, `minPrice`, `maxPrice`, `sort` (`CREATED_AT` | `PRICE`), `direction`, `page`, `size`, `mine`
- `GET /api/v1/market/listings/history`
- `GET /api/v1/market/buy-orders`
- `POST /api/v1/market/buy-orders` body `{ "itemDefinitionId", "quantity", "maxUnitPrice" }`
- `POST /api/v1/market/buy-orders/{id}/fulfill` body `{ "itemInstanceId", "quantity" }`
- `DELETE /api/v1/market/buy-orders/{id}`

Characters start with Blacksmith, Alchemist, and Hunter at rank 1. Crafting jobs complete from
`startedAt` / `completesAt` (no sleeping worker threads). The rolled output, including rarity, is
persisted when the job starts so refresh cannot reroll quality. Claim grants the item and profession
XP exactly once. Salvage is allowed only at the Craftsmen Ward and rejects equipped or market-listed
items.

Player-market listings charge a configurable listing fee (1%) and sale fee (5%). Buy orders escrow
`quantity × maxUnitPrice` gold until partial fills, a complete fill, or cancel. Concurrent fills
lock the order row first.

### Global chat & office mode (Task 8)

Authenticated session required. CSRF is required for POST only.

- `GET /api/v1/chat/messages` — last 100 messages, oldest first
- `POST /api/v1/chat/messages` body `{ "body": "..." }` (max 500 characters, plain text)
- `GET /api/v1/chat/stream` — Server-Sent Events; optional `after` / `Last-Event-ID` replay

Server rules: one message per character every 2 seconds (serialized on the character row),
HTML/markup rejected, character name and timestamp stored with each message. The SPA loads
history over REST, then listens on SSE with `after` / `Last-Event-ID` replay capped to the
same last-100 window, and reconnects automatically.

The game shell is a three-column layout (character, main content, activity) plus a persistent
global chat bar. **Office mode** (compact) is a header toggle that removes decorative backgrounds,
tightens spacing, and hides location flavor text. The preference is stored in `localStorage`
under `greyhaven.uiMode`.

## Task 8 closeout

Implementation summary: global chat is REST POST + Spring MVC SSE (no WebSockets). Office mode
is a client-only compact layout. The playable vertical slice (auth, world, combat, expeditions,
market, activity, chat) is wired through `/api/v1` and the game shell.

Project tree:

```text
backend/src/main/java/com/example/game/{account,character,world,combat,item,inventory,expedition,market,activity,chat,shared}
backend/src/main/resources/db/migration
frontend/src/{api,auth,components,pages,ui}
docs/{MVP_SPEC.md,tasks/}
docker-compose.yml
```

Known technical debt:

- Chat SSE is in-process (`ChatSseHub`); a second application instance would not share live fans-out.
- `chat_messages` is append-only; old rows are not pruned (reads still cap at 100).
- Compact mode does not yet collapse the three-column layout on very short viewports.

Intentionally deferred (post-MVP / later Phase 2): clans, realtime PvP WebSockets, skill trees,
shared/clan professions, durability, multiple characters/regions, raids, world bosses, Redis, Kafka,
Kubernetes.

Phase 2 recommendations: migrate chat to WebSockets only if PvP or presence needs bidirectional
frames; add chat retention; keep marketplace and combat on the modular monolith until a real
scale requirement appears.

## Frontend

```bash
cd frontend
npm install
npm run dev
```

App: `http://localhost:5173`

Vite proxies `/api` and `/actuator` to `http://localhost:8080`.

Production build:

```bash
cd frontend
npm run build
```

Frontend unit tests:

```bash
cd frontend
npm test
```

## API endpoints

All gameplay routes require an authenticated session cookie except bootstrap, register, login,
and actuator health.

| Method | Path |
|--------|------|
| GET | `/api/v1/bootstrap` |
| POST | `/api/v1/auth/register` |
| POST | `/api/v1/auth/login` |
| POST | `/api/v1/auth/logout` |
| GET | `/api/v1/me` |
| POST | `/api/v1/characters` |
| GET | `/api/v1/character` |
| POST | `/api/v1/character/attributes` |
| GET | `/api/v1/world/location` |
| GET | `/api/v1/world/destinations` |
| GET | `/api/v1/world/nearby` |
| POST | `/api/v1/world/move` |
| GET | `/api/v1/inventory` |
| POST | `/api/v1/inventory/{itemId}/equip` |
| POST | `/api/v1/inventory/{itemId}/unequip` |
| POST | `/api/v1/inventory/{itemId}/use` |
| POST | `/api/v1/encounters/search` |
| GET | `/api/v1/encounters/current` |
| POST | `/api/v1/encounters/{id}/fight` |
| POST | `/api/v1/encounters/{id}/ignore` |
| GET | `/api/v1/combat/current` |
| POST | `/api/v1/combat/{id}/actions` |
| POST | `/api/v1/combat/{id}/acknowledge` |
| GET | `/api/v1/expeditions/current` |
| POST | `/api/v1/expeditions` |
| POST | `/api/v1/expeditions/{id}/claim` |
| GET | `/api/v1/market/listings` |
| GET | `/api/v1/market/listings/history` |
| POST | `/api/v1/market/listings` |
| POST | `/api/v1/market/listings/{id}/buy` |
| DELETE | `/api/v1/market/listings/{id}` |
| GET | `/api/v1/market/buy-orders` |
| POST | `/api/v1/market/buy-orders` |
| POST | `/api/v1/market/buy-orders/{id}/fulfill` |
| DELETE | `/api/v1/market/buy-orders/{id}` |
| GET | `/api/v1/crafting/professions` |
| GET | `/api/v1/crafting/recipes` |
| GET | `/api/v1/crafting/jobs/current` |
| POST | `/api/v1/crafting/jobs` |
| POST | `/api/v1/crafting/jobs/{id}/claim` |
| POST | `/api/v1/items/{itemId}/salvage` |
| GET | `/api/v1/activity` |
| GET | `/api/v1/dev/diagnostics` (local/test only) |
| GET | `/api/v1/chat/messages` |
| POST | `/api/v1/chat/messages` |
| GET | `/api/v1/chat/stream` |
| GET | `/actuator/health` |

## Database tables

Managed by Flyway (`backend/src/main/resources/db/migration/V1__phase3_baseline.sql`).
Phase 1–2 incremental scripts are archived at `backend/src/main/resources/db/archive/phase1-phase2` and are not applied.
After pulling this baseline, recreate the local Compose volume (`docker compose down -v && docker compose up -d`).

`schema_meta`, `accounts`, `characters`, `locations`, `location_connections`,
`item_definitions`, `item_definition_modifiers`, `item_instances`, `equipment`,
`affix_definitions`, `item_instance_affixes`, `monster_definitions`,
`monster_loot_entries`, `location_encounter_weights`, `encounters`, `combat_sessions`,
`combat_events`, `combat_reward_items`, `combat_status_effects`,
`combat_technique_definitions`, `weapon_masteries`, `character_techniques`,
`technique_loadout_slots`, `dungeon_definitions`, `dungeon_rooms`, `dungeon_room_edges`,
`dungeon_runs`, `dungeon_run_rooms`, `character_unique_drops`, `merchant_definitions`,
`merchant_stock`, `market_listings`, `market_buy_orders`, `market_buy_order_fills`,
`salvage_outputs`, `expeditions`, `expedition_reward_items`, `character_professions`,
`crafting_recipes`, `crafting_recipe_inputs`, `crafting_jobs`, `arena_defense_profiles`,
`pvp_matches`, `pvp_match_snapshots`, `pvp_match_events`, `pvp_match_statuses`,
`pvp_battle_history`, `activity_entries`, `chat_messages`, `game_telemetry_events`,
`flyway_schema_history`.

## Profiles

| Profile | Purpose |
|---------|---------|
| `local` | Default local development against Docker PostgreSQL |
| `test`  | Automated tests (Testcontainers PostgreSQL) |

Flyway migrations live in `backend/src/main/resources/db/migration`.
Hibernate `ddl-auto` is `validate` — schema changes must go through Flyway.

PostgreSQL 18 Compose volume is mounted at `/var/lib/postgresql` (not `/var/lib/postgresql/data`).

## Phase 2 verification

`GET /api/v1/dev/diagnostics` is authenticated and enabled only when `greyhaven.diagnostics.enabled=true` (`local` and `test` profiles). The default is off; production answers 404.

Automated coverage: backend unit/integration tests (including telemetry and `GET /api/v1/dev/diagnostics` on the test profile) and Selenium `Phase2SeleniumIT` for inventory, equipment, crafting, market, Arena defense, Office Mode, and reload.

Manual checklist before calling Phase 2 complete:

1. Login and load a character
2. Gain XP, level up, allocate attributes
3. Equip several armor slots and compare a Rare item
4. Unlock mastery/technique and change loadout
5. Win a Combat 2.0 fight with status effects
6. Enter the Ruined Keep dungeon and defeat the boss
7. Configure Arena defense and complete an Arena challenge
8. Craft an item, salvage unwanted gear, list on the Market, create or fill a buy order
9. Switch to Office Mode, close the browser, return, and confirm persistent state

