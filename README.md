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

## Profiles

| Profile | Purpose |
|---------|---------|
| `local` | Default local development against Docker PostgreSQL |
| `test`  | Automated tests (Testcontainers PostgreSQL) |

Flyway migrations live in `backend/src/main/resources/db/migration`.
Hibernate `ddl-auto` is `validate` — schema changes must go through Flyway.

PostgreSQL 18 Compose volume is mounted at `/var/lib/postgresql` (not `/var/lib/postgresql/data`).
