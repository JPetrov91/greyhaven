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