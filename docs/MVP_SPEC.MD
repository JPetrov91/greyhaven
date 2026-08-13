Browser MMORPG MVP — Master Technical Specification
1. Project Goal

Build an MVP of a modern browser-based persistent RPG inspired by classic browser MMORPGs of the 2000s such as Fight Club / Combats, DWAR and similar games, while applying modern UX and game design principles.

The primary target audience is adult office workers who want to play during short breaks or keep the game open in a browser tab while working.

The game must provide meaningful RPG/MMO depth without requiring continuous attention, long uninterrupted sessions, real-time movement, or high APM gameplay.

The target session pattern is:

2–5 minute micro-interactions during the day;
one meaningful 20–30 minute active session;
asynchronous activities that continue while the player is offline or working.

The MVP must validate the following hypothesis:

A persistent RPG can provide the progression, loot, exploration, economy and social feeling of a client MMORPG while being playable in short, interruption-friendly browser sessions.

2. Core Product Principles

These principles are mandatory and should influence all implementation decisions.

2.1 Office-first gameplay

The player must be able to stop interacting with the game at almost any moment without being punished.

Avoid mechanics requiring:

continuous attention;
frame-perfect interaction;
several uninterrupted minutes of clicking;
permanent loss because the player became inactive;
mandatory online presence at a specific time.

A meeting, phone call, browser tab switch or 30-minute interruption should not destroy the player's current activity.

2.2 Decisions instead of mechanical execution

The game should automate repetitive actions while requiring players to make meaningful decisions.

Bad:

Attack
Attack
Attack
Attack
Attack
Attack

Good:

Heavy Attack
Defensive Stance
Feint
Poisoned Strike
Use Potion
Retreat

Each action should change the tactical situation.

2.3 Persistent world

The world continues to exist while the player is offline.

When returning, the player should immediately understand what happened:

Expedition completed.
3 market orders sold.
A rare item was found.
Your character recovered.
New world event appeared.

The activity feed is therefore an important MVP feature.

2.4 Browser accessibility, client-game depth

Do not attempt to imitate a desktop MMORPG client inside a browser.

Do not implement WASD movement.

Instead preserve:

character builds;
equipment;
loot;
exploration;
combat;
economy;
persistent progression;
social interaction;
world state.

Remove:

unnecessary walking;
repetitive combat input;
loading screens;
long mandatory sessions;
excessive animations;
manual routine.
2.5 Server authority

The server is authoritative for all gameplay.

The client must never calculate or decide:

damage;
rewards;
XP;
loot;
movement validity;
item ownership;
equipment validity;
combat outcomes;
expedition results;
market transactions.

The frontend sends player intentions.

Example:

{
  "action": "HEAVY_ATTACK"
}

The backend calculates the result and returns the new state.

3. MVP Scope

The MVP should contain one small but complete vertical slice of the game.

Included
account registration and login;
one player character per account;
character attributes and level progression;
one city/region;
several connected locations;
location movement;
PvE encounters;
turn-based tactical combat;
monsters;
loot;
inventory;
equipment;
character statistics;
asynchronous expeditions;
basic player marketplace;
global chat;
persistent activity feed;
normal UI mode;
compact office UI mode.
Explicitly excluded from MVP

Do NOT implement yet:

clans;
guild wars;
territory ownership;
raids;
world bosses;
realtime PvP;
ranked PvP;
complex crafting;
professions;
player housing;
magic;
skill trees;
multiple characters;
multiple regions;
procedural world generation;
mobile application;
microservices;
Kubernetes;
Kafka;
event sourcing;
Redis unless a real requirement appears;
AI-generated quests;
advanced admin panel.

These are future features.

4. MVP Game World

Create one region called:

Greyhaven

Greyhaven contains the following locations.

                     NORTH ROAD
                         |
                         |
FOREST -------- CITY SQUARE -------- OLD TOWN
                    |
                    |
                  MARKET
                    |
                    |
                 TAVERN

Locations:

CITY_SQUARE

Safe central location.

Available actions:

inspect location;
move;
view nearby players.
TAVERN

Safe location.

Available actions:

view global chat;
start an expedition;
inspect completed expeditions.
MARKET

Safe location.

Available actions:

browse listings;
create listing;
buy item;
cancel own listing.
OLD_TOWN

Dangerous location.

Available actions:

search for encounter;
move.
FOREST

Dangerous location.

Available actions:

search for encounter;
move;
start Forest expedition.
NORTH_ROAD

Dangerous location.

Available actions:

search for encounter;
move.

Movement between connected locations is instantaneous for the MVP.

Do not introduce arbitrary movement timers yet.

The backend must still model world connectivity explicitly so travel times can be added later.

5. Character Model

Each account has exactly one character.

Character fields:

id
accountId
name
level
experience

strength
agility
endurance
perception

currentHealth
maxHealth

currentStamina
maxStamina

gold

currentLocationId

createdAt
updatedAt

Initial attributes:

Level:       1
Strength:    5
Agility:     5
Endurance:   5
Perception:  5

Gold:        100

Initial equipment should be basic starter equipment.

6. Derived Character Statistics

Derived statistics must be calculated by the backend.

Example MVP formulas:

Max HP =
100 + Endurance * 12

Max Stamina =
50 + Endurance * 4 + Agility * 2

Physical Damage =
weaponDamage + Strength * 1.5

Accuracy =
70 + Perception * 2

Dodge =
Agility * 1.5

Critical Chance =
5 + Perception * 0.5

Exact balancing constants must be isolated from business logic.

Do not scatter numerical constants throughout the code.

Use dedicated configuration or game-balance classes.

Example:

CombatBalance
ProgressionBalance
CharacterBalance

The game formulas are expected to change frequently.

Architecture must make balancing easy.

7. Character Progression

Level progression for MVP:

Level 1 -> 2: 100 XP
Level 2 -> 3: 250 XP
Level 3 -> 4: 450 XP
Level 4 -> 5: 700 XP
Level 5 -> 6: 1000 XP

Maximum MVP level:

10

On level-up:

+2 attribute points

Player may distribute points between:

Strength;
Agility;
Endurance;
Perception.

Attribute allocation must be server validated.

No respec system is required for MVP.

8. Item Model

Item definitions and owned item instances must be separate concepts.

ItemDefinition

Represents game data.

Example:

IRON_SWORD
LEATHER_ARMOR
OLD_DAGGER
HEALING_POTION
WOLF_PELT

Fields:

id
code
name
description
type
rarity
baseValue
requiredLevel

Types:

WEAPON
ARMOR
CONSUMABLE
MATERIAL

Rarities:

COMMON
UNCOMMON
RARE
EPIC
ItemInstance

Represents an actual item owned by a character.

Fields:

id
itemDefinitionId
ownerCharacterId
quantity
createdAt

For the MVP, weapons and armor do not require randomized affixes.

Architecture should allow them later.

9. Equipment

Equipment slots:

WEAPON
ARMOR

A character may equip one item in each slot.

Equipment modifies character combat statistics.

Example starter items:

Rusty Sword
Damage: 6

Worn Leather Armor
Armor: 3

Equip and unequip operations must be transactional and server validated.

10. MVP Monsters

Implement approximately five monster definitions.

Example:

Street Thug
Level: 1
HP: 70
Damage: 5–8
XP: 20
Giant Rat
Level: 1
HP: 50
Damage: 3–6
XP: 15
Forest Wolf
Level: 2
HP: 100
Damage: 7–11
XP: 30
Bandit
Level: 3
HP: 130
Damage: 10–15
XP: 45
Bandit Veteran
Level: 5
HP: 220
Damage: 15–22
XP: 80

Monster data should be stored as game definitions rather than hardcoded directly into combat logic.

11. Encounter System

Dangerous locations allow:

Search for encounter

Calling the action asks the backend to generate an encounter based on the current location.

Example:

FOREST

70% Forest Wolf
20% Bandit
10% Nothing Found

The encounter is persisted.

A character may have no more than one unresolved encounter.

Encounter states:

AVAILABLE
COMBAT_STARTED
RESOLVED
EXPIRED

Example response:

{
  "encounterId": "...",
  "type": "MONSTER",
  "monster": {
    "name": "Forest Wolf",
    "level": 2
  }
}

Player may:

Fight
Ignore
12. Combat System

Combat is server-side and turn based.

A combat session has:

id
characterId
monsterDefinitionId
status
roundNumber
playerHealth
playerStamina
enemyHealth
createdAt
updatedAt

Statuses:

ACTIVE
PLAYER_WON
PLAYER_LOST
PLAYER_ESCAPED
13. Combat Actions

For MVP implement:

QUICK_ATTACK
HEAVY_ATTACK
DEFEND
PRECISE_ATTACK
USE_POTION
RETREAT
QUICK_ATTACK

Balanced attack.

normal damage
normal accuracy
small stamina cost
HEAVY_ATTACK
+40% damage
-20% accuracy
high stamina cost
PRECISE_ATTACK
-20% damage
+25% accuracy
increased critical chance
DEFEND
greatly reduces incoming damage this round
restores small amount of stamina
USE_POTION

Consumes one potion from inventory.

RETREAT

Attempts to leave combat.

Escape chance depends on Agility.

14. Combat Round

Player selects one action.

Backend performs:

validate action
↓
calculate player action
↓
calculate enemy action
↓
apply effects
↓
update stamina
↓
check death
↓
check combat completion
↓
persist state
↓
return combat log

Example response:

{
  "round": 4,
  "events": [
    {
      "type": "PLAYER_ATTACK",
      "message": "You strike the Forest Wolf for 18 damage."
    },
    {
      "type": "ENEMY_ATTACK",
      "message": "The Forest Wolf bites you for 7 damage."
    }
  ],
  "player": {
    "health": 82,
    "stamina": 31
  },
  "enemy": {
    "health": 41
  }
}

Combat must be safely resumable after browser refresh or closing the tab.

There must be no requirement to act within a few seconds.

15. Combat Rewards

After victory:

award XP;
generate loot;
update character;
create activity feed event.

Example:

Victory

+30 XP
+12 gold

Wolf Pelt ×1
Healing Potion ×1

All rewards must be generated exactly once.

Combat reward processing must be idempotent.

Refreshing or repeating requests must never duplicate rewards.

16. Inventory

Inventory displays:

item name;
rarity;
quantity;
item type;
equipment status;
relevant stats.

Operations:

equip
unequip
use
sell on market

Default capacity:

40 slots

For stackable items, one stack counts as one slot.

17. Asynchronous Expedition System

This is one of the main MVP features.

The player can start an expedition from the Tavern.

Example expedition:

Forest Patrol

Duration:

20 minutes

Possible results:

gold
XP
materials
equipment
nothing
minor injury

Player chooses a strategy:

CAUTIOUS
BALANCED
AGGRESSIVE

Example effects:

CAUTIOUS
lower reward
lower risk
BALANCED
normal reward
normal risk
AGGRESSIVE
higher reward
higher injury chance

An expedition has:

id
characterId
expeditionType
strategy
startedAt
completesAt
claimedAt
status

Statuses:

ACTIVE
COMPLETED
CLAIMED

Important:

Do not create a background thread sleeping for 20 minutes.

Completion must be based on timestamps.

Example:

now >= completesAt

The result may be lazily generated when the user next queries or claims the expedition.

Result generation must be deterministic or persisted so that repeated requests cannot reroll rewards.

Only one expedition may be active at a time in MVP.

18. Marketplace

Implement a minimal player-driven marketplace.

A player can:

browse listings
create listing
buy listing
cancel own listing

Listing:

id
sellerCharacterId
itemInstanceId
quantity
price
status
createdAt
soldAt

Statuses:

ACTIVE
SOLD
CANCELLED

Marketplace purchase must use a database transaction.

The transaction must guarantee:

buyer has enough gold
listing is still active
seller still owns item
item transferred once
gold transferred once
listing becomes SOLD

Concurrent purchase attempts must not duplicate an item.

Use database locking or another safe concurrency mechanism.

19. Activity Feed

Every important personal event creates an activity entry.

Examples:

You defeated a Forest Wolf.
You reached level 3.
Your expedition returned.
You found Iron Sword.
Your marketplace listing sold for 80 gold.
You bought Leather Armor.

Activity entry:

id
characterId
type
message
createdAt
readAt

Frontend should show the most recent entries in a persistent side panel.

On login this becomes the player's immediate summary of what happened.

20. Global Chat

Implement a basic global chat.

Requirements:

authenticated users only;
character name displayed;
timestamps;
last 100 messages available;
message max length: 500 characters;
basic server-side rate limit;
server-side validation;
HTML must never be rendered from user input.

Recommended MVP transport:

POST message using REST
SSE stream for receiving new messages

Do not introduce WebSocket infrastructure solely for MVP chat.

The architecture may migrate to WebSockets later for realtime PvP.

21. User Interface

The frontend is a desktop-first responsive SPA.

Primary layout:

+------------------------------------------------------------+
| Logo | Character | World | Inventory | Market | Settings   |
+---------------+--------------------------+-----------------+
|               |                          |                 |
| CHARACTER     |       MAIN CONTENT       | ACTIVITY        |
|               |                          |                 |
| Level         |     location / combat    | expedition      |
| HP            |     inventory / market   | loot            |
| Stamina       |                          | market           |
| Equipment     |                          | world            |
|               |                          |                 |
+---------------+--------------------------+-----------------+
| GLOBAL CHAT                                              |
+----------------------------------------------------------+

Desktop usability is more important than mobile UX for MVP.

22. Office Mode

Implement a UI toggle:

Normal Mode
Compact Mode

Normal mode may contain:

location artwork placeholders;
larger panels;
decorative UI;
animations.

Compact mode should:

remove decorative imagery;
reduce padding;
reduce animation;
prioritize text and information density;
use minimal vertical space;
remain usable in a narrow browser window.

Persist preference in localStorage.

This is an important product feature, not an Easter egg.

23. Frontend Technology

Use:

React 19
TypeScript
Vite
React Router
TanStack Query

Avoid unnecessary state-management libraries.

Use TanStack Query for server state.

Use React Context or simple local state for UI-only state unless a stronger requirement emerges.

Frontend must not duplicate backend domain logic.

24. Backend Technology

Use:

Java 25
Spring Boot 4.1
Spring MVC
Spring Security
Spring Data JPA
Hibernate
Jakarta Validation
Flyway
PostgreSQL 18

Testing:

JUnit 5
Mockito
AssertJ
Testcontainers

Build:

Maven
25. Backend Architecture

Use a modular monolith.

Do NOT create microservices.

Suggested package structure:

com.example.game

account
character
world
combat
item
inventory
expedition
market
activity
chat
shared

Each feature module should preferably contain:

api
application
domain
infrastructure

Do not apply architectural ceremony mechanically.

The goal is clear dependency boundaries, not creating hundreds of tiny files.

26. Domain Logic

Controllers must remain thin.

Bad:

@PostMapping("/combat/action")
public CombatResponse attack(...) {
    // combat calculations
    // XP calculations
    // inventory changes
    // loot generation
}

Good:

CombatController
       |
       v
CombatApplicationService
       |
       v
CombatEngine

CombatEngine should contain combat rules and should ideally be testable without Spring.

The same principle applies to:

ExpeditionResolver
LootGenerator
CharacterProgressionService
MarketService
27. API Design

Use REST endpoints under:

/api/v1

Example endpoints:

POST   /api/v1/auth/register
POST   /api/v1/auth/login
POST   /api/v1/auth/logout
GET    /api/v1/me

POST   /api/v1/characters
GET    /api/v1/character

GET    /api/v1/world/location
POST   /api/v1/world/move

POST   /api/v1/encounters/search
POST   /api/v1/encounters/{id}/fight
POST   /api/v1/encounters/{id}/ignore

GET    /api/v1/combat/current
POST   /api/v1/combat/{id}/actions

GET    /api/v1/inventory
POST   /api/v1/inventory/{itemId}/equip
POST   /api/v1/inventory/{itemId}/unequip
POST   /api/v1/inventory/{itemId}/use

GET    /api/v1/expeditions/current
POST   /api/v1/expeditions
POST   /api/v1/expeditions/{id}/claim

GET    /api/v1/market/listings
POST   /api/v1/market/listings
POST   /api/v1/market/listings/{id}/buy
DELETE /api/v1/market/listings/{id}

GET    /api/v1/activity

GET    /api/v1/chat/messages
POST   /api/v1/chat/messages
GET    /api/v1/chat/stream

Exact DTO structures may evolve during implementation.

Never expose JPA entities directly from controllers.

28. Authentication

Use Spring Security.

For MVP use secure session-based authentication with an HttpOnly cookie.

Do not store authentication credentials or bearer tokens in localStorage.

Support:

register
login
logout
current authenticated user

Passwords must be securely hashed using Spring Security-supported password hashing.

Enable CSRF protection appropriate for the SPA architecture.

Use the Vite development proxy so frontend development does not require unsafe production CORS configuration.

29. Database

Use PostgreSQL.

Schema must be managed entirely through Flyway migrations.

Never rely on:

hibernate.ddl-auto=create
hibernate.ddl-auto=update

for production schema management.

Suggested tables:

accounts
characters
locations
location_connections
item_definitions
item_instances
equipment
monster_definitions
encounters
combat_sessions
combat_events
expeditions
market_listings
activity_entries
chat_messages

Add appropriate:

primary keys;
foreign keys;
unique constraints;
indexes;
check constraints where useful.
30. Concurrency and Transaction Safety

Particular care is required for:

combat rewards
market purchases
item transfer
equipment changes
expedition claims
level-up rewards

Duplicate HTTP requests must not duplicate resources or rewards.

Use transactions and database constraints rather than trusting the client.

31. Error Handling

Use one consistent API error structure.

Example:

{
  "code": "INSUFFICIENT_GOLD",
  "message": "You do not have enough gold to purchase this item.",
  "timestamp": "..."
}

Create centralized exception handling.

Do not expose internal exception stack traces to the frontend.

32. Observability

For MVP provide:

structured application logs;
request correlation ID if practical;
Spring Boot Actuator health endpoint.

Do not build a full Prometheus/Grafana stack yet.

33. Docker Development Environment

Provide:

docker-compose.yml

for PostgreSQL.

Application development should support:

docker compose up -d

then:

./mvnw spring-boot:run

Frontend:

npm install
npm run dev

A README must document the complete startup process.

34. Seed Data

Use deterministic development seed data for:

Greyhaven locations;
connections;
item definitions;
monster definitions;
expedition definitions.

Do not require manual SQL setup after running migrations.

35. Testing Requirements

Important domain logic must have unit tests.

Especially:

CombatEngine
damage calculation
stamina calculation
character progression
loot generation
expedition resolution
market transaction rules
equipment validation

Integration tests should use Testcontainers PostgreSQL for important persistence flows.

At minimum implement integration tests for:

register/login
character creation
combat reward persistence
expedition claiming
market purchase
36. MVP User Journey

A new player should be able to perform the following complete flow.

Register account
↓
Create character
↓
Enter Greyhaven
↓
Inspect character
↓
Move to Old Town
↓
Search for encounter
↓
Fight enemy
↓
Receive XP and loot
↓
Equip new weapon
↓
Move to Tavern
↓
Start 20-minute expedition
↓
Browse market
↓
Write message in chat
↓
Leave the game
↓
Return later
↓
See completed expedition in activity feed
↓
Claim expedition
↓
Sell loot on marketplace

If this complete flow works cleanly, the MVP is successful from a technical perspective.

37. Definition of Done

The MVP is complete when:

The application can be started locally using documented commands.
Registration and authentication work.
A character can be created.
World locations and movement work.
Encounters work.
Combat is persistent and interruption-safe.
Combat rewards work exactly once.
XP and leveling work.
Inventory and equipment work.
Expeditions continue based on real time while the user is offline.
Expedition rewards can be claimed exactly once.
Marketplace transactions are concurrency safe.
Activity feed reports meaningful events.
Global chat works.
Normal and compact UI modes work.
Reloading the browser does not lose server-side gameplay state.
Critical domain logic has automated tests.
The README explains how to run the project.
No excluded post-MVP functionality has been implemented unnecessarily.
IMPLEMENTATION STRATEGY FOR CURSOR

Do not attempt to implement the entire specification in one pass.

Execute the following tasks sequentially.

TASK 1 — Project Bootstrap and Architecture
Goal

Create a production-quality foundation for the application without implementing game mechanics yet.

Backend

Create Spring Boot project using:

Java 25
Spring Boot 4.1
Maven
PostgreSQL
Flyway
Spring Security
Spring Data JPA
Validation
Actuator
Testcontainers

Create initial modules:

account
character
world
combat
item
inventory
expedition
market
activity
chat
shared

Create Docker Compose PostgreSQL configuration.

Create:

application.yml
application-local.yml
application-test.yml

Configure Flyway.

Create global API exception handling.

Create health endpoint.

Frontend

Create:

React
TypeScript
Vite
React Router
TanStack Query

Implement basic application shell.

Create routes:

/login
/register
/game

Create main three-column game layout.

No game mechanics yet.

Required output

After implementation:

run backend tests;
run frontend build;
provide a concise summary;
list important architectural decisions;
list all commands required to run the project.

Do not proceed to Task 2 automatically.

TASK 2 — Authentication and Character Creation
Goal

Implement the complete account and character onboarding flow.

Backend:

Account
Character

Implement:

register
login
logout
current user
character creation
current character

Rules:

unique email;
unique character name;
one character per account;
secure password hashing;
session authentication;
HttpOnly cookie;
CSRF protection.

Character starts with defined MVP default stats and 100 gold.

Frontend:

Implement:

registration page
login page
character creation page
authenticated routing
character summary panel

Tests:

registration
duplicate email
authentication
unauthenticated API access
character creation
duplicate character name
second character rejection

Do not implement world or combat yet.

Do not proceed automatically.

TASK 3 — World and Location System
Goal

Implement Greyhaven and player location.

Create:

Location
LocationConnection

Seed all MVP locations.

Backend must validate that movement is only possible between connected locations.

Implement:

GET current location
POST move
GET available destinations
GET nearby characters

Movement is instantaneous.

Frontend game view must show:

current location
description
available destinations
available actions
nearby characters

Browser refresh must preserve location.

Tests must verify:

valid movement
invalid movement
location persistence
nearby characters

Do not implement combat yet.

TASK 4 — Items, Inventory and Character Equipment
Goal

Create the complete basic item system before combat uses it.

Implement:

ItemDefinition
ItemInstance
Inventory
Equipment

Seed MVP item definitions.

Implement:

inventory retrieval
equip
unequip
use healing potion
derived character statistics

Create dedicated stat-calculation classes.

Do not embed combat formulas inside JPA entities.

Frontend:

Implement inventory and equipment screens.

Include:

rarity
stats
quantity
equip action
unequip action

Tests must cover:

equipment requirements
ownership validation
derived stats
consumable removal
inventory capacity
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
TASK 6 — Asynchronous Expeditions and Activity Feed
Goal

Implement the core office-first asynchronous gameplay mechanic.

Implement:

Expedition
ExpeditionResolver
ActivityEntry

Create Forest Patrol expedition.

Duration:

20 minutes

Strategies:

CAUTIOUS
BALANCED
AGGRESSIVE

Do not use sleeping threads or long-running scheduled tasks.

Use timestamps.

Implement:

start expedition
inspect expedition
detect completion
claim result

Results must be generated only once.

Implement activity events for:

combat victory
level up
item found
expedition completed
expedition claimed

Frontend:

Add persistent Activity panel.

Add expedition screen with:

strategy selection
remaining time
completion state
claim action
reward display

The countdown is visual only.

The server timestamp determines real completion.

Test clock-dependent code using an injectable Clock.

Do not use Instant.now() directly throughout domain logic.

TASK 7 — Player Marketplace
Goal

Introduce the first meaningful persistent multiplayer economy.

Implement:

create listing
browse active listings
filter by item type
buy listing
cancel own listing

Market purchase must be transactional.

Prevent:

double purchase
negative gold
selling another player's item
selling equipped item
buying own listing
duplicating item stacks

Use appropriate database locking and constraints.

Frontend:

Implement marketplace screen with:

item
rarity
seller
price
buy button
filters

Implement own listings view.

Generate activity event when:

item sells
item is bought
listing is cancelled

Include concurrency integration test where two buyers attempt to purchase the same listing.

Exactly one purchase must succeed.

TASK 8 — Global Chat, Office Mode and MVP Polish
Goal

Finish the social and UX layer and make the vertical slice feel like a coherent game.

Implement global chat using:

REST POST
Server-Sent Events

Requirements:

last 100 messages
500 character maximum
rate limiting
plain text only
authentication required

Implement reconnect behavior for SSE.

Frontend:

Finish primary game layout.

Provide:

Character panel
Main content
Activity panel
Global chat
Navigation

Implement:

Normal Mode
Compact Office Mode

Compact mode should remove unnecessary:

artwork
animations
large spacing
decorative elements

Persist UI preference locally.

Add loading states.

Add useful empty states.

Add error states.

Avoid modal dialogs for routine actions where possible.

Run complete MVP flow manually.

Run complete automated test suite.

Run frontend production build.

Update README.

Finally produce:

implementation summary;
final project tree;
API endpoint list;
database table list;
known technical debt;
intentionally deferred features;
recommendations for Phase 2.

Do not implement Phase 2.

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

PRODUCT NORTH STAR

Whenever a gameplay or UX decision is ambiguous, optimize for the following player:

An adult player has 20 minutes between work tasks. They open the browser, immediately understand what changed since their last visit, make several meaningful RPG decisions, progress their character, interact with the persistent world, start something that will continue while they are away, and close the tab without anxiety that they are missing the game.

The MVP should always optimize for that experience.