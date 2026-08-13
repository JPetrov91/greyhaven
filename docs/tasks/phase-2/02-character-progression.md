TASK 2 — Character Progression 2.0
Goal

Implement the complete Phase 2 character progression foundation.

Implement:

level 1–30
XP table
attribute points
derived stats
attribute requirements support
respec

Create dedicated progression domain services.

Possible components:

ExperienceCurve
CharacterProgressionService
CharacterStatCalculator
AttributeAllocationService
RespecService

Update Character API.

Update character frontend.

Character UI must display:

level
XP / XP required
available attribute points
primary attributes
derived stats

Implement level-up notifications/activity.

Tests

At minimum:

single level-up
multiple level-ups from one XP reward
level 30 cap
attribute point awarding
invalid allocation
respec
insufficient respec gold
derived stat calculation
legacy character progression

Do not implement item affixes or mastery yet.