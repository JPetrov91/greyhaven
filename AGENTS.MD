# Project Instructions 
This repository contains a browser-based persistent RPG. 
The authoritative product and technical specification is: 
docs/MVP_SPEC.md 
Before implementing any task: 
1. Read this file. 
2. Read docs/MVP_SPEC.md. 
3. Inspect the existing repository. 
4. Preserve the existing architecture. 
5. Implement only the explicitly requested task. 
6. Do not implement future tasks.

GLOBAL CURSOR / CODING AGENT RULES

Apply these rules to every implementation task.

Rule 1 — Inspect before changing

Before implementing a task:

inspect the existing repository;
understand existing conventions;
reuse existing architecture;
do not recreate already implemented functionality.
Rule 2 — Do not silently change architecture

Do not replace established technologies or architectural patterns without a strong technical reason.

Do not introduce:

microservices
Kafka
Redis
GraphQL
WebFlux
CQRS
event sourcing
Kubernetes

unless explicitly requested.

Rule 3 — Prefer simple production-quality solutions

Avoid both:

hacky prototype code

and:

enterprise architecture astronautics

The target is a clean, maintainable modular monolith.

Rule 4 — Backend owns game rules

Never trust calculations supplied by the client.

Client requests describe intent only.

Rule 5 — Database integrity matters

Use:

transactions
constraints
unique indexes
foreign keys
locking where necessary

to preserve game economy integrity.

Never depend exclusively on frontend validation.

Rule 6 — No placeholder implementations

Do not leave:

TODO
mock implementation
return null
temporary fake service

for functionality required by the current task.

Rule 7 — Do not implement future tasks prematurely

If Task 4 is being implemented, do not decide to also build Task 7 because it might be useful.

Keep scope controlled.

Rule 8 — Keep domain logic testable

Prefer pure Java game logic where possible.

Example:

CombatEngine
LootGenerator
ExpeditionResolver
CharacterStatCalculator

should not depend unnecessarily on:

HTTP
Spring MVC
JPA EntityManager
frontend DTOs
Rule 9 — Time must be injectable

Use:

java.time.Clock

for game mechanics involving real-world time.

This is especially important for:

expeditions
cooldowns
future market expiration
future timed events
Rule 10 — Randomness must be injectable

Combat, loot and expedition randomness should be abstracted behind a controllable random provider.

Tests must be deterministic.

Do not make important unit tests dependent on Math.random() or uncontrolled randomness.

Rule 11 — API contracts

Use explicit request/response DTOs.

Never expose persistence entities as JSON API models.

Validate all incoming requests.

Rule 12 — Migrations

Every database schema change requires a new Flyway migration.

Never modify an already-applied migration during normal development.

Rule 13 — Tests are part of implementation

A task is not complete merely because the application compiles.

Implement appropriate:

unit tests
integration tests

for meaningful business rules.

Rule 14 — Finish every task with verification

Before declaring a task complete:

compile backend
run backend tests
build frontend
check migrations
check application startup

Report failures honestly.


# Phase 3 Rules

Phase 3 is defined by:

- docs/PHASE_3_SPEC.md
- docs/PHASE_3_EXECUTION.md
- docs/tasks/phase-3/

Phase 3 theme:

Game Experience & Productization.

Phase 1 and Phase 2 created the technical and gameplay systems.
Phase 3 must turn those systems into a coherent playable game experience.

Primary Phase 3 priorities:

1. Establish a clean technical baseline.
2. Design and implement the complete Level 1–10 player journey.
3. Introduce the Quest/NPC framework.
4. Refine combat, PvE and Arena rather than replacing them.
5. Bring UI/UX close to release quality.
6. Introduce the basic Clan and social foundation.
7. Improve onboarding, return experience, diagnostics and production quality.

The complete Level 1–10 journey is the main gameplay vertical slice of Phase 3.

Do not introduce major Phase 4+ systems.

Explicitly out of Phase 3:

- clan wars
- territory control
- alliances
- diplomacy
- large-scale clan projects
- regional political economy
- large-scale world wars
- major global world-event framework
- large collaborative raids
- housing
- pets
- mounts
- second continent
- prestige
- seasonal reset architecture

Future systems may be considered architecturally, but must not be implemented unless explicitly required by the current task.

## Implementation Philosophy

Prefer improving and completing existing systems over introducing new parallel systems.

Do not create:

- Combat 3.0
- Inventory 3.0
- Market 3.0
- Character 3.0

unless a fundamental architectural problem requires it.

Phase 3 is primarily refinement, integration and productization.

Server remains authoritative for gameplay.

Frontend sends player intent.

Use existing domain models wherever possible.

Do not duplicate:

- item models
- character models
- combat engines
- market logic
- progression logic

for new Phase 3 systems.

Maintain:

- deterministic tests
- injectable Clock
- injectable RNG
- transactional economy operations
- exact-once rewards
- refresh-safe gameplay
- interruption-safe gameplay
- asynchronous-first design

## Cursor Workflow

Every Phase 3 task must follow:

PLAN
→ Human Review
→ IMPLEMENT
→ AI REVIEW
→ FIX
→ Manual Verification
→ Git Commit

Do not combine multiple major Phase 3 tasks into one Cursor Agent run.

A new Cursor Chat is recommended for every major task.

Do not begin the next task before the current task is stable.