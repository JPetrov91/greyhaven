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
./mvnw spring-boot:run
```

On Windows (Git Bash / PowerShell):

```bash
cd backend
./mvnw.cmd spring-boot:run
```

Useful endpoints:

- Health: `http://localhost:8080/actuator/health`
- Bootstrap probe: `http://localhost:8080/api/v1/bootstrap`

Run tests:

```bash
cd backend
./mvnw test
```

### Selenium (Task 2 browser automation)

Requires Docker (Testcontainers PostgreSQL), Node.js deps in `frontend/`, and Google Chrome.

```bash
cd frontend
npm install

cd ../backend
./mvnw verify -Pselenium
```

These tests start Spring Boot on a random port, launch Vite against that API, and drive Chrome headless through registration, login, logout, character creation, and the Task 2 conflict/redirect cases.

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
