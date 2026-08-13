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