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