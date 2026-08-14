# Phase 2 — Fast Improvements

## Purpose

This document contains small but important gameplay, UX and technical improvements discovered during Phase 2 development.

A Fast Improvement is not the primary feature of a Phase 2 task, but it is closely related to a system already being modified and should therefore be implemented together with that task when practical.

Fast Improvements exist to prevent obvious quality-of-life or foundational issues from being postponed merely because they were not explicitly listed in the original task specification.

---

# Rules

A Fast Improvement may be implemented as part of a task only when:

1. it directly relates to modules already being modified by the current task;
2. it does not introduce a major new gameplay system;
3. it does not require changing the Phase 2 architecture;
4. it does not introduce Phase 3 functionality;
5. it can be implemented with proper tests;
6. it does not compromise the primary task's completion.

Fast Improvements must not become uncontrolled scope expansion.

If an improvement requires a substantial new subsystem, major database redesign, or broad architectural change, it must be promoted into a dedicated task instead.

Every Fast Improvement must be assigned to a specific Phase 2 task.

---

# FI-001 — Passive Health and Stamina Recovery

**Target Task:** Phase 2 Task 2 — Character Progression 2.0

**Status:** REQUIRED

## Problem

After combat, characters currently lack a proper persistent recovery mechanic for Health and Stamina.

For an office-first browser RPG, the player should naturally recover while doing other things or while away from the game.

Recovery should be fast for new characters so that early gameplay is not interrupted unnecessarily.

At higher levels, recovery should become somewhat slower so that Health and Stamina remain meaningful resources.

The system must not require the browser to remain open.

---

## Core Requirements

Implement passive out-of-combat recovery for:

```text
Health
Stamina
```

Recovery must:

* happen in real-world time;
* continue while the player is offline;
* be calculated by the backend;
* not require scheduled per-character jobs;
* not require sleeping threads;
* not depend on the frontend countdown;
* use `java.time.Clock`;
* be safely deterministic in tests.

The implementation should follow the same general philosophy as asynchronous expeditions:

```text
stored state
+
last recovery timestamp
+
elapsed real time
=
current recovered state
```

---

## Combat Interaction

Passive recovery must NOT occur while the character is inside an active combat session.

Example:

```text
Combat ACTIVE
→ passive recovery paused
```

When combat ends:

```text
PLAYER_WON
PLAYER_LOST
PLAYER_ESCAPED
```

the recovery baseline must restart from the combat completion time.

This prevents an exploit where a player leaves an active combat open for several hours and returns fully healed.

Combat-specific stamina regeneration remains part of CombatEngine and is separate from this system.

---

## Initial Recovery Curve

Recovery should be percentage-based so increasing maximum HP does not make high-level characters take disproportionately extreme amounts of time to recover.

Initial balance values:

| Level | HP per minute | Stamina per minute |
| ----: | ------------: | -----------------: |
|   1–5 |    20% Max HP |    40% Max Stamina |
|  6–10 |           15% |                30% |
| 11–20 |           10% |                20% |
| 21–30 |          7.5% |                15% |

Approximate full recovery from zero:

| Level |   Health | Stamina |
| ----: | -------: | ------: |
|   1–5 |    5 min | 2.5 min |
|  6–10 |  6.7 min | 3.3 min |
| 11–20 |   10 min |   5 min |
| 21–30 | 13.3 min | 6.7 min |

These are initial balance values and must be configurable.

Do not hardcode them throughout domain logic.

Recommended responsibility:

```text
CharacterRecoveryBalance
```

or an equivalent existing balance component.

---

## Recovery Calculation

The exact implementation may follow existing project conventions, but conceptually:

```text
elapsedTime =
now - lastRecoveryAt
```

then:

```text
recoveredHealth =
maxHealth
* configuredHealthRecoveryPercent
* elapsedMinutes
```

and equivalent logic for Stamina.

The final values must always satisfy:

```text
currentHealth <= maxHealth
currentStamina <= maxStamina
```

Recovery must never produce negative values.

---

## Persistence

Character recovery requires enough persistent state to calculate elapsed recovery safely.

Recommended concept:

```text
lastRecoveryAt
```

Do not continuously update every character in the database through background polling.

Recovery should be calculated lazily when relevant character state is accessed or mutated.

Examples:

```text
GET character
start encounter
start combat
perform character action
login / character load
```

The implementation must avoid repeated-request exploits by updating the recovery baseline correctly.

---

## Future Extensibility

The design should make future modifiers possible without implementing them now.

Examples for later phases:

```text
Tavern recovery bonus
camp/rest bonus
food
injuries
buffs
special locations
```

Do NOT implement these modifiers as part of FI-001.

Only ensure the recovery service is not designed in a way that makes them impossible later.

---

## Suggested Domain Components

Possible implementation:

```text
CharacterRecoveryService
CharacterRecoveryBalance
```

The recovery calculation itself should be independently unit-testable.

---

## API / Frontend

Character responses should provide current server-authoritative:

```text
currentHealth
maxHealth
currentStamina
maxStamina
```

The frontend may show recovery progress.

The frontend must not authoritatively increment Health or Stamina.

A visual client-side countdown/interpolation is allowed only as presentation.

Whenever authoritative state is required, the backend value wins.

---

## Acceptance Criteria

The improvement is complete when:

1. an injured character recovers HP over real time;
2. spent Stamina recovers over real time;
3. recovery continues while logged out;
4. early-level characters recover faster than high-level characters;
5. values never exceed maximums;
6. active combat pauses passive recovery;
7. leaving combat starts a new recovery period;
8. browser refresh does not alter recovery correctness;
9. repeated API calls cannot increase recovery beyond elapsed time;
10. recovery values are configurable;
11. time behavior is tested using injected `Clock`;
12. existing Phase 1 character data migrates safely.

---

## Required Tests

At minimum:

```text
no elapsed time -> no recovery

partial minute recovery

multiple-minute recovery

health capped at max

stamina capped at max

different level recovery rates

offline elapsed-time recovery

active combat prevents recovery

combat completion resets recovery baseline

repeated state retrieval does not duplicate recovery

Clock-controlled deterministic test
```

---

# FI-002 — Experience Progress Visibility

**Target Task:** Phase 2 Task 2 — Character Progression 2.0

**Status:** REQUIRED

## Problem

The player must immediately understand how close the character is to the next level.

Displaying only total XP or only the current level provides poor progression feedback.

Character progression is a central Phase 2 mechanic and therefore XP progress must always be clearly visible.

---

## Core Requirement

Display both:

```text
current XP progress toward next level
```

and:

```text
remaining XP until next level
```

Recommended presentation:

```text
Level 11

XP
1,240 / 2,000

████████████░░░░░░░░ 62%

760 XP until Level 12
```

The UI does not need to show all three forms everywhere.

However the primary Character/Progression screen should provide:

```text
current / required XP
+
visual progress bar
```

and may additionally show:

```text
XP remaining
```

---

## Backend Progression Response

The backend progression representation should make the distinction between total lifetime XP and XP within the current level explicit.

Recommended response fields:

```text
level
totalExperience
experienceIntoCurrentLevel
experienceRequiredForNextLevel
experienceRemaining
progressPercent
```

Example:

```json
{
  "level": 11,
  "totalExperience": 8470,
  "experienceIntoCurrentLevel": 1240,
  "experienceRequiredForNextLevel": 2000,
  "experienceRemaining": 760,
  "progressPercent": 62.0
}
```

Exact DTO naming may follow existing project conventions.

Avoid ambiguous fields such as:

```text
xp
requiredXp
```

when their meaning is not obvious.

---

## Level Cap

At Level 30:

do not display misleading:

```text
184830 / null
```

or:

```text
0 XP remaining
```

Instead display an explicit state:

```text
Level 30 — MAX
```

and either:

* hide next-level progress;
* or show a completed progress bar clearly marked `MAX LEVEL`.

---

## Server Authority

The XP curve is server-authoritative.

Frontend must not contain a separate copy of the XP table.

The frontend should receive everything required to render progression from the backend.

This prevents the frontend and backend XP curves from diverging after future balance changes.

---

## UI Locations

At minimum display compact XP progression in:

### Character Panel

Example:

```text
Lv. 11
XP 1,240 / 2,000
████████████░░░
```

### Full Character / Progression Screen

More detailed:

```text
Level 11

1,240 / 2,000 XP
760 XP until Level 12
62%
```

Optional future locations:

```text
combat reward screen
activity feed
level-up notification
```

Do not duplicate excessive XP information throughout every screen.

---

## Level-Up UX

When XP causes a level-up, the interface should make the result clear.

Example:

```text
LEVEL UP

Level 11 → Level 12

+2 Attribute Points
```

If one reward produces multiple levels, the result should remain correct.

Example:

```text
Level 3 → Level 5

+4 Attribute Points
```

Do not assume every XP reward produces at most one level.

---

## Acceptance Criteria

The improvement is complete when:

1. current XP progress is visible;
2. XP required for the next level is visible;
3. XP remaining can be displayed;
4. progression bar uses server-provided progression data;
5. XP table is not duplicated in frontend code;
6. multiple level-ups produce correct UI;
7. Level 30 has a clear MAX LEVEL state;
8. browser refresh preserves correct progression display.

---

## Required Tests

Backend:

```text
experienceIntoCurrentLevel calculation

experienceRequiredForNextLevel calculation

experienceRemaining calculation

progress percentage

multiple-level progression

level 30 max-level representation
```

Frontend:

```text
normal XP progress rendering

near-level-up rendering

MAX LEVEL rendering
```
