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