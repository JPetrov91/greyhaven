# Task 06 — Quest & NPC Interaction Framework

## Purpose

Introduce a reusable Quest and basic NPC Interaction framework capable of supporting the approved Level 1–10 journey.

The framework must integrate existing gameplay systems rather than duplicating them.

---

# 1. Goals

Implement:

- Quest definitions;
- player quest state;
- quest prerequisites;
- quest chains;
- multiple objectives;
- objective progress;
- rewards;
- exact-once completion;
- NPC interaction;
- Quest Log;
- quest tracking;
- basic quest-related UI;
- integration events/hooks with existing gameplay.

---

# 2. Architecture Principle

Do NOT create a Quest system that understands only:

monsterId + requiredKillCount.

The architecture must support multiple objective types.

At the same time:

Do NOT build a generic enterprise rules engine.

Use explicit, understandable game-domain abstractions.

---

# 3. Initial Objective Types

Support at minimum the types actually needed by approved Level 1–10 content.

The framework should be able to support:

KILL
COLLECT
VISIT_LOCATION
DEFEAT_ENEMY
COMPLETE_DUNGEON
CRAFT_ITEM
ACQUIRE_ITEM
TALK_TO_NPC
COMPLETE_EXPEDITION
WIN_ARENA_MATCH

It is acceptable to implement only the subset immediately used by Task 07 if:

- extension mechanism is clear;
- unsupported types are documented.

However, avoid architecture tied to one objective type.

---

# 4. Quest Definition

Conceptually a QuestDefinition contains:

id
code
name
description
category/type
level recommendation
prerequisites
objectives
rewards
nextQuest / chain metadata
starting NPC/location
turn-in NPC/location
repeatability

Exact model must follow repository conventions.

---

# 5. Quest State

Player quest state should distinguish at minimum:

AVAILABLE
ACTIVE
COMPLETED

If useful:

READY_TO_TURN_IN
FAILED

Do not add states without gameplay need.

---

# 6. Quest Objective

An objective should contain:

type
target reference
required amount
optional display text
ordering if relevant

Player progress must be persisted safely.

---

# 7. Event Integration

Quest progress should react to authoritative server gameplay events/state changes.

Examples:

Combat victory
→ KILL / DEFEAT_ENEMY progress.

Item acquisition
→ ACQUIRE_ITEM / COLLECT progress.

Travel
→ VISIT_LOCATION.

Craft completion
→ CRAFT_ITEM.

Expedition completion
→ COMPLETE_EXPEDITION.

Do not have frontend increment quest progress.

---

# 8. Idempotency

Quest progress must not double-count because of:

- repeated HTTP request;
- refresh;
- event replay;
- repeated reward claim;
- transaction retry.

Quest rewards must be granted exactly once.

---

# 9. Collect Objectives

Explicitly determine whether COLLECT means:

A. possess N items;
or
B. acquire N quest-specific progress units.

Do not ambiguously mix both semantics.

If items must be consumed on turn-in, do so transactionally.

---

# 10. Quest Rewards

Initial reward support:

XP
Gold
Item
Resource
Unlock

Rewards should reuse existing progression/inventory systems.

Do not directly mutate unrelated fields in Quest controllers.

---

# 11. Unlock Reward

Support an extensible unlock concept where required by approved journey.

Examples:

location
expedition
system access
merchant stock

Do not build a universal entitlement platform.

Only implement concrete unlocks actually needed.

---

# 12. NPC Definitions

Implement basic NPC definition support.

NPC may contain:

id
code
name
title
description
portrait/image
location
available interactions

NPC actions may include:

TALK
SHOP
QUEST
QUEST_TURN_IN

Reuse Merchant NPC representation where practical.

Do not create incompatible duplicate NPC identities if Merchant system already has NPC metadata.

---

# 13. Dialogue

Keep dialogue intentionally simple.

Support enough to show:

NPC name
portrait
text
limited response/action buttons

Do NOT implement:

branching CRPG dialogue trees
skill checks
relationship system
cinematics
voice
complex narrative scripting

---

# 14. Quest Log

Frontend needs:

AVAILABLE where appropriate
ACTIVE
COMPLETED

Primary UX:

quest name
short objective
progress
recommended level
location/context
reward preview where appropriate

Allow tracking a small number of quests.

Recommended initial maximum:

1–3 tracked quests.

Do not create a 20-item permanent HUD tracker.

---

# 15. Quest Tracking

Tracked quest should surface concise information in main UI.

Example:

Shadows Over Greyhaven

Reach the Old Mine

3/5 Bandits Defeated

The tracker must not duplicate full Quest Log.

---

# 16. Quest Completion UX

Completion should clearly communicate:

Quest Complete
XP
Gold
Items
Unlocks
next available quest

Do not require excessive modal confirmations.

---

# 17. APIs

Use /api/v1.

Potential shape:

GET /api/v1/quests
GET /api/v1/quests/{id}
POST /api/v1/quests/{id}/accept
POST /api/v1/quests/{id}/turn-in
POST /api/v1/quests/{id}/track
DELETE /api/v1/quests/{id}/track

NPC:

GET /api/v1/world/npcs/{id}
POST /api/v1/world/npcs/{id}/interactions/...

Exact API follows current conventions.

---

# 18. Persistence

Use Flyway migrations from the new Phase 3 baseline.

Possible tables:

quest_definition
quest_objective_definition
quest_reward_definition
character_quest
character_quest_objective

But do not force DB representation before inspecting existing definition-storage approach.

Static QuestDefinition data may belong in config/seed system instead of fully normalized tables if consistent with project conventions.

---

# 19. Transactions

Quest accept, progress where necessary, turn-in and rewards must have appropriate transaction boundaries.

Particularly:

turn-in
→ validate
→ consume required items if any
→ XP
→ Gold
→ Item rewards
→ unlocks
→ mark completed

must not partially commit.

---

# 20. Tests

Unit:

objective evaluation
prerequisites
progress
completion
reward calculation/dispatch
tracking rules

Integration:

accept quest
combat updates progress
item acquisition updates progress
visit location updates progress
turn-in
exact-once reward
refresh/retry
multi-objective quest
quest chain prerequisite
failed transaction rollback

Use Testcontainers for database-critical flows.

---

# 21. Out of Scope

No:

daily quest ecosystem
procedural quests
complex branching dialogue
faction reputation
global quest events
clan quests
world-event quests
quest editor UI
script language

---

# 22. Acceptance Criteria

Quest framework works through at least one complete representative integration flow:

NPC
→ accept quest
→ perform gameplay action
→ objective updates
→ quest completes
→ return/turn-in if required
→ receive exact-once reward
→ next quest unlocks.

Refresh must not break state.