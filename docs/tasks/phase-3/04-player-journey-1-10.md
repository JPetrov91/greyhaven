# Task 04 — Phase 3 Game Design Foundation & Level 1–10 Player Journey

## Status

Phase 3
Task 04
GAME DESIGN GATE

This task is primarily game-design and documentation work.

Do not implement gameplay code as part of this task.

---

# 1. Purpose

Phase 1 and Phase 2 created the core technical and gameplay systems.

The project now contains systems such as:

- character progression;
- attributes;
- equipment;
- item rarity and affixes;
- weapon families;
- weapon mastery;
- combat techniques;
- Combat 2.0;
- statuses;
- locations;
- enemies;
- loot;
- expeditions;
- Market;
- NPC merchants;
- crafting;
- professions;
- Arena;
- asynchronous PvP;
- dungeons or dungeon infrastructure.

However, the existence of these systems does not automatically create a coherent game.

Phase 3 must define the actual player experience.

The purpose of Task 04 is to design the complete player journey from:

Character Creation

through:

Level 1

to:

Level 10.

The result must answer:

> What does the player actually do at every stage of the first 10 levels, what does the player learn, what choices become available, what content becomes relevant, and why does the player want to continue?

---

# 2. Primary Deliverable

Create:

docs/game-design/LEVEL_1_10_PLAYER_JOURNEY.md

This document becomes an authoritative Phase 3 game-design source for Tasks 05–11.

Later implementation tasks must follow the approved journey unless explicitly changed through a documented game-design decision.

Additional locked design in the same folder (index: `docs/game-design/README.md`): starting-experience BR, first-quest BR, weapon/starter kit, Locations/Talk chrome.

---

# 3. Core Design Principle

Levels 1–10 are not merely low-stat versions of Level 30 gameplay.

They are:

- onboarding;
- first progression arc;
- first build discovery;
- first exploration arc;
- first economic loop;
- first combat learning curve;
- first meaningful RPG milestone.

Complexity must be introduced intentionally.

Do not expose every gameplay system at Level 1.

The player must gradually learn the game through play.

---

# 4. Phase 3 Vertical Slice

The Phase 3 primary vertical slice is:

Character Creation
→ Level 1
→ Level 2
→ Level 3
→ Level 4
→ Level 5
→ Level 6
→ Level 7
→ Level 8
→ Level 9
→ Level 10
→ first major milestone / dungeon climax

The journey should feel like one coherent introductory RPG arc.

---

# 5. Required Analysis Before Designing

Inspect the actual repository and current Phase 1/2 specifications.

Determine what already exists for:

- character starting state;
- starting equipment;
- starting Gold;
- XP curve;
- attribute points;
- health/stamina;
- passive recovery;
- weapon mastery;
- combat techniques;
- equipment requirements;
- item rarity;
- existing enemies;
- existing locations;
- existing expeditions;
- existing crafting;
- existing professions;
- existing Market;
- NPC merchants;
- Arena;
- dungeon systems;
- activity feed;
- existing UI.

Do not design a fictional game that ignores the implementation.

When an existing implementation conflicts with the desired journey, explicitly identify the conflict.

Do not silently invent a replacement.

---

# 6. Level-by-Level Design

For each level from 1 through 10 define at minimum:

## Player State

- approximate total playtime;
- expected equipment quality;
- expected weapon choice;
- approximate primary attributes;
- expected weapon mastery;
- expected active techniques;
- expected Gold;
- expected consumables;
- relevant unlocked systems.

## Player Goal

What is the immediate understandable objective?

Examples:

- learn basic combat;
- buy first real weapon;
- reach North Road;
- investigate missing caravan;
- defeat first elite;
- craft first item;
- enter first dungeon.

Avoid goals like:

> gain XP until Level 4.

XP is progression feedback, not narrative/gameplay motivation.

## New Concept Introduced

Examples:

- equipment;
- attribute allocation;
- stamina;
- defend;
- weapon identity;
- technique;
- status effect;
- armor;
- accuracy/dodge;
- expedition;
- crafting;
- Arena;
- elite enemy;
- dungeon.

Only introduce concepts when the player has enough context to understand them.

## Locations

Which locations should be relevant at this stage?

Do not send players through every available location simply because it exists.

## Enemies

Which enemy archetypes should appear?

What gameplay lesson does each enemy teach?

## Equipment Progression

What meaningful equipment upgrades should exist?

Do not define only numerical upgrades.

Consider:

- weapon-family choice;
- armor category;
- shield/off-hand decisions;
- requirements;
- rarity introduction.

## Quest/Narrative Direction

Define what narrative or objective carries the player forward.

Exact quest implementation happens later.

Task 04 must define the required progression beats.

## Feature Unlocks

Specify when major systems become player-facing.

Candidates include:

- attributes;
- full Inventory;
- merchants;
- Player Market;
- expeditions;
- weapon mastery;
- combat techniques;
- crafting;
- professions;
- Arena Training Grounds;
- PvP Arena;
- dungeon.

Do not assume every existing system needs to unlock before Level 10.

---

# 7. Required Progression Milestones

Design several major milestones.

At minimum evaluate:

## First Combat

The first battle should teach the minimum viable combat interaction.

The player should not need to understand:

- five statuses;
- four techniques;
- mastery;
- armor penetration;
- complex item affixes;

before winning the first fight.

---

## First Meaningful Equipment Choice

At some early point the player should encounter a real choice between weapon styles.

Example conceptual choice:

Sword
Axe
Mace
Dagger
Bow

Do not permanently lock the player into a class.

The choice should teach weapon identity.

---

## First Technique

The first technique should feel like a meaningful new combat option rather than another button that deals slightly more damage.

---

## First Enemy Counter

The player should encounter an enemy that makes a previously learned mechanic important.

Example:

High Dodge enemy
→ accuracy matters.

Heavy Armor enemy
→ armor interaction matters.

Telegraphed heavy attack
→ defense/control matters.

---

## First Elite

The first elite should be mechanically different from normal enemies.

---

## First Economy Decision

Examples:

- buy guaranteed merchant upgrade;
- keep Gold;
- buy potion;
- buy Player Market item.

---

## First Crafting Interaction

If crafting belongs inside Level 1–10, define why and when.

Do not expose crafting just because the system exists.

---

## First Arena Interaction

Training Grounds may be introduced before real ranked PvP.

Determine appropriate timing.

---

## Level 10 Milestone

Level 10 should feel like the end of the first meaningful RPG chapter.

Prefer a dungeon / boss / important quest-chain conclusion.

It should test mechanics introduced during Levels 1–9.

Level 10 should not simply be:

XP bar filled
→ Level 10.

---

# 8. Complexity Curve

Create an explicit complexity curve.

Example format:

| Level | New Complexity |
|------|----------------|
| 1 | Basic attack, HP, stamina |
| 2 | Equipment choice |
| 3 | Attributes |
| 4 | First technique |
| ... | ... |

Do not use this example blindly.

Derive the actual curve.

The curve must avoid both extremes:

## Too fast

Player sees 12 systems immediately.

## Too slow

First several hours contain only Basic Attack.

---

# 9. Player Freedom

The player journey should provide guidance without becoming a rigid linear campaign.

Distinguish:

## Recommended Path

The journey the game naturally presents.

## Optional Activities

Examples:

- extra PvE;
- Market;
- Arena practice;
- crafting;
- expeditions.

The player should be able to deviate without breaking progression.

---

# 10. Office-First Constraints

The game targets players who may:

- play for 2–5 minutes;
- play for 20–30 minutes;
- leave suddenly;
- return hours later.

Therefore the journey must not depend on:

- uninterrupted one-hour sessions;
- mandatory scheduled content;
- punishing session loss;
- long real-time commitments.

Identify natural stopping points.

Example:

quest objective completed
→ safe return state.

---

# 11. Required Journey Table

The document must contain a master table with approximately:

| Level | Target Time | Main Goal | New System | Location | Enemy Lesson | Gear State | Quest Beat | Unlock |
|---|---|---|---|---|---|---|---|---|

This table is a summary.

Detailed explanations must follow.

---

# 12. Required Full Journey Walkthrough

After the summary, describe a representative new player playthrough.

Example structure:

## Character Creation

What the player sees and chooses.

## First 10 Minutes

What happens.

## Level 2

What changes.

...

## Level 10

What the milestone means.

The walkthrough must describe the experience from the player's perspective.

Do not describe only backend systems.

---

# 13. Required Game-Design Questions

Explicitly answer:

1. When does the player make the first meaningful build choice?
2. When does weapon mastery begin to matter?
3. When does the first technique become available?
4. When do statuses become relevant?
5. When should the player first visit Market?
6. When should the player first use an NPC Merchant?
7. When should Expeditions become available?
8. When should Crafting become available?
9. When should Training Grounds become available?
10. When should real PvP become available?
11. What is the first challenging enemy?
12. What is the first elite?
13. What is the first boss?
14. What makes Level 10 memorable?
15. Which existing Phase 2 systems should remain unavailable until after Level 10?

---

# 14. Design Risks

Identify risks such as:

- feature overload;
- excessive grind;
- no reason to change equipment;
- weapon families feeling identical;
- crafting being irrelevant;
- merchant replacing loot;
- loot replacing merchant;
- Arena opening too early;
- Arena opening too late;
- quests becoming a mandatory checklist;
- too much combat repetition;
- insufficient player choice.

Provide mitigation proposals.

---

# 15. Out of Scope

Do NOT:

- implement code;
- create database migrations;
- design Levels 11–30 in detail;
- create Clan gameplay;
- create territory systems;
- create world wars;
- create large story campaign;
- create dozens of final quest texts;
- create full economic formulas.

Task 05 handles detailed economy/progression numbers.

Task 06 implements Quest infrastructure.

Task 07 creates actual Level 1–10 content.

---

# 16. Acceptance Criteria

Task 04 is complete when:

1. LEVEL_1_10_PLAYER_JOURNEY.md exists;
2. Levels 1–10 have explicit gameplay purpose;
3. system unlock order is defined;
4. enemy-mechanic learning curve is defined;
5. equipment/build learning curve is defined;
6. first major milestones are defined;
7. Level 10 has a meaningful climax;
8. player freedom vs guided path is defined;
9. office-first constraints are respected;
10. implementation conflicts are identified;
11. unresolved game-design questions are clearly listed;
12. no production code was changed.