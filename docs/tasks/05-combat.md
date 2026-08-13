TASK 5 — Encounters and PvE Combat
Goal

Implement the primary active gameplay loop.

Create:

MonsterDefinition
Encounter
CombatSession
CombatEvent
CombatEngine

Implement dangerous-location encounter search.

Implement actions:

QUICK_ATTACK
HEAVY_ATTACK
PRECISE_ATTACK
DEFEND
USE_POTION
RETREAT

Combat must survive:

browser refresh
logout
temporary disconnection

No combat action timer.

Implement:

XP
gold rewards
loot
level-up
attribute points

Reward processing must be exactly-once.

Frontend:

Create combat interface containing:

player HP
player stamina
enemy HP
combat actions
combat log
reward screen

Write extensive CombatEngine unit tests.

Add integration test proving that repeated completion requests cannot duplicate loot or XP.

At this stage a player must be able to perform the complete loop:

explore
fight
loot
equip
become stronger