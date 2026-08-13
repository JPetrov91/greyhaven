TASK 6 — Combat 2.0 and Status Effects
Goal

Upgrade Phase 1 combat to support real build diversity.

Implement generic status engine.

Initial statuses:

BLEED
POISON
STUN
ARMOR_BREAK
OFF_BALANCE
GUARDED

Extend CombatEngine to account for:

attributes
equipment stats
weapon family
mastery
techniques
statuses
stamina
accuracy
dodge
critical
armor

Implement diminishing-return armor logic.

Implement status stacking and expiration.

Implement anti-stun-lock behavior.

Improve enemy AI architecture.

Migrate current Phase 1 combat sessions safely where practical.

Active legacy combats may alternatively finish using legacy rules if migration complexity would compromise integrity; this choice must be documented.

Frontend:

Redesign combat screen using Task 4 components.

Display:

statuses
techniques
stamina cost
combat log
enemy identity
Tests

CombatEngine requires extensive deterministic unit testing.

Cover:

hit
miss
critical
dodge
armor
bleed
poison
stun
armor break
status expiration
stamina exhaustion
technique interaction
player victory
player defeat
retreat
reward exactly-once

At the end of this task, perform a manual 30–60 minute gameplay review before moving forward.