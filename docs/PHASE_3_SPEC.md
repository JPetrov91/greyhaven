# Phase 3 — Game Experience & Productization

## 1. Purpose

Phase 3 transforms the existing RPG systems into a coherent, near-release-quality game experience.

Phase 1 proved that the browser RPG architecture works.

Phase 2 introduced RPG depth:

- character progression;
- itemization;
- weapon mastery;
- combat techniques;
- Combat 2.0;
- expanded PvE;
- Arena;
- crafting;
- economy;
- advanced Market.

Phase 3 asks a different question:

> Can a completely new player create a character, play from Level 1 to Level 10, understand the game, make meaningful build decisions, interact with the world and other players, and feel that they are playing a real near-release MMORPG rather than testing disconnected systems?

The primary Phase 3 vertical slice is:

Level 1 → Level 10.

---

# 2. Phase 3 Pillars

Phase 3 consists of six major pillars.

## 2.1 Technical Foundation

Before new Phase 3 gameplay work:

- consolidate Flyway migrations;
- establish a clean database baseline;
- audit architecture;
- fix important architectural debt;
- audit test coverage;
- create missing safety-net tests.

---

## 2.2 Level 1–10 Game Design

Design the actual player experience.

Define:

- expected time per level;
- XP progression;
- Gold progression;
- feature unlock order;
- equipment acquisition;
- merchant progression;
- loot progression;
- weapon mastery progression;
- technique unlock progression;
- enemy progression;
- quest progression;
- dungeon progression;
- Arena introduction;
- crafting introduction;
- expedition introduction.

The game should no longer merely have a leveling system.

It must have a designed progression journey.

---

## 2.3 Gameplay Integration

Phase 3 introduces a Quest/NPC framework and uses it to connect existing systems.

Quest content should integrate:

- locations;
- enemies;
- loot;
- dungeons;
- merchants;
- crafting;
- expeditions;
- Arena;
- character progression.

Phase 3 should refine Combat 2.0 rather than replace it.

---

## 2.4 Release-Quality UX

The existing game UI should be brought close to release quality.

This includes:

- unified design system;
- main shell;
- Character;
- Inventory;
- Equipment;
- World;
- Combat;
- Arena;
- Market;
- Crafting;
- Expeditions;
- Dungeon;
- Quests;
- Activity;
- Chat;
- Clan;
- Player Profile.

Normal Mode and Office Mode are both first-class interfaces.

---

## 2.5 Social Foundation

Introduce the first real social MMORPG structure.

Phase 3 includes:

- clans;
- clan membership;
- clan applications;
- invites;
- roles;
- permissions;
- clan identity;
- clan profile;
- clan members;
- clan chat;
- clan activity;
- clan tags;
- player profile / inspection.

Phase 3 does NOT include:

- clan wars;
- territory control;
- alliances;
- diplomacy;
- clan politics;
- large clan progression systems.

---

## 2.6 Product Quality

Phase 3 should improve:

- onboarding;
- contextual guidance;
- activity feed;
- notifications;
- return-to-game experience;
- errors;
- loading;
- reconnect behavior;
- persistence;
- telemetry;
- balancing tools;
- diagnostics;
- performance;
- moderation foundations;
- production readiness.

---

# 3. Phase 3 Primary Success Criterion

At the end of Phase 3:

A new player should be able to:

Create Character
→ Understand Greyhaven
→ Learn basic combat
→ Obtain equipment
→ Make first build decisions
→ Use the Market
→ Complete quests
→ Unlock additional systems
→ Fight increasingly complex enemies
→ Use techniques and statuses
→ Enter Training Grounds / Arena
→ Complete first meaningful dungeon
→ Reach Level 10

without developer assistance.

The complete experience should feel intentional and coherent.

---

# 4. Level 1–10 Design Philosophy

Levels 1–10 are not merely an early version of Level 30 gameplay.

They are the game's teaching and retention layer.

Complexity must be introduced progressively.

Conceptually:

Levels 1–2:
basic movement through locations,
equipment,
basic combat.

Levels 2–4:
attributes,
weapon identity,
first techniques.

Levels 4–6:
status effects,
enemy archetypes,
expeditions,
Market depth.

Levels 6–8:
build decisions,
crafting,
elite encounters,
advanced quests.

Levels 8–10:
Arena,
complex enemies,
dungeon,
boss,
first complete build milestone.

Exact unlocks must be finalized in the game-design tasks.

---

# 5. Combat Philosophy

Do not build Combat 3.0.

Phase 3 must validate and refine Combat 2.0.

Focus on:

- pacing;
- meaningful action choice;
- stamina economy;
- enemy readability;
- enemy archetypes;
- status usefulness;
- weapon identity;
- technique usefulness;
- consumables;
- retreat;
- failure feedback;
- result presentation;
- boss encounter design.

Combat depth must come from meaningful decisions, not additional system count.

---

# 6. Training Grounds

Arena receives a Training Grounds mode.

**Shipped as:** a separate SAFE location, Sparring Yard (`SPARRING_YARD`), not an Arena tab. Record: `docs/tasks/phase-3/sparring-yard.md`. Mockup: `docs/mockups/sparring-yard.png`.

Training Grounds contains generated NPC opponents. The yard also hosts unranked live duels for levels 1–10. Ranked Arena stays in the Arena building.

Supported levels:

1–10.

Bots must:

- use normal Character/Combat systems;
- use the same Combat engine;
- use constrained random generation;
- use level-based power budgets;
- use build archetypes;
- use deterministic RNG / seeds;
- create immutable combat snapshots.

Bots must never pretend to be human players.

Training Grounds is primarily:

- practice;
- build testing;
- progression validation;
- balance testing.

It must not become the most efficient XP/Gold farming method.

---

# 7. Quest Framework

Phase 3 introduces quests as a reusable progression framework.

Quest objectives must support multiple objective types.

Initial types may include:

- KILL
- COLLECT
- VISIT_LOCATION
- DEFEAT_ENEMY
- COMPLETE_DUNGEON
- CRAFT_ITEM
- ACQUIRE_ITEM
- TALK_TO_NPC
- COMPLETE_EXPEDITION
- WIN_ARENA_MATCH

Do not create a Quest system tied exclusively to kill counters.

Quest rewards may include:

- XP
- Gold
- items
- resources
- unlocks

Quest completion and rewards must be server-authoritative and exact-once.

---

# 8. NPC Interaction

NPCs may support contextual actions such as:

- Talk
- Shop
- Quest
- Turn In

Phase 3 dialogue should remain intentionally simple.

Do not build a complex CRPG dialogue engine.

---

# 9. UI Philosophy

Phase 3 adopts the approved Veridia visual direction.

The interface should feel like:

dark fantasy MMORPG
+
modern desktop game
+
high information density.

Avoid:

- generic SaaS appearance;
- mobile F2P presentation;
- excessive decorative borders;
- unreadable low-contrast text;
- oversized card-based layouts.

Use atmospheric art selectively.

Functional information must remain easy to scan.

---

# 10. Office Mode

Office Mode becomes production-quality.

It must:

- preserve gameplay functionality;
- reduce art;
- reduce animation;
- reduce spacing;
- increase information density;
- remain readable in narrow browser windows;
- use the same information architecture as Normal Mode.

Office Mode must not disguise the game as work software.

---

# 11. Explicit Phase 3 Exclusions

Do NOT implement:

- clan wars;
- territorial ownership;
- political systems;
- alliances;
- diplomacy;
- regional taxes;
- world-scale economy;
- large server wars;
- complex world events;
- large raids;
- housing;
- pets;
- mounts;
- Level 30 full content design;
- prestige;
- seasons;
- battle pass;
- monetization systems.

---

# 12. Technical Principles

Continue using the existing modular monolith.

Do not introduce microservices.

Do not introduce Kafka.

Do not introduce CQRS/event sourcing.

Do not introduce a second combat engine.

Do not introduce a second item model.

Use:

- Spring transactions;
- DB constraints;
- deterministic RNG;
- Clock;
- Testcontainers;
- server-authoritative state;
- transactional settlement;
- explicit module boundaries.

---

# 13. Phase 3 Completion

Phase 3 is complete only when:

- clean database baseline exists;
- architecture audit has no unresolved critical findings;
- critical gameplay paths have test safety nets;
- Level 1–10 journey is documented;
- Level 1–10 content exists;
- quests work;
- combat has been refined;
- Training Grounds works;
- early dungeon works;
- Level 1–10 balance has been validated;
- release-quality UI foundation exists;
- Office Mode is production-quality;
- core existing features have been productized;
- clan foundation exists;
- player social identity exists;
- onboarding works;
- return experience works;
- telemetry and diagnostics exist;
- full Level 1–10 flow audit passes;
- final Phase 3 integration audit passes.