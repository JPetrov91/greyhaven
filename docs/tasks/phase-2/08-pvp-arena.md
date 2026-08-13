TASK 8 — PvP Foundation and Async Arena
Goal

Introduce player-vs-player combat only after build diversity exists.

Implement:

player inspection
Arena profile
Arena defense configuration
opponent selection
Arena challenge
battle snapshot
rating
battle history
Arena rewards

Primary mode:

Asynchronous Arena

Implement simple structured defensive strategy.

Do not implement a scripting language.

Ensure:

defender can be offline
match uses snapshot
rating updates exactly once
rewards exactly once
self-challenge impossible
repeat farming mitigated

Implement casual live duel after Arena works.

Duels are:

unranked
optional
interruption-friendly

Do not introduce mandatory realtime infrastructure if existing REST/SSE mechanisms can satisfy requirements cleanly.

If WebSocket is genuinely required, propose the change before implementing it.

Tests

Include:

Arena snapshot consistency
rating calculation
duplicate completion
repeat-opponent protection
offline defender
equipment changes after match start
duel action resolution
disconnect behavior

Perform a balance review across major build archetypes.