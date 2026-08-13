# Browser MMORPG — Phase 2 Master Technical & Game Design Specification

## 1. Phase 2 Purpose

Phase 1 validated the fundamental browser-RPG concept:

* persistent character;
* world locations;
* PvE encounters;
* basic combat;
* loot;
* inventory;
* equipment;
* asynchronous expeditions;
* marketplace;
* activity feed;
* chat;
* office-friendly interaction model.

Phase 2 must transform this technical MVP into a substantially deeper RPG.

The primary product question for Phase 2 is:

> Is developing, optimizing and playing one character interesting for dozens of hours?

The secondary question is:

> Once meaningful character builds exist, is it interesting to use them against both PvE content and other players?

Phase 2 therefore focuses on:

```text
Character Progression
        ↓
Itemization
        ↓
Presentation / UX
        ↓
Build System
        ↓
Combat Depth
       /      \
     PvE      PvP
       \      /
       Economy
          ↓
Balance / Telemetry
```

---

# 2. Phase 2 Product Goal

At the end of Phase 2 a player should be able to:

* progress from level 1 toward level 30;
* specialize character attributes;
* equip a complete equipment set;
* compare meaningful item upgrades;
* discover items with different rarities and modifiers;
* specialize in weapon families;
* unlock combat techniques;
* create different viable character builds;
* encounter enemies requiring different tactical approaches;
* participate in asynchronous competitive PvP;
* challenge another online player to an optional duel;
* collect resources;
* craft useful items;
* participate in a functioning player-driven marketplace;
* leave the game and safely return later;
* understand why their build succeeds or fails;
* experiment with character setups without starting a new account.

Phase 2 should make the following conversation between players possible:

> "I switched from heavy armor to a dodge build, replaced my axe with a dagger, changed two techniques and now my Arena win rate against slow STR builds is much better."

If such conversations cannot meaningfully happen, the build system is not deep enough.

---

# 3. Product Principles

All Phase 1 principles remain valid.

Phase 2 adds the following.

## 3.1 Depth without unnecessary complexity

The game should support meaningful builds without overwhelming players with dozens of meaningless statistics.

Prefer:

```text
Damage
Armor
Accuracy
Dodge
Critical Chance
Block
Bleed
```

over:

```text
37 different secondary attributes
```

Every visible stat should affect a player decision.

---

## 3.2 Build choices must have opportunity costs

A build must not be able to maximize everything.

Investment in:

```text
Strength
```

should mean less investment in:

```text
Agility
Perception
Endurance
```

Heavy armor should provide protection but sacrifice mobility.

High-damage weapons should have weaknesses.

Character specialization should create strengths and weaknesses.

---

## 3.3 No mandatory class system

Do not introduce:

```text
Warrior
Rogue
Hunter
Tank
```

as permanent classes.

The player's effective archetype should emerge from:

```text
attributes
+
equipment
+
weapon family
+
weapon mastery
+
combat techniques
```

---

## 3.4 Avoid irreversible beginner traps

Build choices should matter, but early mistakes must not permanently ruin a character.

Phase 2 must introduce controlled attribute respec.

The game should encourage experimentation.

---

## 3.5 Office-first remains mandatory

No Phase 2 feature may require constant attention.

This includes PvP.

Ranked competitive gameplay should primarily use asynchronous systems.

Live interaction is optional.

---

# 4. Phase 2 Scope

## Included

Phase 2 includes:

* level progression 1–30;
* explicit XP curve;
* attribute allocation;
* attribute respec;
* derived combat statistics;
* expanded equipment slots;
* weapon families;
* armor categories;
* item rarity;
* randomized item modifiers;
* item comparison;
* equipment requirements;
* weapon mastery;
* combat techniques;
* active technique loadouts;
* status effects;
* Combat 2.0;
* expanded PvE enemy archetypes;
* additional Greyhaven locations;
* elite enemies;
* mini-bosses;
* one dungeon;
* asynchronous Arena PvP;
* optional live/casual duels;
* PvP rating and battle history;
* basic crafting;
* three professions;
* resource processing;
* item salvage;
* expanded marketplace;
* buy orders;
* economy sinks;
* UX/design system;
* improved Office Mode;
* telemetry;
* balance tooling.

---

# 5. Explicit Phase 2 Exclusions

Do NOT implement:

* clans;
* guilds;
* clan chat;
* clan treasury;
* clan projects;
* clan wars;
* territory control;
* diplomacy;
* alliances;
* parties;
* raids;
* world bosses;
* server-wide political systems;
* player governments;
* regional taxation;
* player-owned settlements;
* housing;
* large-scale collaborative PvE;
* social contracts between clans;
* territory-based regional economy;
* large world map expansion;
* second major region;
* multiple player characters;
* pets;
* mounts;
* legendary item tier;
* prestige levels;
* seasonal character resets;
* battle passes;
* monetization systems.

These belong to Phase 3 or later.

---

# 6. Compatibility With Phase 1

Phase 2 must extend the existing application.

It must NOT recreate the project.

Existing architecture remains:

```text
Java backend
Spring Boot
PostgreSQL
Flyway
React
TypeScript
TanStack Query
modular monolith
```

Existing Phase 1 modules should be extended rather than replaced.

---

# 7. Migration Rules

Existing MVP users must not lose progression.

Migration must preserve:

* accounts;
* characters;
* current XP;
* level;
* gold;
* inventory;
* equipment;
* expeditions;
* marketplace listings where possible;
* activity history.

Existing:

```text
WEAPON
```

equipment should migrate to:

```text
MAIN_HAND
```

Existing:

```text
ARMOR
```

equipment should migrate to:

```text
CHEST
```

Existing Phase 1 items must receive valid Phase 2 definitions.

Do not delete player-owned item instances merely because the item model changes.

If an old item cannot participate in the new modifier system, it may remain a valid zero-affix legacy item.

Database migration must be performed through new Flyway migrations.

Never rewrite previously applied Phase 1 migrations.

---

# 8. Character Level System

Phase 2 maximum character level:

```text
30
```

Level 30 is a temporary Phase 2 cap, not necessarily the permanent game cap.

---

# 9. Experience Curve

Progression should follow four approximate stages:

```text
Level 1–5
very fast onboarding

Level 6–10
fast early progression

Level 11–20
normal RPG progression

Level 21–30
long-term Phase 2 progression
```

Initial XP requirements:

| Current Level | XP to Next Level | Total XP Required |
| ------------: | ---------------: | ----------------: |
|             1 |              100 |                 0 |
|             2 |              180 |               100 |
|             3 |              280 |               280 |
|             4 |              400 |               560 |
|             5 |              550 |               960 |
|             6 |              720 |             1,510 |
|             7 |              900 |             2,230 |
|             8 |            1,100 |             3,130 |
|             9 |            1,350 |             4,230 |
|            10 |            1,650 |             5,580 |
|            11 |            2,000 |             7,230 |
|            12 |            2,400 |             9,230 |
|            13 |            2,850 |            11,630 |
|            14 |            3,350 |            14,480 |
|            15 |            3,900 |            17,830 |
|            16 |            4,500 |            21,730 |
|            17 |            5,200 |            26,230 |
|            18 |            6,000 |            31,430 |
|            19 |            6,900 |            37,430 |
|            20 |            7,900 |            44,330 |
|            21 |            9,000 |            52,230 |
|            22 |           10,200 |            61,230 |
|            23 |           11,500 |            71,430 |
|            24 |           12,900 |            82,930 |
|            25 |           14,400 |            95,830 |
|            26 |           16,000 |           110,230 |
|            27 |           17,700 |           126,230 |
|            28 |           19,500 |           143,930 |
|            29 |           21,400 |           163,430 |
|            30 |              MAX |           184,830 |

These numbers are initial balance values.

They must be stored as game balance configuration rather than scattered across application code.

XP progression must be easy to rebalance.

---

# 10. Attribute System

Keep the four primary attributes:

```text
STRENGTH
AGILITY
ENDURANCE
PERCEPTION
```

Initial values remain compatible with MVP:

```text
Strength     5
Agility      5
Endurance    5
Perception   5
```

Each level-up grants:

```text
2 Attribute Points
```

Maximum manually allocated value for one primary attribute during Phase 2:

```text
40
```

Item bonuses may temporarily exceed this value.

---

# 11. Attribute Identity

## Strength

Primary functions:

* physical damage;
* heavy equipment requirements;
* heavy weapon requirements;
* block effectiveness;
* some armor penetration interactions.

---

## Agility

Primary functions:

* dodge;
* stamina efficiency;
* initiative-related mechanics;
* retreat chance;
* light weapon requirements.

---

## Endurance

Primary functions:

* maximum HP;
* maximum stamina;
* survivability;
* heavy armor requirements.

---

## Perception

Primary functions:

* accuracy;
* critical chance;
* detection-related PvE checks;
* ranged weapon requirements.

---

# 12. Derived Statistics

All derived statistics are backend-authoritative.

Initial formulas may use:

```text
Max HP =
100
+ END * 12
+ Level * 5
```

```text
Max Stamina =
50
+ END * 4
+ AGI * 3
```

```text
Base Physical Power =
STR * 1.5
```

```text
Base Accuracy Rating =
75
+ PER * 1.5
```

```text
Base Dodge Rating =
AGI * 1.2
```

```text
Base Critical Chance =
5%
+ PER * 0.35%
```

Derived values are then modified by:

```text
equipment
techniques
status effects
weapon mastery
```

Exact formulas must live in dedicated balance components.

Examples:

```java
CharacterBalance
CombatBalance
ItemBalance
ProgressionBalance
PvPBalance
CraftingBalance
```

---

# 13. Attribute Respec

Phase 2 introduces attribute respec.

Goal:

Allow experimentation without making build choices meaningless.

Rules:

```text
Level 1–10:
free respec

Level 11+:
gold cost
```

Initial respec cost formula:

```text
500 + Level * 100 gold
```

This value is balance configuration.

Respec returns only manually allocated attribute points.

Base attributes remain unchanged.

Equipment bonuses are unaffected.

Respec must not be possible during active combat.

---

# 14. Equipment System 2.0

Replace the MVP two-slot system with:

```text
HEAD
CHEST
HANDS
LEGS
FEET

MAIN_HAND
OFF_HAND

AMULET
RING
```

Total:

```text
9 slots
```

Phase 2 intentionally has only one ring slot.

Additional accessory complexity may come later.

---

# 15. Equipment Rules

Items define valid slots.

Examples:

```text
Helmet
→ HEAD

Leather Armor
→ CHEST

Sword
→ MAIN_HAND

Shield
→ OFF_HAND

Bow
→ MAIN_HAND
→ two-handed
```

Two-handed weapons occupy:

```text
MAIN_HAND
```

and require:

```text
OFF_HAND = empty
```

Equipping an incompatible item must fail server-side.

---

# 16. Weapon Families

Phase 2 weapon families:

```text
SWORD
AXE
MACE
DAGGER
BOW
```

Shields are equipment but not a weapon mastery family.

---

# 17. Weapon Identity

## Sword

Identity:

```text
balanced
accurate
counter-oriented
```

Strengths:

* flexibility;
* reliable hit chance;
* defensive techniques.

---

## Axe

Identity:

```text
high damage
bleed
armor destruction
```

Weaknesses:

* stamina cost;
* lower accuracy.

---

## Mace

Identity:

```text
armor penetration
stun
control
```

Weaknesses:

* slower;
* less critical synergy.

---

## Dagger

Identity:

```text
critical hits
mobility
poison
finishers
```

Weaknesses:

* lower raw damage;
* poor against heavy armor without specialization.

---

## Bow

Identity:

```text
accuracy
precision
target control
```

Weaknesses:

* two-handed;
* weaker defensive options.

---

# 18. Armor Categories

Armor pieces have:

```text
LIGHT
MEDIUM
HEAVY
```

---

## Light Armor

Characteristics:

```text
low armor
high mobility
possible dodge bonuses
low requirements
```

---

## Medium Armor

Characteristics:

```text
balanced armor
balanced mobility
```

---

## Heavy Armor

Characteristics:

```text
high armor
higher STR/END requirements
dodge penalties
```

Equipment decisions must therefore affect build identity.

---

# 19. Item Definition vs Item Instance

Preserve the Phase 1 distinction.

## ItemDefinition

Represents static game data.

Example:

```text
STEEL_LONGSWORD
```

## ItemInstance

Represents the player's actual item.

Two instances of:

```text
STEEL_LONGSWORD
```

may have different:

* rolled stats;
* rarity;
* affixes.

---

# 20. Item Rarity

Phase 2 rarities:

```text
COMMON
UNCOMMON
RARE
EPIC
```

Do not introduce Legendary yet.

Initial modifier counts:

| Rarity   | Affixes |
| -------- | ------: |
| Common   |       0 |
| Uncommon |       1 |
| Rare     |       2 |
| Epic     |       3 |

Items may also have slightly randomized base stat rolls.

Keep base variance small.

Recommended initial range:

```text
95%–105%
```

of definition base values.

---

# 21. Item Affixes

Affixes are divided into:

```text
PREFIX
SUFFIX
```

Example prefixes:

```text
Sharp
→ increased damage

Balanced
→ accuracy

Vicious
→ critical chance

Reinforced
→ armor

Quick
→ reduced stamina cost
```

Example suffixes:

```text
of Strength
→ STR

of the Fox
→ AGI

of Vitality
→ END

of Precision
→ PER
```

Combat-oriented advanced affixes may include:

```text
Bleed Chance
Block
Dodge
Armor Penetration
Poison Effectiveness
```

Affixes must have item-type compatibility rules.

Example:

```text
Armor bonus
```

must not randomly appear on a potion.

---

# 22. Affix Design Rule

Avoid overly complicated conditional modifiers.

Good:

```text
+4% Critical Chance
```

Good:

```text
+8% Damage against Bleeding enemies
```

Avoid:

```text
+3.2% damage while below 43% stamina after dodging during the previous two rounds
```

Item tooltips must remain readable within several seconds.

---

# 23. Equipment Requirements

Items may require:

```text
minimum level
minimum STR
minimum AGI
minimum END
minimum PER
```

Example:

```text
Mercenary Great Axe

Level 12
STR 18
END 10
```

A character that stops meeting requirements after respec must automatically have invalid equipment unequipped.

This operation must be transactional.

---

# 24. Inventory Improvements

Inventory must support:

* item rarity;
* sorting;
* filtering;
* equipment comparison;
* affix display;
* item type display;
* weapon family display;
* requirement display;
* salvage availability;
* market availability.

Frontend must clearly distinguish:

```text
equipped
usable
unusable
upgrade
downgrade
```

without calculating authoritative game values independently.

---

# 25. Item Comparison

When hovering or selecting equipment:

```text
Current Item
vs
Candidate Item
```

show differences.

Example:

```text
Damage       +4
Accuracy     -3
Critical     +2%
Strength     +1
Armor        -6
```

Do not reduce items to a single universal:

```text
Item Power
```

score in Phase 2.

Different builds should value different items differently.

---

# 26. Weapon Mastery

Each character has separate mastery progression for:

```text
Sword
Axe
Mace
Dagger
Bow
```

Mastery levels:

```text
0–10
```

Mastery XP is earned by successfully participating in relevant combat using that weapon family.

Mastery progression must be slower than character leveling.

A character may develop multiple weapon masteries.

---

# 27. Mastery Rewards

Initial milestone model:

```text
Mastery 2
→ technique unlock

Mastery 4
→ technique unlock

Mastery 6
→ technique unlock

Mastery 8
→ advanced technique unlock

Mastery 10
→ mastery passive
```

Mastery should unlock options rather than merely providing large raw damage bonuses.

---

# 28. Combat Technique System

Combat actions become data-driven techniques.

A character may learn multiple techniques.

A player equips a limited combat loadout.

Initial limit:

```text
4 active techniques
```

Core actions such as basic attack may remain universally available outside these four slots if needed for UX.

The loadout cannot be changed during combat.

---

# 29. Example Sword Techniques

Potential initial Sword progression:

```text
Riposte
Deep Cut
Guard Break
Duelist's Tempo
```

---

# 30. Example Axe Techniques

```text
Rending Chop
Cleave
Shatter Armor
Executioner
```

---

# 31. Example Mace Techniques

```text
Crushing Blow
Concussive Strike
Break Guard
Overwhelm
```

---

# 32. Example Dagger Techniques

```text
Feint
Poisoned Strike
Evasive Cut
Finisher
```

---

# 33. Example Bow Techniques

```text
Aimed Shot
Crippling Shot
Piercing Shot
Rapid Shot
```

Exact numerical balance should be configured separately.

The architecture must support adding new techniques without rewriting CombatEngine.

---

# 34. Status Effect System

Phase 2 introduces a generic status effect engine.

Initial statuses:

```text
BLEED
POISON
STUN
ARMOR_BREAK
OFF_BALANCE
GUARDED
```

Do not implement dozens of effects.

---

# 35. Bleed

Initial concept:

* causes damage at the end of a round;
* stackable;
* maximum 3 stacks;
* finite duration.

---

# 36. Poison

Initial concept:

* damage over time;
* longer duration than Bleed;
* may interact with Alchemist-created consumables.

---

# 37. Stun

Initial concept:

* prevents one action;
* must have anti-chain protection.

After being stunned, a combatant receives short temporary Stun immunity.

The system must prevent permanent stun-lock.

---

# 38. Armor Break

Initial concept:

* reduces effective armor;
* limited stacking;
* temporary duration.

---

# 39. Off Balance

Initial concept:

* temporary tactical vulnerability;
* may increase effectiveness of specific follow-up techniques.

This exists primarily to enable action combinations.

---

# 40. Guarded

Applied by defensive actions.

Reduces incoming damage for the current/next relevant combat resolution.

---

# 41. Combat 2.0

Combat remains:

```text
server authoritative
turn based
interruptible
persistent
```

No action must require reaction within a few seconds.

---

# 42. Combat Round Structure

A round should support:

```text
validate actor state
↓
resolve active effects
↓
resolve selected technique
↓
apply damage / statuses
↓
enemy decision
↓
enemy action
↓
resolve round-end effects
↓
update stamina
↓
check defeat
↓
persist complete round
```

Combat resolution must remain deterministic under controlled random input for testing.

---

# 43. Combat Randomness

Do not call:

```java
Math.random()
```

directly from domain logic.

Randomness must remain injectable.

Tests must be capable of forcing:

```text
hit
miss
critical
loot result
status application
```

---

# 44. Accuracy

Hit probability should consider:

```text
attacker accuracy
technique modifier
defender dodge
status effects
equipment
```

Final hit chance should have hard boundaries.

Initial recommended bounds:

```text
minimum 5%
maximum 95%
```

No build should become literally unhittable.

---

# 45. Critical Hits

Critical chance is influenced by:

```text
PER
equipment
weapon
technique
statuses
```

Recommended Phase 2 hard cap:

```text
35%
```

unless a specific technique explicitly overrides it.

---

# 46. Armor

Physical armor reduces physical damage.

Avoid a simple linear system where high armor eventually produces zero damage.

Use a diminishing-return model.

Exact formula belongs in CombatBalance.

A minimum amount of relevant damage should generally pass through unless an ability explicitly blocks it.

---

# 47. Stamina

Stamina becomes a meaningful combat resource.

High-value techniques require more stamina.

A player who repeatedly uses expensive attacks should eventually need to:

```text
Defend
use low-cost technique
change tactical plan
```

Stamina should regenerate partly between rounds and fully outside combat according to existing/rest rules.

---

# 48. Enemy Combat AI

Enemy behavior should be archetype-driven.

Examples:

```text
AGGRESSIVE
DEFENSIVE
CONTROL
ASSASSIN
ARMORED
BERSERKER
```

Enemy AI must not know the player's future selected action.

It may react only to currently known state.

---

# 49. Build Definition

A build consists of:

```text
Primary Attributes
+
Equipment
+
Weapon Family
+
Mastery
+
Technique Loadout
+
Consumables
```

Do not create a backend entity called:

```text
CharacterClass
```

to represent builds.

---

# 50. Build Presets

Phase 2 may support a small number of saved build presets.

Recommended:

```text
3
```

A preset may store:

* equipment references;
* technique loadout.

It must not duplicate items.

Switching presets should equip the corresponding owned items.

Preset switching is not allowed during combat.

---

# 51. PvE Content Expansion Philosophy

Do not expand the world primarily by adding map size.

Expand the existing Greyhaven region by adding mechanical variety.

The player should encounter enemies that test different builds.

---

# 52. Greyhaven Expansion

Potential Phase 2 additions:

```text
Greyhaven

City
├ City Square
├ Old Town
├ Market
├ Tavern
├ Arena
├ Craftsmen Ward
└ Harbour

Surroundings
├ Forest
├ North Road
├ Sewers
├ Old Mine
├ Bandit Camp
└ Ancient Ruins
```

Do not add another major kingdom or continent.

---

# 53. PvE Progression Bands

Suggested approximate difficulty bands:

```text
Old Town
Level 1–5

Forest
Level 3–8

Sewers
Level 5–10

Old Mine
Level 8–15

Bandit Camp
Level 12–20

Ancient Ruins
Level 18–30
```

These are recommendations rather than hard locks.

Players may enter dangerous areas early if world rules permit.

The game should communicate danger clearly.

---

# 54. Enemy Archetypes

Phase 2 should provide approximately:

```text
15–20 meaningful enemy definitions
```

rather than dozens of stat clones.

Examples:

## Armored Guard

```text
high armor
low dodge

counter:
Armor Break
```

## Assassin

```text
high dodge
high critical
low health

counter:
Accuracy / defense
```

## Berserker

```text
high damage
poor defense

counter:
Guard / control
```

## Plague Wolf

```text
applies Bleed
```

## Shielded Bandit

```text
high block
```

## Marksman

```text
high accuracy
lower survivability
```

---

# 55. Elite Enemies

Introduce elite variants.

Elite enemies:

* are visibly marked;
* have stronger mechanics;
* have better loot tables;
* are not merely enemies with doubled HP.

---

# 56. Mini-Bosses

Introduce several mini-boss encounters.

Each should have at least one identifiable mechanic.

Example:

```text
Bandit Lieutenant

Phase 1:
normal

Below 50% HP:
aggressive techniques

Weakness:
control / stamina management
```

---

# 57. First Dungeon

Implement one proper Phase 2 dungeon.

The dungeon remains browser-native.

Example structure:

```text
Entrance
   |
Guard Room
   |
Courtyard
 /      \
Armory   Prison
 \      /
 Command Hall
      |
     Boss
```

Movement is decision-based rather than WASD.

Dungeon should contain:

* branching path;
* multiple encounters;
* optional room;
* resource/loot decision;
* mini-boss or elite;
* final boss;
* meaningful reward.

Dungeon progress must survive page reload.

---

# 58. PvP Philosophy

PvP is introduced only after the character/build system is functional.

Primary PvP mode:

```text
Asynchronous Arena
```

Secondary PvP mode:

```text
Casual Duel
```

Phase 2 does NOT contain:

* clan PvP;
* territory PvP;
* open-world PK;
* mass battles.

---

# 59. Player Inspection

Players may inspect another character.

Display:

* level;
* equipment;
* visible primary attributes;
* weapon mastery;
* selected public build information;
* Arena rating.

Do not expose private account information.

---

# 60. Asynchronous Arena

Arena is the main competitive system for the office-first audience.

Players can attack another player's Arena defense without requiring the defender to be online.

---

# 61. Arena Defense Configuration

Player configures defensive behavior.

Initial system should remain understandable.

Example rules:

```text
Preferred technique:
Quick Attack

When HP < 40%:
Use Healing Potion

When Stamina < 25%:
Defend

Against low-health enemy:
Use Finisher
```

Do not create a full scripting language.

Use structured configurable strategy rules.

---

# 62. Arena Match Snapshot

When a match begins, create a combat snapshot containing:

```text
attacker stats
attacker equipment
attacker technique loadout

defender stats
defender equipment
defender defense strategy
```

Changing equipment after match creation must not mutate an active Arena combat state.

---

# 63. Arena Attack Flow

Example:

```text
Open Arena
↓
Select opponent
↓
Inspect public build
↓
Start challenge
↓
Fight defender AI
↓
Result
↓
Rating update
↓
Battle history
```

The attacker actively controls their actions.

The defender is controlled according to their configured defense strategy.

---

# 64. Arena Rating

Initial rating:

```text
1000
```

Use a simple rating model suitable for MVP competitive testing.

The rating implementation must be isolated so it can later be replaced.

Do not build the entire game around one hardcoded Elo formula.

---

# 65. Arena Anti-Abuse

Prevent farming one opponent repeatedly.

At minimum:

* repeated matches against the same player provide reduced or zero rating reward for a configurable period;
* a player cannot fight themselves;
* obvious duplicate reward requests are idempotent;
* rating updates occur exactly once.

---

# 66. Arena Rewards

Arena should provide modest rewards.

PvP rewards must not become the dominant source of normal economic currency.

Consider a separate:

```text
Arena Mark
```

currency for PvP-oriented cosmetic or gameplay-sidegrade rewards.

Do not create mandatory PvP-exclusive best-in-slot items during Phase 2.

---

# 67. Casual Live Duel

Allow one player to challenge another online player.

Duels are:

```text
unranked
optional
low pressure
```

Both players submit actions for a round.

The round resolves when both actions are available.

If one player becomes temporarily inactive, the duel must degrade gracefully.

Do not punish work interruptions with meaningful progression loss.

A duel may eventually expire without ranking penalty.

---

# 68. PvP Battle History

Store recent Arena and Duel results.

Example:

```text
WIN vs Kain
Rating +14

LOSS vs Morrigan
Rating -9
```

Provide access to recent battle details.

---

# 69. Economy Phase 2 Goal

Phase 1 Marketplace mostly redistributes loot.

Phase 2 must create an economic loop:

```text
PvE
↓
resources
↓
processing
↓
crafting
↓
equipment / consumables
↓
players
↓
market
```

---

# 70. Crafting Professions

Initial professions:

```text
BLACKSMITH
ALCHEMIST
HUNTER
```

---

# 71. Blacksmith

Produces and processes:

* metal materials;
* weapons;
* armor components;
* selected armor items.

---

# 72. Alchemist

Produces:

* healing potions;
* combat consumables;
* poisons;
* utility consumables.

---

# 73. Hunter

Produces/processes:

* hides;
* leather;
* animal materials;
* selected light equipment components.

---

# 74. Profession Progression

Each profession has:

```text
Rank 1–10
```

Profession XP is gained by crafting relevant recipes.

Characters may learn all three basic professions during Phase 2.

To preserve future economic specialization, architecture must support later profession specialization.

Do not implement irreversible specialization yet.

---

# 75. Recipe Model

Recipe definition should include:

```text
id
profession
requiredProfessionRank
requiredCharacterLevel
inputs
goldCost
duration
outputDefinition
outputQuantity
possibleRarityRange
```

---

# 76. Crafting Time

Crafting is asynchronous when appropriate.

Use timestamp-based completion:

```text
startedAt
completesAt
```

Never sleep server threads.

Crafting continues while player is offline.

---

# 77. Crafting Results

Equipment recipes may produce varying rarity within controlled limits.

Example:

```text
Steel Longsword Recipe

Common      likely
Uncommon    possible
Rare        uncommon
Epic        very rare
```

Probability depends on:

```text
profession rank
recipe
balance configuration
```

Crafting outcome randomness must be persisted exactly once.

Refreshing must not reroll the item.

---

# 78. Salvage

Player may destroy unwanted equipment.

Example:

```text
Rare Steel Sword
↓
Steel
Weapon Components
```

Salvage provides an item sink.

Salvage must be irreversible and explicitly confirmed in UI.

Equipped items cannot be salvaged without unequipping.

Market-listed items cannot be salvaged.

---

# 79. Marketplace 2.0

Expand Phase 1 marketplace.

Add:

* stronger filtering;
* rarity filtering;
* level filtering;
* weapon family filtering;
* price sorting;
* affix-aware item display;
* listing history;
* buy orders.

---

# 80. Buy Orders

A player may request:

```text
Iron Ore ×100
Maximum unit price: 12 gold
```

Another player may fulfill the order partially or fully.

Buy-order funds must be reserved safely.

A character cannot create buy orders using gold they no longer possess.

Transactions must remain concurrency safe.

---

# 81. Marketplace Fees

Introduce configurable economic sinks.

Initial model:

```text
listing fee
+
sale fee
```

Suggested initial values:

```text
Listing Fee: 1%
Sale Fee:    5%
```

These values must be balance configuration.

---

# 82. No Equipment Durability Yet

Do NOT add durability/repair in Phase 2.

The economy needs sinks, but durability would introduce repetitive maintenance that conflicts with office-first design.

Use:

* market fees;
* crafting costs;
* consumables;
* salvage;
* respec costs;

as initial economic sinks.

---

# 83. UX / Visual Design Goal

Phase 2 should move the application from:

```text
functional MVP interface
```

toward:

```text
recognizable game product
```

without stopping gameplay development for a complete visual rewrite.

---

# 84. Design System

Create a reusable UI design system.

At minimum define:

* typography scale;
* spacing;
* panels;
* buttons;
* inputs;
* tabs;
* badges;
* item rarity presentation;
* stat presentation;
* tooltips;
* progress bars;
* status effects;
* modal/dialog conventions;
* notification conventions;
* loading states;
* empty states;
* error states.

---

# 85. Character Screen Redesign

Character screen should become one of the game's major interfaces.

It should show:

```text
portrait / identity
level
XP
primary attributes
derived statistics
equipment
weapon mastery
technique loadout
available attribute points
```

Information hierarchy matters.

Do not display every internal combat variable by default.

Advanced details may be available through expanded statistics.

---

# 86. Equipment Presentation

Provide a clear equipment layout corresponding to body slots.

It does not require a fully animated 3D character.

A high-quality 2D paper-doll layout is sufficient.

---

# 87. Item Tooltip

Tooltip should include:

```text
Name
Rarity
Item Type
Weapon/Armor Type
Main Stats
Affixes
Requirements
Comparison
Market-related availability where relevant
```

Tooltips must remain readable.

---

# 88. Normal Mode

Normal Mode should support:

* location artwork;
* richer presentation;
* subtle animation;
* immersive panels;
* dark-fantasy identity.

---

# 89. Office Mode 2.0

Office Mode remains a first-class feature.

Improve:

* information density;
* minimal artwork;
* smaller spacing;
* reduced animation;
* lower visual noise;
* efficient navigation;
* narrow-window usability.

Game functionality must remain identical.

Only presentation changes.

---

# 90. Combat UI 2.0

Display:

```text
player HP
player stamina
enemy HP

statuses
techniques
technique costs
cooldowns/restrictions if present
combat log
enemy archetype information
```

Hovering a technique should explain exactly what it does.

Do not require players to memorize undocumented combat formulas.

---

# 91. Telemetry Purpose

By Phase 2, balance cannot rely purely on intuition.

The game must begin collecting structured gameplay telemetry.

This is for game balancing and product analysis.

---

# 92. Required Progression Metrics

Track aggregate metrics such as:

```text
XP earned per hour/session
time between levels
level distribution
attribute distribution
respec usage
```

---

# 93. Required Combat Metrics

Track:

```text
combat started
combat won/lost
combat duration
round count
technique usage
weapon usage
damage distribution
status application
retreat rate
```

---

# 94. PvP Metrics

Track:

```text
Arena participation
Arena win rate
rating distribution
weapon-family win rate
build-stat distribution
repeat-opponent rate
```

---

# 95. Economy Metrics

Track:

```text
gold created
gold destroyed
market transaction volume
average prices
item creation
item salvage
crafting frequency
profession distribution
```

---

# 96. Privacy / Telemetry Rule

Do not place sensitive account data into game telemetry.

Use internal player/character identifiers where needed.

Game telemetry is not an excuse to log passwords, session tokens, emails or private chat contents.

---

# 97. Balance Configuration

Balance values should be centralized and data-driven where practical.

Examples:

```text
XP curve
drop chances
affix ranges
monster stats
technique values
profession probabilities
market fees
Arena rating constants
```

Do not spread magic numbers across controllers and services.

---

# 98. Admin/Developer Balance Support

A complete administrative game-master UI is not required.

However developers should be able to inspect:

* character progression;
* equipment;
* monster definitions;
* economy state;
* Arena statistics.

Development-only commands or endpoints may exist if secured appropriately and excluded from production access.

---

# 99. Backend Architectural Rules

Continue modular monolith.

Potential module evolution:

```text
account
character
progression
world
combat
item
inventory
mastery
pve
pvp
expedition
crafting
market
activity
chat
telemetry
shared
```

Do not split modules merely for aesthetic symmetry.

Prefer clear boundaries over excessive package ceremony.

---

# 100. Server Authority

Frontend may preview calculated information returned from the server.

It may not authoritatively calculate:

* XP rewards;
* item generation;
* affixes;
* crafting result;
* damage;
* hit chance;
* rating change;
* market settlement;
* profession XP.

---

# 101. API Evolution

Continue using:

```text
/api/v1
```

where changes remain backward-compatible.

Do not create `/api/v2` solely because Phase 2 exists.

Potential new endpoints include:

```text
GET  /api/v1/character/progression
POST /api/v1/character/attributes
POST /api/v1/character/respec

GET  /api/v1/character/masteries
GET  /api/v1/character/techniques
PUT  /api/v1/character/technique-loadout

GET  /api/v1/equipment
POST /api/v1/equipment/presets/{id}/activate

GET  /api/v1/pvp/arena
GET  /api/v1/pvp/arena/opponents
POST /api/v1/pvp/arena/challenges
GET  /api/v1/pvp/history

POST /api/v1/pvp/duels
POST /api/v1/pvp/duels/{id}/accept
POST /api/v1/pvp/duels/{id}/actions

GET  /api/v1/crafting/professions
GET  /api/v1/crafting/recipes
POST /api/v1/crafting/jobs
POST /api/v1/crafting/jobs/{id}/claim

POST /api/v1/items/{id}/salvage

GET  /api/v1/market/buy-orders
POST /api/v1/market/buy-orders
POST /api/v1/market/buy-orders/{id}/fulfill
```

Exact DTO structures should follow existing repository conventions.

---

# 102. Transaction Safety

Phase 2 adds new economy-critical operations.

Special care is required for:

```text
level-up rewards
attribute allocation
respec
equipment changes
item generation
loot claiming
crafting claiming
salvage
market purchases
buy-order fulfillment
Arena rewards
Arena rating changes
```

All reward-producing flows must be idempotent.

---

# 103. Testing Requirements

Important game logic must be unit-testable without the entire Spring context.

Examples:

```text
ExperienceCurve
CharacterStatCalculator
ItemGenerator
AffixGenerator
EquipmentValidator
MasteryProgression
TechniqueResolver
StatusEffectEngine
CombatEngine
ArenaRatingCalculator
ArenaDefenseStrategy
CraftingResolver
SalvageCalculator
```

---

# 104. Integration Testing

Important persistence flows must use PostgreSQL Testcontainers.

Required Phase 2 integration scenarios include:

* legacy character migration;
* XP level-up;
* attribute allocation;
* respec;
* equipment requirement validation;
* generated item persistence;
* mastery progression;
* persistent Combat 2.0;
* dungeon progress persistence;
* Arena reward exactly-once;
* concurrent Arena completion protection;
* crafting claim exactly-once;
* salvage;
* buy-order concurrent fulfillment.

---

# 105. Performance Expectations

Do not prematurely optimize for millions of players.

However avoid obvious N+1 queries in:

* inventory;
* marketplace;
* Arena opponent list;
* character inspection;
* activity feed.

Pagination must be used for potentially large marketplace and PvP history queries.

---

# 106. Phase 2 Player Journey

A representative Phase 2 player should be able to:

```text
Login
↓
See activity since last visit
↓
Gain enough XP to level up
↓
Allocate attribute points
↓
Equip a new Rare weapon
↓
Compare it with current equipment
↓
Notice a weapon mastery unlock
↓
Equip a new combat technique
↓
Enter a harder PvE location
↓
Fight an enemy using status mechanics
↓
Receive crafting resources
↓
Craft or salvage equipment
↓
List useful item on Market
↓
Configure Arena defense
↓
Attack another player's build
↓
Review Arena result
↓
Start asynchronous activity
↓
Close browser safely
```

This should feel like one coherent game, not a collection of independent systems.

---

# 107. Phase 2 Definition of Done

Phase 2 is complete when:

1. Existing Phase 1 users migrate without destructive data loss.
2. Level progression works from 1 to 30.
3. XP requirements are configurable.
4. Attribute allocation works.
5. Respec works safely.
6. Derived statistics are server-authoritative.
7. Nine equipment slots work.
8. Weapon and armor families affect gameplay.
9. Common/Uncommon/Rare/Epic equipment works.
10. Affix generation works.
11. Item comparison works.
12. Equipment requirements work.
13. Weapon mastery works.
14. Combat techniques unlock and can be equipped.
15. Status effects work.
16. Combat 2.0 is persistent and interruption-safe.
17. Multiple meaningful build archetypes exist.
18. Expanded PvE content exercises different mechanics.
19. One complete dungeon works.
20. Async Arena works.
21. Arena rating and battle history work.
22. Casual duel works.
23. Crafting works.
24. Profession progression works.
25. Salvage works.
26. Buy orders work safely.
27. Marketplace remains concurrency-safe.
28. Design system exists.
29. Character/Inventory/Combat interfaces are substantially improved.
30. Office Mode remains fully functional.
31. Game telemetry exists for progression, combat, PvP and economy.
32. Critical systems have automated tests.
33. Phase 1 functionality remains operational.
34. No Phase 3 Social MMO functionality was prematurely implemented.

---

# IMPLEMENTATION PLAN FOR CURSOR

Phase 2 must be implemented sequentially.

Do not implement the whole Phase 2 in one Agent run.

---

# TASK 1 — Phase 2 Foundation and Migration Preparation

## Goal

Prepare the existing Phase 1 repository for Phase 2 without changing core gameplay yet.

## Required Work

Inspect:

```text
AGENTS.md
docs/MVP_SPEC.md
current codebase
current Flyway migrations
current entities
current APIs
```

Create:

```text
docs/PHASE_2_SPEC.md
```

if not already present.

Establish or improve centralized balance infrastructure.

Prepare model changes necessary for future:

```text
levels
attributes
equipment slots
item rarity
item affixes
mastery
techniques
```

Do not implement the full systems yet.

Create Flyway migration strategy for legacy data.

Document conversion of:

```text
WEAPON → MAIN_HAND
ARMOR → CHEST
```

Determine how existing XP representation maps to the Phase 2 total-XP model.

No player progression may be silently lost.

## Verification

* existing Phase 1 tests pass;
* migrations work from clean database;
* migrations work from Phase 1 schema;
* existing characters still load;
* existing inventory still loads;
* existing combat/expedition/market functionality remains operational.

Do not proceed automatically.

---

# TASK 2 — Character Progression 2.0

## Goal

Implement the complete Phase 2 character progression foundation.

Implement:

```text
level 1–30
XP table
attribute points
derived stats
attribute requirements support
respec
```

Create dedicated progression domain services.

Possible components:

```text
ExperienceCurve
CharacterProgressionService
CharacterStatCalculator
AttributeAllocationService
RespecService
```

Update Character API.

Update character frontend.

Character UI must display:

```text
level
XP / XP required
available attribute points
primary attributes
derived stats
```

Implement level-up notifications/activity.

## Tests

At minimum:

```text
single level-up
multiple level-ups from one XP reward
level 30 cap
attribute point awarding
invalid allocation
respec
insufficient respec gold
derived stat calculation
legacy character progression
```

Do not implement item affixes or mastery yet.

---

# TASK 3 — Itemization and Equipment 2.0

## Goal

Create the full Phase 2 item/equipment foundation.

Implement equipment slots:

```text
HEAD
CHEST
HANDS
LEGS
FEET
MAIN_HAND
OFF_HAND
AMULET
RING
```

Implement:

```text
weapon family
armor category
two-handed rules
level requirements
attribute requirements
rarity
base stat rolls
prefixes
suffixes
random affix generation
```

Create:

```text
ItemGenerator
AffixGenerator
EquipmentValidator
ItemStatCalculator
```

Migrate legacy items safely.

Implement item comparison API data.

Update inventory/equipment frontend.

## Tests

Cover:

```text
slot compatibility
two-handed weapon logic
requirements
respec causing invalid equipment
rarity generation
affix compatibility
deterministic item generation
legacy items
item ownership
transaction safety
```

Do not implement mastery or crafting yet.

---

# TASK 4 — Phase 2 UX and Visual Foundation

## Goal

Create a coherent reusable interface before adding further gameplay complexity.

Do not redesign backend architecture.

Create/refine:

```text
design tokens
typography
buttons
panels
tabs
tooltips
badges
rarity styles
progress bars
stat components
item cards
status badge components
loading states
error states
empty states
```

Redesign:

```text
Character screen
Inventory
Equipment
Item tooltip
Item comparison
```

Improve:

```text
Normal Mode
Office Mode
```

Office Mode must remain information-dense and low-distraction.

Do not spend the task creating large quantities of final artwork.

Use appropriate placeholders where final art direction is not yet approved.

Verify accessibility:

* readable contrast;
* keyboard basics;
* useful focus states;
* semantic controls.

Do not proceed automatically.

---

# TASK 5 — Weapon Mastery and Combat Techniques

## Goal

Implement character build progression beyond attributes and equipment.

Create:

```text
WeaponMastery
MasteryProgression
CombatTechniqueDefinition
CharacterTechnique
TechniqueLoadout
```

Implement mastery for:

```text
Sword
Axe
Mace
Dagger
Bow
```

Mastery range:

```text
0–10
```

Implement technique unlock milestones.

Implement active technique loadout:

```text
4 slots
```

Create initial techniques for each weapon family.

Technique definitions must be data-driven.

Frontend:

Create:

```text
Mastery screen
Technique collection
Technique loadout editor
```

## Tests

Cover:

```text
mastery XP
weapon-family validation
unlock thresholds
duplicate unlock prevention
invalid loadout
loadout size
weapon compatibility
```

Do not implement PvP yet.

---

# TASK 6 — Combat 2.0 and Status Effects

## Goal

Upgrade Phase 1 combat to support real build diversity.

Implement generic status engine.

Initial statuses:

```text
BLEED
POISON
STUN
ARMOR_BREAK
OFF_BALANCE
GUARDED
```

Extend CombatEngine to account for:

```text
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
```

Implement diminishing-return armor logic.

Implement status stacking and expiration.

Implement anti-stun-lock behavior.

Improve enemy AI architecture.

Migrate current Phase 1 combat sessions safely where practical.

Active legacy combats may alternatively finish using legacy rules if migration complexity would compromise integrity; this choice must be documented.

Frontend:

Redesign combat screen using Task 4 components.

Display:

```text
statuses
techniques
stamina cost
combat log
enemy identity
```

## Tests

CombatEngine requires extensive deterministic unit testing.

Cover:

```text
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
```

At the end of this task, perform a manual 30–60 minute gameplay review before moving forward.

---

# TASK 7 — PvE Content Expansion and Dungeon

## Goal

Provide content that actually tests the Phase 2 build system.

Add Greyhaven locations such as:

```text
Arena
Craftsmen Ward
Harbour
Sewers
Old Mine
Bandit Camp
Ancient Ruins
```

Do not add a second major region.

Expand enemy roster to approximately:

```text
15–20 mechanically meaningful enemies
```

Implement:

```text
enemy archetypes
elite enemies
mini-bosses
improved loot tables
difficulty bands
```

Implement first dungeon.

Dungeon requirements:

```text
persistent progress
branching path
optional encounter
elite/mini-boss
final boss
meaningful reward
safe browser refresh
```

Ensure enemy mechanics reward different builds.

## Acceptance Question

At the end of Task 7, ask:

> Does changing build meaningfully change how difficult different PvE enemies feel?

If not, do not proceed to PvP before correcting build/combat interaction.

---

# TASK 8 — PvP Foundation and Async Arena

## Goal

Introduce player-vs-player combat only after build diversity exists.

Implement:

```text
player inspection
Arena profile
Arena defense configuration
opponent selection
Arena challenge
battle snapshot
rating
battle history
Arena rewards
```

Primary mode:

```text
Asynchronous Arena
```

Implement simple structured defensive strategy.

Do not implement a scripting language.

Ensure:

```text
defender can be offline
match uses snapshot
rating updates exactly once
rewards exactly once
self-challenge impossible
repeat farming mitigated
```

Implement casual live duel after Arena works.

Duels are:

```text
unranked
optional
interruption-friendly
```

Do not introduce mandatory realtime infrastructure if existing REST/SSE mechanisms can satisfy requirements cleanly.

If WebSocket is genuinely required, propose the change before implementing it.

## Tests

Include:

```text
Arena snapshot consistency
rating calculation
duplicate completion
repeat-opponent protection
offline defender
equipment changes after match start
duel action resolution
disconnect behavior
```

Perform a balance review across major build archetypes.

---

# TASK 9 — Crafting, Professions and Economy 2.0

## Goal

Turn loot and Market into a broader economic system.

Implement professions:

```text
Blacksmith
Alchemist
Hunter
```

Implement:

```text
profession ranks 1–10
profession XP
recipes
resources
crafting jobs
timestamp completion
crafting result
salvage
```

Crafting rewards must resolve exactly once.

Generated crafted equipment must use the existing item generation system.

Expand Marketplace with:

```text
filters
rarity
weapon family
sorting
buy orders
listing fee
sale fee
```

Buy orders must reserve funds.

Concurrent fulfillment must be safe.

Implement economic activity events.

## Tests

Include:

```text
profession progression
recipe validation
missing materials
crafting completion
duplicate claim
rarity roll
salvage
salvage equipped item rejection
listing fee
sale fee
buy-order reservation
partial fulfillment
concurrent fulfillment
```

---

# TASK 10 — Telemetry, Balance, Integration and Final Polish

## Goal

Stabilize Phase 2 instead of adding another major gameplay feature.

Implement structured gameplay telemetry for:

```text
progression
combat
PvP
economy
crafting
```

Create developer-accessible balance diagnostics.

Perform architecture audit.

Review:

```text
XP curve
attribute distribution
weapon balance
armor balance
technique usage
monster difficulty
Arena win rates
economy creation/destruction
crafting output
market activity
```

Improve UX issues found during full gameplay.

Perform final visual coherence pass across:

```text
Character
Inventory
Equipment
Combat
PvE
Arena
Crafting
Market
Office Mode
```

Do not add Phase 3 systems.

---

# PHASE 2 FINAL END-TO-END TEST

Before declaring Phase 2 complete, manually execute:

```text
Login
↓
Load existing or new character
↓
Gain XP
↓
Level up
↓
Allocate attributes
↓
Equip several armor slots
↓
Find Rare item
↓
Compare equipment
↓
Unlock weapon mastery
↓
Unlock technique
↓
Change technique loadout
↓
Fight enemies using Combat 2.0
↓
Apply and receive status effects
↓
Enter dungeon
↓
Defeat boss
↓
Configure Arena defense
↓
Attack another player
↓
Receive Arena rating result
↓
Craft an item
↓
Salvage unwanted item
↓
Create Market listing
↓
Create or fulfill buy order
↓
Switch to Office Mode
↓
Close browser
↓
Return later
↓
Verify all persistent state
```

---

# GLOBAL PHASE 2 CURSOR RULES

All existing Phase 1 agent rules remain active.

Add the following.

## Rule 1 — Phase 1 is production history

Do not recreate existing functionality merely because a cleaner design is possible.

Prefer controlled migrations and incremental refactoring.

---

## Rule 2 — Preserve player data

Never introduce a destructive migration without explicit human approval.

---

## Rule 3 — Build depth before content quantity

Do not solve shallow gameplay by creating hundreds of items or monsters.

Fix systems first.

---

## Rule 4 — Build diversity must be measurable

When adding a new stat, affix, weapon or technique, explain what meaningful player decision it creates.

---

## Rule 5 — No class system

Do not introduce permanent Warrior/Rogue/etc. classes.

---

## Rule 6 — No Phase 3 social systems

Do not implement:

```text
clans
guilds
territories
raids
world bosses
politics
diplomacy
```

even if they appear useful for future architecture.

Design only the minimal extensibility required by current Phase 2 systems.

---

## Rule 7 — No speculative infrastructure

Do not introduce:

```text
Kafka
microservices
event sourcing
Kubernetes
additional databases
```

without an explicit demonstrated requirement.

---

## Rule 8 — Combat remains deterministic in tests

All randomness must remain injectable.

---

## Rule 9 — Economy operations require database integrity

Do not rely solely on application-level checks for:

```text
gold
items
market state
crafting rewards
Arena rewards
```

Use transactions and appropriate constraints/locking.

---

## Rule 10 — UI does not own game rules

Frontend may display server-provided previews.

Frontend must not become the source of truth for character statistics or combat results.

---

## Rule 11 — Balance values change frequently

Do not couple balance constants deeply to persistence or HTTP layers.

---

## Rule 12 — Every task ends with regression verification

Before declaring a task complete:

```text
compile backend
run complete backend test suite
run relevant integration tests
build frontend
verify Flyway
verify startup
verify previous Phase functionality
```

---

# PRODUCT NORTH STAR — PHASE 2

When design decisions are ambiguous, optimize for this experience:

> A player opens the game during a work break. They immediately see useful progress, gain or evaluate an item, make a meaningful character-build decision, fight a short tactical encounter, interact with the persistent economy or Arena, start something asynchronous, and return to work. Their decisions matter, but an unexpected interruption never ruins the session.

Phase 2 succeeds when the player does not merely want:

> a higher level.

They want:

> a better character build.
